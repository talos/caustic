###
#   SimpleScraper Back 0.0.1
#
#   Copyright 2010, AUTHORS.txt
#   Licensed under the MIT license.
#
#   schema.rb : Database definitions.
###

require 'rubygems'
require 'dm-core'
require 'dm-types'
require 'dm-migrations'
require 'dm-validations'
require 'dm-timestamps'
require 'json'

module SimpleScraper
  class Database
    module Schema
      module DataMapper::Model
        def raw_name
          DataMapper::Inflector.underscore(name.split('::').last)
        end

        # TODO THIS IS ASSUMING EVERYTHING LIVES IN THE '/' DIRECTORY
        def location
          #'/editor/' + self.raw_name + '/'
          # @settings[:directory] + self.raw_name + '/'
          '/' + self.raw_name + '/'
        end
      end
      
      class User
        include DataMapper::Resource
        
        property :id,   DataMapper::Property::Serial, :accessor => :private
        
        property :immutable_name, DataMapper::Property::String, :required => true, :unique => true, :accessor => :private
        property :title, DataMapper::Property::String, :required => true, :unique => true
        
        has n, :scrapers,   :child_key => [ :creator_id ]
        has n, :defaults,   :child_key => [ :creator_id ]
        has n, :datas,      :child_key => [ :creator_id ]
        has n, :web_pages,  :child_key => [ :creator_id ]
        
        has n, :urls,    :child_key => [ :creator_id ]
        has n, :posts,   :child_key => [ :creator_id ]
        has n, :headers, :child_key => [ :creator_id ]
        has n, :cookies, 'Cookie',  :child_key => [ :creator_id ]
        has n, :regexes, :child_key => [ :creator_id ]
        
        # Only the user can modify his/her own resource.
        def editable_by? (user)
          user == self ? true : false
        end
        
        def can_edit? (resource)
          resource.editable_by? self
        end

        def full_name
          title
        end
        
        # Default title to immutable name.
        before :valid? do
          send(:title=, immutable_name) if title.nil?
        end
        
        def location
          "/#{full_name}"
        end
        
        ## TODO: DRY this out
        def associations
          model.relationships.collect do |name, relationship|
            {
              :name => name,
              :model_location => relationship.target_model.location,
              :location => location + '/' + name + '/',
              :collection => send(name)
            }
          end
        end
      end
      
      # A Resource owned by a creator, with editors.  Can be checked to see if it is editable by a user.
      module Resource
        def self.included(base)
          base.class_eval do
            include DataMapper::Resource
            
            property :id,   DataMapper::Property::Serial, :accessor => :private
            
            property :created_at, DataMapper::Property::DateTime, :writer => :private
            property :updated_at, DataMapper::Property::DateTime, :writer => :private

            property :title, DataMapper::Property::String, :required => true, :unique_index => :creator_name_deleted
            # Make sure names don't contain a slash.
            validates_with_method :title, :validate_title

            property :description, DataMapper::Property::Text
            
            belongs_to :creator, :model => 'User', :required => true
            property :creator_id, DataMapper::Property::Integer, :required => true, :writer => :private, :unique_index => :creator_name_deleted
            
            property :deleted_at, DataMapper::Property::ParanoidDateTime, :writer => :private, :unique_index => :creator_name_deleted

            has n, :editors, :model => 'User', :through => DataMapper::Resource
            property :last_editor_id, DataMapper::Property::Integer, :accessor => :private
            
            # Destroy links before destroying resource.
            before :destroy do
              model.many_to_many_relationships.each do |name, relationship|
                send(name).intermediaries.destroy
              end
            end

            # Keep track of our last editor.
            after :save do
              if @last_editor
                last_editor_id = @last_editor.attribute_get(:id)
              end
            end
            
            def self.related_model (relationship)
              relationships[relationship.to_sym] ? relationships[relationship.to_sym].target_model : nil
            end
            
            def self.many_to_many_relationships
              relationships.select { |name, relationship| relationship.class == DataMapper::Associations::ManyToMany::Relationship }
            end
            
            def self.many_to_many_recursive_relationships
              many_to_many_relationships.select do |name, relationship|
                if @do_not_recurse
                  @do_not_recurse.include? name.to_sym ? false : true
                else
                  true
                end
              end
            end
            
            def self.do_not_recurse (*relationships)
              @do_not_recurse = [] if @do_not_recurse.nil?
              @do_not_recurse.push(*relationships)
            end
          end
          
          def do_not_recurse
            @do_not_recurse
          end
          
          def validate_title
            if title.index('/')
              [ false, 'Title cannot contain a slash.' ]
            else
              true
            end
          end

          def editable_by? (user)
            creator == user or editors.get(user.attribute_get(:id)) ? true : false
          end

          def modify (new_attributes, last_updated_at, editor)
            if updated_at != last_updated_at
              raise DataMapper::UpdateConflictError.new('This resource has been updated by ' + SimpleScraper::Users.get(last_editor_id) + ' since you last loaded it.  Changes not saved.')
            end
            super new_attributes, last_updated_at, editor
            @last_editor = editor
          end
          
          # TODO: does not work with CPK
          def unlink(relationship_name, link)
            relationship = model.relationships[relationship_name.to_sym]
            raise DataMapper::UnknownRelationshipError.new unless relationship.class == DataMapper::Associations::ManyToMany::Relationship
            raise DataMapper::UnknownRelationshipError.new unless relationship.target_model == link.model
            
            send(relationship_name).intermediaries.get([*[key, link.key].flatten], [*[key, link.key].flatten]).destroy
          end

          def full_name
            creator.full_name + '/' + title
          end

          # Safely change a resource's attributes
          def modify (new_attributes, last_updated_at, editor)
            new_attributes.delete_if do |name, value|
              if not attributes.keys.include? name.downcase.to_sym # Delete attributes not specified in the model
                true
              elsif private_methods.include? name + '=' or value == ''  # Remove private attributes and blank strings.
                true
              end
            end
            self.attributes=(new_attributes)
          end
          
          # Resource location.
          def location
            creator.location + '/' + relationships[:creator].inverse.name.to_s + '/' + attribute_get(:title)
          end

          def immutables
            immutables = attributes.select do |name, value|
              private_methods.include?(name.to_s + '=')
            end
            immutables.collect { |name, value| {:name => name, :value => value}}
          end
          
          def mutable_attributes
            attributes.select do |name, value|
              public_methods.include?(name.to_s + '=')
            end
          end

          def mutables
            mutable_attributes.collect { |name, value| {:name => name, :value => value}}
          end
          
          def associations
            model.many_to_many_relationships.collect do |name, relationship|
              {
                :name => name,
                :model_location => relationship.target_model.location,
                :location => location + '/' + name + '/',
                :collection => send(name).collect do |resource|
                  # Substitute link location for real location
                  {
                    :full_name => resource.full_name,
                    :location => "#{location}/#{name}/#{resource.attribute_get(:id)}",
                    #:associations => resource.associations
                  }
                end
              }
            end
          end

          #def export(recurse = true)
          def export (options = {})
            settings = {
              :into => {},
              :recurse => false
            }.merge(options)
            
            dest = settings[:into]
            
            relevant_attributes = Hash[mutable_attributes]
            relevant_attributes.delete(:description)
            relevant_attributes.delete(:title)
            relevant_relationships = Hash[model.many_to_many_recursive_relationships.collect do |name, relationship|
                                            [
                                             name, send(name).collect do |resource|
                                               ## Add related objects into the destination object.
                                               resource.export(:into => dest, :recurse => settings[:recurse])
                                               resource.full_name
                                             end
                                            ]
                                          end]
            relevant_relationships.delete('editors')

            obj = relevant_attributes.merge(relevant_relationships)

            dest[model.raw_name] = {} if dest[model.raw_name].nil?
            dest[model.raw_name][full_name] = obj
            dest
          end

          def to_json
            export(:recurse => true).to_json
          end
        end
      end
      
      class Default
        include Resource
        
        has n, :datas, :through => DataMapper::Resource
        do_not_recurse :datas
        
        has n, :substitutes_for, 'Scraper', :through => DataMapper::Resource
        property :value, String
      end
      
      class Data
        include Resource
        
        has n, :defaults, :through => DataMapper::Resource
        has n, :scrapers, :through => DataMapper::Resource
      end
      
      class Scraper
        include Resource
        
        has n, :datas, :through => DataMapper::Resource #, :recurse => false
        do_not_recurse :datas
        
        property :regex,        String,  :default => '//' # Regexp.new('')
        property :match_number, Integer,                     :required => false
        property :publish,      Boolean, :default => false,  :required => true
        
        has n, :web_pages,                  :through => DataMapper::Resource
        has n, :source_scrapers, 'Scraper', :through => DataMapper::Resource
      end
      
      class Regex
        include Resource
        
        property :regex, String, :default => '//' # Regexp.new('')
      end
      
      class WebPage
        include Resource
        
        has n, :scrapers, :through => DataMapper::Resource #, :recurse => false
        do_not_recurse :scrapers
        
        has n, :terminates, 'Regex', String
        
        has n, :urls,           :through => DataMapper::Resource
        has n, :posts,          :through => DataMapper::Resource
        has n, :headers,        :through => DataMapper::Resource
        has n, :cookies, 'Cookie', :through => DataMapper::Resource
      end
      
      class Url
        include Resource
        
        has n, :web_pages, :through => DataMapper::Resource #, :recurse => false
        do_not_recurse :web_pages
        
        property :value, DataMapper::Property::URI
      end
        
      class Post
        include Resource

        has n, :web_pages, :through => DataMapper::Resource #, :recurse => false
        do_not_recurse :web_pages
        
        property :name,  String
        property :value, String
      end

      class Header
        include Resource

        has n, :web_pages, :through => DataMapper::Resource #, :recurse => false
        do_not_recurse :web_pages

        property :name,  String
        property :value, String
      end

      #class CookieHeaders
      class Cookie
        include Resource
        
        storage_names[:default] = 'cookie_headers'
        
        has n, :web_pages, :through => DataMapper::Resource #, :recurse => false
        do_not_recurse :web_pages

        property :name,  String
        property :value, String
      end
    end
  end
end
