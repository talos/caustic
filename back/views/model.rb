require 'json'

module SimpleScraper
  class Application
    module Views
      class Model < Layout
        MAX_RESOURCES = 100
        
        def name
          @model.raw_name
        end
        
        def location
          @model.location
        end
        
        def associations
          filters = {}
          @model.properties.each do |property|
            if params.include? property.name.to_s
              filters[property.name.to_sym.like] = params[property.name.to_s]
            end
          end
          filters[:limit] = MAX_RESOURCES
          @model.all(filters).collect do |resource|
            {
              :name => resource.full_name,
              :location => resource.location
            }
          end
        end
        
        def to_json
          associations.collect { |resource| { :value => resource[:name] } }.to_json
        end
      end
    end
  end
end
