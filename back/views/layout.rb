module SimpleScraper
  class Application
    module Views
      class Layout < Mustache
        def can_edit
          return unless @user
          @user.can_edit? @resource
        end
        
        def user_name
          return unless @user
          @user.name
        end

        def user_location
          return unless @user
          @user.location
        end
        
        def model_name
          return unless @model
          @model.raw_name
        end
        
        def model_location
          return unless @model
          @model.location
        end

        def resource_name
          return unless @resource
          @resource.name
        end
        
        def resource_location
          return unless @resource
          @resource.location
        end
        
        def resource_last_updated
          return unless @resource
          return unless @resource.has_method? :updated_at
          return @resource.updated_at
        end

        def relationship_name
          @relationship_name
        end

        def related_model_name
          return unless @related_model
          @related_model.name
        end
        
        def related_resource_name
          return unless @related_resource
          @related_resource.name
        end

        def related_resource_location
          return unless @related_resource
          @related_resource.location
        end
      end
    end
  end
end
