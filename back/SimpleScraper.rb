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
require 'mustache_json'
require 'lib/database'
require 'lib/rpx'

require 'rack-flash'


#use Rack::Flash

module SimpleScraper
  class Application < Sinatra::Base
    register Mustache::Sinatra
    #require 'views/layout'
    
    configure do
      use Rack::Flash
      
      file_path = File.dirname(__FILE__)
      db = Database.new
      
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
      set :resource_dir, '/'
    end

    helpers do
      def resource_error (*resources)
        @resource_errors = resources.collect { |resource| resource.errors.to_a }.flatten
        puts @resource_errors
        halt 500
      end

      # Attempt to use request headers to determine the format of response.
      # Most likely, the browser will permit any response -- in that case, XHR 
      # requests receive JSON while non XHR requests receive HTML.
      def determine_request_type
        json_ok = request.accept.include?('*/*') or request.accept.find { |header| header =~ /json/ }
        html_ok = request.accept.include?('*/*') or request.accept.find { |header| header =~ /html/ }
        puts 'XHR: ' + request.xhr?.to_s
        if json_ok and not html_ok
          :json
        elsif html_ok and not json_ok
          :html
        elsif request.xhr?
          :json
        else
          :html
        end
        #puts @request_type.to_s
      end

      # If the client is requesting json, use the json utility on the mustache template.
      # Otherwise, run the mustache template.
      def mustache_response (template, options = {})
        
        case determine_request_type 
        when :html then
          mustache template, options
        when :json then
          #puts instance_variables.inspect
          # klass = mustache_class(template, options)
          # output = klass.new
          # #output.render({})
          # puts output.methods.inspect
          # puts output.context
          # puts output.context.methods.inspect
          # puts output.to_json
          #warn 'mustache class name: ' + mustache_class(template, options).to_json
          #Views
        end
      end
    end
    
    error do
      #to_output(env['sinatra.error'] ? env['sinatra.error'] : response)
      puts 'danger, will robinson!!'
      @error = env['sinatra.error'] ? env['sinatra.error'] : response
      puts @resource.saved?
      puts @related_resource.saved?
      if @resource
        puts @resource.errors.to_a.inspect
      end
      if @related_resource
        puts @related_resource.errors.to_a.inspect
      end
      puts 'flash error: '
      flash[:error] = 'there was an error.'
      puts flash[:error]
      mustache_response :error, :layout => false
    end

    error DataMapper::SaveFailureError do
      puts 'savefailurerror'
    end

    not_found do
      mustache_response :not_found, :layout => false
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
      mustache_response :login, :layout => false
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
      mustache_response :model
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
      not_found unless @resource
      mustache_response :resource
    end
    
    # Replace a resource.
    put '/:model/:resource_id' do
      if @resource
        @resource.modify params
        @resource.save or resource_error @resource
      elsif @model
        @resource = @model.create(params.merge({:creator => @user}))
      end
      mustache_response :created # @resource_dir + @resource.location
    end
    
    # Delete a resource and all its links.
    delete options.resource_dir + ':model/:resource_id' do
      not_found unless @resource
      @resource.destroy ? @resource.destroy : not_found
      mustache_response :destroyed
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
      @related_resource = @relationship.first_or_new(:creator => @user, :name => params[:name])
      @related_resource.save or resource_error @related_resource
      @relationship << @related_resource
      @resource.save or resource_error @resource, @related_resource
      mustache_response :created  # @related_resource.location
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
      @resource.save or resource_error @resource, @related_resource
      mustache_response :created # @resource_dir + @related_resource.location
    end

    # Delete a tagging.
    delete options.resource_dir + ':model/:resource_id/:relationship/:related_id' do
      not_found unless @related_resource
      @resource.untag(@relationship_name, @related_resource)
      mustache_response :untagged
    end
    
    # Collect scrapers: this pulls any interpreters, gatherers, and generators that eventually link to a piece of
    # data that would be published for an information in an area.
    # TODO this is a view, and should be handled as such.
    get options.resource_dir + 'scraper/:area/:info' do
      creator = find_model('user').first(:id => params[:creator]) #or return not_found # Creator is optional.
      area = find_model('area').first(:name => params[:area]) or return not_found
      info = find_model('info').first(:name => params[:info]) or return not_found

      mustache_response :scraper, :creator => creator, :area => area, :info => info
    end
  end
end
