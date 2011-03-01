#!/usr/bin/ruby

##
#  SimpleScraper Back 0.0.1
#  Copyright 2010, AUTHORS.txt
#  Licensed under the MIT license.
#
#  rpx.rb : RPX helper.
##

require 'rubygems'
require 'net/http'
require 'net/https'
require 'json'

module RPX
  # Courtesy http://blog.saush.com/2009/04/02/write-a-sinatra-based-twitter-clone-in-200-lines-of-ruby-code/
  class Authentication
    def initialize (options)
      @api_key = options[:api_key] or raise Error.new('RPX needs an api key.')
      @rpx_uri = URI.parse 'https://rpxnow.com/api/v2/auth_info'
    end
    
    def login(params)
      token = params[:token]
      req = Net::HTTP::Post.new(@rpx_uri.path)
      req.set_form_data({:token => token, :apiKey => @api_key, :format => 'json', :extended => 'true'})
      http = Net::HTTP.new(@rpx_uri.host, @rpx_uri.port)
      http.use_ssl = true if @rpx_uri.scheme == 'https'
      json = JSON.parse(http.request(req).body)
      
      if json['stat'] == 'ok'
        identifier = json['profile']['identifier']
        nickname = json['profile']['preferredUsername']
        nickname = json['profile']['displayName'] if nickname.nil?
        email = json['profile']['email']
        {
          :identifier => identifier,
          :nickname   => nickname,
          :email      => email
        }
      else
        raise RuntimeError, 'Cannot log in. Try another account!' 
      end
    end
  end
end
