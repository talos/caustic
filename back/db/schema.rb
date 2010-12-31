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

# Extend default String length from 50 to 255
DataMapper::Property::String.length(255)
DataMapper::Model.raise_on_save_failure = false

module DataMapper::Model
  def self.find_model (model_name)
    self.descendants.find { |c| c.name =~ Regexp.new('(^|:)' + model_name + '$', Regexp::IGNORECASE) }
  end
end

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

module SimpleScraper
  # All editable resources have a name, a description, a creator, and blessed editors.
  module Editable
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        belongs_to :creator, :model => 'User', :key => true
        property :name, String, :key => true
        
        property :description, DataMapper::Property::Text
        
        has n, :editors, :model => 'User', :through => DataMapper::Resource
        
        def inspect # inspect is taken over to return attributes, and lists of tags as arrays.
          inspection = attributes.clone
          inspection.delete_if { |k, v| [:creator_name].include?(k) }
          self.class.tag_types.each do |tag_type|
            inspection[tag_type] = []
            send(tag_type).all.each do |tag|
              #inspection[tag_name][tag.id] = tag.name
              inspection[tag_type].push(tag.name)
             end
          end
          inspection
        end
      end
    end
  end

  class User
    include DataMapper::Resource

    property :name, String, :key => true
  end
  
  class Area
    include Editable
  end
  
  class TargetArea
    include Editable
  end

  class Type
    include Editable
  end

  class TargetType
    include Editable
  end

  class Publish # Applies to all a type's areas.
    include Editable
    
    has n, :types, :through => Resource
  end
  
  class Default # Applies to all an area's types.
    include Editable
    
    has n, :areas, :through => Resource
    property :value, String
  end

  class Interpreter
    include Editable

    has n, :areas, :through => Resource
    has n, :types, :through => Resource

    property :source_attribute, String
    property :regex, String
    property :match_number, Integer
    property :target_attribute, String
  end

  class Generator
    include Editable

    has n, :areas, :through => Resource
    has n, :types, :through => Resource
    
    property :source_attribute, String
    property :regex, String
    has n, :target_areas, :through => Resource
    has n, :target_types, :through => Resource
    property :target_attribute, String
  end
  
  class Gatherer
    include Editable

    has n, :areas, :through => Resource
    has n, :types, :through => Resource
    # has n, :posts, :through => Resource
    # has n, :headers, :through => Resource
    # has n, :cookies, 'Cookie', :through => Resource

    property :url, Text
  end

  class Post
    include Editable

    has n, :gatherers, :through => Resource

    property :post_name,  String
    property :post_value, String
  end

  class Header
    include Editable

    has n, :gatherers, :through => Resource

    property :header_name,  String
    property :header_value, String
  end

  class Cookie
    include Editable

    has n, :gatherers, :through => Resource

    property :cookie_name,  String
    property :cookie_value, String
  end
end

DataMapper.finalize
#DataMapper.auto_upgrade!
