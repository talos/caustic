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

#user = SimpleScraper::User.first_or_create(:id => 'test')

# Helper functions to interface with the DB.
# params: creator, model, id, tag_creator, tag_model, tag_id
module SimpleScraper
  SimpleScraper::MAX_RECORDS = 100
  def SimpleScraper::find_user_model(params)
    #user = SimpleScraper::User.first(:id => params[:creator]) or return
    SimpleScraper::User.tagging_types.include? params[:model].downcase or return
    SimpleScraper::User.taggings.select { |k, v| k == params[:model].downcase }.flatten[1].child_model
  end
  def SimpleScraper::find_tag_model(params)
    #model = find_model(params) or return
    model = find_user_model(params) or return
    model.tag_types.include? params[:tag_model].downcase or return
    model.tag_types.select { |k, v| k == params[:tag_model].downcase }.flatten[1].child_model
  end
  def SimpleScraper::first_resource(params)
    model = find_user_model(params) or return
    model.first(:creator_id => params[:creator], :id => params[:id])
  end
  def SimpleScraper::first_or_create_resource(params)
    model = find_user_model(params) or return
    model.first_or_create(:creator_id => params[:creator], :id => params[:id])
  end
  def SimpleScraper::first_tag(params)
    find_tag_model(params) or return
    resource = first_resource(params) or return
    resource.send(params[:tag_model].downcase).first(:creator_id => params[:tag_creator], :id => params[:tag_id])
  end
  def SimpleScraper::first_or_create_tag(resource_creator_id, resource_model_name, resource_id, tag_creator_id, tag_model_name, tag_id)
    find_tag_model(params) or return
    resource = first_resource(params) or return
    resource.send(params[:tag_model].downcase).first_or_create(:creator_id => params[:tag_creator], :id => params[:tag_id])
  end
  def SimpleScraper::compile_errors(*resources)
    errors = {}
    resources.each do |resource|
      errors[resource.location] = resource.errors.to_a
    end
    {:errors => errors}
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
  SimpleScraper::User.create(:id => params[:id])
end

# Display the existing members of a model.  Limited to the top 100, with an optional query string.
get '/:model/' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  like_criteria = {}
  model.properties.each do |property|
    like_criteria[property.name.to_sym.like] = params[property.name] + '%' if params.include? property.name
  end
  model.all({:limit => SimpleScraper::MAX_RECORDS}.merge(like_criteria)).collect {|resource| resource.location }.to_json
end

# Display the possible tags for a model.
get '/:model/:tag_model/' do
  tag_model = SimpleScraper::find_tag_model(params) or return not_found
  redirect tag_model.location
end

# Describe a user.
get '/user/:id' do
  user = SimpleScraper::User.first(:id => params[:id]) or return not_found
  user.describe.to_json
end

# Get a resource by id.
get '/user/:creator/:model/:id' do
  resource = SimpleScraper::first_resource(params) or return not_found
  resource.describe.to_json
end

# Put (replace) an existing resource by id.
# TODO: Currently renaming buggers up any taggings.
put '/user/:creator/:model/:current_id' do
  # TODO: Logged in user must be the creator or an editor.
  params.delete('creator_id') # Prevent creator change.
  return error unless params[:model].downcase != 'user' # prevent user creation
  if(!params['id'])
    params['id'] = params[:current_id]
  end
  resource = SimpleScraper::first_or_create_resource(params) or return error
  params.delete_if do |param_name, param_value| # Delete attributes not specified in the model
    not resource.attributes.keys.include? param_name.downcase.to_sym
  end
  resource.attributes= params
  resource.save or return error SimpleScraper::compile_errors(resource).to_json
  resource.location
end

# Delete a resource by id.  Delete all affiliated taggings.
delete '/user/:creator/:model/:id' do
  resource = SimpleScraper::first_resource(params) or return not_found
  
  # Delete affiliated taggings.
  resource.class.tagging_types.each do |tagging_type|
    resource.send(tagging_type).all.each { |link| link.destroy }
  end

  resource.destroy or error SimpleScraper::compile_errors(resource).to_json
end

# Redirect to the location of the resource the tagging points to.
get '/user/:creator/:model/:id/:tag_model/:tag_creator/:tag_id' do
  tag = SimpleScraper::first_tag(params) or return not_found
  redirect tag.location
end

# Tag a resource.  Create the tag if it does not yet exist.
put '/user/:creator/:model/:id/:tag_model/:tag_creator/:tag_id' do
  resource = SimpleScraper::first_resource(params) or return not_found
  tag = SimpleScraper::first_or_create_tag(params) or return error
  
  resource.send(params[:tag_model].downcase) << tag
  resource.save or tag.send(params[:model].downcase + 's') << resource
  if tag.save
    tag.location
  else
    error SimpleScraper::compile_errors(resource, tag)
  end
end

# Delete a tag.
delete '/user/:creator/:model/:id/:tag_model/:tag_creator/:tag_id' do
  resource = SimpleScraper::first_resource(params) or return not_found
  tag = SimpleScraper::first_tag(params) or return not_found
  
  join_model = resource.send(params[:tag_model].downcase).send('through').target_model
  if(params[:tag_model].downcase == params[:model].downcase + 's') # self-join
    join_model.first(:source => resource, :target => tag).destroy
  else
    join_model.first(params[:model].downcase.to_sym => model, params[:tag_model].downcase.sub(/s$/, '').to_sym => tag).destroy or error compile_errors join_model
  end
end

# List areas & infos covered by a certain creator.
get '/:creator/' do
  
end

# List infos covered by a certain creator & area.
get '/:creator/scrapers/:area/' do
  
end

# Get all the gatherers, generators, and interpreters associated with an area, info, and creator.
# Returns any of the aforementioned objects if they fall within the ancestor tree of the specified area.
get '/:creator/scrapers/:area/:info' do

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
  puts area_ids.to_json
  
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
