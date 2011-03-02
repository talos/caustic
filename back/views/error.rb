module SimpleScraper
  class Application
    module Views
      def errors
        env['sinatra.error'].inspect or response
      end
    end
  end
end
