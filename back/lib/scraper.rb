#######
#
# Scrapers are a bit too complicated to be wrapped up in a view.  This instantiates them.
#
#######

module SimpleScraper
  class Scraper
    def defaults
      @area.defaults
    end
    
    def gatherers
      @interpreters.collect { |interpreter| interpreter.gatherers.to_a }.flatten
    end

    def initialize ( area, info, database, creator=nil )
      @area, @db = area, database
      model = @db.get_model(:interpreter)
      interpreters =
        model.all(model.areas.id => @area.attribute_get(:id), model.infos.id => info.attribute_get(:id))
      if(creator)
        interpreters = interpreters & model.all(:creator => creator)
      end
      
      # Recursively add interpreter sources.
      @interpreters = []
      def check interpreter
        @interpreters << interpreter
        interpreter.interpreter_sources.each do |interpreter_source|
          unless @interpreters.include? interpreter_source
            check interpreter_source
          end
        end
      end
      
      interpreters.to_a.each do |interpreter|
        check interpreter
      end

      @interpreters = @interpreters - defaults.substitutes_for_interpreters.to_a
    end

    def interpreters
      @interpreters
    end
  end
end
