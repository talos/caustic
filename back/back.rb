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
  def to_hash(key, value)
    keys = all.collect { |element| element.attribute_get(key) }
    values = all.collect { |element| element.attribute_get(value) }
    Hash[keys.zip(values)]
  end
end

module DataMapper::Resource
  def to_json
    attributes.to_json
  end
end

class Namespace
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations

  is :tree

  def attributes
    hash = {}
    
    hash[:parent] = parent ? parent.attributes[:name] : nil
    hash[:informations] = informations.collect {|information| information.attributes[:type_name] }

    hash
  end
end

class Type
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations
  has n, :publish_fields
  
  is :tree

  def attributes
    hash = {}
    
    hash[:parent] = parent ? parent.attributes[:name] : nil
    hash[:informations] = informations.collect {|information| information.attributes[:namespace] }
    hash[:publishFields] = publish_fields.collect {|publishField| publishField.name }

    hash
  end
end

class PublishField
  include DataMapper::Resource

  belongs_to :type, :key => true
  property :name, String, :key => true
end

class Information
  include DataMapper::Resource
  
  belongs_to :namespace, :key => true
  belongs_to :type, :key => true

  has n, :default_fields
  
  has n, :gatherers, :through => Resource
  has n, :to_fields
  has n, :to_informations

  def attributes
    hash = {}
    
    hash[:gatherers] = gatherers.collect {|gatherer| gatherer.attributes}
    hash[:toFields] = to_fields.collect {|toField|  toField.attributes}
    hash[:toInformations] = to_informations.collect {|toInformation| toInformation.attributes}
    
    hash
  end
end

class DefaultField
  include DataMapper::Resource

  belongs_to :information, :key => true
  property :name, String, :key => true
  property :value, String
end

class ToField
  include DataMapper::Resource
  
  belongs_to :information, :key => true
  property :input_field, String, :key => true
  property :match_number, Integer, :key => true
  property :regex, String
  property :destination_field, String, :key => true
end

class ToInformation
  include DataMapper::Resource
  
  belongs_to :information, :key => true
  property :input_field, String, :key => true
  property :regex, String
  belongs_to :destination_information, :model => 'Information', :key => true
  property :destination_field, String, :key => true
end

class Gatherer
  include DataMapper::Resource
  
  property :name,   String, :key => true

  has n, :urls
  has n, :gets
  has n, :posts
  has n, :headers
  has n, :cookies, 'Cookie'

  is :tree, :order => :name

  def attributes
    hash = {}

    hash[:urls] = urls.collect { |url| url.value }
    hash[:gets] = gets.to_hash(:name, :value)
    hash[:posts] = posts.to_hash(:name, :value)
    hash[:headers] = headers.to_hash(:name, :value)
    hash[:cookies] = cookies.to_hash(:name, :value)

    hash[:parents] = ancestors.collect { |parent| parent.attributes }
    
    hash
  end
end

class Url
  include DataMapper::Resource
  
  belongs_to :gatherer,  :key => true

  property :value, String, :key => true
end

class Get
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String
end

class Post
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String
end

class Header
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String
end

class Cookie
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String
end

DataMapper.finalize
DataMapper.auto_migrate!

# CONFIG
configure do
  set :show_exceptions, false
end

# GET

# Get a list of all the available namespaces.
get '/namespace/' do
  Namespace.all.collect {|namespace| namespace.attributes[:name] }.to_json
end

# Get details on a specific namespace.
get '/namespace/:name' do
  Namespace.first(:name => params[:name]).to_json
end

# Get a list of all the available types.
get '/type/' do
  Type.all.collect {|namespace| namespace.attributes[:name] }.to_json
end

# Get details on a specific type.
get '/type/:name' do
  Type.first(:name => params[:name]).to_json
end

# Get a list of all the available Information.
get '/information/' do
  Information.all.collect {|information| [information.attributes[:namespace], information.attributes[:type]] }.to_json
end

# Get details on a specific Information.
get '/information/:namespace/:type' do
  Information.first(:namespace_name => params[:namespace], :type_name => params[:type]).to_json
end

# Get a list of all the available Gatherers.
get '/gatherer/' do
  Gatherer.all.collect {|gatherer| gatherer.attributes[:name]}.to_json
end

# Get details on a specific Gatherer.
get '/gatherer/:name' do
  Gatherer.first(:name => params[:name]).to_json
end

