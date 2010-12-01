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
  # Convert a collection of resources to a hash, using specified key and value.
  def to_hash(key, value)
#    keys = all.collect { |element| element.attribute_get(key) }
#    values = all.collect { |element| element.attribute_get(value) }
#    Hash[keys.zip(values)]
    hash = {}
    all.each { |resource| hash[resource.attribute_get(key)] = resource.attribute_get(value) }
    hash
  end

  # Get an array with the identity of each element.
  def identify_all(query = DataMapper::Undefined)
    all(query).collect { |resource| resource.identify }
  end
end

module DataMapper::Resource
  def to_json
    export.to_json
  end
  
  def identify
    nil
  end

  def export
    attribute_get(:value) or nil
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

class Area
  include DataMapper::Resource

  property :name, String, :key => true
  
  has n, :types, :through => :informations
  has n, :informations, :through => Resource

  has n, :default_fields

  def identify
    'area/' + attribute_get(:name) + '/'
  end
  
  def export
    {
      :type => types.identify_all,
      :default_field => default_fields.identify_all
    }
  end
end

class Type
  include DataMapper::Resource

  property :name, String, :key => true

  has n, :informations #, :unique => true
  has n, :areas, :through => :informations
  has n, :publish_fields #, :unique => true
  
  def identify
    'type/' + attribute_get(:name) + '/'
  end

  def export
#    @name = attribute_get(:name)
    
#    hash[:areas] = areas.collect {|area| area.attribute_get(:name) }
#    hash[:publish_fields] = publish_fields.collect {|publish_field| publish_field.attribute_get(:name) }
    {
      :area => areas.identify_all,
      :publish_field => publish_fields.identify_all
    }
  end
end

class PublishField
  include DataMapper::Resource

  belongs_to :type, :key => true
  property :value, String, :key => true

  def identify
    attribute_get(:type_name) + '/publish_field/' + attribute_get(:value)
  end
end

class Information
  include DataMapper::Resource
  
  belongs_to :type, :key => true
  property :name, String, :key => true
  has n, :areas, :through => Resource #, :unique => true
  
  has n, :gatherers, :through => Resource
  has n, :to_fields
  has n, :to_informations
  
  def identify
    'information/' + attribute_get(:type_name) + '/' + attribute_get(:name) + '/'
  end

  def export
#    hash[:default_fields] = default_fields.to_hash(:name, :value)
#    hash[:gatherers] = gatherers.collect { |gatherer| gatherer.export }
#    hash[:to_fields] = to_fields.collect { |to_field| to_field.export }
#    hash[:to_informations] = to_informations.collect { |to_information| to_information.export }
    {
#      :default_field => default_fields.identify_all,
      :gatherer => gatherers.identify_all,
      :to_field => to_fields.identify_all,
      :to_information => to_informations.identify_all
    }
  end
end

class DefaultField
  include DataMapper::Resource

  belongs_to :area, :key => true
  property :name, String, :key => true
  property :value, String

  def identify
    area.identify + 'default_field/' + attribute_get(:name)
  end
end

