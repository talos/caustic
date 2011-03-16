module SimpleScraper
  class Application
    module Views
      class Layout < Mustache
        def logout_location
          @options.logout_location
        end

        def login_location
          @options.login_location
        end
        
        def user_name
          return unless @user
          @user.full_name
        end

        def user_location
          '/'
        end
      end
    end
  end
end
