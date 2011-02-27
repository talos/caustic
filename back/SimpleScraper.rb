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
  set :raise_errors, false
  set :show_exceptions, false
  set :sessions, true
  set :public, File.dirname(__FILE__) + './front'
end

module SimpleScraper
  class User
    # Courtesy http://blog.saush.com/2009/04/02/write-a-sinatra-based-twitter-clone-in-200-lines-of-ruby-code/
    # TODO :: Should this even be here?  Should move the API key stuff elsewhere, obviously.
    def self.from_token(token)
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
        # Synthesize a unique name.
        SimpleScraper::User.first_or_create(:name => nickname + '@' + URI.parse(identifier).host)
      else
        raise RuntimeError, 'Cannot log in. Try another account!' 
      end
    end
  end
  
  class SimpleScraper::Exception < RuntimeError
  end
  
  class SimpleScraper::ResourceError < SimpleScraper::Exception
    def initialize(*resources)
      @errors = {}
      resources.each do |resource|
        @errors[resource.class.to_s] = resource.errors.to_a
      end
      super @errors.to_s
    end
    def to_json
      {:errors => @errors}.to_json
    end
  end

  class SimpleScraper::PermissionsError < SimpleScraper::Exception
    def initialize (user, resource)
      @message = ( user.nickname ? user.nickname : user.name ) + 
        ' lacks permissions to modify ' + resource.model.raw_name + 
        ' ' + resource.full_name
      super(@message)
    end
  end
end

helpers do
  def to_output body
    body.to_json
  end
end

error do
  to_output(env['sinatra.error'] ? env['sinatra.error'] : response)
end

not_found do
  to_output 'Not found'
end

# Find our user.
before do
  @user = SimpleScraper::User.get(session[:user_id])
  puts params.to_json
end

# Try to find our model.
before '/:model/*' do
  @model = DataMapper::Model.descendants.find { |model| model.raw_name == params[:model] }
end

# Try find our resource.
before '/:model/:resource_id*' do 
  @resource = @model ? @model.get(params[:resource_id]) : nil
  if @resource
    # If we have a resource, do a permissions check for PUT, DELETE, and POST.
    if @resource and ['PUT', 'DELETE', 'POST'].include? request.request_method
      raise SimpleScraper::PermissionsError.new(@user, @resource) unless @user.can_edit? @resource
    end
  end
end

# Try find our relationship -- must be a valid one (listed in tag_names)
before '/:model/:resource_id/:relationship*' do
  if @resource
    relationship_name = params[:relationship].to_sym
    if @resource.model.tag_names.include? relationship_name
      @relationship = @resource.send(relationship_name)
    end
  end
end

# If that worked, try to find our related resource.
before '/:model/:resource_id/:relationship/:related_id' do
  if @relationship
    @related_resource = @relationship.get(params[:related_id])
  end
end

get '/' do
  if @user.nil?
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
    user = SimpleScraper::User.from_token params[:token]
    raise SimpleScraper::ResourceError.new(user) unless user.saved?
    session[:user_id] = user.attribute_get(:id)
    redirect '/#' + user.location
  else
    error "No RPX login token."
  end
end

###### RESOURCE MODELS
# Display the existing members of a model.  Limited to the top 100, with an optional query string.
get '/:model/' do
  list = @model.all_like(params).collect do |resource|
    {
      :id => resource.attribute_get(:id),
      :name => resource.full_name
    }
  end
  to_output list
end

###### RESOURCES
# Describe a resource.
get '/:model/:resource_id' do
  @resource ? to_output(@resource.describe) : not_found
end

# Replace a resource.
put '/:model/:resource_id' do
  if @resource
    @resource.modify params
  elsif @model
    @resource = @model.create(params.merge({:creator => @user}))
  end
  @resource.saved? ? to_output(@resource.location) : raise(SimpleScraper::ResourceError.new(@resource))
end

# Delete a resource and all its links.
delete '/:model/:resource_id' do
  @resource ? to_output(@resource.destroy) : not_found
end

###### TAG MODELS
# Redirect to the tag's model.
get '/:model/:relationship/' do
  related_model = @model ? @model.related_model(params[:relationship]) : not_found
  related_model ? redirect(related_model.location + '?' + request.query_string) : not_found
end

####### TAGS
# Create a new tag.  Returns the location of the new tag.  This also creates resources.
put '/:model/:resource_id/:relationship/' do
  @relationship ? to_output(@relationship.first_or_create(:creator => @user, :name => params[:name]).location) : not_found
end

# Redirect to the location of the actual resource.
get '/:model/:resource_id/:relationship/:related_id' do
  @related_resource ? redirect(@related_resource.location) : not_found
end

# Relate two known resources, possibly creating or replacing the second.
put '/:model/:resource_id/:relationship/:related_id' do
  if @related_resource.nil?
    puts params.to_json
    puts @model.related_model(params[:relationship]).all.to_json
    @related_resource = @model.related_model(params[:relationship]).get(params[:related_id]) or not_found
  end
  @relationship << @related_resource
  to_output(@resource.save) or raise SimpleScraper::ResourceError(@resource, @related_resource)
end

# Delete a tagging.
delete '/:resource_model/:resource_id/:relationship/:relationship_id' do
  @related_resource ? to_output(@resource.untag(params[:relationship], @related_resource)) : not_found
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
  
  to_output object
end