class ToField
  include DataMapper::Resource
  
  belongs_to :information, :key => true
  property :input_field, String, :key => true
  property :match_number, Integer, :key => true
  property :regex, String
  property :destination_field, String, :key => true

  def identify
    information.identify + input_field + '/' + match_number.to_s + '/to/' + destination_field
  end

  def export
    {
      :input_field  => attribute_get(:input_field),
      :match_number => attribute_get(:match_number),
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
    information.identify + input_field + '/to/' + destination_information.identify + destination_field
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
    'gatherer/' + attribute_get(:name) + '/'
  end

  def export
#    hash[:urls] = urls.collect { |url| url.value }
#    hash[:gets] = gets.to_hash(:name, :value)
#    hash[:posts] = posts.to_hash(:name, :value)
#    hash[:headers] = headers.to_hash(:name, :value)
#    hash[:cookies] = cookies.to_hash(:name, :value)
    
#    hash[:parents] = ancestors.collect { |parent| parent.export }
    {
      :url => urls.identify_all,
      :get => gets.identify_all,
      :post => posts.identify_all,
      :header => headers.identify_all,
      :cookie => cookies.identify_all,
    
      :gatherer => ancestors.identify_all
    }
  end
end

class Url
  include DataMapper::Resource
  
  belongs_to :gatherer,  :key => true

  property :value, String, :key => true

  def identify
    gatherer.identify + 'url/' + attribute_get(:value)
  end
end

class Get
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String

  def identify
    gatherer.identify + 'get/' + attribute_get(:name)
  end
end

class Post
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String

  def identify
    gatherer.identify + 'post/' + attribute_get(:name)
  end
end

class Header
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String

  def identify
    gatherer.identify + 'header/' + attribute_get(:name)
  end
end

class Cookie
  include DataMapper::Resource
  
  belongs_to :gatherer, :key => true
  property :name,  String, :key => true
  property :value, String

  def identify
    gatherer.identify + 'cookie/' + attribute_get(:name)
  end
end

DataMapper.finalize
DataMapper.auto_migrate!

# CONFIG
configure do
  set :raise_errors, true
  set :show_exceptions, false
end

# GET

# Get a list of all the available areas.
get '/area/' do
  Area.identify_all.to_json
end

# Get details on a specific area.
get '/area/:name' do
  @area = Area.first(:name => params[:name]) or return not_found
  @area.export.to_json
end

# Get a list of all the available types.
get '/type/' do
  Type.identify_all.to_json
end

# Get details on a specific type.
get '/type/:name' do
  @type = Type.first(:name => params[:name]) or return not_found
  @type.export.to_json
end

# Get a list of all the available Informations.
get '/information/' do
  Information.identify_all.to_json
end

# Get details on an Information by name.
get '/information/:type/:name' do
#  @information = Area.first(:name => params[:area]).informations.first(:type_name => params[:type]) or return not_found
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @information.export.to_json
end

# Get a list of all the available Informations of a specific type in a specific area.
get '/information/:type/in/:area/' do
#  @information = Area.first(:name => params[:area]).informations.first(:type_name => params[:type]) or return not_found
#  @information.to_json
#  Information.all(:type => params[:type]
  @area = Area.first(:name => params[:area]) or return not_found
  @area.informations.identify_all(:type_name => params[:type]).to_json
end


# Get a list of all the available Gatherers.
get '/gatherer/' do
  Gatherer.identify_all.to_json
end

# Get details on a specific Gatherer.
get '/gatherer/:name' do
  Gatherer.first(:name => params[:name]).export.to_json
end

# PUT / POST / DELETE

# Create a new type.
put '/type/:name' do
  @type = Type.first_or_new(:name => params[:name])
  @type.save ? true.to_json : {:type => @type.errors.to_a}.to_json
end

# Delete a type.  This can only be done if there are no dependencies.
delete '/type/:name' do
  @type = Type.first(:name => params[:name]) or return not_found
  @type.delete
  @type.save ? true.to_json : {:type => @type.errors.to_a }.to_json
end

# Add a publish_field to a type.
put '/type/:type/publish/:publish_field' do
  @type = Type.first(:name => params[:type]) or return not_found
  @publish_field = PublishField.first_or_new(:value => params[:publish_field])
  @type.publish_fields << @publish_field
  @type.save ? true.to_json : {:type => @type.errors.to_a, :publish_field => @publish_field.errors.to_a}.to_json
end

# Delete a publish_field from a type.
delete '/type/:type/publish/:publish_field' do
  @type = Type.first(:name => params[:type]) or return not_found
  @type.publish_fields.first(:value => params[:publish_field]).delete
  @type.save ? true.to_json : {:type => @type.errors.to_a}.to_json
end

# Create a new Area.
put '/area/:area' do
  @area = Area.first_or_new(:name => params[:area])
  @area.save ? true.to_json : {:area => @area.errors.to_a }.to_json
end

# Delete an area.
delete '/area/:area' do
  @area = Area.first(:name => params[:area]) or return not_found
  @area.delete
  @area.save ? true.to_json : {area => @area.errors.to_a }.to_json
end

# Add a DefaultField to an Area.
put '/area/:area/default/:name' do
  @area = Area.first(:name => params[:area]) or return not_found
  @default_field = DefaultField.first_or_new(:name => params[:name], :value => params[:value])
  @area.default_fields << @default_field
  @area.save ? true.to_json : {:area => @area.errors.to_a, :default_field => @default_field.errors.to_a }.to_json
end

# Delete a DefaultField from an Area.
delete '/area/:area/default/:name' do
  @area = Area.first(:name => params[:area]) or return not_found
  @default_field = @area.default_fields.first(:name => params[:name]) or return not_found
  @area.default_fields.delete(@default_field)
  @area.save ? true.to_json : {:area => @area.errors.to_a }.to_json
end

# Create a new Information.
put '/information/:type/:name' do
  @type = Type.first(:name => params[:type]) or return not_found

  @information = Information.first_or_new(:type => @type, :name => params[:name])

  @information.save ? true.to_json : {:information => @information.errors.to_a, :area => @area.errors.to_a, :type => @type.errors.to_a}.to_json
end

# Tag an existing Information with a new Area.  Creates area if it doesn't exist yet.
put '/information/:type/:name/area/:area' do
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  
  @area = Area.first_or_create(:name => params[:area])
  @information.areas << @area

  @information.save ? true.to_json : {:information => @information.errors.to_a, :area => @area.errors.to_a}.to_json
end

# Delete an Area tag from an information.
delete '/information/:type/:name/area/:area' do
  @area = Area.first(:name => params[:name]) or return not_found
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @information.areas.delete(@area)
  @information.save ? true.to_json : {:information => @information.errors.to_a, :area => @area.errors.to_a}.to_json
end

# Add a gatherer to an Information.
put '/information/:type/:name/gatherer/:gatherer' do
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found

  @information.gatherers << @gatherer
  @information.save ? true.to_json : {:information => @information.errors.to_a, :gatherer => @gatherer.errors.to_a}.to_json
end

# Delete a Gatherer from an Information.  Does not eliminate the Gatherer itself.
delete '/information/:type/:name/gatherer/:gatherer' do
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @gatherer = @information.gatherers.first(:name => params[:gatherer]) or return not_found
  @information.gatherers.delete(@gatherer)
  @information.save ? @information.to_json : {:information => @information.errors.to_a }.to_json
end

# Add a ToField to an Information.  Regex is inside the post.
put '/information/:type/:name/:input_field/:match_number/to/:destination_field' do
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @to_field = ToField.first_or_new(:input_field => params[:input_field], :match_number => params[:match_number], :regex => params[:regex], :destination_field => params[:destination_field])
  @information.to_fields << @to_field
  @information.save ? true.to_json : {:information => @information.errors.to_a, :to_field => @to_field.errors.to_a}.to_json
end

# Delete a ToField from an Information
delete '/information/:type/:name/:input_field/:match_number/to/:destination_field' do
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @to_field = @information.to_fields.first(:input_field => params[:input_field], :match_number => params[:match_number], :destination_field => params[:destination_field]) or return not_found
  @information.to_fields.delete(@to_field)
  @information.save ? true.to_json : {:information => @information.errors.to_a }.to_json
end

# Add a ToInformation to an Information.
put '/information/:type/:name/:input_field/to/information/:destination_type/:destination_name/:destination_field' do
  @information = Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @destination_information = Information.first(:type_name => params[:destination_type], :name => params[:destination_name]) or return not_found
  @to_information = ToInformation.first_or_new(:input_field => params[:input_field], :regex => params[:regex], :destination_information => @destination_information, :destination_field => params[:destination_field])
  @information.to_informations << @to_information
  @information.save ? true.to_json : {:information => @information.errors.to_a, :destination_information => @destination_information.errors.to_a, :to_information => @to_information.errors.to_a}.to_json
end

# Delete a ToInformation from an Information.
delete '/information/:type/:id/:input_field/to/information/:destination_type/:destination_name/:destination_field' do
  @information = Area.first(:name => params[:area]).informations.first(:type_name => params[:type]) or return not_found
  @destination_information = Information.first(:type_name => params[:destination_type], :name => params[:destination_name]) or return not_found
  @to_information = @information.first(:input_field => params[:input_field], :regex => params[:regex], :destination_information => @destination_information, :destination_field => params[:destination_field]) or return not_found
  
  @information.to_informations.delete(@to_information)
  @information.save ? true.to_json : {:information => @information.errors.to_a, :destination_information => @destination_information.errors.to_a}.to_json
end


# Create a new Gatherer.
put '/gatherer/:name' do
  @gatherer = Gatherer.first_or_new(:name => params[:name]) or return not_found
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Delete a Gatherer.
delete '/gatherer/:name' do
  @gatherer = Gatherer.first(:name => params[:name]) or return not_found
  @gatherer.delete
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a parent to a Gatherer.
put '/gatherer/:child/parent/:parent' do
  @child =  Gatherer.first(:name => params[:child]) or return not_found
  @parent = Gatherer.first(:name => params[:parent]) or return not_found
  @child.parent = @parent
  @child.save ? true.to_json : {:parent => @parent.errors.to_a, :child => @child.errors.to_a }.to_json
end

# Remove a parent relationship from a Gatherer. Does not eliminate the parent.
delete '/gatherer/:child/parent' do
  @child =  Gatherer.first(:name => params[:child]) or return not_found
  @child.parent.delete
  @child.save ? true.to_json : {:child => @child.errors.to_a}.to_json
end

# Add a child to a Gatherer.
put '/gatherer/:parent/child/:child' do
  @child =  Gatherer.first(:name => params[:child]) or return not_found
  @parent = Gatherer.first(:name => params[:parent]) or return not_found
  @child.parent = @parent
  @child.save ? true.to_json : {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Remove a child relationship from a Gatherer. Does not eliminate the child.
# Fails if the child is not a child of the specified parent.
delete '/gatherer/:parent/child/:child' do
  @parent = Gatherer.first(:name => params[:parent]) or return not_found
  @parent.children.first(:name => params[:child]).delete
  @parent.save ? true.to_json : {:parent => @parent.errors.to_a}.to_json
end

# Add a URL to a Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/url' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @url = @gatherer.urls.first_or_new(:value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :url => @url.errors.to_a}.to_json
end

# Delete a URL from a gatherer.
delete '/gatherer/:gatherer/url/:value' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.urls.first(:value => params[:value]).delete
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a GET to a Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/get/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @get = @gatherer.gets.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :get => @get.errors.to_a}.to_json
end

# Delete a GET from a Gatherer.
delete '/gatherer/:gatherer/get/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.gets.first(:name => params[:name]).delete
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a POST to a Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/post/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @post = @gatherer.posts.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :post => @post.errors.to_a}.to_json
end

# Delete a POST from a Gatherer.
delete '/gatherer/:gatherer/post/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.posts.first(:name => params[:name]).delete
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a Header to a Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/header/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @header = @gatherer.headers.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :header => @header.errors.to_a}.to_json
end

# Delete a Header from a Gatherer.
delete '/gatherer/:gatherer/header/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.headers.first(:name => params[:name]).delete
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end 

# Add a Cookie to a Gatherer. Value is in the post data.
put '/gatherer/:gatherer/cookie/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @cookie = @gatherer.cookies.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :cookie =>  @cookie.errors.to_a}.to_json
end

# Delete a Cookie from a Gatherer.
delete '/gatherer/:gatherer/cookie/:name' do
  @gatherer = Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.cookies.first(:name => params[:name]).delete
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  nil.to_json
end
