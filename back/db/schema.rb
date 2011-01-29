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
DataMapper::Model.raise_on_save_failure = false
module SimpleScraper
  SimpleScraper::MAX_RECORDS = 100
  SimpleScraper::KEY_SEP = '.'
  def self.explode_key(key_string)
    key_string.split(SimpleScraper::KEY_SEP)
  end
  def self.join_key(*key_string)
    key_string.join(SimpleScraper::KEY_SEP)
  end
end

module DataMapper::Model
  def self.find(name)
    self.descendants.find { |model| model.raw_name == name }
  end

  def all_like(unfiltered)
    filtered = {}
    properties.each do |property|
      filtered[property.name.to_sym.like] = unfiltered[property.name] + '%' if unfiltered.include? property.name
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

  def criteria_from_key_string(*key_strings)
    Hash[key_names.zip(SimpleScraper::explode_key(SimpleScraper::join_key(*key_strings)))]
  end

  def first_from_string(*key_strings)
    first criteria_from_key_string *key_strings
  end

  def first_or_create_from_string(*key_strings)
    first_or_create criteria_from_key_string *key_strings
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
    '/' + model.raw_name + '/' + key.join('.')
  end
  
  def update_attributes(new_attributes)
    new_attributes.delete_if do |name, value| # Delete attributes not specified in the model
      not attributes.keys.include? name.downcase.to_sym
    end
    attributes= new_attributes
  end
  
  # Returns attributes, and lists of tags as arrays.
  def describe
    description = attributes.clone
    self.class.tag_names.each do |tag_name|
      description[tag_name.to_s + '/'] = []
      send(tag_name).all.each do |tag|
        description[tag_name.to_s + '/'] << tag.location
      end
    end
    description
  end
end

