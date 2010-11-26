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
    export.to_json
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

class Namespace
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations

  is :tree

  def export
    hash = {}
    
    hash[:parent] = parent ? parent.attribute_get(:name) : nil
    hash[:informations] = informations.collect {|information| information.attribute_get(:type_name) }

    hash
  end
end

class Type
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations
  has n, :publish_fields
  
  is :tree

  def export
    hash = {}
    
    @name = attribute_get(:name)
    
    hash[:parent] = parent ? parent.attribute_get(:name) : nil
    hash[:informations] = informations.collect {|information| information.namespace.attribute_get(:name) }
    hash[:publishFields] = publish_fields.collect {|publishField| publishField.attribute_get(:name) }

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

  def export
    hash = {}
    
    namespaces = namespace.ancestors.collect { |ns| ns.attribute_get(:name) }.push(namespace.attribute_get(:name))
    
    @type = Type.first(:name => type_name)
    types = @type.ancestors.collect { |t| t.attribute_get(:name) }.push(@type.attribute_get(:name))
    
    informations = Information.all(:namespace_name => namespaces, :type_name => types)
    
    hash[:defaultFields] = {}
    hash[:gatherers] = []
    hash[:toFields] = []
    hash[:toInformations] = []
    
    informations.collect { |information| hash[:defaultFields].merge!(information.default_fields.to_hash(:name, :value)) }
    informations.collect { |information| hash[:gatherers].push(gatherers.collect {|gatherer| gatherer.export} ).flatten }
    informations.collect { |information| hash[:toFields].push(information.to_fields.collect {|toField| toField.export} ).flatten }
    informations.collect { |information| hash[:toInformations].push(information.to_informations.collect {|toInformation| toInformation.export} ).flatten }
    
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

  def export
    hash = {}
    
    hash[:inputField] = attribute_get(:input_field)
    hash[:matchNumber] = attribute_get(:match_number)
    hash[:regex] = attribute_get(:regex)
    hash[:destinationField] = attribute_get(:destination_field)
    
    hash
  end
end

class ToInformation
  include DataMapper::Resource
  
  belongs_to :information, :key => true
  property :input_field, String, :key => true
  property :regex, String
  belongs_to :destination_information, :model => 'Information', :key => true
  property :destination_field, String, :key => true

  def export
    hash = {}
    
    hash[:inputField] = attribute_get(:input_field)
    hash[:regex] = attribute_get(:regex)
    hash[:destinationNamespace] = destination_information.namespace_name
    hash[:destinationType] = destination_information.type_name
    hash[:destinationField] = attribute_get(:destination_field)
    
    hash
  end
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

  def export
    hash = {}

    hash[:urls] = urls.collect { |url| url.value }
    hash[:gets] = gets.to_hash(:name, :value)
    hash[:posts] = posts.to_hash(:name, :value)
    hash[:headers] = headers.to_hash(:name, :value)
    hash[:cookies] = cookies.to_hash(:name, :value)
    
    hash[:parents] = ancestors.collect { |parent| parent.export }
    
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
  set :raise_errors, true
  set :show_exceptions, false
end

# GET

# Get a list of all the available namespaces.
get '/namespace/' do
  Namespace.all.collect {|namespace| namespace.attribute_get(:name) }.to_json
end

# Get details on a specific namespace.
get '/namespace/:name' do
  Namespace.first(:name => params[:name]).to_json
end

# Get a list of all the available types.
get '/type/' do
  Type.all.collect {|namespace| namespace.attribute_get(:name) }.to_json
end

# Get details on a specific type.
get '/type/:name' do
  Type.first(:name => params[:name]).to_json
end

# Get a list of all the available Informations.
get '/information/' do
  Information.all.collect {|information| [information.attribute_get(:namespace), information.attribute_get(:type)] }.to_json
end

# Get details on a specific Information.
get '/information/:namespace/:type' do
  Information.first(:namespace_name => params[:namespace], :type_name => params[:type]).to_json
end

# Get a list of all the available Gatherers.
get '/gatherer/' do
  Gatherer.all.collect {|gatherer| gatherer.attribute_get(:name)}.to_json
end

# Get details on a specific Gatherer.
get '/gatherer/:name' do
  Gatherer.first(:name => params[:name]).to_json
end

# POST / DELETE

# Create a new namespace.
post '/namespace/:name' do
  @namespace = Namespace.new(:name => params[:name])
  @namespace.save ? @namespace.to_json : {:namespace => @namespace.errors.to_a}.to_json
end

# Delete a namespace.  This can only be done if there are no dependencies.
delete '/namespace/:name' do
  @namespace = Namespace.first(:name => params[:name])
  @namespace.delete
  @namespace.save ? @namespace.to_json :  {:namespace => @namespace.errors.to_a}.to_json
end

