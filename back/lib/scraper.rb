#######
#
# Scrapers are a bit too complicated to be wrapped up in a view.  This instantiates them.
#
#######

module SimpleScraper
  class Scraper
    private
    def get_area_ids(check_area)
      @area_ids << check_area.attribute_get(:id)
      if @area_ids.length == @area_ids.uniq.length # We have not added anything redundant.
        check_area.follow_areas.each { |assoc_area| get_area_ids(assoc_area) }
      else
        @area_ids.uniq!
      end
    end
    
    def get_data_ids (check_datas)
      return unless check_datas.length > 0
      
      check_data_ids = check_datas.collect { |check_data| check_data.attribute_get(:id) } - @data_ids 
      @data_ids.push(*check_data_ids)
      
      # Collecting through a :through association in DataMapper is bugged.
      # See http://datamapper.lighthouseapp.com/projects/20609-datamapper/tickets/1431 .
      # To get around this, convert collection to array before iterating over it.
      check_datas_ary = check_datas.to_a
      
      interpreters = check_datas_ary.collect { |check_data| check_data.interpreter_sources.all.to_a }.flatten
      generators   = check_datas_ary.collect { |check_data| check_data.generator_sources.all.to_a   }.flatten
      gatherers    = check_datas_ary.collect { |check_data| check_data.gatherer_sources.all.to_a    }.flatten

      # TODO this isn't working right.
      @gatherers.push(*gatherers)
      @gatherers.push(*interpreters.collect { |interpreter| interpreter.gatherers.all.to_a }.flatten )
      @gatherers.push(*generators.collect   { |generator|   generator.gatherers.all.to_a   }.flatten )
      @gatherers.uniq!

      additional_datas = []
      additional_datas.push(*interpreters.collect { |interpreter| interpreter.source_datas.all.to_a }.flatten)
      additional_datas.push(*generators.collect   { |generator|   generator.source_datas.all.to_a   }.flatten)

      get_data_ids additional_datas
    end

    public
    def defaults
      @db.get_model(:default).all(@db.get_model(:default).areas.id => @area_ids)
    end
    
    def gatherers
      @gatherers
    end
    
    def generators
      @data_model.all(:id => @data_ids).generator_sources
    end

    def initialize ( area, info, database, creator=nil )
      @info, @db = info, database
      @area_ids = []
      get_area_ids(area)
      @info_id = @info.attribute_get(:id)
      @data_model = @db.get_model(:data)
    
      data_collection = @data_model.all(@data_model.areas.id => @area_ids) & \
      @data_model.all(@data_model.infos.id => @info_id)
      if(creator)
        data_collection = data_collection & @data_model.all(:creator => creator)
      end
    
      @data_ids = []
      @gatherers = []
      get_data_ids(data_collection)
    end

    def interpreters
      @data_model.all(:id => @data_ids).interpreter_sources
    end

    def publishes
      @info.publishes
    end
  end
end
