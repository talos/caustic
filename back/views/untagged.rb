module SimpleScraper
  class Application
    module Views
      class Untagged < Mustache
        def related_resource
          @related_resource.name
        end

        def resource
          @resource.name
        end
      end
    end
  end
end
