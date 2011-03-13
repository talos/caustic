module SimpleScraper
  class Application
    module Views
      class Home < Layout
        # User can always edit their own home.
        def can_edit
          true
        end
        
        def user_name
          @user.name
        end
        
        def resources
          @user.class.relationships.collect do |name, relationship|
            {
              :name => name.to_s,
              :location => @user.location + '/' + name.to_s + '/',
              :model => relationship.target_model.raw_name,
              :model_location => relationship.target_model.location,
              :resources => @user.send(name).all.collect do |resource|
                {
                  :name => resource.full_name,
                  :location => resource.location
                }
              end
            }
          end
        end
      end
    end
  end
end
