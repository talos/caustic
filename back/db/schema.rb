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
require 'json'

DataMapper.setup(:default, 'sqlite://' + Dir.pwd + '/db.sqlite')

# Extend default String length from 50 to 500
DataMapper::Property::String.length(500)
DataMapper::Model.raise_on_save_failure = true
module SimpleScraper
  SimpleScraper::MAX_RECORDS = 100
end

module DataMapper::Model
  def all_like(unfiltered)
    filtered = {}
    properties.each do |property|
      filtered[property.name.to_sym.like] = unfiltered[property.name.to_s] if unfiltered.include? property.name.to_s
    end
    all({:limit => SimpleScraper::MAX_RECORDS}.merge(filtered))
  end
  
  def raw_name
    name.sub(%r{^[^:]*:+}, '').downcase
  end
  
  def location
    '/' + raw_name + '/'
  end

  def related_model (relationship)
    relationships[relationship.to_sym] ? relationships[relationship.to_sym].target_model : nil
  end
end

# Convenience methods for collecting tags.
module DataMapper::Model::Relationship
  # Tags are always either one-to-many or many-to-many.
  def tag(name, *args)
    has(n, name, *args)
    name = name.to_sym
    if(!@tag_names)
      @tag_names = []
    end
    @tag_names << name
  end
  def tag_names
     @tag_names or []
  end
  def many_to_manys
    many_to_manys = relationships.select do |k, r|
      r.class == DataMapper::Associations::ManyToMany::Relationship
    end
    Hash[*many_to_manys.collect { |relationship| [relationship[0].to_sym, relationship[1].target_model] }.flatten]
  end
  # Helps make tagging classes.
  def tagging(model1, model2)
    belongs_to model1, :key => true
    belongs_to model2, :key => true
  end
end

