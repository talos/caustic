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
          @resource.methods.include?(:creator) ? @resource.creator.nickname : nil
        end

        def model
          @model.raw_name
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
          @resource_dir + @resource.location
        end
        
        def mutables
          list_attributes( @resource.attributes.keys.select do |attribute_name|
            @resource.public_methods.include?(attribute_name.to_s + '=')
          end)
        end

        def relationships
          #puts 'resource is: ' + @resource.inspect
          @resource.class.tag_names.collect do |relationship_name|
            {
              :name => relationship_name,
              :location => @resource_dir + @resource.location + '/' + relationship_name.to_s + '/',
              :related_model => @model.related_model(relationship_name).raw_name,
              :related_model_location => @resource_dir + @model.related_model(relationship_name).location,
              :links => @resource.send(relationship_name).all.collect do |related_resource|
                {
                  :creator_name => related_resource.creator.nickname,
                  :name  => related_resource.name,
                  :resource_location => @resource_dir + related_resource.location,
                  :tag_location => @resource_dir + @resource.location + '/' + relationship_name.to_s + '/' + related_resource.attribute_get(:id).to_s
                }
              end
            }
          end
        end
      end
    end
  end
end
