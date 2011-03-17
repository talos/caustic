require 'json'

module SimpleScraper
  class Application
    module Views
      class Model < Layout
        MAX_RESOURCES = 100
        
        def associations
          filters = {}
          @model.properties.each do |property|
            if params.include? property.name.to_s
              filters[property.name.to_sym.like] = params[property.name.to_s]
            end
          end
          filters[:limit] = MAX_RESOURCES
          [{
             :name => @model.raw_name,
             :size => @model.all(filters).length,
             :model_location => @model.location,
             :location => @model.location,
             :collection => @model.all(filters)
           }]
        end
        
        def to_json
          associations.first[:collection].to_a.collect { |resource| { :value => resource.full_name } }.to_json
        end
      end
    end
  end
end
