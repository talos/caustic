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
    def initialize(options = {})
      @settings = {
        :file => '/db/simplescraper.db',
        :directory => '/'
      }.merge(options)
      
      #DataMapper::Logger.new($stdout, :debug)
      DataMapper.setup(:default, ENV['DATABASE_URL'] || 'sqlite3://' + Dir.pwd + @settings[:file])
      
      # Extend default String length from 50 to 500
      DataMapper::Property::String.length(500)
      #DataMapper::Model.raise_on_save_failure = true
      
      DataMapper.finalize
      
      begin
        DataMapper.auto_upgrade!
      rescue Exception => e
        DataMapper.auto_migrate!
      end
    end

    def directory
      @settings[:directory]
    end
    
    def user_model
      Schema::User
    end

    def get_model (name)
      DataMapper::Model.descendants.find do |model|
        if model.respond_to? :raw_name
          model.raw_name.to_sym == name.to_sym
        end
      end
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
