#!/usr/bin/ruby

###
#   SimpleScraper Back 0.0.1
#
#   Copyright 2010, AUTHORS.txt
#   Licensed under the MIT license.
#
#   SimpleScraper.rb : Execute this file to start.
###

require 'rubygems'
require 'sinatra/base'
require 'mustache/sinatra'
require 'lib/database'
require 'lib/rpx'

module SimpleScraper
  class Application < Sinatra::Base
    
    db = Database.new
    
    configure do
      register Mustache::Sinatra
      
      set :logging, true
      set :raise_errors, false
      set :show_exceptions, false
      set :sessions, true
      set :database, db
      set :users, db.get_model(:user)
      set :session_id, :user_id
      set :authentication => RPX::Authentication.new(:api_key => '344cef0cc21bc9ff3b406a7b2c2a2dffc79d39dc')
      set :mustache, {
        :views     => 'views/',
        :templates => 'templates/'
      }
    end
    
    error do
      #to_output(env['sinatra.error'] ? env['sinatra.error'] : response)
      mustache :error
    end

    not_found do
      #to_output 'Not found'
      mustache :not_found
    end
    
    get '/' do
      if @user.nil? and @login
        redirect '/login'
      else
        mustache :index
      end
    end
    
    ###### LOGIN
    before do
      @user = options.users.get(session[options.session_id])
    end
    
    get '/login' do
      mustache :login
    end
    
    post '/login' do
      user_params = options.authentication.login(params)
      default_name = user_params[:nickname] + '@' + URI.parse(user_params[:identifier]).host
      user = options.users.first_or_create(:name => default_name)
      
      session[options.session_id] = user.attribute_get(:id)
      puts user.location
      redirect user.location
    end
    
    ###### RESOURCE MODELS
    # Try to find our model.
    before '/:model/*' do
      @model = find_model(params[:model]) or not_found
    end
    
    get '/:model/' do
      mustache :model
    end
    
    ###### RESOURCES
    # Try find our resource.
    before '/:model/:resource_id*' do 
      @resource = @model.get(params[:resource_id]) or return
      # If we have a resource, do a permissions check for PUT, DELETE, and POST.
      if ['PUT', 'DELETE', 'POST'].include? request.request_method
        unless @user.can_edit? @resource
          raise RuntimeError.new((user.nickname ? user.nickname : user.name ) + 
                                 ' lacks permissions to modify ' + resource.model.raw_name + 
                                 ' ' + resource.full_name)
        end
      end
    end
    
    # Describe a resource.
    get '/:model/:resource_id' do
      @resource ? mustache(:resource) : not_found
    end
    
    # Replace a resource.
    put '/:model/:resource_id' do
      if @resource
        @resource.modify params
      elsif @model
        @resource = @model.create(params.merge({:creator => @user}))
      end
      @resource.location
    end
    
    # Delete a resource and all its links.
    delete '/:model/:resource_id' do
      @resource ? @resource.destroy : not_found
    end
    
    ###### TAG MODELS
    # Redirect to the tag's model.  [this is in bad form. it's gonna go.]
    # get '/:model/:relationship/' do
    #   related_model = @model ? @model.related_model(params[:relationship]) : not_found
    #   related_model ? redirect(related_model.location + '?' + request.query_string) : not_found
    # end
    
    ####### TAGS
    before '/:model/:resource_id/' do
      not_found unless @resource
    end
    
    # Try find our relationship -- must be a valid one (listed in tag_names)
    before '/:model/:resource_id/:relationship/*' do
      @relationship_name = params[:relationship].to_sym
      not_found unless @model.tag_names.include? @relationship_name
      @relationship = @resource.send(relationship_name)
      @related_model = @model.related_model(params[:relationship])
    end
    
    # Create a new tag.  Returns the location of the new tag.  This also creates resources.
    put '/:model/:resource_id/:relationship/' do
      @relationship.first_or_create(:creator => @user, :name => params[:name]).location
    end
    
    # If that worked, try to find our related resource.
    before '/:model/:resource_id/:relationship/:related_id' do
      @related_resource = @relationship.get(params[:related_id])
    end
    
    # Redirect to the location of the actual resource.
    get '/:model/:resource_id/:relationship/:related_id' do
      @related_resource ? redirect(@related_resource.location) : not_found
    end

    # Relate two known resources, possibly creating or replacing the second.
    put '/:model/:resource_id/:relationship/:related_id' do
      if @related_resource.nil?
        @related_resource = @related_model.get(params[:related_id]) or not_found
      end
      @relationship << @related_resource
      @resource.save
      @related_resource.location
    end

    # Delete a tagging.
    delete '/:model/:resource_id/:relationship/:related_id' do
      @related_resource ? @resource.untag(@relationship_name, @related_resource) : not_found
    end
    
    # Collect scrapers: this pulls any interpreters, gatherers, and generators that eventually link to a piece of
    # data that would be published for an information in an area.
    # TODO this is a view, and should be handled as such.
    get '/scrapers/:area/:info' do
      creator = find_model('user').first(:id => params[:creator]) #or return not_found # Creator is optional.
      area = find_model('area').first(:name => params[:area]) or return not_found
      info = find_model('info').first(:name => params[:info]) or return not_found

      mustache :scraper, :creator => creator, :area => area, :info => info
    end
  end
end
