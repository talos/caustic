#!/usr/bin/ruby

###
#   SimpleScraper Back 0.0.1
#
#   Copyright 2010, AUTHORS.txt
#   Licensed under the MIT license.
#
#   SimpleScraper.rb : Sinatra paths.  Execute this file to start.
###

$:<< Dir.pwd
$:<< Dir.pwd + '/db'

require 'rubygems'
require 'sinatra'
require 'db/schema'

# CONFIG
configure do
  set :raise_errors, true
  set :show_exceptions, false
  set :sessions, true
  set :public, File.dirname(__FILE__) + './front'
end

# Helper functions to interface with the DB.
# params: resource_model, resource_id, relationship, relationship_id
module SimpleScraper
  SimpleScraper::RESERVED_WORDS = [:resource_model, :resource_id, :relationship, :relationship_id, :creator_id]
  module Resource
    def self.find_model(params)
      DataMapper::Model.find(params[:resource_model])
    end

    def self.first(params)
      model = find_model(params) or return
      model.first_from_key(params[:resource_id])
    end
    
    def self.first_or_create(params)
      model = find_model(params) or return
      resource = model.first_or_new_from_key(params[:resource_id])
      params.delete_if { |param, value| SimpleScraper::RESERVED_WORDS.include? param }
      resource.safe_attributes= params
      resource.save or raise SimpleScraper::Exception.from_resources(resource)
      resource
    end
  end

  module Tag
    def self.find_model(params)
      model = SimpleScraper::Resource.find_model(params) or return
      model.tag_models[params[:relationship]]
    end
    
    def self.first(params)
      tag_model = find_model(params) or return
      resource = SimpleScraper::Resource.first(params) or return
      resource.send(params[:relationship]).first(tag_model.criteria_from_key_string(params[:relationship_id]))
    end

    def self.first_or_create(params)
      tag_model = find_model(params) or return
      resource = SimpleScraper::Resource.first(params) or return

      if tag_model == resource.model and params[:resource_id] == params[:relationship_id] # prevent self-following
        raise SimpleScraper::Exception.new("Cannot tag a resource with itself.") 
      end

      tag_resource = tag_model.first_or_new_from_key(params[:relationship_id])
      resource.send(params[:relationship]) << tag_resource
      
      params.delete_if { |key, value| SimpleScraper::RESERVED_WORDS.include? key }
      tag_resource.safe_attributes= params
      
      tag_resource.save or raise SimpleScraper::Exception.from_resources(tag_resource)
      resource.save or raise SimpleScraper::Exception.from_resources(resource)
      tag_resource
      #  resource.send(params[:relationship].downcase) << tag
      #  resource.save or tag.send(params[:model].downcase + 's') << resource
    end
  end
  
  class SimpleScraper::Exception < RuntimeError
    def self.from_resources(*resources)
      errors = {}
      resources.each do |resource|
        errors[resource.class.to_s] = resource.errors.to_a
      end
      SimpleScraper::Exception.new(errors)
    end
    def initialize(errors)
      @errors = errors
    end
    def to_json
      {:errors => @errors}.to_json
    end
  end
end

get '/' do
  redirect '/index.html'
end

# Login!
post '/login' do
  # TODO: session handling.
  #  session[:user] = params[:user]
  not_found
end

# Signup!
post '/signup' do
  # TODO: handle this in a real way.
  user = SimpleScraper::User.create(:name => params[:name])
  user.location.to_json
end

###### RESOURCE MODELS
# Display the existing members of a model.  Limited to the top 100, with an optional query string.
get '/:resource_model/' do
  model = SimpleScraper::Resource.find_model(params) or return not_found
  model.all_like(params).collect {|resource| resource.location }.to_json
end

###### RESOURCES
# Create a new resource. Returns the location of the new resource.
put '/:resource_model/' do
  begin
    resource = SimpleScraper::Resource.first_or_create(params) or return not_found
    resource.location.to_json
  rescue SimpleScraper::Exception => exception
    error exception.to_json
  end
