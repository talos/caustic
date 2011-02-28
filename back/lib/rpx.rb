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

module SimpleScraper
  # Courtesy http://blog.saush.com/2009/04/02/write-a-sinatra-based-twitter-clone-in-200-lines-of-ruby-code/
  module RPX
    def self.user_info_from_token(token)
      u = URI.parse('https://rpxnow.com/api/v2/auth_info')
      # This needs to be located elsewhere.
      apiKey = '344cef0cc21bc9ff3b406a7b2c2a2dffc79d39dc'
      req = Net::HTTP::Post.new(u.path)
      req.set_form_data({'token' => token, 'apiKey' => apiKey, 'format' => 'json', 'extended' => 'true'})
      http = Net::HTTP.new(u.host,u.port)
      http.use_ssl = true if u.scheme == 'https'
      json = JSON.parse(http.request(req).body)
      
      if json['stat'] == 'ok'
        identifier = json['profile']['identifier']
        nickname = json['profile']['preferredUsername']
        nickname = json['profile']['displayName'] if nickname.nil?
        email = json['profile']['email']
        # Synthesize a unique name.
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
