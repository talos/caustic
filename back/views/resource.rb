module SimpleScraper
  class Application
    module Views
      class Resource < Layout
        private
        def list_attributes (attributes_names)
          attributes_names.collect do |attribute_name|
            {
              :name => attribute_name,
              :value => @resource.attribute_get(attribute_name)
            }
          end
        end
        
        public
        
        def immutables
          list_attributes( @resource.attributes.keys.select do |attribute_name|
            @resource.private_methods.include?(attribute_name.to_s + '=')
          end)
        end
        
        def mutables
          list_attributes( @resource.attributes.keys.select do |attribute_name|
            @resource.public_methods.include?(attribute_name.to_s + '=')
          end)
        end

        def relationships
          @resource.class.tag_names.collect do |relationship_name|
            {
              :name => relationship_name,
              :location => @resource.location + '/' + relationship_name.to_s + '/',
              :related_model => @model.related_model(relationship_name).raw_name,
              :related_model_location => @model.related_model(relationship_name).location,
              :links => @resource.send(relationship_name).all.collect do |related_resource|
                {
                  :name  => related_resource.full_name,
                  :resource_location => related_resource.location,
                  :tag_location => @resource.location + '/' + relationship_name.to_s + '/' + related_resource.attribute_get(:id).to_s
                }
              end
            }
          end
        end
      end
    end
  end
end