# POST / DELETE

# Create a new namespace.
post '/namespace/:name' do
  @namespace = Namespace.new(:name => params[:name])
  
  @namespace.save.to_s
end

# Delete a namespace.  This can only be done if there are no dependencies.
delete '/namespace/:name' do
  @namespace = Namespace.first(:name => params[:name])
  @namespace.delete
  @namespace.save.to_s
end

# Add a parent to a namespace.
post '/namespace/:child/parent/:parent' do
  @child = Namespace.first(:name => params[:child])
  @parent = Namespace.first(:name => params[:parent])
  @child.parent = @parent
  @child.save.to_s
end

# Remove a namespace's parent relationship.
# Does not eliminate the parent.
delete '/namespace/:child/parent' do
  @child = Namespace.first(:name => params[:child])
  @child.parent.delete
  @child.save.to_s
end

# Add a child to a namespace.
post '/namespace/:parent/child/:child' do
  @child = Namespace.first(:name => params[:child])
  @parent = Namespace.first(:name => params[:parent])
  @child.parent = @parent
  @child.save.to_s  
end

# Eliminate the relationship between a namespace and one of its children.
# Does not eliminate the child.  Fails if the child is not a child of the
# specified parent.
delete '/namespace/:parent/child/:child' do
  @parent = Namespace.first(:name => params[:parent])
  @parent.children.first(:name => params[:child]).delete
  @parent.save.to_s
end

# Create a new type.
post '/type/:name' do
  @type = Type.new(:name => params[:name])
  @type.save.to_s
end

# Delete a type.  This can only be done if there are no dependencies.
delete '/type/:name' do
  @type = Type.first(:name => params[:name])
  @type.delete
  @type.save.to_s
end

# Add a parent to a Type.
post '/type/:child/parent/:parent' do
  @child = Type.first(:name => params[:child])
  @parent = Type.first(:name => params[:parent])
  @child.parent = @parent
  @child.save.to_s
end

# Remove a Type's parent relationship.  Does not eliminate either the child or parent Type.
delete '/type/:child/parent' do
  @child = Type.first(:name => params[:child])
  @child.parent.delete
  @child.save.to_s
end

# Add a child to a Type.
post '/type/:parent/child/:child' do
  @child = Type.first(:name => params[:child])
  @parent = Type.first(:name => params[:parent])
  @child.parent = @parent
  @child.save.to_s
end

# Remove a Type's child relationship.  Does not eliminate either the child or parent Type.
# Fails if the specified child is not a child of the specified parent.
delete '/type/:parent/child/:child' do
  @parent = Type.first(:name => params[:parent])
  @parent.children.first(:name => params[:child]).delete
  @parent.save.to_s
end

# Add a publishField to a type.
post '/type/:type/publishField/:publishField' do
  @type = Type.first(:name => params[:type])
  @publishField = PublishField.new(:name => params[:publishField])
  @type.publish_fields << @publishField
  @type.save.to_s
end

# Delete a publishField from a type.
delete '/type/:type/publishField/:publishField' do
  @type = Type.first(:name => params[:type])
  @type.publish_fields.first(:name => params[:publishField]).destroy
  @type.save.to_s
end

# Create a new Information.
post '/information/:namespace/:type' do
  @information = Information.new
  @namespace = Namespace.first(:name => params[:namespace])
  @type = Type.first(:name => params[:type])

  @namespace.informations << @information
  @type.informations << @information

#  puts @type.save.to_s
#  puts @namespace.save.to_s
  
  @information.save.to_s
#  @information.save.to_s + ', ' + @type.save.to_s + ', ' + @namespace.save.to_s
end

# Delete an Information.
delete '/information/:namespace/:type' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.destroy.to_s
end

# Add a gatherer to an Information.
post '/information/:namespace/:type/gatherer/:gatherer' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @gatherer = Gatherer.first(:name => params[:gatherer])

  @information.gatherers << @gatherer
  @information.save.to_s
end

# Add a DefaultField to an Information.
post '/information/:namespace/:type/defaultField/:name' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @defaultField = DefaultField.new(:name => params[:name], :value => params[:value])
  @information.default_fields << @defaultField
  @information.save.to_s
end

# Delete a DefaultField from an Information.
delete '/information/:namespace/:type/defaultField/:name' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.default_fields.first(:name => params[:name]).delete
  @information.save.to_s
end

