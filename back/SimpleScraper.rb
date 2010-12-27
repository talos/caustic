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

get '/front/css/:page' do
  serve_page('../front/css/' + params[:page])
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
  ['area/', 'type/', 'publish/', 'default/', 'interpreter/', 'generator/', 'gatherer/', \
  'url/', 'post/', 'header/', 'cookie/'].to_json
end

get '/back/:collection' do
  redirect '/back/:collection/'
end

# Display the existing members of a collection.
get '/back/:collection/' do
  collection = DataMapper::Model.find_collection(params[:collection]) or return not_found
  collection.all.collect {|resource| resource.id}.to_json
end

# Post to a collection, retrieving the path to put something else there.
post '/back/:collection/' do
  collection = DataMapper::Model.find_collection(params[:collection]) or return not_found
  resource = collection.new(:user => user)
  resource.save or return error resource.errors.to_a.to_s
  (request.path_info + resource.id.to_s).to_json
end

# Put (replace) an existing resource by ID.
put '/back/:collection/:id' do
  # TODO: Logged in user must be the creator or an editor.
  collection = DataMapper::Model.find_collection(params[:collection]) or return not_found
  resource = collection.first_or_create(:user => user, :id => params[:id]) or return error
  params.delete('id')
  params.delete('user_id') #prevent user change
  params.each do |k, v|
    params.delete(k) unless resource.attributes.keys.include?(k.to_sym) # delete nonexistent attributes
  end
  resource.attributes= params
  resource.save or error resource.errors.to_a.to_json
end

# Get a resource by ID
get '/back/:collection/:id' do
  resource = DataMapper::Model.find_collection(params[:collection]).
    first({:user => user, :id => params[:id]}) or return not_found
  resource.inspect.to_json
end

# Delete a resource by ID
delete '/back/:collection/:id' do
  collection = DataMapper::Model.find_collection(params[:collection]) or return not_found
  resource = collection.first({:user => user, :id => params[:id]}) or return not_found
  resource.destroy or error resource.errors.to_a.to_json
end

# Tag a resource.
put '/back/:collection/:id/:tag/:tag_id' do
  collection = DataMapper::Model.find_collection(params[:collection]) or return not_found
  resource = collection.first({:user => user, :id => params[:id]}) or return not_found
  tag_name = params[:tag].downcase
  tag_relationship = resource.class.relationships[tag_name] or return not_found
  tag_resource = tag_relationship.target_model.first(:id => params[:tag_id]) or return not_found

  resource.send(tag_name) << tag_resource
  resource.save or error resource.errors.to_a.to_json
end

# Delete a tag.
delete '/back/:collection/:id/:tag/:tag_id' do
  collection = DataMapper::Model.find_collection(params[:collection]) or return not_found
  resource = collection.first({:user => user, :id => params[:id]}) or return not_found
  tag_plural_name = params[:tag].downcase
  tag_name = tag_plural_name.sub(/s$/, '')
  tag_relationship = resource.class.relationships[tag_plural_name] or return not_found
  tag_resource = resource.send(tag_plural_name).first(:id => params[:tag_id]) #buggy?
  tagging_relationship_name = resource.class.tagging_names.find { |t| t =~ Regexp.new(tag_name)}
  
  source_id_name = params[:collection].downcase + '_id'
  target_id_name = tag_name + '_id'
  link = resource.send(tagging_relationship_name).first(source_id_name => params[:id], target_id_name => params[:tag_id])
  link.destroy or error link.errors.to_a.to_json
#    .first(params[:collection].downcase.to_sym => resource, tag_name.to_sym => tag_resource).to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  'Not found'.to_json
end