end
# Describe a resource.
get '/:resource_model/:resource_id' do
  resource = SimpleScraper::Resource.first(params) or return not_found
  resource.describe.to_json
end
# Replace a resource.
put '/:resource_model/:resource_id' do
  begin
    resource = SimpleScraper::Resource.first_or_create(params) or return not_found
    resource.location.to_json
  rescue SimpleScraper::Exception => exception
    error exception.to_json
  end
end
# Delete a resource and all its links.
delete '/:resource_model/:resource_id' do
  resource = SimpleScraper::Resource.first(params) or return not_found
  resource.class.tag_names.each do |tag_name|
    resource.send(tag_name).all.each { |link| link.destroy }
  end
  resource.destroy or error SimpleScraper::Exception.from_resources(resource).to_json
end

###### TAG MODELS
# Redirect to the tag's model.
get '/:resource_model/:relationship/' do
  tag_model = SimpleScraper::Tag.find_model(params) or not_found
  puts tag_model.location.to_json
  redirect tag_model.location
end

####### TAGS
# Create a new tag.  Returns the location of the new tag.
put '/:resource_model/:resource_id/:relationship/' do
  begin
    tag_resource = SimpleScraper::Tag.first_or_create(params) or return not_found
    tag_resource.location.to_json
  rescue SimpleScraper::Exception => exception
    error exception.to_json
  end
end
# Redirect to the location of the actual resource.
get '/:resource_model/:resource_id/:relationship/:relationship_id' do
  tag_resource = SimpleScraper::Tag.first(params) or return not_found
  tag_resource.location
end
# Replace a tag.
put '/:resource_model/:resource_id/:relationship/:relationship_id' do
  begin
    tag_resource = SimpleScraper::Tag.first_or_create(params) or return not_found
    tag_resource.location
  rescue SimpleScraper::Exception => exception
    error exception.to_json
  end
end
# Delete a tagging.
delete '/:resource_model/:resource_id/:relationship/:relationship_id' do
  #tag_relationship = SimpleScraper::Relationship.first(params) or return not_found
  #tag_relationship.destroy or error

  resource = SimpleScraper::Resource.first(params) or return not_found
  tag = SimpleScraper::Tag.first(params) or return not_found
  
  resource.send(params[:relationship]).first(tag.model.raw_name.to_sym => tag).destroy or error SimpleScraper::Exception.from_resources(resource).to_json
end

# Redirect to the location of the actual resource, including possible further redirects.
# get '/:resource_model/:resource_id/:tag_model/:tag_id/*' do
#   resource = SimpleScraper::Resource.first(params) or return not_found
#   resource.model.tags[params[:tag]] ? redirect resource.model.tags[params[:tag]].location + '/' + params[:splat][0] : not_found
# end

# # Redirect until we're at the level where a tagging is possible.
# put '/:resource_model/:resource_id/:tag_model/:tag_id/*' do
#   resource = SimpleScraper::Resource.first(params) or return not_found
#   resource.model.tags[params[:tag]] ? redirect resource.model.tags[params[:tag]].location + '/' + params[:splat][0] : not_found
# end

