#!/usr/bin/ruby

###
#   SimpleScraper Back 0.0.1
#
#   Copyright 2010, AUTHORS.txt
#   Licensed under the MIT license.
#
#   database.rb : The database.  Requires the schema.
###

require 'lib/schema'

module SimpleScraper
  class Database
    def initialize
      DataMapper.setup(:default, ENV['DATABASE_URL'] || 'sqlite3://' + Dir.pwd + '/simplescraper.db')
      
      # Extend default String length from 50 to 500
      DataMapper::Property::String.length(500)
      #DataMapper::Model.raise_on_save_failure = true
      
      DataMapper.finalize
      DataMapper.auto_upgrade!
    end
    
    def get_model (name)
      DataMapper::Model.descendants.find { |model| model.raw_name.to_sym == name.to_sym }
    end
    
    class ResourceError < RuntimeError
      def initialize(*resources)
        @errors = {}
        resources.each do |resource|
          @errors[resource.class.to_s] = resource.errors.to_a
        end
        super @errors.inspect
      end
    end
  end
end
