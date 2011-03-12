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
#require 'dm-sqlite-adapter'

# Convenience methods for collecting tags.
module SimpleScraper
  class Database
    module Schema
      # All resources have a name, a description, a creator, blessed editors, and paranoia.
      module EditableResource
        def self.included(base)
          base.class_eval do
            include DataMapper::Resource
            
            def self.tag(name, *args)
              has(n, name, *args)
              name = name.to_sym
              if(!@tag_names)
                @tag_names = []
              end
              @tag_names << name
            end
            
            def self.tag_names
              @tag_names or []
            end
            
            def self.raw_name
              DataMapper::Inflector.underscore(name.split('::').last)
            end
            
            def self.related_model (relationship)
              relationships[relationship.to_sym] ? relationships[relationship.to_sym].target_model : nil
            end

            # Model location.
            # TODO THIS IS ASSUMING EVERYTHING LIVES IN THE '/editor/' DIRECTORY
            def self.location
              '/editor/' + self.raw_name + '/'
              # @settings[:directory] + self.raw_name + '/'
            end

            property :id,   DataMapper::Property::Serial, :accessor => :private
            
            property :created_at, DataMapper::Property::DateTime, :writer => :private
            property :updated_at, DataMapper::Property::DateTime, :writer => :private
            
            # Destroy tags before destroying resource.
            before :destroy do
              model.relationships.each do |name, relationship|
                if relationship.class == DataMapper::Associations::ManyToMany::Relationship
                  send(name).intermediaries.destroy
                end
              end
            end
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
          
          # Untag -- does not work with CPK
          def untag (tag_name, tag)
            relationship = model.relationships[tag_name.to_sym]
            raise DataMapper::UnknownRelationshipError unless relationship.target_model == tag.model
            
            if relationship.class == DataMapper::Associations::OneToMany::Relationship
              tag.destroy
            elsif relationship.class == DataMapper::Associations::ManyToMany::Relationship
              send(tag_name).intermediaries.get([*[key, tag.key].flatten], [*[key, tag.key].flatten]).destroy
            end
          end

          # Resource location.
          def location
            model.location + attribute_get(:id).to_s
          end

        end
      end
      
      # A Resource owned by a creator, with editors.  Can be checked to see if it is editable by a user.
      module CreatedResource
        def self.included(base)
          base.class_eval do
            include EditableResource
            
            property :name, DataMapper::Property::String, :required => true, :unique_index => :creator_name_deleted
            # Make sure names don't contain a slash.
            validates_with_method :name, :validate_name

            property :description, DataMapper::Property::Text
            
            belongs_to :creator, :model => 'User', :required => true
            property :creator_id, DataMapper::Property::Integer, :required => true, :writer => :private, :unique_index => :creator_name_deleted
            
            property :deleted_at, DataMapper::Property::ParanoidDateTime, :writer => :private, :unique_index => :creator_name_deleted

            tag :editors, :model => 'User', :through => DataMapper::Resource
            #has 1, :last_editor, :model => 'User', :through => DataMapper::Resource #, :default => creator
            property :last_editor_id, DataMapper::Property::Integer, :accessor => :private
            
            # Keep track of our last editor.
            after :save do
              if @last_editor
                last_editor_id = @last_editor.attribute_get(:id)
              end
            end
          end
          
          def validate_name
            if name.index('/')
              [ false, 'Name cannot contain a slash.  Slashes are used to separate creator from resource name.' ]
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
          
          def full_name
            creator.name + '/' + name
          end
        end
      end
      
      class User
        include EditableResource

        property :immutable_name, DataMapper::Property::String, :required => true, :unique => true, :accessor => :private
        property :name, DataMapper::Property::String, :required => true, :unique => true
        
        tag :areas,          :child_key => [ :creator_id ]
        tag :infos,          :child_key => [ :creator_id ]
        tag :interpreters,   :child_key => [ :creator_id ]
        tag :gatherers,      :child_key => [ :creator_id ]
        
        # Only the user can modify his/her own resource.
        def editable_by? (user)
          user == self ? true : false
        end
        
        def can_edit? (resource)
          resource.editable_by? self
        end

        def full_name
          name
        end

        # Default name to immutable name.
        before :valid? do
          send(:name=, immutable_name) if name.nil?
        end
      end

      class Default
        include CreatedResource
        
        tag :areas,       :through => Resource
        tag :substitutes_for_interpreters, 'Interpreter', :through => Resource
        property :value, String #, :default => '' #, :required => true
      end
      
      class Area
        include CreatedResource
        
        tag :defaults,     :through => Resource
        tag :interpreters, :through => Resource
      end

      class Info
        include CreatedResource
        
        tag :interpreters, :through => Resource
      end

      class Interpreter
        include CreatedResource
        
        tag :areas, :through => Resource
        tag :infos, :through => Resource
        
        property :regex, String, :default => '//' # Regexp.new('')
        property :match_number, Integer
        property :publish, Boolean, :default => false,  :required => true

        tag :gatherers, :through => Resource
        tag :interpreter_sources, 'Interpreter', :through => Resource
      end
      
      class Regex
        include CreatedResource
        
        property :regex, String, :default => '//' # Regexp.new('')
      end

      class Gatherer
        include CreatedResource
        
        tag :interpreters, :through => Resource
        
        tag :terminates, 'Regex', String
        
        tag :urls, :through => Resource
        tag :posts, :through => Resource
        tag :headers, :through => Resource
        tag :cookie_headers, :through => Resource
      end
      
      class Url
        include CreatedResource
        
        tag :gatherers, :through => Resource
        
        property :value, DataMapper::Property::URI
      end
      
      class Post
        include CreatedResource

        tag :gatherers, :through => Resource

        property :post_name,  String
        property :value, String
      end

      class Header
        include CreatedResource

        tag :gatherers, :through => Resource

        property :header_name,  String
        property :value, String
      end

      class CookieHeader
        include CreatedResource

        tag :gatherers, :through => Resource

        property :cookie_name,  String
        property :value, String
      end
    end
  end
end
