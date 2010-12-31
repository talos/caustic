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
  #set :public, Dir.pwd ++ './front'
end

user = SimpleScraper::User.first_or_create(:name => 'test')

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
  ['area', 'type', 'publish', 'default', 'interpreter', 'generator', 'gatherer', 'post', 'header', 'cookie'].to_json
end

get '/back/:model' do
  redirect '/back/:model/'
end

# Display the existing members of a model.
get '/back/:model/' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  model.all.collect {|resource| resource.name}.to_json
end

# Put (replace) an existing resource by name.
# TODO: Currently renaming buggers up any taggings.
put '/back/:model/:oldname' do
  # TODO: Logged in user must be the creator or an editor.
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first_or_create(:creator => user, :name => params[:oldname]) or return error
  params.delete('creator_name') # Prevent creator change.
  if(not params['name']) # Name does not need to be specified in the header.
    params['name'] = params[:oldname]
  end
  params.delete_if do |param_name, param_value| # Delete attributes not specified in the model
    not resource.attributes.keys.include? param_name.downcase.to_sym
  end
  resource.attributes= params
  resource.save or error resource.errors.to_a.to_json
end

# Get a resource by name.
get '/back/:model/:name' do
  resource = DataMapper::Model.find_model(params[:model]).
    first({:creator => user, :name => params[:name]}) or return not_found
  resource.inspect.to_json
end

# Delete a resource by name.  Delete all affiliated taggings.
delete '/back/:model/:name' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:creator => user, :name => params[:name]}) or return not_found
  
  # Delete affiliated taggings.
  resource.class.tagging_types.each do |tagging_type|
    resource.send(tagging_type).all.each { |link| link.destroy }
  end

  resource.destroy or error resource.errors.to_a.to_json
end

# Tag a resource.  Create the tag if it does not yet exist.
put '/back/:model/:name/:tag/:tag_id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:creator => user, :name => params[:name]}) or return not_found
  tag_type = params[:tag].downcase
  
  tag_relationship = resource.class.relationships[tag_type] or return not_found
  tag_model = tag_relationship.target_model
  tag_key = tag_model.key.first.name.to_sym
  tag_resource = tag_model.first_or_create(:creator => user, tag_key => params[:tag_id])\
  or return error tag_resource.errors.collect { |e| e.to_s }.to_json

  link_keys = {
    params[:model].downcase + '_' + model.key.first.name.to_s => params[:name],
    params[:model].downcase + '_creator_name' => user.name,
    tag_type.downcase.sub(/s$/, '') + '_' + tag_model.key.first.name.to_s => params[:tag_id],
    tag_type.downcase.sub(/s$/, '') + '_creator_name' => user.name
  }
  
  link = tag_relationship.through.target_model.first_or_new(link_keys) or error
  link.save or error link.errors.collect { |e| e.to_s }.to_json
  # puts link.to_json
  # puts tag_relationship.through.target_model.name.to_json
  # puts tag_model.name.to_json
  # puts tag_resource.to_json
  # puts tag_type.to_json
  
  #resource.send(tag_type) << tag_resource
  #puts resource.send(tag_type).all.to_json
  
  #resource.save or error resource.errors.collect { |e| e.to_s }.to_json
end

# Delete a tag.
delete '/back/:model/:name/:tag/:tag_id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:creator => user, :name => params[:name]}) or return not_found
  tag_type = params[:tag].downcase
  
  tag_relationship = resource.class.relationships[tag_type] or return not_found
  tag_model = tag_relationship.target_model
  
  source_key = params[:model].downcase + '_' + model.key.first.name.to_s
  target_key = tag_type.downcase.sub(/s$/, '') + '_' + tag_model.key.first.name.to_s

  link = tag_relationship.through.target_model.first(source_key => params[:name], target_key => params[:tag_id]) or not_found
  link.destroy or error link.errors.to_a.to_json
end

# Get all the gatherers, generators, and interpreters associated with an area, type, and creator
get '/client/:creator/:area/:type' do
  models = [ SimpleScraper::Gatherer, SimpleScraper::Interpreter, SimpleScraper::Generator ]
  publish_collection = SimpleScraper::Publish.all
  default_collection = SimpleScraper::Default.all
  resources = {
    :defaults => {}
  }
  models.each do |model|
    resources[model] = (model.all(model.creator.name => params[:creator]) & \
                        model.all(model.areas.name => params[:area]) & \
                        model.all(model.types.name => params[:type]))
  end

  default_collection = (SimpleScraper::Default.all(SimpleScraper::Default.areas.name => params[:area]) & \
                        SimpleScraper::Default.all(SimpleScraper::Default.creator.name => params[:creator]))
  publish_collection = (SimpleScraper::Publish.all(SimpleScraper::Publish.types.name => params[:type]) & \
                        SimpleScraper::Publish.all(SimpleScraper::Publish.creator.name => params[:creator]))
  
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
    posts, headers, cookies = {}, {}, {}
    SimpleScraper::Post.all(SimpleScraper::Post.gatherers.name => gatherer.name).each { |post|
       posts[post.post_name] = post.post_value
    }
    SimpleScraper::Header.all(SimpleScraper::Header.gatherers.name => gatherer.name).each { |header|
       headers[header.header_name] = header.header_value
    }
    SimpleScraper::Cookie.all(SimpleScraper::Cookie.gatherers.name => gatherer.name).each { |cookie|
       cookies[cookie.cookie_name] = cookie.cookie_value
    }
    puts gatherer.name
    puts gatherer.attribute_get('url')
    object[:gatherers][gatherer.creator_name + '/' + gatherer.name] = {
      :url => gatherer.url,
      :posts => posts,
      :headers => headers,
      :cookies => cookies
    }
  end
  
  resources[SimpleScraper::Interpreter].each do |interpreter|
    object[:interpreters][interpreter.creator_name + '/' + interpreter.name] = {
      :source_attribute => interpreter.source_attribute,
      :regex => interpreter.regex,
      :match_number => interpreter.match_number,
      :target_attribute => interpreter.target_attribute
    }
  end
  
  resources[SimpleScraper::Generator].each do |generator|
    target_areas = SimpleScraper::TargetArea.all(SimpleScraper::TargetArea.generators.name => generator.name)
    target_types = SimpleScraper::TargetType.all(SimpleScraper::TargetType.generators.name => generator.name)
    object[:generators][generator.creator_name + '/' + generator.name] = {
      :source_attribute => generator.source_attribute,
      :regex => generator.regex,
      :target_areas => target_areas.collect {|target_area| target_area.name},
      :target_types => target_types.collect {|target_type| target_type.name},
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
