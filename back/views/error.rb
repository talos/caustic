module SimpleScraper
  class Application
    module Views
      class Error < Mustache
        def errors
          env['sinatra.error'].inspect or response
        end
      end
    end
  end
end
