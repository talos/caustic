#!/usr/bin/ruby

##
#  SimpleScraper Back 0.0.1
#  Copyright 2010, AUTHORS.txt
#  Licensed under the MIT license.
#
#  login.rb : Login module for Sinatra.
##

require 'rubygems'
require 'net/http'
require 'mustache'
require 'lib/rpx'

module SimpleScraper
  module Controller
    module Login
      # Find our user.
      before do
        @user = find_model(:user).get(session[:user_id])
      end
      
      get '/login' do
        mustache :login
      end
      
      # Login!
      post '/login' do
        if params[:token]
          user_params = SimpleScraper::RPX.user_info_from_token params[:token]
          default_name = user_params[:nickname] + '@' + URI.parse(user_params[:identifier]).host
          
          user = find_model(:user).first_or_create(:name => default_name)
          raise SimpleScraper::ResourceError.new(user) unless user.saved?
          
          session[:user_id] = user.attribute_get(:id)
          redirect '/#' + user.location
        else
          error "No RPX login token."
        end
      end
    end
  end
end

