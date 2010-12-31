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

puts File.dirname(__FILE__) + './front'

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
  ['publish', 'default', 'interpreter', 'generator', 'gatherer', 'post', 'header', 'cookie'].to_json
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
  tag_key = tag_model.key.first.name.to_sym # Does not support multi-key right now.
  tag_resource = tag_relationship.target_model.first_or_create(:creator => user, tag_key => params[:tag_id]) or return error
  
  resource.send(tag_type) << tag_resource
  
  resource.save or error tag_resource.errors.collect { |e| e.to_s }.to_json
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

# Get all the gatherers, generators, and interpreters associated with an area, type, or creator
get '/client/' do
  #params.delete_if {|k, v| not [:area, :type, :creator].include?(k.downcase.to_sym) }
  conditions = {}
  if(params[:area])
#    conditions[:areas] = {:name => params[:area]}
    conditions[:areas] = SimpleScraper::Area.all(:name => params[:area])
  end
  if(params[:type])
    conditions[:types] = {:name => params[:type]}
  end
  if(params[:creator])
    conditions[:creators] = {:name => params[:creator]}
  end
  object = {
    :publishes => [],
    :defaults => {},
    :gatherers => {},
    :interpreters => {},
    :generators => {}
  }
  puts conditions.to_json
  puts SimpleScraper::Gatherer.all.to_a.to_json
  areas = SimpleScraper::Area.all(:name => params[:area])
  puts SimpleScraper::Gatherer.first.areas.first.name.to_json
  
  puts SimpleScraper::Gatherer.all(:areas => [areas]).to_a.to_json
  SimpleScraper::Gatherer.all(conditions).each do |gatherer|
    object[:gatherers][gatherer.name] = {
      :url => '',
      :posts => {},
      :headers => {},
      :cookies => {}
    }
  end
  
  SimpleScraper::Interpreter.all(conditions).each do |interpreter|
    object[:interpreters][interpreter.name] = {
      :source_attribute => interpreter.source_attribute,
      :regex => interpreter.regex,
      :match_number => interpreter.match_number,
      :target_attribute => interpeter.target_attribute
    }
  end
  
  SimpleScraper::Generator.all(conditions).each do |generator|
    object[:generators][generator.name] = {
      :source_attribute => generator.source_attribute,
      :regex => generator.regex,
      :target_areas => [],
      :target_types => [],
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
