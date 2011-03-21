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
require 'lib/scraper'
require 'lib/database'
require 'lib/rpx'

module SimpleScraper
  class Application < Sinatra::Base
    register Mustache::Sinatra
    require 'views/layout'
    
    configure do
      file_path = File.dirname(__FILE__)
      #db = Database.new( :directory => resource_dir )
      db = Database.new( )
      
      set :logging, true
      set :raise_errors, true
      #set :show_exceptions, false
      set :sessions, true
      set :static, true
      set :public, file_path + '/public'
      
      set :database, db
      set :users, db.user_model
      set :index_location, '/'
      set :home_location, '/home'
      set :login_location, '/login'
      set :registration_location, '/login'
      set :logout_location, '/logout'
      js_dir = '/js'
      local_js = [
                 'jquery-1.5.1.min.js',
                 'jquery-ui-1.8.10.custom.min.js',
                 'jquery-form.js',
                 'jquery.cookie.js',
                 'simplescraper.js'
                ]
      set :javascripts, local_js.collect { |file| "#{js_dir}/#{file}" }

      set :default_jquery_theme, 'smoothness'
      
      css_dir = '/css'
      local_css = [
                   'simplescraper.css'
                  ]
      set :css_dir, css_dir
      set :stylesheets, local_css.collect { |file| "#{css_dir}/#{file}" }
      
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
        if request.request_method == 'GET'
          case params[:format]
          when 'html'
            return :html
          when 'json'
            return :json
          end
        end
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
      mustache :error
    end
    
    not_found do
      mustache :not_found
    end
    
    before do
      @user = options.users.get(session[options.session_id])
      @db = options.database
      @options = options
      
      # Recursion only meaningful for GET
      if request.request_method == 'GET'
        @recurse = params[:recurse] ? true : false
      end

      case determine_request_type
      when :html
        theme = params[:theme] ? params[:theme] : options.default_jquery_theme
        options.stylesheets << "#{options.css_dir}/#{theme}/jquery-ui-1.8.10.custom.css"
        @html_format = true
      when :json
        @json_format = true
      end
    end
    
    get options.index_location do
      mustache :index
    end

    # Force the user to log in before going home.
    get options.home_location do
      if @user.nil?
        redirect options.login_location
      else
        mustache :home
      end
    end
    
    # Allow user to change name via home
    put options.home_location do
      if params[:title] and not @user.nil?
        @user.title = params[:title]
        @user.save or resource_error @user
      end
    end
    
    ###### LOGIN
    get options.login_location do
      @login_url = request.url
      mustache :login
    end
    
    post options.login_location do
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
      redirect options.home_location
    end
    
    get options.logout_location do
      session[options.session_id] = nil
      mustache :logout
    end
    
    # Try to find our model.
    before options.database.directory + ':model/*' do
      @model = @db.get_model(params[:model]) or not_found
    end

    get options.database.directory + ':model/' do
      mustache :model #, :layout => :model
    end

    before options.database.directory + ':model/:creator_title/*' do
      @creator  = options.users.first(:title => params[:creator_title]) or not_found
      # @creator.model.relationships.keys.include? params[:model] or not_found
      # @model = @creator.send(params[:model])
    end
    
    # Create a new resource.  Returns the location of the new resource
    put options.database.directory + ':model/:creator_title/' do
      if @creator != @user
        error "You may not create resources for another user."
      end
      @resource = @model.first_or_new(:creator => @user, :title => params[:title])
      @resource.save or resource_error @resource, @related_resource
      mustache :created  # @related_resource.location
    end
    
    ###### RESOURCES
    # Try find our resource.
    before options.database.directory + ':model/:creator_title/:resource_title*' do 
      @resource = @model.first(:title => CGI.unescape(params[:resource_title])) or return
      @can_edit = @user.nil? ? false : @user.can_edit?(@resource)
      # If we have a resource, do a permissions check for PUT, DELETE, and POST.
      if ['PUT', 'DELETE', 'POST'].include? request.request_method and !@can_edit
        error "#{@user.full_name} lacks permissions to modify #{@resource.model.raw_name} #{resource.full_name}"
      end
    end
    
    # Describe a resource.
    get options.database.directory + ':model/:creator_title/:resource_title' do
      not_found unless @resource
      # Tests will be stored with the resource and displayed.
      if params[:test]
        @resource.test params
      end
      mustache :resource
    end
    
    # Replace a resource.
    put options.database.directory + ':model/:creator_title/:resource_title' do
      if @resource
        @resource.modify params, params[:last_updated_at], @user
        @resource.save or resource_error @resource
      elsif @model
        @resource = @model.create(params.merge({:creator => @user}))
      end
      mustache :created # @resource_dir + @resource.location
    end
    
    # Delete a resource and all its links.
    delete options.database.directory + ':model/:creator_title/:resource_title' do
      not_found unless @resource
      @resource.destroy ? @resource.destroy : not_found
      mustache :destroyed
    end
    
    ####### LINKS
    before options.database.directory + ':model/:creator_title/:resource_title/*' do
      not_found unless @resource
    end
    
    # Link relationship must be many-to-many
    before options.database.directory + ':model/:creator_title/:resource_title/:relationship/*' do
      @relationship_name = params[:relationship].to_sym
      not_found unless @model.many_to_many_relationships.find { |name, relationship| name == @relationship_name.to_s }
      @relationship = @resource.send(@relationship_name)
      @related_model = @model.related_model(@relationship_name)
    end
    
    # Create a new link.  Returns the location of the new link.  This also creates resources.
    put options.database.directory + ':model/:creator_title/:resource_title/:relationship/' do
      # Split related resource into creator/resource components
      if @related_model.raw_name == 'user'
        @related_resource = @related_model.first(:title => params[:title]) or not_found
      else
        split_title = params[:title].split '/'
        if split_title.length == 1
          @related_resource = @related_model.first_or_new(:creator => @user, :title => params[:title])
        elsif split_title.length == 2
          creator = @db.get_model(:user).first(:title => split_title[0]) or not_found
          @related_resource = @related_model.first_or_new(:creator => creator, :title => split_title[1])
        else
          error 'You may only use one slash, to separate the creator from the title of the resource.'
        end
      end

      # @related_resource.save or resource_error @related_resource
      @relationship << @related_resource
      @resource.save or resource_error @resource, @related_resource
      mustache :created  # @related_resource.location
    end
    
    # If that worked, try to find our related resource.
    before options.database.directory + ':model/:creator_title/:resource_title/:relationship/:related_id' do
      @related_resource = @relationship.get(params[:related_id])
    end
    
    # Redirect to the location of the actual resource.
    get options.database.directory + ':model/:creator_title/:resource_title/:relationship/:related_id' do
      @related_resource ? redirect(@related_resource.location) : not_found
    end
    
    # Relate two known resources, possibly creating or replacing the second.
    put options.database.directory + ':model/:creator_title/:resource_title/:relationship/:related_id' do
      if @related_resource.nil?
        @related_resource = @related_model.get(params[:related_id]) or not_found
      end
      @relationship << @related_resource
      @resource.save or resource_error @resource, @related_resource
      mustache :created # @related_resource.location
    end

    # Delete a link.
    delete options.database.directory + ':model/:creator_title/:resource_title/:relationship/:related_id' do
      not_found unless @related_resource
      @resource.unlink(@relationship_name, @related_resource)
      mustache :unlinked
    end
  end
end