# Add a ToField to an Information.  Regex is inside the post.
post '/information/:namespace/:type/:input_field/:match_number/to/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @toField = ToField.new(:input_field => params[:input_field], :match_number => params[:match_number], :regex => params[:regex], :destination_field => params[:destination_field])
  @information.to_fields << @toField
  @information.save.to_s
end

# Delete a ToField from an Information
delete '/information/:namespace/:type/:input_field/:match_number/to/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.to_fields.first(:input_field => params[:input_field], :match_number => params[:match_number], :destination_field => params[:destination_field]).delete
  @information.save.to_s
end

# Add a ToInformation to an Information.
post '/information/:namespace/:type/:input_field/to/:destination_namespace/:destination_type/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @destinationInformation = Information.first(:namespace_name => params[:destination_namespace], :type_name => params[:destination_type])
  @toInformation = ToInformation.new(:input_field => params[:input_field], :regex => params[:regex], :destination_information => @destinationInformation, :destination_field => params[:destination_field])
  @information.to_informations << @toInformation
  @information.save.to_s
end

# Delete a ToInformation from an Information.
delete '/information/:namespace/:type/:input_field/to/:destination_namespace/:destination_type/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @destinationInformation = Information.first(:namespace_name => params[:destination_namespace], :type => params[:destination_type])
  @information.to_informations.first(:input_field => params[:input_field], :destination_information => @destinationInformation,  :destination_field => params[:destination_field]).delete
  @information.save.to_s
end

# Delete a Gatherer from an Information.  Does not eliminate the Gatherer itself.
delete '/information/:namespace/:type/gatherer/:gatherer' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.gatherers.first(:name => params[:gatherer]).delete
  @information.save.to_s
end

# Create a new Gatherer.
post '/gatherer/:name' do
  @gatherer = Gatherer.new(:name => params[:name])
  
  @gatherer.save.to_s
end

# Delete a Gatherer.
delete '/gatherer/:name' do
  @gatherer = Gatherer.first(:name => params[:name])
  @gatherer.destroy.to_s
end

# Add a parent to a Gatherer.
post '/gatherer/:child/parent/:parent' do
  @child =  Gatherer.first(:name => params[:child])
  @parent = Gatherer.first(:name => params[:parent])
  
  @child.parent = @parent
  @child.save.to_s
end

# Remove a parent relationship from a Gatherer. Does not eliminate the parent.
delete '/gatherer/:child/parent' do
  @child =  Gatherer.first(:name => params[:child])
  @child.parent.delete
  @child.save.to_s
end

# Add a child to a Gatherer.
post '/gatherer/:parent/child/:child' do
  @child =  Gatherer.first(:name => params[:child])
  @parent = Gatherer.first(:name => params[:parent])
  
  @child.parent = @parent
  @child.save.to_s
end

# Remove a child relationship from a Gatherer. Does not eliminate the child.
# Fails if the child is not a child of the specified parent.
delete '/gatherer/:parent/child/:child' do
  @parent = Gatherer.first(:name => params[:parent])
  @parent.children.first(:name => params[:child]).delete
  @parent.save.to_s
end

# Add a URL to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/url' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.urls.new(:value => params[:value])
  @gatherer.save.to_s
end

# Delete a URL from a gatherer.
delete '/gatherer/:gatherer/url/:value' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.urls.first(:value => params[:value]).delete
  @gatherer.save.to_s
end

# Add a GET to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/get/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.gets.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

# Delete a GET from a Gatherer.
delete '/gatherer/:gatherer/get/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.gets.first(:name => params[:name]).delete
  @gatherer.save.to_s
end

# Add a POST to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/post/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.posts.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

# Delete a POST from a Gatherer.
delete '/gatherer/:gatherer/post/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.posts.first(:name => params[:name]).delete
  @gatherer.save.to_s
end

# Add a Header to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/header/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.headers.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

# Delete a Header from a Gatherer.
delete '/gatherer/:gatherer/header/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.headers.first(:name => params[:name]).delete
  @gatherer.save.to_s
end

# Add a Cookie to a Gatherer. Value is in the post data.
post '/gatherer/:gatherer/cookie/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.cookies.new(:name => params[:name], :value => params[:value])
  @gatherer.save.to_s
end

# Delete a Cookie from a Gatherer.
delete '/gatherer/:gatherer/cookie/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.cookies.first(:name => params[:name]).delete
  @gatherer.save.to_s
end
