module SimpleScraper
  class Application
    module Views
      class Logout < Mustache
        def user_name
          @user.name
        end
      end
    end
  end
end
