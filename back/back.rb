#!/usr/bin/ruby

require 'rubygems'
require 'dm-core'
require 'dm-is-tree'
require 'dm-migrations'
require 'dm-constraints'
require 'dm-validations'
require 'json'
require 'sinatra'

DataMapper.setup(:default, 'sqlite://' + Dir.pwd + '/scraper.db')

# Default JSON for DataMapper.
class DataMapper::Collection
  def hash_array
    array = []
    
    self.each do |element|
      array.push(element.to_hash)
    end

    array
  end
end

module DataMapper::Resource
  def to_hash
    attributes
  end
end

class Namespace
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations
  
  is :tree
end

class Type
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations
#  has n, :publishFields, :model => 'Field'

  is :tree
end

class Information
  include DataMapper::Resource
  
  belongs_to :namespace, :key => true
  belongs_to :type, :key => true

  has n, :gatherers, :through => Resource
#  has n, :interpreters

#  has n, :defaultFields, :model => 'Field'

  def to_hash
    hash = {}
    
    hash['gatherers'] = gatherers.hash_array

    hash
  end
end

#class Field
#  include DataMapper::Resource
  
#  property id, Serial
#  property name, String, :required => true
#  property value, String

#  has n, :to_field
#  has n, :to_information
# end

#class ToField
#  include DataMapper::Resource

#  belongs_to 
#end

#class ToInformation
#  include DataMapper::Resource
#end

class Gatherer
  include DataMapper::Resource
  
  property :name,   String, :key => true

  has n, :urls
  has n, :gets
  has n, :posts
  has n, :headers
  has n, :cookies, :model => 'Cookie'

  is :tree, :order => :name

  def to_hash
    hash = {}

    hash['urls'] = urls.hash_array
    hash['gets'] = gets.to_hash
    hash['posts'] = posts.to_hash
    hash['headers'] = headers.to_hash
    hash['cookies'] = cookies.to_hash

    parents = []
    ancestors.each do |parent|
      parents.push(parent.to_hash)
    end
    
    hash['parents'] = parents
    
    hash
  end
end

class Url
  include DataMapper::Resource
  
  belongs_to :gatherer,  :key => true

  property :url, String, :key => true

  def self.hash_array
    array = []
    self.each do |element|
      array.push(element.attribute_get(:url))
    end
    array
  end
end

module RequestAttribute
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true  
  
  property :name,  String,   :key => true
  property :value, String,   :required => true

  def self.to_hash
    hash = {}
    self.each do |element|
      hash[element.attribute_get(:name)] = element.attribute_get(:value)
    end
    hash
  end
end

class Get
  include DataMapper::Resource
  include RequestAttribute
end

class Post
  include DataMapper::Resource
  include RequestAttribute
end

class Header
  include DataMapper::Resource
  include RequestAttribute
end

class Cookie
  include DataMapper::Resource
  include RequestAttribute
end

DataMapper.finalize
DataMapper.auto_migrate!

# GET
get '/namespace/' do
  Namespace.all.hash_array.to_json
end

get '/type/' do
  Type.all.hash_array.to_json
end

get '/information/' do
  Information.all.hash_array.to_json
end

get '/information/:namespace/:type' do
  Information.first(:namespace_name => params[:namespace], :type_name => params[:type]).to_hash.to_json
end

get '/gatherer/' do
  Gatherer.all.hash_array.to_json
end


# POST

post '/namespace/:name' do
  if(!Namespace.first(:name => params[:name]))
    @namespace = Namespace.new(:name => params[:name])
    
    if(params[:parent])
      @parent = Namespace.first(:name => params[:parent])
      if(@parent) 
        @namespace.parent = @parent
      end
    end
    @namespace.save.to_s
  end
end

post '/namespace/:child/parent/:parent' do
  @child = Namespace.first(:name => params[:child])
  @parent = Namespace.first(:name => params[:parent])
  @child.parent = @parent
  @child.save.to_s
end

post '/type/:name' do
  if(!Type.first(:name => params[:name]))
    @type = Type.new(:name => params[:name])
    
    if(params[:parent])
      @parent = Type.first(:name => params[:parent])
      if(@parent) 
        @type.parent = @parent
      end
    end
    @type.save.to_s
  end
end

post '/type/:name/parent/:parent' do
  @child = Type.first(:name => params[:child])
  @parent = Type.first(:name => params[:parent])
  @child.parent = @parent
  @child.save.to_s
end

post '/information/:namespace/:type' do
  @information = Information.new
  @namespace = Namespace.first(:name => params[:namespace])
  @type = Type.first(:name => params[:type])

  @namespace.informations << @information
  @type.informations << @information

  @information.save.to_s + ', ' + @type.save.to_s + ', ' + @namespace.save.to_s
end

post '/information/:namespace/:type/gatherer/:gatherer' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @gatherer = Gatherer.first(:name => params[:gatherer])

  @information.gatherers << @gatherer
  @information.save.to_s
end

post '/gatherer/:name' do
  @gatherer_name = params[:name]

  if(!Gatherer.first(:name => @gatherer_name))
    @gatherer = Gatherer.new(:name => @gatherer_name)

    if(@parent_name)
      @parent = Gatherer.first(:name => params[:parent])
      @gatherer.parent = @parent
    end
    if(@url_value)
      @gatherer.urls.new(:url => @url_value)
    end
    @gatherer.save.to_s
  end
end

post '/gatherer/:child/parent/:parent' do
  @child =  Gatherer.first(:name => params[:child])
  @parent = Gatherer.first(:name => params[:parent])
  
  @child.parent = @parent
  @child.save.to_s
end

post '/gatherer/:gatherer/url/:url' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.urls.new(:url => params[:url])
  @gatherer.save.to_s
end

post '/gatherer/:gatherer/get/:name/:value' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.gets.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

post '/gatherer/:gatherer/post/:name/:value' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.posts.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

post '/gatherer/:gatherer/header/:name/:value' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.headers.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

post '/gatherer/:gatherer/cookie/:name/:value' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.cookies.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

