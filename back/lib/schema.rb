#!/usr/bin/ruby

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

# Convenience methods for collecting tags.
module SimpleScraper
  module Schema
    # Tagging model factory.
    # Works as Tagging.new(:source, :target) or Tagging.new(:source => 'SourceModel', :target => 'TargetModel')
    module Tagging
      def self.new(name, *args)
        klass = Class.new do
          include DataMapper::Resource
          if args.length == 2
            belongs_to args[0].to_sym, :key => true
            belongs_to args[1].to_sym, :key => true
          elsif args[0].class == Hash and args[0].length == 2
            args = args[0].to_a
            belongs_to args[0][0].to_sym, args[0][1], :key => true
            belongs_to args[1][0].to_sym, args[1][1], :key => true
          end
        end
        SimpleScraper::Schema.const_set(name.camelize.to_sym, klass)
        name.underscore.pluralize.to_sym
      end
    end
    
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
            name.split('::').last.underscore
          end
          
          def self.location
            raw_name + '/'
          end
          
          def self.related_model (relationship)
            relationships[relationship.to_sym] ? relationships[relationship.to_sym].target_model : nil
          end
          
          property :id,   DataMapper::Property::Serial, :accessor => :private
          
          property :created_at, DataMapper::Property::DateTime, :writer => :private

          # Destroy tags before destroying resource.
          before :destroy do
            model.relationships.each do |name, relationship|
              if relationship.class == DataMapper::Associations::ManyToMany::Relationship
                send(name).intermediaries.destroy
              end
            end
          end
        end
        
        # Safely change a resource's attributes.
        def modify (new_attributes)
          new_attributes.delete_if do |name, value| # Delete attributes not specified in the model
            not attributes.keys.include? name.downcase.to_sym
          end
          new_attributes.delete_if do |name, value|
            private_methods.include? name + '=' # Remove private attributes.
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
        
        def location
          model.raw_name + '/' + [*key].join('.')
        end
      end
    end
    
    # A Resource owned by a creator, with editors.  Can be checked to see if it is editable by a user.
    module CreatedResource
      def self.included(base)
        base.class_eval do
          include EditableResource
          
          property :name, DataMapper::Property::String, :required => true, :unique_index => :creator_name_deleted
          property :description, DataMapper::Property::Text
          
          belongs_to :creator, :model => 'User', :required => true
          property :creator_id, DataMapper::Property::Integer, :required => true, :writer => :private, :unique_index => :creator_name_deleted
          
          property :updated_at, DataMapper::Property::DateTime, :writer => :private
          property :deleted_at, DataMapper::Property::ParanoidDateTime, :writer => :private, :unique_index => :creator_name_deleted

          tag :editors, :model => 'User', :through => DataMapper::Resource
        end
        
        def editable_by? (user)
          creator == user or editors.get(user.attribute_get(:id)) ? true : false
        end
      end
    end
    
    class User
      include EditableResource

      property :name, DataMapper::Property::String, :required => true, :unique => true
      property :nickname, DataMapper::Property::String, :unique => true
      
      tag :datas,          :child_key => [ :creator_id ]
      tag :areas,          :child_key => [ :creator_id ]
      tag :infos,          :child_key => [ :creator_id ]
      tag :field_names,    :child_key => [ :creator_id ]
      tag :defaults,       :child_key => [ :creator_id ]
      tag :patterns,       :child_key => [ :creator_id ]
      tag :interpreters,   :child_key => [ :creator_id ]
      tag :generators,     :child_key => [ :creator_id ]
      tag :gatherers,      :child_key => [ :creator_id ]
      tag :posts,          :child_key => [ :creator_id ]
      tag :urls,           :child_key => [ :creator_id ]
      tag :headers,        :child_key => [ :creator_id ]
      tag :cookie_headers, :child_key => [ :creator_id ]
      
      def nickname
        super.nil? ? name : super
      end
      
      # Only the user can modify his/her own resource.
      def editable_by? (user)
        user == self ? true : false
      end
      
      def can_edit? (resource)
        resource.editable_by? self
      end

      # Protect name from reassignment.  Only nickname should change.
      before :name= do
        throw :halt unless name.nil?
      end
      
      # Cannot destroy.
      before :destroy do
        throw :halt
      end
    end
    
    class FieldName
      include CreatedResource
      
      tag :datas,    :through => Resource
      tag :defaults, :through => Resource

      tag :infos, :through => :publish_fields, :via => :info
      has n, :publish_fields
    end

    class Default
      include CreatedResource
      
      tag :areas,       :through => Resource
      tag :field_names, :through => Resource
      property :value, String, :default => '' #, :required => true
    end

    class Area
      include CreatedResource
      
      tag :defaults,  :through => Resource
      tag :datas,     :through => Resource
      
      has n, :area_links, :model => 'AreaLink', :child_key => [:source_id]
      tag :follow_areas,  :model => self, :through => :area_links, :via => :target
    end

    class Info
      include CreatedResource
      
      tag :publishes, 'FieldName', :through => :publish_fields, :via => :field_name
      has n, :publish_fields
      tag :datas,       :through => Resource
    end

    class Data
      include CreatedResource
      
      tag :areas,       :through => Resource
      tag :infos,       :through => Resource
      tag :field_names, :through => Resource
      
      tag :generator_sources, 'Generator', :through => :generator_source_datas, :via => :generator
      has n, :generator_source_datas
      tag :generator_targets, 'Generator', :through => :generator_target_datas, :via => :generator
      has n, :generator_target_datas
      
      tag :interpreter_sources, 'Interpreter', :through => :interpreter_source_datas, :via => :interpreter
      has n, :interpreter_source_datas
      tag :interpreter_targets, 'Interpreter', :through => :interpreter_target_datas, :via => :interpreter
      has n, :interpreter_target_datas
      
      tag :gatherer_targets, 'Gatherer', :through => :gatherer_target_datas, :via => :gatherer
      has n, :gatherer_target_datas
    end

    class Interpreter
      include CreatedResource
      
      tag :source_datas, 'Data', :through => :interpreter_source_datas, :via => :data
      has n, :interpreter_source_datas
      tag :target_datas, 'Data', :through => :interpreter_target_datas, :via => :data
      has n, :interpreter_target_datas
      
      tag :patterns,  :through => Resource
      tag :gatherers, :through => Resource

      property :match_number, Integer, :default => 0, :required => true
      property :terminate_on_complete, Boolean, :default => false, :required => true
    end
    
    class Pattern
      include CreatedResource
      
      tag :interpreters, :through => Resource
      
      property :regex, DataMapper::Property::Regexp
    end

    class Generator
      include CreatedResource

      tag :source_datas, 'Data', :through => :generator_source_datas, :via => :data
      has n, :generator_source_datas
      tag :target_datas, 'Data', :through => :generator_target_datas, :via => :data
      has n, :generator_target_datas
      
      tag :gatherers, :through => Resource
      tag :patterns,  :through => Resource
    end
    
    class Gatherer
      include CreatedResource
      
      tag :interpreters, :through => Resource
      tag :generators,   :through => Resource
      tag :target_datas, 'Data', :through => :gatherer_target_datas, :via => :data
      has n, :gatherer_target_datas
      
      tag :stops, 'Pattern', :through => Resource
      
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
    
    # Rockin'.
    Tagging.new('AreaLink', :source => 'Area', :target => 'Area')
    Tagging.new('PublishField', :info, :field_name)
    Tagging.new('InterpreterSourceData', :interpreter, :data)
    Tagging.new('InterpreterTargetData', :interpreter, :data)
    Tagging.new('GeneratorSourceData', :generator, :data)
    Tagging.new('GeneratorTargetData', :generator, :data)
    Tagging.new('GathererTargetData', :gatherer, :data)
  end
end
