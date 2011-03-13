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
        
        def can_edit
          return unless @user
          @user.can_edit? @resource
        end

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
          @resource.class.many_to_many_relationships.collect do |name, relationship|
            {
              :name => name.to_s,
              :location => @resource.location + '/' + name.to_s + '/',
              :model => @model.related_model(name).raw_name,
              :model_location => @model.related_model(name).location,
              :resources => @resource.send(name).all.collect do |related_resource|
                {
                  :name  => related_resource.full_name,
                  #:absolute_location => related_resource.location,
                  :location => @resource.location + '/' + name.to_s + '/' + related_resource.attribute_get(:id).to_s
                }
              end
            }
          end
        end
      end
    end
  end
end
