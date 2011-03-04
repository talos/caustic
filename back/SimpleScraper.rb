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
    register Mustache::Sinatra
    #require 'views/layout'
    
    configure do
      file_path = File.dirname(__FILE__)
      db = Database.new
      
      set :logging, true
      #set :raise_errors, false
      #set :show_exceptions, false
      set :sessions, true
      set :static, true
      set :public, file_path + '/public/'

      set :database, db
      set :users, db.get_model(:user)
      set :session_id, :user_id
      set :authentication => RPX::Authentication.new(:api_key => '344cef0cc21bc9ff3b406a7b2c2a2dffc79d39dc')
      set :mustache, {
        :views     => file_path + '/views/',
        :templates => file_path + '/templates/'
      }
      set :resource_dir, '/'
    end
    
    error do
      puts 'error'
      #to_output(env['sinatra.error'] ? env['sinatra.error'] : response)
      @error = env['sinatra.error'] ? env['sinatra.error'] : response
      puts @resource.saved?
      puts @related_resource.saved?
      if @resource
        puts @resource.errors.to_a.inspect
      end
      if @related_resource
        puts @related_resource.errors.to_a.inspect
      end
      mustache :error, :layout => false
    end

    not_found do
      mustache :not_found, :layout => false
      content_type 'text/html'
      '<p>This page has not been found.</p>' + ( ' ' * 512 )
    end
    
    before do
      @path = request.path
      @user = options.users.get(session[options.session_id])
      @resource_dir = options.resource_dir
    end

    get '/' do
      if @user.nil?
        redirect '/login'
      else
        redirect @resource_dir + @user.location
      end
    end
    
    ###### LOGIN
    get '/login' do
      mustache :login, :layout => false
    end
    
    post '/login' do
      user_params = options.authentication.login(params)
      default_name = user_params[:nickname] + '@' + URI.parse(user_params[:identifier]).host
      user = options.users.first_or_create(:name => default_name)
      
      session[options.session_id] = user.attribute_get(:id)
      redirect @resource_dir + user.location
    end
    
    ###### RESOURCE MODELS
    # Try to find our model.
    before options.resource_dir + ':model/*' do
      @model = options.database.get_model(params[:model]) or not_found
    end
    
    get options.resource_dir + ':model/' do
      mustache :model
    end
    
    ###### RESOURCES
    # Try find our resource.
    before options.resource_dir + ':model/:resource_id*' do 
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
    get options.resource_dir + ':model/:resource_id' do
      @resource ? mustache(:resource) : not_found
    end
    
    # Replace a resource.
    put '/:model/:resource_id' do
      if @resource
        @resource.modify params
      elsif @model
        @resource = @model.create(params.merge({:creator => @user}))
      end
      @resource_dir + @resource.location
    end
    
    # Delete a resource and all its links.
    delete options.resource_dir + ':model/:resource_id' do
      @resource ? @resource.destroy : not_found
    end
    
    ###### TAG MODELS
    # Redirect to the tag's model.  [this is in bad form. it's gonna go.]
    # get options.resource_dir + ':model/:relationship/' do
    #   related_model = @model ? @model.related_model(params[:relationship]) : not_found
    #   related_model ? redirect(related_model.location + '?' + request.query_string) : not_found
    # end
    
    ####### TAGS
    before options.resource_dir + ':model/:resource_id/' do
      not_found unless @resource
    end
    
    # Try find our relationship -- must be a valid one (listed in tag_names)
    before options.resource_dir + ':model/:resource_id/:relationship/*' do
      @relationship_name = params[:relationship].to_sym
      not_found unless @model.tag_names.include? @relationship_name
      @relationship = @resource.send(@relationship_name)
      @related_model = @model.related_model(@relationship_name)
    end
    
    # Create a new tag.  Returns the location of the new tag.  This also creates resources.
    put options.resource_dir + ':model/:resource_id/:relationship/' do
      puts 'name: ' + params[:name]
      @related_resource = @relationship.first_or_new(:creator => @user, :name => params[:name])
      @related_resource.save
      @related_resource.location
    end
    
    # If that worked, try to find our related resource.
    before options.resource_dir + ':model/:resource_id/:relationship/:related_id' do
      @related_resource = @relationship.get(params[:related_id])
    end
    
    # Redirect to the location of the actual resource.
    get options.resource_dir + ':model/:resource_id/:relationship/:related_id' do
      @related_resource ? redirect(@resource_dir + @related_resource.location) : not_found
    end

    # Relate two known resources, possibly creating or replacing the second.
    put options.resource_dir + ':model/:resource_id/:relationship/:related_id' do
      if @related_resource.nil?
        @related_resource = @related_model.get(params[:related_id]) or not_found
      end
      @relationship << @related_resource
      @resource.save
      @resource_dir + @related_resource.location
    end

    # Delete a tagging.
    delete options.resource_dir + ':model/:resource_id/:relationship/:related_id' do
      @related_resource ? @resource.untag(@relationship_name, @related_resource) : not_found
    end
    
    # Collect scrapers: this pulls any interpreters, gatherers, and generators that eventually link to a piece of
    # data that would be published for an information in an area.
    # TODO this is a view, and should be handled as such.
    get options.resource_dir + 'scrapers/:area/:info' do
      creator = find_model('user').first(:id => params[:creator]) #or return not_found # Creator is optional.
      area = find_model('area').first(:name => params[:area]) or return not_found
      info = find_model('info').first(:name => params[:info]) or return not_found

      mustache :scraper, :creator => creator, :area => area, :info => info
    end
  end
end