# Get all the gatherers, generators, and interpreters associated with an area, info, and creator.
# Returns any of the aforementioned objects if they fall within the ancestor tree of the specified area.
# TODO: correct compilation of scrapers.
get '/scrapers/:creator/:area/:info' do

  creator = SimpleScraper::User.first(:id => params[:creator]) or return not_found
  area = SimpleScraper::Area.first(:id => params[:area]) or return not_found
  info = SimpleScraper::Info.first(:id => params[:info]) or return not_found

  #area_ids = area.ancestors.collect{ |parent_area| parent_area.attribute_get(:id) }.push(area.attribute_get(:id))
  # Collect associated areas non-redundantly.
  #area_ids = [params[:area_id]]
  area_ids = []
  def get_area_ids(check_area, area_ids)
    area_ids.push(check_area.attribute_get(:id))
    if area_ids.length == area_ids.uniq.length
      check_area.areas.each { |assoc_area| get_area_ids(assoc_area, area_ids) }
    else
      area_ids.uniq!
    end
  end
  get_area_ids(SimpleScraper::Area.first(:id => params[:area]), area_ids)
  #SimpleScraper::Area.all(SimpleScraper::Area
  
  models = [ SimpleScraper::Gatherer, SimpleScraper::Interpreter, SimpleScraper::Generator ]
  publish_collection = SimpleScraper::Publish.all
  default_collection = SimpleScraper::Default.all
  
  resources = {
    :defaults => {}
  }
  models.each do |model|
    resources[model] = (model.all(model.creator.id => params[:creator]) & \
                        model.all(model.areas.id => areas) & \
                        model.all(model.infos.id => params[:info]))
  end
  
  default_collection = (SimpleScraper::Default.all(SimpleScraper::Default.areas.id => area_ids) & \
                        SimpleScraper::Default.all(SimpleScraper::Default.creator.id => params[:creator]))
  publish_collection = (SimpleScraper::Publish.all(SimpleScraper::Publish.infos.id => params[:info]) & \
                        SimpleScraper::Publish.all(SimpleScraper::Publish.creator.id => params[:creator]))
  
  resources[:publishes] = publish_collection.collect {|publish| publish.name }
  default_collection.each do |default|
    resources[:defaults][default.name] = default.value
  end
  
  object = {
    :publishes => resources[:publishes],
    :defaults => resources[:defaults],
    :gatherers => {},
    :interpreters => {},
    :generators => {}
  }
  
  resources[SimpleScraper::Gatherer].each do |gatherer|
    urls, posts, headers, cookies = [], {}, {}, {}
    SimpleScraper::Url.all(SimpleScraper::Url.gatherers.id => gatherer.attribute_get(:id)).each do |url|
      urls.push(url.value)
    end
    SimpleScraper::Post.all(SimpleScraper::Post.gatherers.id => gatherer.attribute_get(:id)).each do |post|
      posts[post.name] = post.value
    end
    SimpleScraper::Header.all(SimpleScraper::Header.gatherers.id => gatherer.attribute_get(:id)).each do |header|
      headers[header.name] = header.value
    end
    SimpleScraper::CookieHeader.all(SimpleScraper::CookieHeader.gatherers.id => gatherer.attribute_get(:id)).each do |cookie|
      cookies[cookie.name] = cookie.value
    end
    object[:gatherers][gatherer.creator_id + '/' + gatherer.attribute_get(:id)] = {
      :urls => urls,
      :posts => posts,
      :headers => headers,
      :cookies => cookies
    }
  end
  
  resources[SimpleScraper::Interpreter].each do |interpreter|
    source_attributes = []
    if interpreter.source_attribute != ''
      source_attributes.push(interpreter.source_attribute)
    end
    SimpleScraper::Gatherer.all(SimpleScraper::Gatherer.interpreters.id => interpreter.attribute_get(:id)).each do |gatherer|
      source_attributes.push('gatherer.' + gatherer.creator_id + '/' + gatherer.attribute_get(:id))
    end
    object[:interpreters][interpreter.creator_id + '/' + interpreter.attribute_get(:id)] = {
      :source_attributes => source_attributes,
      :regex => interpreter.regex,
      :match_number => interpreter.match_number,
      :target_attribute => interpreter.target_attribute
    }
  end
  
  resources[SimpleScraper::Generator].each do |generator|
    source_attributes = []
    if generator.source_attribute != ''
      source_attributes.push(generator.source_attribute)
    end
    SimpleScraper::Gatherer.all(SimpleScraper::Gatherer.generators.id => generator.attribute_get(:id)).each do |gatherer|
      source_attributes.push('gatherer.' + gatherer.creator_id + '/' + gatherer.attribute_get(:id))
    end
    object[:generators][generator.creator_id + '/' + generator.attribute_get(:id)] = {
      :source_attributes => source_attributes,
      :regex => generator.regex,
      :target_area => generator.target_area,
      :target_info => generator.target_info,
      :target_attribute => generator.target_attribute
    }
  end
  
  object.to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  'Not found'.to_json
end
