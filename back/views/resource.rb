module SimpleScraper
  class Application
    module Views
      class Resource < Mustache
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
        def creator
          @resource.methods.include?('creator') ? @resource.creator : nil
        end

        def model
          @resource.model
        end

        def name
          @resource.name
        end
        
        def immutables
          list_attributes( @resource.attributes.keys.select do |attribute_name|
            @resource.private_methods.include?(attribute_name.to_s + '=')
          end)
        end
        
        def location
          # @resource.location
          # @request.path
          @resource_dir + @resource.location
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
              :location => @resource_dir + @resource.location + '/' + relationship_name.to_s + '/',
              :links => @resource.send(relationship_name).all.collect do |related_resource|
                {
                  :name  => related_resource.name,
                  :location => @resource_dir + related_resource.location
                }
              end
            }
          end
        end
      end
    end
  end
end
