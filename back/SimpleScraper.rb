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
end

user = SimpleScraper::User.create(:name => 'test')

# GET
def serve_page(location)
  if(File.exists?(location))
    File.open(location)
  else
    'Could not open ' + location
  end
end

# Serve the front end page.
get '/front/' do
  serve_page('../front/index.html')
end

get '/front/js/:page' do
  serve_page('../front/js/' + params[:page])
end

get '/front/css/:page' do # Gah that is irritating.
  [200, {'Content-Type' => 'text/css'}, serve_page('../front/css/' + params[:page])]
end

# Login!
post '/login' do
#  session[:user] = params[:user]
  not_found
end

get '/' do
  redirect '/back/'
end

# Display the editable resources.
get '/back/' do
  ['publish', 'default', 'interpreter', 'generator', 'gatherer', \
  'url', 'post', 'header', 'cookie'].to_json
end

get '/back/:model' do
  redirect '/back/:model/'
end

# Display the existing members of a model.
get '/back/:model/' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  model.all.collect {|resource| resource.id}.to_json
end

# Post to a model, retrieving the id to new resource.
post '/back/:model/' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.new(:user => user)
  resource.save or return error resource.errors.to_a.to_s
  resource.id.to_s.to_json
end

# Put (replace) an existing resource by ID.
put '/back/:model/:id' do
  # TODO: Logged in user must be the creator or an editor.
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first_or_create(:user => user, :id => params[:id]) or return error
  params.delete('id')
  params.delete('user_id') #prevent user change
  params.each do |k, v|
    params.delete(k) unless resource.attributes.keys.include?(k.to_sym) # delete nonexistent attributes
  end
  resource.attributes= params
  resource.save or error resource.errors.to_a.to_json
end

# Get a resource by ID.
get '/back/:model/:id' do
  resource = DataMapper::Model.find_model(params[:model]).
    first({:user => user, :id => params[:id]}) or return not_found
  resource.inspect.to_json
end

# Delete a resource by ID.  Delete all affiliated taggings.
delete '/back/:model/:id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:user => user, :id => params[:id]}) or return not_found

  # Delete affiliated taggings.
  resource.class.tagging_types.each do |type|
    puts resource.send(type).all.to_a
    resource.send(type).all.each do |link|
      link.destroy
    end
  end

  resource.destroy or error resource.errors.to_a.to_json
end

# Tag a resource.  Create the tag if it does not yet exist.
put '/back/:model/:id/:tag/:tag_id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:user => user, :id => params[:id]}) or return not_found
  tag_type = params[:tag].downcase
  
  tag_relationship = resource.class.relationships[tag_type] or return not_found
  tag_model = tag_relationship.target_model
  tag_key = tag_model.key.first.name.to_sym # Does not support multi-key right now.
  tag_resource = tag_relationship.target_model.first_or_create(tag_key => params[:tag_id]) or return error
  
  resource.send(tag_type) << tag_resource
  
  resource.save or error tag_resource.errors.collect { |e| e.to_s }.to_json
end

# Delete a tag.
delete '/back/:model/:id/:tag/:tag_id' do
  model = DataMapper::Model.find_model(params[:model]) or return not_found
  resource = model.first({:user => user, :id => params[:id]}) or return not_found
  tag_type = params[:tag].downcase
  
  tag_relationship = resource.class.relationships[tag_type] or return not_found
  tag_model = tag_relationship.target_model
  
  source_key = params[:model].downcase + '_' + model.key.first.name.to_s
  target_key = tag_type.downcase.sub(/s$/, '') + '_' + tag_model.key.first.name.to_s

  puts source_key
  puts params[:id]
  puts target_key
  puts params[:tag_id]

  link = tag_relationship.through.target_model.first(source_key => params[:id], target_key => params[:tag_id]) or not_found
  link.destroy or error link.errors.to_a.to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  'Not found'.to_json
end
