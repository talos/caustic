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
end

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

# Display the editable resources.
get '/back/' do
  DataMapper::Model.editable_descendants.collect {|resource| resource.to_s.sub('SimpleScraper::', '') }.to_json
end

get '/back/:resource' do
  redirect '/back/:resource/'
end

# Display the existing members of a resource.
get '/back/:resource/' do
  @resource = DataMapper::Model.find_resource(params[:resource]) or not_found
  @resource.all
end

# Get the ID to create a new resource.
post '/back/:resource/' do
  @resource = DataMapper::Model.find_resource(params[:resource]) or not_found
  @resource.create.attributes.to_json
end

# Replace an existing resource by ID
put '/back/:resource/:id' do
  DataMapper::Model.find_resource(params[:resource]).new(params).id
end

# Delete a resource by ID
delete '/back/:resource/:id' do
  DataMapper::Model.find_resource(params[:resource]).new(params).delete(params[:id])
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  'Not found'.to_json
end