module SimpleScraper
  # All editable resources have a name, a description, a creator, and blessed editors.
  module Editable
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        property :creator_id, String, :key => true

        belongs_to :creator, :model => 'User', :key => true
        property :id, String, :key => true
        
        property :description, DataMapper::Property::Text
        
        tag :editors, :model => 'User', :through => DataMapper::Resource
      end
    end
  end

  class User
    include DataMapper::Resource
    
    property :id, String, :key => true
    
    tag :areas,          :child_key => [ :creator_id ]
    tag :infos,          :child_key => [ :creator_id ]
    tag :publishes,      :child_key => [ :creator_id ]
    tag :defaults,       :child_key => [ :creator_id ]
    tag :patterns,       :child_key => [ :creator_id ]
    tag :interpreters,   :child_key => [ :creator_id ]
    tag :generators,     :child_key => [ :creator_id ]
    tag :gatherers,      :child_key => [ :creator_id ]
    tag :posts,          :child_key => [ :creator_id ]
    tag :urls,           :child_key => [ :creator_id ]
    tag :headers,        :child_key => [ :creator_id ]
    tag :cookie_headers, :child_key => [ :creator_id ]
  end

  class GeneratorTargetArea
    include DataMapper::Resource
    tagging :generator, :area
  end
  
  class GeneratorTargetInfo
    include DataMapper::Resource
    tagging :generator, :info
    #belongs_to :generator, :key => true
    #belongs_to :info, :key => true
  end

  class GeneratorSourceArea
    include DataMapper::Resource
    tagging :generator, :area
    # belongs_to :generator, :key => true
    # belongs_to :area, :key => true
  end

  class GeneratorSourceInfo
    include DataMapper::Resource
    tagging :generator, :info
    #belongs_to :generator, :key => true
    #belongs_to :info, :key => true
  end

  class InterpreterTargetAttribute
    include DataMapper::Resource
    tagging :interpreter, :attribute
  end

  class InterpreterSourceAttribute
    include DataMapper::Resource
    tagging :interpreter, :attribute
  end
  
  class GeneratorTargetAttribute
    include DataMapper::Resource
    tagging :generator, :attribute
  end

  class GeneratorSourceAttribute
    include DataMapper::Resource
    tagging :generator, :attribute
  end
  
  class GathererTargetAttribute
    include DataMapper::Resource
    tagging :gatherer, :attribute
  end

  class GathererSourceAttribute
    include DataMapper::Resource
    tagging :gatherer, :attribute
  end
  
  class AreaLink
    include DataMapper::Resource
    
    belongs_to :source, 'Area', :key => true
    belongs_to :target, 'Area', :key => true
  end
  
  class Area
    include Editable
    
    tag :defaults, :through => Resource

    tag :gatherers, :through => Resource
    tag :interpreters, :through => Resource
    tag :generator_sources, 'Generator', :through => :generator_source_areas, :via => :generator
    has n, :generator_source_areas
    tag :generator_targets, 'Generator', :through => :generator_target_areas, :via => :generator
    has n, :generator_target_areas

    has n, :area_links, :model => AreaLink, :child_key => [:source_creator_id, :source_id]
    tag :follow_areas, :model => self, :through => :area_links, :via => :target
  end
  
  class Info
    include Editable
    
    tag :publishes, :through => Resource
    
    tag :gatherers, :through => Resource
    tag :interpreters, :through => Resource
    tag :generator_sources, 'Generator', :through => :generator_source_infos, :via => :generator
    has n, :generator_source_infos
    tag :generator_targets, 'Generator', :through => :generator_target_infos, :via => :generator
    has n, :generator_target_infos
  end

  class Attribute
    include Editable
    
    tag :generator_sources, 'Generator', :through => :generator_source_attributes, :via => :generator
    has n, :generator_source_attributes
    tag :generator_targets, 'Generator', :through => :generator_target_attributes, :via => :generator
    has n, :generator_target_attributes
    
    tag :interpreter_sources, 'Interpreter', :through => :interpreter_source_attributes, :via => :interpreter
    has n, :interpreter_source_attributes
    tag :interpreter_targets, 'Interpreter', :through => :interpreter_target_attributes, :via => :interpreter
    has n, :interpreter_target_attributes

    tag :gatherer_targets, 'Gatherer', :through => :gatherer_target_attributes, :via => :gatherer
    has n, :gatherer_target_attributes
  end

  class Publish # Applies to all an info's areas.
    include Editable
    
    tag :infos, :through => Resource

    property :name, String
  end
  
  class Default # Applies to all an area's infos.
    include Editable
    
    tag :areas, :through => Resource

    property :name, String
    property :value, String
  end

  class Interpreter
    include Editable

    tag :source_areas, 'Area', :through => Resource
    tag :source_infos, 'Info', :through => Resource
    tag :source_attributes, 'Attribute', :through => :interpreter_source_attributes, :via => :attribute
    has n, :interpreter_source_attributes

    #tag :gatherers, :through => Resource

    tag :target_attributes, 'Attribute', :through => :interpreter_target_attributes, :via => :attribute
    has n, :interpreter_target_attributes
    
    tag :patterns, :through => Resource
    
    property :match_number, Integer, :default => 0, :required => true
  end

  class Pattern
    include Editable
    
    tag :interpreters, :through => Resource
    
    property :regex, String
  end

  class Generator
    include Editable

    tag :source_areas, 'Area', :through => :generator_source_areas, :via => :area
    has n, :generator_source_areas
    tag :source_infos, 'Info', :through => :generator_source_infos, :via => :info
    has n, :generator_source_infos
    tag :source_attributes, 'Attribute', :through => :generator_source_attributes, :via => :attribute
    has n, :generator_source_attributes

    #tag :gatherers, :through => Resource

    tag :target_areas, 'Area', :through => :generator_target_areas, :via => :area
    has n, :generator_target_areas
    tag :target_infos, 'Info', :through => :generator_target_infos, :via => :info
    has n, :generator_target_infos
    tag :target_attributes, 'Attribute', :through => :generator_target_attributes, :via => :attribute
    has n, :generator_target_attributes
    
    tag :patterns, :through => Resource
  end

  class Gatherer
    include Editable

    tag :areas, :through => Resource
    tag :infos, :through => Resource
    
    #tag :generators, :through => Resource
    #tag :interpreters, :through => Resource
    
    tag :target_attributes, 'Attribute', :through => :gatherer_target_attributes, :via => :attribute
    has n, :gatherer_target_attributes
    
    tag :stops, 'Pattern', :through => Resource
    
    tag :urls, :through => Resource
    tag :posts, :through => Resource
    tag :headers, :through => Resource
    tag :cookie_headers, :through => Resource
  end
  
  class Url
    include Editable
    
    tag :gatherers, :through => Resource
    
    property :value, String
  end

  class Post
    include Editable

    tag :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

  class Header
    include Editable

    tag :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

  class CookieHeader
    include Editable

    tag :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

end

DataMapper.finalize
DataMapper.auto_migrate!
