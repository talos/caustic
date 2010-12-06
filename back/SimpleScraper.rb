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

require 'rubygems'
require 'db/schema'
require 'sinatra'

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

# Display the available resources.
get '/' do
  ['type/', 'gatherer/'].to_json
end

# Get a list of all the available areas.
get '/area/' do
  SimpleScraper::Area.identify_all.to_json
end

# Get details on a specific area.
get '/area/:name' do
  @area = SimpleScraper::Area.first(:name => params[:name]) or return not_found
  @area.export.to_json
end

# Get a list of all the available types.
get '/type/' do
  SimpleScraper::Type.identify_all.to_json
end

# Get details on a specific type.
get '/type/:name' do
  @type = SimpleScraper::Type.first(:name => params[:name]) or return not_found
  @type.export.to_json
end

# Get a publish field.
get '/type/:type/publish/:publish' do
  @type = SimpleScraper::Type.first(:name => params[:type]) or return not_found
  @publish = @type.publish_fields.first(:name => params[:publish]) or return not_found
  @publish.export.to_json
end

# Get a list of all the available Informations.
get '/type/:type/information/' do
  SimpleScraper::Type.first(:name => params[:type]).informations.identify_all.to_json
end

# Get details on an Information by name.
get '/type/:type/information/:information' do
#  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
#  @information.export.to_json
#  SimpleScraper::Type.first(:name => params[:type]).informations.first(:name => 
  SimpleScraper::Information.first(:type_name => params[:type], :name => params[:information]).export.to_json
end

# Get a list of all the available Informations of a specific type in a specific area.
get '/information/:type/in/:area' do
#  @information = Area.first(:name => params[:area]).informations.first(:type_name => params[:type]) or return not_found
#  @information.to_json
#  SimpleScraper::Information.all(:type => params[:type]
  @area = SimpleScraper::Area.first(:name => params[:area]) or return not_found
  @area.informations.identify_all(:type_name => params[:type]).to_json
end


# Get a list of all the available Gatherers.
get '/gatherer/' do
  SimpleScraper::Gatherer.identify_all.to_json
end

# Get details on a specific SimpleScraper::Gatherer.
get '/gatherer/:name' do
  SimpleScraper::Gatherer.first(:name => params[:name]).export.to_json
end

get '/gatherer/:gatherer/url/' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).urls.identify_all.to_json
end

get '/gatherer/:gatherer/url/:url' do
  nil
end

get '/gatherer/:gatherer/get/' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).gets.identify_all.to_json
end

get '/gatherer/:gatherer/get/:get' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).gets.first(:name => params[:get]).export.to_json or not_found
end

get '/gatherer/:gatherer/post/' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).posts.identify_all.to_json
end

get '/gatherer/:gatherer/post/:post' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).posts.first(:name => params[:post]).export.to_json or not_found
end

get '/gatherer/:gatherer/header/' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).headers.identify_all.to_json
end

get '/gatherer/:gatherer/header/:header' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).headers.first(:name => params[:header]).export.to_json or not_found
end

get '/gatherer/:gatherer/cookie/' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).cookies.identify_all.to_json
end

get '/gatherer/:gatherer/cookie/:cookie' do
  SimpleScraper::Gatherer.first(:name => params[:gatherer]).cookies.first(:name => params[:cookie]).export.to_json or not_found
end

# PUT / POST / DELETE

# Create a new type.
put '/type/:name' do
  @type = SimpleScraper::Type.first_or_new(:name => params[:name])
  @type.save ? true.to_json : {:type => @type.errors.to_a}.to_json
end

# Delete a type.  This can only be done if there are no dependencies.
delete '/type/:name' do
  @type = SimpleScraper::Type.first(:name => params[:name]) or return not_found
  @type.destroy
  @type.destroyed? ? true.to_json : {:type => @type.errors.to_a }.to_json
end

# Add a publish_field to a type.
put '/type/:type/publish/:publish_field' do
  @type = SimpleScraper::Type.first(:name => params[:type]) or return not_found
  @publish_field = SimpleScraper::PublishField.first_or_new(:name => params[:publish_field])
  @type.publish_fields << @publish_field
  @type.save ? true.to_json : {:type => @type.errors.to_a, :publish_field => @publish_field.errors.to_a}.to_json
end

# Delete a publish_field from a type.
delete '/type/:type/publish/:publish_field' do
  @type = SimpleScraper::Type.first(:name => params[:type]) or return not_found
#  @type.publish_fields.delete(@type.publish_fields.first(:name => params[:publish_field]))
#  @type.save ? true.to_json : {:type => @type.errors.to_a}.to_json
  @publish_field = SimpleScraper::PublishField.first_or_new(:name => params[:publish_field])
  @publish_field.destroy
  @publish_field.destroyed? ? true.to_json : {:publish => @publish_field.errors.to_a }.to_json
end

# Create a new SimpleScraper::Area.
put '/area/:area' do
  @area = SimpleScraper::Area.first_or_new(:name => params[:area])
  @area.save ? true.to_json : {:area => @area.errors.to_a }.to_json
