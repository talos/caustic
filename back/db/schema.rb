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
  def self.editable_descendants
    self.descendants
  end

  def self.find_resource (resource_name)
    self.descendants.find { |resource| resource.to_s == 'SimpleScraper::' + resource_name }
  end
end

module SimpleScraper

  # All editable resources have an ID, a description, a creator, and blessed editors.
   module Editable
    def self.included(base)
      base.class_eval do
        include DataMapper::Resource
        
        property :id, DataMapper::Property::Serial
        property :description, DataMapper::Property::Text
        
        belongs_to :creator, 'User'
        has n, :editors, 'User', :through => DataMapper::Resource
      end
    end
  end
  
  module Taggable
    def self.included(base)
      base.class_eval do
        include SimpleScraper::Editable
        
        has n, :areas, :through => DataMapper::Resource
        has n, :types, :through => DataMapper::Resource
      end
    end
  end

  class User
    include DataMapper::Resource
    
    property :id, Serial
    
    has n, :interpreters
    has n, :generators
    has n, :gatherers
  end
  
  class Area
    include DataMapper::Resource

    property :name, String, :key => true
    
    has n, :defaults, :through => Resource
  end

  class Type
    include DataMapper::Resource

    property :name, String, :key => true

    has n, :publishes, :through => Resource
  end

  class Publish
    include Editable
    
    belongs_to :type
    property :name, String
  end
  
  class Default
    include Editable
    
    belongs_to :area
    property :name, String
    property :value, String
  end

  class Interpreter
#    include Editable
    include Taggable

#    property :id, Serial

    property :source_attribute, String
    property :regex, String
    property :match_number, String
    property :destination_attribute, String
  end

  class Generator
#    include Editable
    include Taggable

#    property :id, Serial

    property :source_attribute, String
    property :regex, String, :key => true
    has n, :destination_areas, 'Area', :through => Resource
    has n, :destination_types, 'Type', :through => Resource
    property :destination_attribute, String
  end

  class Gatherer
#    include Editable
    include Taggable

#    property :id, Serial

    has n, :urls
    has n, :posts
    has n, :headers
    has n, :cookies, :model => 'Cookie'
  end

  class GathererAttribute
    include DataMapper::Resource

    property :id, Serial
    belongs_to :gatherer
    
    property :value, String

    property :type, Discriminator
  end

  class Url < GathererAttribute
  end

  class Post < GathererAttribute
    property :name,  String
  end

  class Header < GathererAttribute
    property :name,  String
  end

  class Cookie < GathererAttribute
    property :name,  String
  end
end

DataMapper.finalize
DataMapper.auto_migrate!
