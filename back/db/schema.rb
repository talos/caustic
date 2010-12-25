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
  def self.find_collection (c_name)
    self.descendants.find { |c| c.to_s == 'SimpleScraper::' + c_name }
  end

end

module DataMapper::Model::Relationship
  def tags
    relationships.select { |k, r| r.class == DataMapper::Associations::ManyToMany::Relationship }
  end
  def tag_names
    tags.collect { |k, r| k }
  end
  def taggings
    relationships.select { |k, r| r.class == DataMapper::Associations::OneToMany::Relationship }
  end
  def tagging_names
    taggings.collect { |k, r| k }
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
        
        belongs_to :user
        #has n, :editors, :model => 'User', :through => DataMapper::Resource

        def inspect # inspect is taken over to return attributes, and lists of tags as arrays.
          inspection = attributes.clone
          inspection.delete(:user_id)
#          self.class.tags.each   { |k, r| inspection[k] = send(k).all.collect { |r| r.id }  }
            relationships.select { |k, r| r.class == DataMapper::Associations::ManyToMany::Relationship }\
            .each   { |k, r| inspection[k] = r.child_model.all.collect { |r| r.id }  }
          inspection
        end
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
    property :name, String
    
    has n, :interpreters
    has n, :generators
    has n, :gatherers
  end
  
  class Area
    include Editable

    property :name, String
    
    has n, :defaults, :through => Resource
  end

  class Type
    include Editable

    property :name, String

    has n, :publishes, :through => Resource
  end

  class Publish
    include Editable
    
    has n, :types, :through => Resource
    property :name, String
  end
  
  class Default
    include Editable
    
    has n, :areas, :through => Resource
    property :name, String
    property :value, String
  end

  class Interpreter
    include Taggable

    property :source_attribute, String
    property :regex, String
    property :match_number, String
    property :destination_attribute, String
    
  end

  class Generator
    include Taggable

    property :source_attribute, String
    property :regex, String, :key => true
    has n, :destination_areas, 'Area', :through => Resource
    has n, :destination_types, 'Type', :through => Resource
    property :destination_attribute, String
  end

  class Gatherer
    include Taggable

    has n, :urls, :through => Resource
    has n, :posts, :through => Resource
    has n, :headers, :through => Resource
    has n, :cookies, :model => 'Cookie', :through => Resource
  end

  class Url
    include Editable

    property :id, Serial
    has n, :gatherers, :through => Resource

    property :value, String
  end

  class Post
    include Editable

    property :id, Serial
    has n, :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

  class Header
    include Editable

    property :id, Serial
    has n, :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end

  class Cookie
    include Editable

    property :id, Serial
    has n, :gatherers, :through => Resource

    property :name,  String
    property :value, String
  end
end

DataMapper.finalize
DataMapper.auto_migrate!
