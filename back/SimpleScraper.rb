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
  def self.find_model(name)
    DataMapper::Model.descendants.find { |model| model.raw_name == name }
  end
  
  module Resource
    def self.find_model(params)
      SimpleScraper::find_model(params[:resource_model])
    end

    def self.first(params)
      model = find_model(params) or return
      model.get(params[:resource_id])
    end
    
    def self.first_or_create(params)
      model = find_model(params) or return
      resource = model.first_or_new(:id => params[:resource_id])
      params.delete_if { |param, value| SimpleScraper::RESERVED_WORDS.include? param }
      resource.safe_attributes= params
      resource.save or raise SimpleScraper::Exception.from_resources(resource)
      resource
    end
  end

  module Tag
    def self.find_model(params)
      model = SimpleScraper::Resource.find_model(params) or return
      model.tag_names.include? params[:relationship].to_sym or return
      model.send(params[:relationship]).model
    end
    
    def self.first(params)
      resource = SimpleScraper::Resource.first(params) or return
      resource.model.tag_names.include? params[:relationship].to_sym or return
      
      resource.send(params[:relationship]).get(params[:relationship_id])
    end
    
    def self.first_or_create(params)
      resource = SimpleScraper::Resource.first(params) or return
      resource.model.tag_names.include? params[:relationship].to_sym or return
      if params[:relationship_id]
        tag_model = SimpleScraper::Tag.find_model(params) or return
        tag = tag_model.get(params[:relationship_id]) or return
        resource.send(params[:relationship]) << tag
        resource.save ? resource : false
      elsif params[:name]
        resource.send(params[:relationship]).first_or_create(:name => params[:name])
      end
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
      raise RuntimeError, 'Cannot log in. Try another account!' 
    end
  end
end

get '/' do
  if session[:user_id].nil? 
    redirect '/login'
  #elsif SimpleScraper::User.first(:id => session[:user_id]).nil?
  elsif SimpleScraper::User.get(session[:user_id]).nil?
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
    begin
      rpx_user = SimpleScraper::get_user params[:token]
    rescue Error => exception
      return error exception.to_s
    end
    
    # Synthesize a unique name.
    name = rpx_user[:nickname] + '@' + URI.parse(rpx_user[:identifier]).host
    user = SimpleScraper::User.first_or_new_from_name(name)
    user.save or error SimpleScraper::Exception.from_resources(user).to_json
    session[:user_id] = user.attribute_get(:id)
    redirect '/#' + user.location
  else
    error "No RPX login token."
  end
end

###### RESOURCE MODELS
# Display the existing members of a model.  Limited to the top 100, with an optional query string.
get '/:resource_model/' do
  model = SimpleScraper::Resource.find_model(params) or return not_found
  model.all_like(params).collect do |resource|
    {
      :id => resource.attribute_get(:id),
      :name => resource.full_name
    }
  end .to_json
end

###### RESOURCES
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
  resource.destroy or error SimpleScraper::Exception.from_resources(resource).to_json
end

###### TAG MODELS
# Redirect to the tag's model.
get '/:resource_model/:relationship/' do
  tag_model = SimpleScraper::Tag.find_model(params) or not_found
  redirect tag_model.location + '?' + request.query_string
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
  redirect tag_resource.location
end
# Replace a tag.  This also creates resources.
put '/:resource_model/:resource_id/:relationship/:relationship_id' do
  begin
    tag_resource = SimpleScraper::Tag.first_or_create(params) or return not_found
    tag_resource.location.to_json
  rescue SimpleScraper::Exception => exception
    error exception.to_json
  end
end
# Delete a tagging.
delete '/:resource_model/:resource_id/:relationship/:relationship_id' do
  resource = SimpleScraper::Resource.first(params) or return not_found
  tag = SimpleScraper::Tag.first(params) or return not_found
  
  resource.untag(params[:relationship], tag).to_json or error SimpleScraper::Exception.from_resources(resource, tag).to_json
end

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

  data_ids, gatherers = [], []
  def get_data_ids (check_datas, data_ids, gatherers)
    check_data_ids = check_datas.collect { |check_data| check_data.attribute_get(:id) } - data_ids 
    data_ids.push(*check_data_ids)
    #check_datas.collect { |check_data| puts check_data.describe.to_json }
    #if check_datas.length > 0
    
    # Not sure why this is necessary. Something involving loading??
    check_datas[0].interpreter_targets #.to_a.to_json
    check_datas[0].generator_targets   #.to_a.to_json
    #end
    interpreters = check_datas.collect { |check_data| check_data.interpreter_targets.all.to_a }.flatten
    generators   = check_datas.collect { |check_data| check_data.generator_targets.all.to_a   }.flatten
    gatherers.push(*interpreters.collect { |interpreter| interpreter.gatherers.all.to_a }.flatten )
    gatherers.push(*generators.collect   { |generator|   generator.gatherers.all.to_a   }.flatten )
    gatherers.uniq!
    additional_datas = []
    additional_datas.push(*interpreters.collect { |interpreter| interpreter.source_datas.all.to_a }.flatten)
    additional_datas.push(*generators.collect   { |generator|   generator.source_datas.all.to_a   }.flatten)
    
    if additional_datas.length > 0
      get_data_ids additional_datas, data_ids, gatherers
    end
  end
  get_data_ids data_collection, data_ids, gatherers

  object = {
    :publishes    => info.publishes.collect  { |publish| publish.name },
    :defaults     => SimpleScraper::Default.all(SimpleScraper::Default.areas.id => area_ids).collect { |default| default.name },
    :gatherers    => {},
    :interpreters => {},
    :generators   => {}
  }
  
  gatherers.each do |gatherer|
    object[:gatherers][gatherer.full_name] = gatherer.to_scraper
  end
  
  SimpleScraper::Data.all(:id => data_ids).each do |data|
    data.interpreter_targets.each do |interpreter|
  #SimpleScraper::Interpreter.all(SimpleScraper::Interpreter.target_datas.id => data_ids).each do |interpreter|
      object[:interpreters][interpreter.full_name] = interpreter.to_scraper
    end
  end
  
  SimpleScraper::Data.all(:id => data_ids).each do |data|
    data.generator_targets.each do |generator|
  #SimpleScraper::Interpreter.all(SimpleScraper::Interpreter.target_datas.id => data_ids).each do |interpreter|
      object[:generators][generator.full_name] = generator.to_scraper
    end
  end
  # SimpleScraper::Generator.all(SimpleScraper::Generator.target_datas.id => data_ids).each do |generator|
  #   object[:generators][generator.creator_id + '/' + generator.attribute_get(:name)] = generator.to_scraper
  # end
  
  object.to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  'Not found'.to_json
end
