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
require 'net/http'
require 'net/https'

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

      if params[:name]
        tag_resource = tag_model.first_or_new_from_name(params[:name])
      else
        tag_resource = tag_model.first_or_new_from_key(params[:relationship_id])
      end

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

  # Courtesy http://blog.saush.com/2009/04/02/write-a-sinatra-based-twitter-clone-in-200-lines-of-ruby-code/
  # TODO :: Should this even be here?  Should move the API key stuff elsewhere, obviously.
  def SimpleScraper::get_user(token)
    u = URI.parse('https://rpxnow.com/api/v2/auth_info')
    apiKey = '344cef0cc21bc9ff3b406a7b2c2a2dffc79d39dc'
    req = Net::HTTP::Post.new(u.path)
    req.set_form_data({'token' => token, 'apiKey' => apiKey, 'format' => 'json', 'extended' => 'true'})
    http = Net::HTTP.new(u.host,u.port)
    http.use_ssl = true if u.scheme == 'https'
    json = JSON.parse(http.request(req).body)
    
    if json['stat'] == 'ok'
      identifier = json['profile']['identifier']
      nickname = json['profile']['preferredUsername']
      nickname = json['profile']['displayName'] if nickname.nil?
      email = json['profile']['email']
      {:identifier => identifier, :nickname => nickname, :email => email}
    else
      raise LoginFailedError, 'Cannot log in. Try another account!' 
    end
  end
end

get '/' do
  if session[:user_id].nil? 
    redirect '/login'
  elsif SimpleScraper::User.first(:id => session[:user_id]).nil?
    redirect '/login'
  else
    File.read(File.join('../front', 'index.html'))
  end
end

get '/login' do
  File.read(File.join('../front', 'login.html'))
end

# Login!
post '/login' do
  if params[:token]
    rpx_user = SimpleScraper::get_user params[:token]
    
    # TODO: using the rpx_uxer[:identifier] is clunky, the nickname is non-unique, and the email is privacy-violating...
    user = SimpleScraper::User.first_or_new_from_name(rpx_user[:identifier])
    user.save or error SimpleScraper::Exception.from_resources(user).to_json
    #user = SimpleScraper::User.first_or_create(:name => rpx_user[:identifier])
    session[:user_id] = user.attribute_get(:id)
    #user.location.to_json
    redirect '/#' + user.location
  else
    error "No RPX login token."
  end
end

###### RESOURCE MODELS
# Display the existing members of a model.  Limited to the top 100, with an optional query string.
get '/:resource_model/' do
  model = SimpleScraper::Resource.find_model(params) or return not_found
  model.all_like(params).collect {|resource| resource.location }.to_json
end

###### RESOURCES
# Create a new resource. Returns the location of the new resource.
# Impossible outside of the context of an existing resource (namely, user).
# put '/:resource_model/' do
#   begin
#     resource = SimpleScraper::Resource.first_or_create(params) or return not_found
#     resource.location.to_json
#   rescue SimpleScraper::Exception => exception
#     error exception.to_json
#   end
# end
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
    puts tag_name
    resource.send(tag_name).all.each { |link| puts link.to_json }
    resource.send(tag_name).all.each { |link| link.destroy }
  end
  resource.destroy or error SimpleScraper::Exception.from_resources(resource).to_json
end

###### TAG MODELS
# Redirect to the tag's model.
get '/:resource_model/:relationship/' do
  tag_model = SimpleScraper::Tag.find_model(params) or not_found
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

# Collect scrapers: this pulls any interpreters, gatherers, and generators that eventually link to a piece of
# data that would be published for an information in an area.
get '/scrapers/:area/:info' do
  
  #creator = SimpleScraper::User.first(:id => params[:creator]) #or return not_found # Creator is optional.
  area = SimpleScraper::Area.first(:name => params[:area]) or return not_found
  info = SimpleScraper::Info.first(:name => params[:info]) or return not_found
  
  # Collect associated areas non-redundantly.
  area_ids = []
  def get_area_ids(check_area, area_ids)
    area_ids << check_area.attribute_get(:id)
    if area_ids.length == area_ids.uniq.length
      check_area.follow_areas.each { |assoc_area| get_area_ids(assoc_area, area_ids) }
    else
      area_ids.uniq!
    end
  end
  get_area_ids(area, area_ids)

  info_id = info.attribute_get(:id)
  
  data_collection = SimpleScraper::Data.all(SimpleScraper::Data.areas.id => area_ids) & \
                    SimpleScraper::Data.all(SimpleScraper::Data.infos.id => info_id)
  if(params[:creator])
    data_collection = data_collection & SimpleScraper::Data.all(:creator_id => params[:creator])
  end

  data_ids = []
  gatherers = []
  def get_data_ids (check_datas, data_ids, gatherers)
    check_data_ids = check_datas.collect { |check_data| check_data.attribute_get(:id) } - data_ids 
    data_ids.push(*check_data_ids)
    
    interpreter_collection = SimpleScraper::Interpreter.all(SimpleScraper::Interpreter.target_datas.id => check_data_ids)
    generator_collection   = SimpleScraper::Generator.all(SimpleScraper::Generator.target_datas.id => check_data_ids)
    
    gatherers.push(*interpreter_collection.collect { |interpreter| interpreter.gatherers.all } )
    gatherers.push(*generator_collection.collect   { |generator|   generator.gatherers.all   } )
    gatherers.uniq!
    
    additional_datas = []
    additional_datas.push(*interpreter_collection.collect { |interpreter| interpreter.source_datas.all })
    additional_datas.push(*generator_collection.collect   { |generator|   generator.source_datas.all   })
    
    get_data_ids additional_datas, data_ids, gatherers
  end
  get_data_ids data_collection.all, data_ids, gatherers

  object = {
    :publishes    => SimpleScraper::Publish.all(SimpleScraper::Publish.infos.id => info_id).collect  { |publish| publish.name },
    :defaults     => SimpleScraper::Default.all(SimpleScraper::Default.areas.id => area_ids).collect { |default| default.name },
    :gatherers    => {},
    :interpreters => {},
    :generators   => {}
  }
  
  gatherers.each do |gatherer|
    object[:gatherers][gatherer.creator_id + '/' + gatherer.attribute_get(:name)] = gatherer.to_scraper
  end
  
  SimpleScraper::Interpreter.all(SimpleScraper::Interpreter.target_datas.id => data_ids).each do |interpreter|
    object[:interpreters][interpreter.creator_id + '/' + interpreter.attribute_get(:name)] = interpreter.to_scraper
  end
  
  SimpleScraper::Generator.all(SimpleScraper::Generator.target_datas.id => data_ids).each do |generator|
    object[:generators][generator.creator_id + '/' + generator.attribute_get(:name)] = generator.to_scraper
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
