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

        def home_location
          @options.home_location
        end
      end
    end
  end
end