end

# Delete an area.
delete '/area/:area' do
  @area = SimpleScraper::Area.first(:name => params[:area]) or return not_found
  @area.destroy
  @area.destroyed? ? true.to_json : {area => @area.errors.to_a }.to_json
end

# Add a DefaultField to an SimpleScraper::Area.
put '/area/:area/default/:name' do
  @area = SimpleScraper::Area.first(:name => params[:area]) or return not_found
  @default_field = DefaultField.first_or_new(:name => params[:name], :value => params[:value])
  @area.default_fields << @default_field
  @area.save ? true.to_json : {:area => @area.errors.to_a, :default_field => @default_field.errors.to_a }.to_json
end

# Delete a DefaultField from an SimpleScraper::Area.
delete '/area/:area/default/:name' do
  @area = SimpleScraper::Area.first(:name => params[:area]) or return not_found
  @default_field = @area.default_fields.first(:name => params[:name]) or return not_found
  @area.default_fields.delete(@default_field)
  @area.save ? true.to_json : {:area => @area.errors.to_a }.to_json
end

# Create a new SimpleScraper::Information.
put '/type/:type/information/:name' do
  @type = SimpleScraper::Type.first(:name => params[:type]) or return not_found

  @information = SimpleScraper::Information.first_or_new(:type => @type, :name => params[:name])

  @information.save ? true.to_json : {:information => @information.errors.to_a, :type => @type.errors.to_a}.to_json
end

# Delete a SimpleScraper::Information.
delete '/type/:type/information/:name' do
  @type = SimpleScraper::Type.first(:name => params[:type]) or return not_found
  @information = SimpleScraper::Information.first(:type => @type, :name => params[:name]) or return not_found
  @information.destroy
  @information.destroyed? ? true.to_json : {:information => @information.errors.to_a, :type => @type.errors.to_a}.to_json
end

# Tag an existing Information with a new SimpleScraper::Area.  Creates area if it doesn't exist yet.
put '/type/:type/information/:name/area/:area' do
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  
  @area = SimpleScraper::Area.first_or_create(:name => params[:area])
  @information.areas << @area

  @information.save ? true.to_json : {:information => @information.errors.to_a, :area => @area.errors.to_a}.to_json
end

# Delete an Area tag from an information.
delete '/type/:type/information/:name/area/:area' do
  @area = SimpleScraper::Area.first(:name => params[:name]) or return not_found
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @information.areas.delete(@area)
  @information.save ? true.to_json : {:information => @information.errors.to_a, :area => @area.errors.to_a}.to_json
end

# Add a gatherer to an SimpleScraper::Information.
put '/type/:type/information/:name/gatherer/:gatherer' do
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found

  @information.gatherers << @gatherer
  @information.save ? true.to_json : {:information => @information.errors.to_a, :gatherer => @gatherer.errors.to_a}.to_json
end

# Delete a Gatherer from an SimpleScraper::Information.  Does not eliminate the Gatherer itself.
delete '/type/:type/information/:name/gatherer/:gatherer' do
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @gatherer = @information.gatherers.first(:name => params[:gatherer]) or return not_found
  @information.gatherers.delete(@gatherer)
  @information.save ? @information.to_json : {:information => @information.errors.to_a }.to_json
end

# Add a ToField to an SimpleScraper::Information.  Regex is inside the post.
put '/type/:type/information/:name/:input_field/:match_number/to/:destination_field' do
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @to_field = SimpleScraper::ToField.first_or_new(:input_field => params[:input_field], :match_number => params[:match_number], :regex => params[:regex], :destination_field => params[:destination_field])
  @information.to_fields << @to_field
  @information.save ? true.to_json : {:information => @information.errors.to_a, :to_field => @to_field.errors.to_a}.to_json
end

# Delete a ToField from an Information
delete '/type/:type/information/:name/:input_field/:match_number/to/:destination_field' do
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @to_field = @information.to_fields.first(:input_field => params[:input_field], :match_number => params[:match_number], :destination_field => params[:destination_field]) or return not_found
  @information.to_fields.delete(@to_field)
  @information.save ? true.to_json : {:information => @information.errors.to_a }.to_json
end

# Add a ToInformation to an SimpleScraper::Information.
put '/type/:type/information/:name/:input_field/to/information/:destination_type/:destination_name/:destination_field' do
  @information = SimpleScraper::Information.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @destination_information = SimpleScraper::Information.first(:type_name => params[:destination_type], :name => params[:destination_name]) or return not_found
  @to_information = SimpleScraper::ToInformation.first_or_new(:input_field => params[:input_field], :regex => params[:regex], :destination_information => @destination_information, :destination_field => params[:destination_field])
  @information.to_informations << @to_information
  @information.save ? true.to_json : {:information => @information.errors.to_a, :destination_information => @destination_information.errors.to_a, :to_information => @to_information.errors.to_a}.to_json
end

