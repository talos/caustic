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

user = SimpleScraper::User.first_or_create(:id => 'test')

get '/' do
  redirect '/index.html'
end

# Login!
post '/login' do
#  session[:user] = params[:user]
  not_found
end

# Display the editable resources.
get '/back/' do
  ['area', 'info', 'publish', 'default', 'interpreter', 'generator', 'gatherer', 'post', 'header', 'cookie'].to_json
end

get '/back/:model' do
  redirect '/back/:model/'
end

# Display the existing members of a model.
get '/back/:model/' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  model.all.collect {|resource| resource.attribute_get(:id)}.to_json
end

# Put (replace) an existing resource by id.
# TODO: Currently renaming buggers up any taggings.
put '/back/:model/:oldid' do
  # TODO: Logged in user must be the creator or an editor.
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first_or_create(:creator => user, :id => params[:oldid]) or return error
  params.delete('creator_id') # Prevent creator change.
  if(not params['id']) # ID does not need to be specified in the header.
    params['id'] = params[:oldid]
  end
  params.delete_if do |param_name, param_value| # Delete attributes not specified in the model
    not resource.attributes.keys.include? param_name.downcase.to_sym
  end
  resource.attributes= params
  resource.save or error resource.errors.to_a.to_json
end

# Get a resource by id.
get '/back/:model/:id' do
  resource = DataMapper::Model.find_model(params[:model]).
    first({:creator => user, :id => params[:id]}) or return not_found
  resource.inspect.to_json
end

# Delete a resource by id.  Delete all affiliated taggings.
delete '/back/:model/:id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:creator => user, :id => params[:id]}) or return not_found
  
  # Delete affiliated taggings.
  resource.class.tagging_types.each do |tagging_type|
    resource.send(tagging_type).all.each { |link| link.destroy }
  end

  resource.destroy or error resource.errors.to_a.to_json
end

# Tag a resource.  Create the tag if it does not yet exist.
# Manually creates the tagging too. Boo.
put '/back/:model/:model_id/:tag/:tag_id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:creator => user, :id => params[:model_id]}) or return not_found
  tag_type = params[:tag].downcase
  
  tag_relationship = resource.class.relationships[tag_type] or return not_found
  tag_model = tag_relationship.target_model
  tag_key = tag_model.key.first.name.to_sym
  tag_resource = tag_model.first_or_create(:creator => user, tag_key => params[:tag_id])\
  or return error tag_resource.errors.collect { |e| e.to_s }.to_json

  puts tag_resource.to_json

  link_key_names = tag_relationship.through.target_model.key.collect{|key| key.name}
  link_keys = {}
  link_key_names.each do |link_key_name|
    link_keys[link_key_name] = case link_key_name.to_s
                               when params[:model].downcase + '_' + model.key.first.name.to_s then params[:model_id]
                               when params[:model].downcase + '_creator_id' then user.attribute_get(:id)
                               when tag_model.raw_name.downcase + '_' + tag_model.key.first.name.to_s then params[:tag_id]
                               when tag_model.raw_name.downcase + '_creator_id' then user.attribute_get(:id)
                               else return error link_key_name
                               end
  end
  
  link = tag_relationship.through.target_model.first_or_new(link_keys) or error
  link.save or error link.errors.collect { |e| e.to_s }.to_json
end

# Delete a tag.
delete '/back/:model/:model_id/:tag/:tag_id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:creator => user, :id => params[:model_id]}) or return not_found
  tag_type = params[:tag].downcase
  
  tag_relationship = resource.class.relationships[tag_type] or return not_found
  tag_model = tag_relationship.target_model
  
  source_key = params[:model].downcase + '_' + model.key.first.name.to_s
  target_key = tag_type.downcase.sub(/s$/, '') + '_' + tag_model.key.first.name.to_s

  link = tag_relationship.through.target_model.first(source_key => params[:model_id], target_key => params[:tag_id]) or not_found
  link.destroy or error link.errors.to_a.to_json
end

# List areas & infos covered by a certain creator.
get '/client/:creator/' do
  
end

# List infos covered by a certain creator & area.
get '/client/:creator/:area/' do
  
end

# Get all the gatherers, generators, and interpreters associated with an area, info, and creator.
# Returns any of the aforementioned objects if they fall within the ancestor tree of the specified area.
get '/client/:creator_id/:area_id/:info_id' do

  creator = SimpleScraper::User.first(:id => params[:creator_id]) or return not_found
  area = SimpleScraper::Area.first(:id => params[:area_id]) or return not_found
  info = SimpleScraper::Info.first(:id => params[:info_id]) or return not_found

  area_ids = area.ancestors.collect{ |parent_area| parent_area.attribute_get(:id) }.push(area.attribute_get(:id))
  puts area_ids.to_json

  models = [ SimpleScraper::Gatherer, SimpleScraper::Interpreter, SimpleScraper::Generator ]
  publish_collection = SimpleScraper::Publish.all
  default_collection = SimpleScraper::Default.all
  
  resources = {
    :defaults => {}
  }
  models.each do |model|
    resources[model] = (model.all(model.creator.id => params[:creator_id]) & \
                        model.all(model.areas.id => area_ids) & \
                        model.all(model.infos.id => params[:info_id]))
  end
  
  default_collection = (SimpleScraper::Default.all(SimpleScraper::Default.areas.id => area_ids) & \
                        SimpleScraper::Default.all(SimpleScraper::Default.creator.id => params[:creator_id]))
  publish_collection = (SimpleScraper::Publish.all(SimpleScraper::Publish.infos.id => params[:info_id]) & \
                        SimpleScraper::Publish.all(SimpleScraper::Publish.creator.id => params[:creator_id]))
  
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
    SimpleScraper::Cookie.all(SimpleScraper::Cookie.gatherers.id => gatherer.attribute_get(:id)).each do |cookie|
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
