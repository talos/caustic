module SimpleScraper
  class Application
    module Views
      class Model < Mustache
        MAX_RESOURCES = 100
        
        def name
          @model.raw_name
        end

        def location
          @resource_dir + @model.location
        end

        def resources
          filters = {}
          @model.properties.each do |property|
            if params.include? property.name.to_s
              filters[property.name.to_sym.like] = params[property.name.to_s]
            end
          end
          filters[:limit] = MAX_RESOURCES
          @model.all(filters).collect do |resource|
            {
              :name => (resource.methods.include?(:creator) ? resource.creator.nickname + "'s " : '' ) + resource.name,
              :location => @resource_dir + resource.location
            }
          end
        end
      end
    end
  end
end
