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
              :name => name,
              :collection => @user.send(name)
            }
          end
        end
      end
    end
  end
end
