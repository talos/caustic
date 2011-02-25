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
require 'dm-constraints'
require 'dm-validations'
require 'json'

DataMapper.setup(:default, 'sqlite://' + Dir.pwd + '/db.sqlite')

class DataMapper::Validations::ValidationErrors
  def to_a
    collect{ |error| error.to_s }
  end
  def to_json
    to_a.to_json
  end
end

# Extend default String length from 50 to 500
DataMapper::Property::String.length(500)
#DataMapper::Model.raise_on_save_failure = true
module SimpleScraper
  SimpleScraper::MAX_RECORDS = 100
end

module DataMapper::Model
  def self.find(name)
    self.descendants.find { |model| model.raw_name == name }
  end

  def all_like(unfiltered)
    filtered = {}
    properties.each do |property|
      filtered[property.name.to_sym.like] = unfiltered[property.name.to_s] if unfiltered.include? property.name.to_s
    end
    all({:limit => SimpleScraper::MAX_RECORDS}.merge(filtered))
  end

  def key_names
    key.collect { |k| k.name }
  end
  
  def raw_name
    name.sub(%r{^[^:]*:+}, '').downcase
  end
  
  def location
    '/' + raw_name + '/'
  end

  def criteria_from_key(*key)
    Hash[key_names.zip(key)]
  end

  def first_from_key(*key)
    if(not key)
      return first
    end
    first criteria_from_key *key
  end

  def first_or_new_from_key(*key)
    if(not key)
      return first_or_new
    end
    first_or_new criteria_from_key *key
  end
  
  def first_or_new_from_name(name)
    first_or_new(:name => name)
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
  def tag_relationships
    tag_relationships = relationships.select { |k, r| tag_names.include? k.to_sym }.collect do |relationship|
      [relationship[0].to_sym, relationship[1]]
    end
    Hash[*tag_relationships.flatten]
  end
  def tag_target_models
    tag_target_models = relationships.select { |k, r| tag_names.include? k.to_sym }.collect do |relationship|
      [relationship[0].to_sym, relationship[1].target_model]
    end
    Hash[*tag_target_models.flatten]
  end
  # Helps make tagging classes.
  def tagging(model1, model2)
    belongs_to model1, :key => true
    belongs_to model2, :key => true
  end
end

# Convenience methods for describing resources.
module DataMapper::Resource
  def location
    model.raw_name + '/' + [*key].join('.')
  end
  
  
  # Returns attributes, and lists of tags as arrays.
  def describe
    desc = attributes.clone
    self.class.tag_names.each do |tag_name|
      #desc[tag_name.to_s + '/'] = {}
      desc[tag_name.to_s + '/'] = []
      send(tag_name).all.each do |tag|
        #desc[tag_name.to_s + '/'][tag.attribute_get(:id)] = tag.full_name
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

module SimpleScraper
  # All resources have a name, a description, a creator, blessed editors, and paranoia.
  module EditableResource
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        property :id,   DataMapper::Property::Serial, :accessor => :private
        property :name, DataMapper::Property::String, :required => true, :default => lambda { |r,p| r.model.raw_name }
        property :description, DataMapper::Property::Text
        
        property :created_at, DataMapper::Property::DateTime, :writer => :private, :default => lambda { |r,p| Time.now }, :accessor => :private
        property :modified_at, DataMapper::Property::DateTime, :accessor => :private
        property :deleted_at, DataMapper::Property::ParanoidDateTime, :accessor => :private
        
        # Destroy tags before destroying resource.
        before :destroy do |resource|
          puts 'destroy'
          model.tag_relationships.each do |name, relationship|
            puts name
            puts relationship.class
            if(relationship.class == DataMapper::Associations::ManyToMany::Relationship)
            #relationship.through.target_model.all.each { |link| link.destroy }
              send(name).all.each do |tag|
                puts name
                puts tag.describe.to_json
                untag(name, tag)
              end
            end
          end
        end

        # Keep track of changes.
        after :save do
          puts 'after save'
          modified_at = Time.now
        end

        validates_uniqueness_of :name, :deleted_at
      end

      # Safely change a resource's attributes.
      def safe_attributes= (new_attributes)
        new_attributes.delete_if do |name, value| # Delete attributes not specified in the model
          not attributes.keys.include? name.downcase.to_sym
        end
        new_attributes.delete_if do |name, value|
          private_methods.include? name + '=' # Remove private attributes.
        end
        self.attributes=(new_attributes)
      end
      
      # Tag
      def tag (tag_name, tag)
        raise DataMapper::UnknownRelationshipError unless model.tag_target_models[tag_name] == tag.model
        send(tag_name) << tag
        save
        modified_at = Time.now
        self
      end

      # Untag
      def untag (tag_name, tag)
        raise DataMapper::UnknownRelationshipError unless model.tag_target_models[tag_name] == tag.model
        model.tag_relationships[tag_name].through.target_model.get(*[key, tag.key].flatten).destroy
        reload
        modified_at = Time.now
        self
      end
    end
  end
  
  # A Resource owned by a creator, with editors.
  module CreatedResource
    def self.included(base)
      base.class_eval do
        include EditableResource
        
        belongs_to :creator, :model => 'User', :required => true, :accessor => :private
        property :creator_id, DataMapper::Property::Integer, :required => true, :accessor => :private
        
        tag :editors, :model => 'User', :through => DataMapper::Resource
      end
      
      def full_name
        creator.name + "'s " + name
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
    #tag :gatherers, :through => Resource
    
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
    #tag :gatherers,   :through => Resource
    #tag :interpreters,:through => Resource
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
    
    property :regex, String
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
    
    property :value, String
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
