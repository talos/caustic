module SimpleScraper
  class Application
    module Views
      class Model < Mustache
        MAX_RESOURCES = 100
        
        def name
          @model.raw_name
        end

        def location
          @model.location
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
              :resource => resource
            }
          end
        end
      end
    end
  end
end