# Add a parent to a namespace.
post '/namespace/:child/parent/:parent' do
  @child = Namespace.first(:name => params[:child])
  @parent = Namespace.first(:name => params[:parent])
  @child.parent = @parent
  @child.save ? @child.to_json :  {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Remove a namespace's parent relationship.
# Does not eliminate the parent.
delete '/namespace/:child/parent' do
  @child = Namespace.first(:name => params[:child])
  @parent = @child.parent.delete
  @child.save ? @child.to_json : {:child => @child.errors.to_a}.to_json
end

# Add a child to a namespace.
post '/namespace/:parent/child/:child' do
  @child = Namespace.first(:name => params[:child])
  @parent = Namespace.first(:name => params[:parent])
  @child.parent = @parent
  @child.save ? @child.to_json : {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Eliminate the relationship between a namespace and one of its children.
# Does not eliminate the child.  Fails if the child is not a child of the
# specified parent.
delete '/namespace/:parent/child/:child' do
  @parent = Namespace.first(:name => params[:parent])
  @parent.children.first(:name => params[:child]).delete
  @parent.save ? @parent.to_json :  {:parent => @parent.errors.to_a}.to_json
end

# Create a new type.
post '/type/:name' do
  @type = Type.new(:name => params[:name])
  @type.save ? @type.to_json : {:type => @type.errors.to_a}.to_json
end

# Delete a type.  This can only be done if there are no dependencies.
delete '/type/:name' do
  @type = Type.first(:name => params[:name])
  @type.delete
  @type.save ? @type.to_json : {:type => @type.errors.to_a }.to_json
end

# Add a parent to a Type.
post '/type/:child/parent/:parent' do
  @child = Type.first(:name => params[:child])
  @parent = Type.first(:name => params[:parent])
  @child.parent = @parent
  @child.save ? @child.to_json : {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Remove a Type's parent relationship.  Does not eliminate either the child or parent Type.
delete '/type/:child/parent' do
  @child = Type.first(:name => params[:child])
  @child.parent.delete
  @child.save ? @child.to_json : {:child => @child.errors.to_a}
end

# Add a child to a Type.
post '/type/:parent/child/:child' do
  @child = Type.first(:name => params[:child])
  @parent = Type.first(:name => params[:parent])
  @child.parent = @parent
  @child.save ? true : {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Remove a Type's child relationship.  Does not eliminate either the child or parent Type.
# Fails if the specified child is not a child of the specified parent.
delete '/type/:parent/child/:child' do
  @parent = Type.first(:name => params[:parent])
  @parent.children.first(:name => params[:child]).delete
  @parent.save ? true : {:parent => @parent.errors.to_a}.to_json
end

# Add a publishField to a type.
post '/type/:type/publish/:publish_field' do
  @type = Type.first(:name => params[:type])
  @publish_field = PublishField.new(:name => params[:publish_field])
  @type.publish_fields << @publish_field
  @type.save ? true : {:type => @type.errors.to_a, :publish_field => @publish_field.errors.to_a}.to_json
end

# Delete a publishField from a type.
delete '/type/:type/publish/:publish_field' do
  @type = Type.first(:name => params[:type])
  @type.publish_fields.first(:name => params[:publish_field]).delete
  @type.save ? true : {:type => @type.errors.to_a}.to_json
end

# Create a new Information.
post '/information/:namespace/:type' do
  @information = Information.new
  @namespace = Namespace.first(:name => params[:namespace])
  @type = Type.first(:name => params[:type])

  @namespace.informations << @information
  @type.informations << @information

  @information.save ? true : {:information => @information.errors.to_a, :namespace => @namespace.errors.to_a, :type => @type}.to_json
end

# Delete an Information.
delete '/information/:namespace/:type' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.delete
  @information.save ? true : {:information => @information.errors.to_a}.to_json
end

# Add a gatherer to an Information.
post '/information/:namespace/:type/gatherer/:gatherer' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @gatherer = Gatherer.first(:name => params[:gatherer])

  @information.gatherers << @gatherer
  @information.save ? true : {:information => @information.errors.to_a, :gatherer => @gatherer.errors.to_a}.to_json
end

# Add a DefaultField to an Information.
post '/information/:namespace/:type/default/:name' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @default_field = DefaultField.new(:name => params[:name], :value => params[:value])
  @information.default_fields << @defaultField
  @information.save ? true : {:information => @information.errors.to_a, :default_field => @default_field.errors.to_a }.to_json
end

# Delete a DefaultField from an Information.
delete '/information/:namespace/:type/default/:name' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.default_fields.first(:name => params[:name]).delete
  @information.save ? true : {:information => @information.errors.to_a }.to_json
end

# Add a ToField to an Information.  Regex is inside the post.
post '/information/:namespace/:type/:input_field/:match_number/to/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @to_field = ToField.new(:input_field => params[:input_field], :match_number => params[:match_number], :regex => params[:regex], :destination_field => params[:destination_field])
  @information.to_fields << @to_field
  @information.save ? true : {:information => @information.errors.to_a, :to_field => @to_field.errors.to_a}.to_json
end

# Delete a ToField from an Information
delete '/information/:namespace/:type/:input_field/:match_number/to/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.to_fields.first(:input_field => params[:input_field], :match_number => params[:match_number], :destination_field => params[:destination_field]).delete
  @information.save ? true : {:information => @information.errors.to_a }.to_json
end

# Add a ToInformation to an Information.
post '/information/:namespace/:type/:input_field/to/:destination_namespace/:destination_type/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @destination_information = Information.first(:namespace_name => params[:destination_namespace], :type_name => params[:destination_type])
  @to_information = ToInformation.new(:input_field => params[:input_field], :regex => params[:regex], :destination_information => @destination_information, :destination_field => params[:destination_field])
  @information.to_informations << @to_information
  @information.save ? true : {:information => @information.errors.to_a, :destination_information => @destination_information.errors.to_a, :to_information => @to_information.errors.to_a}.to_json
end

# Delete a ToInformation from an Information.
delete '/information/:namespace/:type/:input_field/to/:destination_namespace/:destination_type/:destination_field' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @destination_information = Information.first(:namespace_name => params[:destination_namespace], :type => params[:destination_type])
  @information.to_informations.first(:input_field => params[:input_field], :destination_information => @destination_information,  :destination_field => params[:destination_field]).delete
  @information.save ? true: {:information => @information.errors.to_a, :destination_information => @destination_information.errors.to_a}.to_json
end

# Delete a Gatherer from an Information.  Does not eliminate the Gatherer itself.
delete '/information/:namespace/:type/gatherer/:gatherer' do
  @information = Information.first(:namespace_name => params[:namespace], :type_name => params[:type])
  @information.gatherers.first(:name => params[:gatherer]).delete
  @information.save ? true: {:information => @information.errors.to_a }.to_json
end

# Create a new Gatherer.
post '/gatherer/:name' do
  @gatherer = Gatherer.new(:name => params[:name])
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Delete a Gatherer.
delete '/gatherer/:name' do
  @gatherer = Gatherer.first(:name => params[:name])
  @gatherer.delete
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a parent to a Gatherer.
post '/gatherer/:child/parent/:parent' do
  @child =  Gatherer.first(:name => params[:child])
  @parent = Gatherer.first(:name => params[:parent])
  @child.parent = @parent
  @child.save ? true : {:parent => @parent.errors.to_a, :child => @child.errors.to_a }.to_json
end

# Remove a parent relationship from a Gatherer. Does not eliminate the parent.
delete '/gatherer/:child/parent' do
  @child =  Gatherer.first(:name => params[:child])
  @child.parent.delete
  @child.save ? true : {:child => @child.errors.to_a}.to_json
end

# Add a child to a Gatherer.
post '/gatherer/:parent/child/:child' do
  @child =  Gatherer.first(:name => params[:child])
  @parent = Gatherer.first(:name => params[:parent])
  @child.parent = @parent
  @child.save ? true : {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Remove a child relationship from a Gatherer. Does not eliminate the child.
# Fails if the child is not a child of the specified parent.
delete '/gatherer/:parent/child/:child' do
  @parent = Gatherer.first(:name => params[:parent])
  @parent.children.first(:name => params[:child]).delete
  @parent.save ? true : {:parent => @parent.errors.to_a}.to_json
end

# Add a URL to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/url' do
  @gatherer = Gatherer.first(:name => params[:gatherer])
  @url = @gatherer.urls.new(:value => params[:value])
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a, :url => @url.errors.to_a}.to_json
end

# Delete a URL from a gatherer.
delete '/gatherer/:gatherer/url/:value' do
  @gatherer = Gatherer.first(:name => params[:gatherer])
  @gatherer.urls.first(:value => params[:value]).delete
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a GET to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/get/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @get = @gatherer.gets.new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a, :get => @get.errors.to_a}.to_json
end

# Delete a GET from a Gatherer.
delete '/gatherer/:gatherer/get/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.gets.first(:name => params[:name]).delete
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a POST to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/post/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @post = @gatherer.posts.new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a, :post => @post.errors.to_a}.to_json
end

# Delete a POST from a Gatherer.
delete '/gatherer/:gatherer/post/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.posts.first(:name => params[:name]).delete
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a Header to a Gatherer.  Value is in the post data.
post '/gatherer/:gatherer/header/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @header = @gatherer.headers.new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a, :header => @header.errors.to_a}.to_json
end

# Delete a Header from a Gatherer.
delete '/gatherer/:gatherer/header/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.headers.first(:name => params[:name]).delete
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a Cookie to a Gatherer. Value is in the post data.
post '/gatherer/:gatherer/cookie/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @cookie = @gatherer.cookies.new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a, :cookie =>  @cookie.errors.to_a}.to_json
end

# Delete a Cookie from a Gatherer.
delete '/gatherer/:gatherer/cookie/:name' do
  @gatherer = Gatherer.first(params[:gatherer])
  @gatherer.cookies.first(:name => params[:name]).delete
  @gatherer.save ? true : {:gatherer => @gatherer.errors.to_a}.to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end
