#!/usr/bin/ruby

###
#   SimpleScraper Back 0.0.1
#
#   Copyright 2010, AUTHORS.txt
#   Licensed under the MIT license.
#
#   test/test.rb : Tests.
###

$:<< Dir.pwd + '/..'

require 'SimpleScraper'
#require 'spec'
require 'test/unit'
require 'rack/test'
require 'json'
require 'cgi'

#ENV['RACK_ENV'] = 'test'

set :environment, :test
set :sessions, true

# @user = Factory(:user)
# User.expects(:authenticate).with(any_parameters).returns(@user)

module SimpleScraper::Test
  NUM_TESTS = 1
  ID_LENGTH = 10
  MAX_LIST = 100
  def SimpleScraper::random_string(length = ID_LENGTH)
    rand(32**length).to_s(32)
  end

  class Resource
    def initialize(location, obj)
      @location = location
      @tags = {}
      @attributes = {}
      obj.each do |name, value|
        if value.class == Array and name.end_with? '/'
          @tags[name] = value
        elsif (value.class != Array and value.class != Hash) and not name.end_with? '/'
          @attributes[name] = value
        else
          raise ArgumentError.new('Attribute "' + name + '": "' + value.to_s + '" (' + value.class.to_s + ') is neither tag nor attribute.')
        end
      end
    end
    def tags
      @tags
    end
    def attributes
      @attributes
    end
    def location
      @location
    end
    def model
      @location.split('/')[1]
    end
    def id
      @location.split('/').last
    end
  end
  
  class Test < Test::Unit::TestCase
    include Rack::Test::Methods
    
    def app
      Sinatra::Application
    end

    def parse response
      JSON.parse(response)
    end
    
    def each_resource resource_name
      get '/' + resource_name + '/'
      assert last_response.ok?
      
      locations = parse(last_response.body)
      locations.each do |location|
        get location
        assert last_response.ok?
        yield Resource.new(location, parse(last_response.body))
      end
    end
    
    def test_001_signs_up
      NUM_TESTS.times do
        post '/signup', {:name => SimpleScraper::random_string}
        assert last_response.ok?, last_response.body
      end
    end

    def test_002_lists_user_locations
      get '/user/'
      assert last_response.ok?, last_response.body
      
      user_locations = parse(last_response.body)
      assert_equal Array, user_locations.class
      assert_equal NUM_TESTS, user_locations.size
    end

    # TODO: test logins
    def test_003_logs_in

    end

    def test_004_creates_resources
      each_resource 'user' do |user|
        user.tags.each do |tag_name, tag_locations|
          NUM_TESTS.times do 
            tag_id = SimpleScraper::random_string
            url = user.location + '/' + tag_name
            put url
            assert last_response.ok?, url + ': ' + last_response.body
          end
        end
      end
    end

    def test_005_tags_resources
      each_resource 'user' do |user|
        user.tags.each do |tag_name, tag_locations|
          tag_locations.each do |tag_location|
            get tag_location
            assert last_response.ok?, tag_location + ': ' + last_response.body
            resource = Resource.new(tag_location, parse(last_response.body))
            resource.tags.each do |tag_name, tag_locations|
              NUM_TESTS.times do
                url = '/' + resource.model + '/' + tag_name # get possible tags
                get url
                tag_model = last_response.location.split('/').last
                follow_redirect!
                assert last_response.ok?, url + ': ' + last_response.body
                possible_tags = parse(last_response.body)
                possible_tags.each do |possible_tag_location|
                  possible_tag_id = possible_tag_location.split('/').last
                  url = resource.location + '/' + tag_name + possible_tag_id 
                  put url
                  if(resource.model == tag_model and possible_tag_id == resource.id)
                    assert_equal 500, last_response.status, 'Allowed recursive self-tagging.'
                  else
                    assert last_response.ok?, url + ': ' + last_response.body
                  end
                end
              end
            end
          end
        end
      end
    end
    
    def test_006_lists_resources
      each_resource('user') do |user|
        user.tags.each do |tag_name, tag_locations|
          url = user.location + '/' + tag_name
          (MAX_LIST * 2).times do 
            put url
            assert last_response.ok?, url + ': ' + last_response.body
          end
          url = '/' + user.model + '/' + tag_name
          get url
          follow_redirect!
          assert last_response.ok?, url + ': ' + last_response.body
          assert_equal MAX_LIST, parse(last_response.body).length
          break
        end
        break
      end
    end
  end
end

