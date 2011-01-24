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

module DataMapper::Model
  def self.find_model (model_name)
    self.descendants.find { |c| c.name =~ Regexp.new('(^|:)' + model_name + '$', Regexp::IGNORECASE) }
  end
end

# Convenience methods for collecting tags.
module DataMapper::Model::Relationship
  def tags
    relationships.select { |k, r| r.class == DataMapper::Associations::ManyToMany::Relationship }
  end
  def tag_types
    tags.collect { |k, r| k }
  end
  def taggings
    relationships.select { |k, r| r.class == DataMapper::Associations::OneToMany::Relationship }
  end
  def tagging_types
    taggings.collect { |k, r| k }
  end
end

# Convenience methods for describing resources.
module DataMapper::Resource
  def location
    '/' + model.name.sub(/^[^:]*:+/, '').downcase + '/' + key.join('/')
  end

  # Returns attributes, and lists of tags as arrays.
  def describe
    description = attributes.clone
    self.class.tag_types.each do |tag_type|
      description[tag_type] = []
      send(tag_type).all.each do |tag|
        description[tag_type].push(tag.attribute_get(:id))
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
        
        has n, :editors, :model => 'User', :through => DataMapper::Resource
      end
    end
  end

  class User
    include DataMapper::Resource
    
    property :id, String, :key => true
    
    has n, :areas,          :child_key => [ :creator_id ]
    has n, :infos,          :child_key => [ :creator_id ]
    has n, :publishes,      :child_key => [ :creator_id ]
    has n, :defaults,       :child_key => [ :creator_id ]
    has n, :interpreters,   :child_key => [ :creator_id ]
    has n, :generators,     :child_key => [ :creator_id ]
    has n, :gatherers,      :child_key => [ :creator_id ]
    has n, :posts,          :child_key => [ :creator_id ]
    has n, :urls,           :child_key => [ :creator_id ]
    has n, :headers,        :child_key => [ :creator_id ]
    has n, :cookie_headers, :child_key => [ :creator_id ]
    
    # Unlike other resources, user qualities are described through one-to-many relationships.
    def describe

    end
  end

  class GeneratorTargetArea
    include DataMapper::Resource

    belongs_to :generator, :key => true
    belongs_to :area, :key => true
  end

  class GeneratorTargetInfo
    include DataMapper::Resource

    belongs_to :generator, :key => true
    belongs_to :info, :key => true
  end

  class GeneratorSourceArea
    include DataMapper::Resource

    belongs_to :generator, :key => true
    belongs_to :area, :key => true
  end

  class GeneratorSourceInfo
    include DataMapper::Resource

    belongs_to :generator, :key => true
    belongs_to :info, :key => true
  end

  class AreaTagging
    include DataMapper::Resource
    
    # property :source_creator_id, String, :key => true
    # property :source_id, String, :key => true
    # property :target_creator_id, String, :key => true
    # property :target_id, String, :key => true
    
    belongs_to :source, 'Area', :key => true
    belongs_to :target, 'Area', :key => true
  end
  
  class Area
    include Editable
    
    has n, :defaults, :through => Resource

    has n, :gatherers, :through => Resource
    has n, :interpreters, :through => Resource
    #has n, :generators, :through => Resource
    has n, :generator_sources, 'Generator', :through => :generator_source_areas, :via => :generator
    has n, :generator_source_areas
    has n, :generator_targets, 'Generator', :through => :generator_target_areas, :via => :generator
    has n, :generator_target_areas

    has n, :area_taggings, :model => AreaTagging, :child_key => [:source_creator_id, :source_id]
    has n, :areas, :model => self, :through => :area_taggings, :via => :target
  end
  
  class Info
    include Editable
    
    has n, :publishes, :through => Resource

    has n, :gatherers, :through => Resource
    has n, :interpreters, :through => Resource
    #has n, :generators, :through => Resource
    has n, :generator_sources, 'Generator', :through => :generator_source_infos, :via => :generator
    has n, :generator_source_infos
    has n, :generator_targets, 'Generator', :through => :generator_target_infos, :via => :generator
    has n, :generator_target_infos
  end

  class Publish # Applies to all an info's areas.
    include Editable
    
    has n, :infos, :through => Resource

    property :name, String
  end
  
  class Default # Applies to all an area's infos.
    include Editable
    
    has n, :areas, :through => Resource

    property :name, String
    property :value, String
  end
  
  class Interpreter
    include Editable

    has n, :source_areas, 'Area', :through => Resource
    has n, :source_infos, 'Info', :through => Resource

    has n, :gatherers, :through => Resource

    property :source_attribute, String
    property :regex, String
    property :match_number, Integer, :default => 0, :required => true
    property :target_attribute, String
  end

  class Generator
    include Editable

    #has n, :areas, :through => Resource
    #has n, :infos, :through => Resource
    has n, :source_areas, 'Area', :through => :generator_source_areas, :via => :area
    has n, :generator_source_areas
    has n, :source_infos, 'Info', :through => :generator_source_infos, :via => :info
    has n, :generator_source_infos

    has n, :gatherers, :through => Resource
    
    #property :target_area, String
    #property :target_info, String
    has n, :target_areas, 'Area', :through => :generator_target_areas, :via => :area
    has n, :generator_target_areas
    has n, :target_infos, 'Info', :through => :generator_target_infos, :via => :info
    has n, :generator_target_infos

    property :target_attribute, String

    property :source_attribute, String
    property :regex, String
  end

  # class CookieGatherer
  #   include DataMapper::Resource

  #   property :cookie_creator_id, String, :key => true
  #   property :cookie_id, String, :key => true
  #   belongs_to :cookie, 'Cookie',
  #     :parent_key => [:creator_id, :id],
  #     :child_key => [:cookie_creator_id, :cookie_id],
  #     :required => true

  #   property :gatherer_creator_id, String, :key => true
  #   property :gatherer_id, String, :key => true
  #   belongs_to :gatherer, 'Gatherer',
  #     :parent_key => [:creator_id, :id],
  #     :child_key => [:gatherer_creator_id, :gatherer_id],
  #     :required => true
  # end

  class Gatherer
    include Editable

    has n, :areas, :through => Resource
    has n, :infos, :through => Resource

    has n, :generators, :through => Resource
    has n, :interpreters, :through => Resource

    has n, :urls, :through => Resource
    has n, :posts, :through => Resource
    has n, :headers, :through => Resource
    has n, :cookie_headers, :through => Resource

    # has n, :cookie_taggings, 'CookieGatherer',
    #   :parent_key => [:creator_id, :id],
    #   :child_key => [:gatherer_creator_id, :gatherer_id]
    
    # has n, :cookies, 'Cookie', :through => :cookie_taggings, :via => :cookie

    #property :url, String
  end
  
  class Url
    include Editable
    
    has n, :gatherers, :through => Resource
    
    property :value, String
  end

  class Post
    include Editable

    has n, :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

  class Header
    include Editable

    has n, :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

  class CookieHeader
    include Editable

    has n, :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

end

DataMapper.finalize
DataMapper.auto_migrate!
