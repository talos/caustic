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
require 'dm-is-tree'
require 'dm-migrations'
require 'dm-constraints'
require 'dm-validations'
require 'json'

DataMapper.setup(:default, 'sqlite://' + Dir.pwd + '/db.sqlite')

# Default JSON for DataMapper.
class DataMapper::Collection
  # Convert a collection of resources to a hash, using specified key and value.
  def to_hash(key, value)
    hash = {}
    all.each { |resource| hash[resource.attribute_get(key)] = resource.attribute_get(value) }
    hash
  end

  # Get an array with the identity of each element.
  def identify_all(query = DataMapper::Undefined)
    all(query).collect { |resource| resource.identify }
  end
end

module DataMapper::Model
  def identify_all(query = DataMapper::Undefined)
    all(query).collect { |resource| resource.identify }
  end
end

module DataMapper::Resource
  def to_json
    export.to_json
  end
  
  def identify
    attribute_get(:name) or nil
  end

  def export
    #attribute_get(:value) or nil
    attributes
  end
end

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

module SimpleScraper
  class Area
    include DataMapper::Resource

    property :name, String, :key => true
    
    has n, :types, :through => :informations
    has n, :informations, :through => Resource

    has n, :default_fields

    def export
      {
        'default/' => default_fields.identify_all
      }
    end
  end

  class Type
    include DataMapper::Resource

    property :name, String, :key => true

    has n, :informations #, :unique => true
    has n, :areas, :through => :informations
    has n, :publish_fields #, :unique => true
    
    def export
      {
        'publish/' => publish_fields.identify_all,
        'information/' => informations.identify_all
      }
    end
  end

  class PublishField
    include DataMapper::Resource

    belongs_to :type, :key => true
    property :name, String, :key => true

    def export
      nil
    end
  end

  class Information
    include DataMapper::Resource
    
    belongs_to :type, :key => true
    property :name, String, :key => true
    has n, :areas, :through => Resource #, :unique => true
    
    has n, :gatherers, :through => Resource
#    has n, :to_fields
#    has n, :to_informations
    has n, :fields
    
    def export
      #    hash[:default_fields] = default_fields.to_hash(:name, :value)
      #    hash[:gatherers] = gatherers.collect { |gatherer| gatherer.export }
      #    hash[:to_fields] = to_fields.collect { |to_field| to_field.export }
      #    hash[:to_informations] = to_informations.collect { |to_information| to_information.export }
      {
        'gatherer/' => gatherers.identify_all,
#        'fields/' => [to_fields.identify_all, to_informations.identify_all].flatten,
        'fields/' => fields.identify_all,
        'areas/' => areas.identify_all
      }
    end
  end

  class DefaultField
    include DataMapper::Resource

    belongs_to :area, :key => true
#    property :name, String, :key => true
    belongs_to :field, :key => true
    property :value, String

  end

  class Field
    include DataMapper::Resource

    belongs_to :information, :key => true
    property :name, String, :key => true

    has n, :to_fields
    has n, :to_informations
  end

  class ToField
    include DataMapper::Resource

    belongs_to :from_field, 'Field', :key => true
    belongs_to :to_field, 'Field', :key => true

    property :regex, String
  end

  class ToInformation

  class ToField
    include DataMapper::Resource
    
    belongs_to :information, :key => true
    property :input_field, String, :key => true
    property :match_number, Integer, :key => true
    property :regex, String
    property :destination_field, String, :key => true

    def identify
      input_field + '/' + match_number.to_s + '/to/' + destination_field
    end

    def export
      {
        :input_field  => attribute_get(:input_field),
        :match_number => attribute_get(:match_number).to_s,
        :regex        => attribute_get(:regex),
        :destination_field => attribute_get(:destination_field)
      }
    end
  end

  class ToInformation
    include DataMapper::Resource
    
    belongs_to :information, :key => true
    property :input_field, String, :key => true
    property :regex, String
    belongs_to :destination_information, :model => 'Information', :key => true
    property :destination_field, String, :key => true

    def identify
      input_field + '/to/' + destination_information.identify + destination_field
    end
    
    def export
      {
        :input_field => attribute_get(:input_field),
        :regex => attribute_get(:regex),
        :destination_information => destination_information.identify,
        :destination_field => attribute_get(:destination_field)
      }
    end
  end

  class Gatherer
    include DataMapper::Resource
    
    property :name,   String, :key => true

    has n, :urls
    has n, :gets
    has n, :posts
    has n, :headers
    has n, :cookies, :model => 'Cookie'

    is :tree, :order => :name

    def identify
      attribute_get(:name)
    end

    def export
      #    hash[:urls] = urls.collect { |url| url.value }
      #    hash[:gets] = gets.to_hash(:name, :value)
      #    hash[:posts] = posts.to_hash(:name, :value)
      #    hash[:headers] = headers.to_hash(:name, :value)
      #    hash[:cookies] = cookies.to_hash(:name, :value)
      
      #    hash[:parents] = ancestors.collect { |parent| parent.export }
      {
        'url/' => urls.identify_all,
        'cookie/' => gets.identify_all,
        'post/' => posts.identify_all,
        'header/' => headers.identify_all,
        'cookie/' => cookies.identify_all,
        
        #      :gatherer => ancestors.collect{ |ancestor| ancestor.identify }
        #      :parent => parent ? parent.attribute_get(:name) : nil
      }
    end
  end

  class Url
    include DataMapper::Resource
    
    belongs_to :gatherer,  :key => true

    property :name, String, :key => true

    def identify
      attribute_get(:name)
    end
  end

  class Get
    include DataMapper::Resource
    
    belongs_to :gatherer, :key => true
    property :name,  String, :key => true
    property :value, String

    def identify
      attribute_get(:name)
    end
  end

  class Post
    include DataMapper::Resource
    
    belongs_to :gatherer, :key => true
    property :name,  String, :key => true
    property :value, String

    def identify
      attribute_get(:name)
    end
  end

  class Header
    include DataMapper::Resource
    
    belongs_to :gatherer, :key => true
    property :name,  String, :key => true
    property :value, String

    def identify
      attribute_get(:name)
    end
  end

  class Cookie
    include DataMapper::Resource
    
    belongs_to :gatherer, :key => true
    property :name,  String, :key => true
    property :value, String

    def identify
      attribute_get(:name)
    end
  end
end

DataMapper.finalize
DataMapper.auto_migrate!
