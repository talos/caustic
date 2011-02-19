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
      filtered[property.name.to_sym.like] = unfiltered[property.name] if unfiltered.include? property.name
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
    tag_relationships = relationships.select { |k, r| tag_names.include? k.to_sym }
    Hash[*tag_relationships.flatten]
  end
  def tag_models
    tag_models = relationships.select { |k, r| tag_names.include? k.to_sym }.collect do |relationship|
      [relationship[0], relationship[1].target_model]
    end
    Hash[*tag_models.flatten]
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
    '/' + model.raw_name + '/' + [*key].join('.')
  end
  
  def safe_attributes= (new_attributes)
    new_attributes.delete_if do |name, value| # Delete attributes not specified in the model
      not attributes.keys.include? name.downcase.to_sym
    end
    self.attributes=(new_attributes)
  end
  
  # Returns attributes, and lists of tags as arrays.
  def describe
    # puts 'describe'
    # puts attributes.to_json
    # puts 'did not crash'
    desc = attributes.clone
    self.class.tag_names.each do |tag_name|
      puts tag_name.to_json
      desc[tag_name.to_s + '/'] = {}
      send(tag_name).all.each do |tag|
        desc[tag_name.to_s + '/'][tag.location] = tag.attribute_get(:name)
      end
    end
    desc
  end
end

module SimpleScraper
  # All editable resources have a name, a description, a creator, and blessed editors.
  module Editable
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        property :id,   DataMapper::Property::Serial
        property :name, DataMapper::Property::String,
                 :required => true,
                 :unique   => true
        property :description, DataMapper::Property::Text
        
        belongs_to :creator, :model => 'User', :required => true
        tag :editors, :model => 'User', :through => DataMapper::Resource
      end
    end
  end
  
  class User
    include DataMapper::Resource
    
    property :id,   Serial
    property :name, String, :required => true
    
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
  end
  
  class InterpreterSourceData
    include DataMapper::Resource
    tagging :interpreter, :data
  end

  class InterpreterTargetData
    include DataMapper::Resource
    tagging :interpreter, :data
  end

  class GeneratorSourceData
    include DataMapper::Resource
    tagging :generator, :data
  end
  
  class GeneratorTargetData
    include DataMapper::Resource
    tagging :generator, :data
  end

  class GathererTargetData
    include DataMapper::Resource
    tagging :gatherer, :data
  end
  
  class AreaLink
    include DataMapper::Resource
    
    belongs_to :source, 'Area', :key => true
    belongs_to :target, 'Area', :key => true
  end

  class FieldName
    include Editable
    
    tag :datas,    :through => Resource
    tag :defaults, :through => Resource

    tag :infos, :through => :publish_fields, :via => :info
    has n, :publish_fields
  end

  class Default
    include Editable
    
    tag :areas,       :through => Resource
    tag :field_names, :through => Resource
    property :value, String, #:required => true,
                    :default => ''
  end

  class Area
    include Editable
    
    tag :defaults,  :through => Resource
    tag :datas,     :through => Resource
    #tag :gatherers, :through => Resource
    
    has n, :area_links, :model => AreaLink, :child_key => [:source_id]
    tag :follow_areas,  :model => self, :through => :area_links, :via => :target
  end

  class PublishField
    include DataMapper::Resource
    tagging :info, :field_name
  end

  class Info
    include Editable
    
    tag :publishes, 'FieldName', :through => :publish_fields, :via => :field_name
    has n, :publish_fields
    tag :datas,       :through => Resource
    #tag :gatherers,   :through => Resource
    #tag :interpreters,:through => Resource
  end

  class Data
    include Editable
    
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
    include Editable
    
    tag :source_datas, 'Data', :through => :interpreter_source_datas, :via => :data
    has n, :interpreter_source_datas
    tag :target_datas, 'Data', :through => :interpreter_target_datas, :via => :data
    has n, :interpreter_target_datas
    
    tag :patterns,  :through => Resource
    tag :gatherers, :through => Resource

    property :match_number, Integer, :default => 0, :required => true
    property :terminate_on_complete, Boolean, :default => false, :required => true

    def to_scraper
      {
        :match_number => match_number,
        :terminate_on_complete => terminate_on_complete,
        :regexes => patterns.collect { |pattern| pattern.regex },
        :source_attributes => source_datas.collect { |source_data| source_data.to_scraper },
        :target_attributes => target_datas.collect { |target_data| target_data.to_scraper }
      }
    end
  end
  
  class Pattern
    include Editable
    
    tag :interpreters, :through => Resource
    
    property :regex, String
  end

  class Generator
    include Editable

    tag :source_datas, 'Data', :through => :generator_source_datas, :via => :data
    has n, :generator_source_datas
    tag :target_datas, 'Data', :through => :generator_target_datas, :via => :data
    has n, :generator_target_datas
    
    tag :gatherers, :through => Resource
    tag :patterns,  :through => Resource

    def to_scraper
      {
        :regexes => patterns.collect { |pattern| pattern.regex },
        :source_attributes => source_datas.collect { |source_data| source_data.to_scraper },
        :target_attributes => target_datas.collect { |target_data| target_data.to_scraper }
      }
    end
  end
  
  class Gatherer
    include Editable
    
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
    include Editable
    
    tag :gatherers, :through => Resource
    
    property :value, String
  end

  class Post
    include Editable

    tag :gatherers, :through => Resource

    property :post_name,  String
    property :value, String
  end

  class Header
    include Editable

    tag :gatherers, :through => Resource

    property :header_name,  String
    property :value, String
  end

  class CookieHeader
    include Editable

    tag :gatherers, :through => Resource

    property :cookie_name,  String
    property :value, String
  end

end

DataMapper.finalize
DataMapper.auto_upgrade!
