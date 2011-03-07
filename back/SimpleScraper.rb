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
require 'json'
require 'lib/database'
require 'lib/rpx'
#require 'CGI'

#require 'rack-flash'
#use Rack::Flash

module SimpleScraper
  class Application < Sinatra::Base
    register Mustache::Sinatra
    require 'views/layout'
    
    configure do
      #use Rack::Flash
      
      resource_dir = '/editor/'
      file_path = File.dirname(__FILE__)
      db = Database.new( :directory => resource_dir )
      
      set :logging, true
      set :raise_errors, true
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
    end

    helpers do
      def resource_error (*resources)
        @resource_errors = resources.collect { |resource| resource.errors.to_a }.flatten
        puts @resource_errors.inspect
        halt 500
      end

      # Attempt to use request headers to determine the format of response.
      # Most likely, the browser will permit any response -- in that case, XHR 
      # requests receive JSON while non XHR requests receive HTML.
      def determine_request_type
        json_ok = request.accept.include?('*/*') or request.accept.find { |header| header =~ /json/ }
        html_ok = request.accept.include?('*/*') or request.accept.find { |header| header =~ /html/ }
        if json_ok and not html_ok
          :json
        elsif html_ok and not json_ok
          :html
        elsif request.xhr?
          :json
        else
          :html
        end
      end

      # If the client is requesting json, use the json utility on the mustache template.
      # Otherwise, run the mustache template.
      def mustache_response (template, options = {}, locals = {})
        
        case determine_request_type 
        when :html then
          mustache template, options, locals
        when :json then
          klass = mustache_class template.to_s, options
          instance = klass.new
          hash = instance.class.public_instance_methods(false).inject({}) do |result, method|
            result[method.to_sym] = instance.send(method)
            result
          end
          # puts template.to_s + ': ' + hash.to_json
          # puts instance.methods.inspect
          # puts instance.class.methods.inspect
          # puts klass.methods.inspect
          # puts instance.class.public_instance_methods(false).inspect
          # puts klass.public_instance_methods(false).inspect
          hash.to_json
        end
      end
    end
    
    error do
      #to_output(env['sinatra.error'] ? env['sinatra.error'] : response)
      @error = env['sinatra.error'] ? env['sinatra.error'] : response
      if @resource
        puts @resource.errors.to_a.inspect
      end
      if @related_resource
        puts @related_resource.errors.to_a.inspect
      end
      mustache_response :error
    end
    
    not_found do
      mustache_response :not_found
    end
    
    before do
      @path = request.path
      @user = options.users.get(session[options.session_id])
      @db = options.database
    end

    get '/' do
      if @user.nil?
        redirect '/login'
      else
        redirect @user.location
      end
    end
    
    ###### LOGIN
    get '/login' do
      mustache_response :login, :layout => false
    end
    
    post '/login' do
      begin
        user_params = options.authentication.login(params)
        immutable_name = user_params[:nickname] + '@' + URI.parse(user_params[:identifier]).host
        user = options.users.first(:immutable_name => immutable_name)
        if(!user)
          user = options.users.new
          user.send(:immutable_name=, immutable_name) # Bypass private method
          user.save or resource_error user
        end
        session[options.session_id] = user.attribute_get(:id)
      rescue
        if session[options.session_id].nil?
          error 'Error logging in.  Try again later.'
        else
          user = @db.get_model(:user).get(session[options.session_id])
        end
      end
      redirect user.location
    end

    ###### LOGOUT
    get '/logout' do
      session[options.session_id] = nil
      mustache_response :logout
    end
    
    ###### RESOURCE MODELS
    # Try to find our model.
    before options.database.directory + ':model/*' do
      @model = @db.get_model(params[:model]) or not_found
    end
    
    get options.database.directory + ':model/' do
      mustache_response :model #, :layout => :model
    end
    
    ###### RESOURCES
    # Try find our resource.
    before options.database.directory + ':model/:resource_id*' do 
      @resource = @model.get(params[:resource_id]) or return
      @can_edit = @user.can_edit? @resource
      # If we have a resource, do a permissions check for PUT, DELETE, and POST.
      if ['PUT', 'DELETE', 'POST'].include? request.request_method and !@can_edit
        error "#{@user.name} lacks permissions to modify #{@resource.model.raw_name} #{resource.full_name}"
      end
    end
    
    # Describe a resource.
    get options.database.directory + ':model/:resource_id' do
      not_found unless @resource
      mustache_response :resource
    end
    
    # Replace a resource.
    put options.database.directory + ':model/:resource_id' do
      if @resource
        @resource.modify params, params[:last_updated_at], @user
        @resource.save or resource_error @resource
      elsif @model
        @resource = @model.create(params.merge({:creator => @user}))
      end
      mustache_response :created # @resource_dir + @resource.location
    end
    
    # Delete a resource and all its links.
    delete options.database.directory + ':model/:resource_id' do
      not_found unless @resource
      @resource.destroy ? @resource.destroy : not_found
      mustache_response :destroyed
    end
    
    ###### TAG MODELS
    # Redirect to the tag's model.  [this is in bad form. it's gonna go.]
    # get options.database.directory + ':model/:relationship/' do
    #   related_model = @model ? @model.related_model(params[:relationship]) : not_found
    #   related_model ? redirect(related_model.location + '?' + request.query_string) : not_found
    # end
    
    ####### TAGS
    before options.database.directory + ':model/:resource_id/*' do
      not_found unless @resource
    end
    
    # Try find our relationship -- must be a valid one (listed in tag_names)
    before options.database.directory + ':model/:resource_id/:relationship/*' do
      @relationship_name = params[:relationship].to_sym
      not_found unless @model.tag_names.include? @relationship_name
      @relationship = @resource.send(@relationship_name)
      @related_model = @model.related_model(@relationship_name)
    end
    
    # Create a new tag.  Returns the location of the new tag.  This also creates resources.
    put options.database.directory + ':model/:resource_id/:relationship/' do
      # Split related resource into creator/resource components.
      if @related_model.relationships.include? :creator
        split_name = params[:name].split '/'
        if split_name.length == 1
          @related_resource = @related_model.first_or_new(:creator => @user, :name => params[:name])
        elsif split_name.length == 2
          creator = @db.get_model(:user).first(:name => split_name[0]) or not_found
          @related_resource = @related_model.first_or_new(:creator => creator, :name => split_name[1])
        else
          error 'You may only use one slash, to separate the creator from the name of the resource.'
        end
      else # can't create a new resource without a creator, only do 'first'.
        puts params[:name]
        @related_resource = @related_model.first(:name => params[:name]) or not_found
        puts @related_resource.inspect
        puts 'relationship: ' + @relationship.inspect
        puts 'relationship: ' + @relationship.class.inspect
        puts 'relationship: ' + @relationship_name.to_s
      end

      # @related_resource.save or resource_error @related_resource
      @relationship << @related_resource
      @resource.save or resource_error @resource, @related_resource
      mustache_response :created  # @related_resource.location
    end
    
    # If that worked, try to find our related resource.
    before options.database.directory + ':model/:resource_id/:relationship/:related_id' do
      @related_resource = @relationship.get(params[:related_id])
    end
    
    # Redirect to the location of the actual resource.
    get options.database.directory + ':model/:resource_id/:relationship/:related_id' do
      @related_resource ? redirect(@related_resource.location) : not_found
    end
    
    # Relate two known resources, possibly creating or replacing the second.
    put options.database.directory + ':model/:resource_id/:relationship/:related_id' do
      if @related_resource.nil?
        @related_resource = @related_model.get(params[:related_id]) or not_found
      end
      @relationship << @related_resource
      @resource.save or resource_error @resource, @related_resource
      mustache_response :created # @related_resource.location
    end

    # Delete a tagging.
    delete options.database.directory + ':model/:resource_id/:relationship/:related_id' do
      not_found unless @related_resource
      @resource.untag(@relationship_name, @related_resource)
      mustache_response :untagged
    end
    
    # Collect scrapers: this pulls any interpreters, gatherers, and generators that eventually link to a piece of
    # data that would be published for an information in an area.
    # TODO this is a view, and should be handled as such.
    get '/scraper/:area/:info' do
      @creator = @db.get_model(:user).first(:id => params[:creator]) #or return not_found # Creator is optional.
      @area = @db.get_model(:area).first(:name => CGI::unescape(params[:area])) or return not_found
      @info = @db.get_model(:info).first(:name => CGI::unescape(params[:info])) or return not_found
      
      mustache_response :scraper
    end
  end
end