# Delete a ToInformation from an SimpleScraper::Information.
delete '/type/:type/information/:name/:input_field/to/information/:destination_type/:destination_name/:destination_field' do
  @information = SimpleScraper::Area.first(:name => params[:area]).informations.first(:type_name => params[:type], :name => params[:name]) or return not_found
  @destination_information = SimpleScraper::Information.first(:type_name => params[:destination_type], :name => params[:destination_name]) or return not_found
  @to_information = @information.first(:input_field => params[:input_field], :regex => params[:regex], :destination_information => @destination_information, :destination_field => params[:destination_field]) or return not_found
  
  @information.to_informations.delete(@to_information)
  @information.save ? true.to_json : {:information => @information.errors.to_a, :destination_information => @destination_information.errors.to_a}.to_json
end


# Create a new SimpleScraper::Gatherer.
put '/gatherer/:name' do
  @gatherer = SimpleScraper::Gatherer.first_or_new(:name => params[:name]) or return not_found
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Delete a SimpleScraper::Gatherer.
delete '/gatherer/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:name]) or return not_found
  @gatherer.destroy
  @gatherer.destroyed? ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a parent to a SimpleScraper::Gatherer.
put '/gatherer/:child/parent/:parent' do
  @child =  SimpleScraper::Gatherer.first(:name => params[:child]) or return not_found
  @parent = SimpleScraper::Gatherer.first(:name => params[:parent]) or return not_found
  @child.parent = @parent
  @child.save ? true.to_json : {:parent => @parent.errors.to_a, :child => @child.errors.to_a }.to_json
end

# Remove a parent relationship from a SimpleScraper::Gatherer. Does not eliminate the parent.
delete '/gatherer/:child/parent' do
  @child =  SimpleScraper::Gatherer.first(:name => params[:child]) or return not_found
  @child.parent.destroy
  @child.save ? true.to_json : {:child => @child.errors.to_a}.to_json
end

# Add a child to a SimpleScraper::Gatherer.
put '/gatherer/:parent/child/:child' do
  @child =  SimpleScraper::Gatherer.first(:name => params[:child]) or return not_found
  @parent = SimpleScraper::Gatherer.first(:name => params[:parent]) or return not_found
  @child.parent = @parent
  @child.save ? true.to_json : {:child => @child.errors.to_a, :parent => @parent.errors.to_a}.to_json
end

# Remove a child relationship from a SimpleScraper::Gatherer. Does not eliminate the child.
# Fails if the child is not a child of the specified parent.
delete '/gatherer/:parent/child/:child' do
  @parent = SimpleScraper::Gatherer.first(:name => params[:parent]) or return not_found
  @parent.children.delete(@parent.children.first(:name => params[:child]))
  @parent.save ? true.to_json : {:parent => @parent.errors.to_a}.to_json
end

# Add a URL to a SimpleScraper::Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/url' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @url = @gatherer.urls.first_or_new(:name => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :url => @url.errors.to_a}.to_json
end

# Delete a URL from a gatherer.
delete '/gatherer/:gatherer/url/:value' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @url = @gatherer.urls.first(:name => params[:value])
  @url.destroy
  @gatherer.destroyed? ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a GET to a SimpleScraper::Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/get/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @get = @gatherer.gets.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :get => @get.errors.to_a}.to_json
end

# Delete a GET from a SimpleScraper::Gatherer.
delete '/gatherer/:gatherer/get/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.gets.first(:name => params[:name]).destroy
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a POST to a SimpleScraper::Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/post/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @post = @gatherer.posts.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :post => @post.errors.to_a}.to_json
end

# Delete a POST from a SimpleScraper::Gatherer.
delete '/gatherer/:gatherer/post/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.posts.first(:name => params[:name]).destroy
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

# Add a Header to a SimpleScraper::Gatherer.  Value is in the post data.
put '/gatherer/:gatherer/header/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @header = @gatherer.headers.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :header => @header.errors.to_a}.to_json
end

# Delete a Header from a SimpleScraper::Gatherer.
delete '/gatherer/:gatherer/header/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.headers.first(:name => params[:name]).destroy
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end 

# Add a Cookie to a SimpleScraper::Gatherer. Value is in the post data.
put '/gatherer/:gatherer/cookie/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @cookie = @gatherer.cookies.first_or_new(:name => params[:name], :value => params[:value])
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a, :cookie =>  @cookie.errors.to_a}.to_json
end

# Delete a Cookie from a SimpleScraper::Gatherer.
delete '/gatherer/:gatherer/cookie/:name' do
  @gatherer = SimpleScraper::Gatherer.first(:name => params[:gatherer]) or return not_found
  @gatherer.cookies.first(:name => params[:name]).destroy
  @gatherer.save ? true.to_json : {:gatherer => @gatherer.errors.to_a}.to_json
end

error do
  puts 'Sinatra Error: ' + env['sinatra.error']
  'Sinatra Error: ' + env['sinatra.error'].name
end

not_found do
  nil.to_json
end