module SimpleScraper
  # All resources have a name, a description, a creator, blessed editors, and paranoia.
  module EditableResource
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        property :id,   DataMapper::Property::Serial, :accessor => :private
        
        property :created_at, DataMapper::Property::DateTime, :writer => :private, :default => lambda { |r,p| Time.now }, :accessor => :private
        property :modified_at, DataMapper::Property::DateTime, :accessor => :private
        property :deleted_at, DataMapper::Property::ParanoidDateTime, :accessor => :private
        
        # Destroy tags before destroying resource.
        before :destroy do
          model.many_to_manys.each do |name, relationship|
            send(name).intermediaries.destroy
          end
        end

        # Keep track of changes.
        after :save do
          modified_at = Time.now
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
        save
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
      
      # Returns attributes, and lists of tags as arrays.
      def describe
        desc = attributes.clone
        self.class.tag_names.each do |tag_name|
          desc[tag_name.to_s + '/'] = []
          send(tag_name).all.each do |tag|
            desc[tag_name.to_s + '/'] << {
              :name  => tag.full_name,
              :id    => tag.attribute_get(:id),
              :model => tag.model.raw_name
            }
          end
        end
        desc
      end
    end
  end
  
  # A Resource owned by a creator, with editors.  Can be checked to see if it is editable by a user.
  module CreatedResource
    def self.included(base)
      base.class_eval do
        include EditableResource
        
        property :name, DataMapper::Property::String, :required => true #, :default => lambda { |r,p| r.model.raw_name }
        property :description, DataMapper::Property::Text

        belongs_to :creator, :model => 'User', :required => true
        property :creator_id, DataMapper::Property::Integer, :required => true, :accessor => :private
        
        tag :editors, :model => 'User', :through => DataMapper::Resource

        #validates_uniqueness_of :creator, :name, :deleted_at
      end
      
      def full_name
        (creator.nickname ? creator.nickname : creator.name) + "'s " + name
      end
      
      def editable_by? (user)
        puts editors.get(user.attribute_get(:id))
        creator == user or editors.get(user.attribute_get(:id)) ? true : false
      end
    end
  end
  
  module Tagging
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        property :created_at, DataMapper::Property::DateTime, :writer => :private, :default => lambda { |r,p| Time.now }
        property :modified_at, DataMapper::Property::DateTime
        property :deleted_at, DataMapper::Property::ParanoidDateTime
      end
    end
  end
  
  class User
    include EditableResource

    property :name, DataMapper::Property::String, :required => true, :unique => true #, :default => lambda { |r,p| r.model.raw_name }
    property :nickname, DataMapper::Property::String, :unique => true
    
    tag :datas,          :child_key => [ :creator_id ]
    tag :areas,          :child_key => [ :creator_id ]
    tag :infos,          :child_key => [ :creator_id ]
    tag :field_names,    :child_key => [ :creator_id ]
    #tag :publish_fields, :child_key => [ :creator_id ]
    tag :defaults,       :child_key => [ :creator_id ]
    tag :patterns,       :child_key => [ :creator_id ]
    tag :interpreters,   :child_key => [ :creator_id ]
    tag :datas,          :child_key => [ :creator_id ]
    tag :generators,     :child_key => [ :creator_id ]
    tag :gatherers,      :child_key => [ :creator_id ]
    tag :posts,          :child_key => [ :creator_id ]
    tag :urls,           :child_key => [ :creator_id ]
    tag :headers,        :child_key => [ :creator_id ]
    tag :cookie_headers, :child_key => [ :creator_id ]
    
    def full_name
      name
    end
    
    # Only the user can modify his/her own resource.
    def editable_by? (user)
      user == self ? true : false
    end
    
    def can_edit? (resource)
      resource.editable_by? self
    end

    # Protect name from reassignment
    before :name= do
      throw :halt unless name.nil?
    end
  end
  
  class InterpreterSourceData
    include Tagging
    tagging :interpreter, :data
  end

  class InterpreterTargetData
    include Tagging
    tagging :interpreter, :data
  end

  class GeneratorSourceData
    include Tagging
    tagging :generator, :data
  end
  
  class GeneratorTargetData
    include Tagging
    tagging :generator, :data
  end

  class GathererTargetData
    include Tagging
    tagging :gatherer, :data
  end
  
  class AreaLink
    include Tagging
    
    belongs_to :source, 'Area', :key => true
    belongs_to :target, 'Area', :key => true
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
    property :value, String, :default => '', :required => true
  end

  class Area
    include CreatedResource
    
    tag :defaults,  :through => Resource
    tag :datas,     :through => Resource
    
    has n, :area_links, :model => AreaLink, :child_key => [:source_id]
    tag :follow_areas,  :model => self, :through => :area_links, :via => :target
  end

  class PublishField
    include Tagging
    tagging :info, :field_name
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
    
    # Cross-product area/info/field_names
    def to_scraper
      object = []
      areas.each do |area|
        infos.each do |info|
          field_names.each do |field_name|
            object << [area.name, info.name, field_name.name]
          end
        end
      end
      object
    end
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

    def to_scraper
      _source_attributes, _target_attributes = [], []
      source_datas.each { |source_data| _source_attributes.push(*source_data.to_scraper) }
      target_datas.each { |target_data| _target_attributes.push(*target_data.to_scraper) }
      {
        :match_number => match_number,
        :terminate_on_complete => terminate_on_complete,
        :regexes => patterns.collect { |pattern| pattern.regex },
        :source_attributes => _source_attributes,
        :target_attributes => _target_attributes,
        :gatherers => gatherers.collect { |gatherer| gatherer.full_name }
      }
    end
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

    def to_scraper
      _source_attributes, _target_attributes = [], []
      source_datas.each { |source_data| _source_attributes.push(*source_data.to_scraper) }
      target_datas.each { |target_data| _target_attributes.push(*target_data.to_scraper) }
      {
        :regexes => patterns.collect { |pattern| pattern.regex },
        :source_attributes => _source_attributes,
        :target_attributes => _target_attributes,
        :gatherers => gatherers.collect { |gatherer| gatherer.full_name }
      }
    end
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
    
    def to_scraper
      _posts, _headers, _cookies, _target_field_names = {}, {}, {}, []
      _urls = self.urls.collect{ |url| url.value }
      self.posts.each do |post|
        _posts[post.post_name] = post.value
      end
      self.headers.each do |header|
        _headers[header.header_name] = header.value
      end
      self.cookie_headers.each do |cookie_header|
        _cookies[cookie_header.cookie_name] = cookie_header.value
      end
      {
        :urls => _urls,
        :posts => _posts,
        :headers => _headers,
        :cookies => _cookies,
        :target_attributes => target_datas.collect { |target_data| target_data.to_scraper }
      }
    end
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

DataMapper.finalize
DataMapper.auto_upgrade!
