require 'json'

module SimpleScraper
  class Application
    module Views
      class Scraper < Mustache
        private
        def get_area_ids(check_area)
          puts check_area.inspect
          @area_ids << check_area.attribute_get(:id)
          if @area_ids.length == @area_ids.uniq.length # We have not added anything redundant.
            check_area.follow_areas.each { |assoc_area| get_area_ids(assoc_area) }
          else
            @area_ids.uniq!
          end
        end
        
        def get_data_ids (check_datas)
          check_data_ids = check_datas.collect { |check_data| check_data.attribute_get(:id) } - @data_ids 
          @data_ids.push(*check_data_ids)
          
          # Not sure why this is necessary. Something involving loading??
          check_datas[0].interpreter_targets
          check_datas[0].generator_targets
          
          interpreters = check_datas.collect { |check_data| check_data.interpreter_targets.all.to_a }.flatten
          generators   = check_datas.collect { |check_data| check_data.generator_targets.all.to_a   }.flatten
          @gatherers.push(*interpreters.collect { |interpreter| interpreter.gatherers.all.to_a }.flatten )
          @gatherers.push(*generators.collect   { |generator|   generator.gatherers.all.to_a   }.flatten )
          @gatherers.uniq!
          additional_datas = []
          additional_datas.push(*interpreters.collect { |interpreter| interpreter.source_datas.all.to_a }.flatten)
          additional_datas.push(*generators.collect   { |generator|   generator.source_datas.all.to_a   }.flatten)
          
          if additional_datas.length > 0
            get_data_ids additional_datas
          end
        end

        #Cross-product area/info/field_names
        def identify_data (data)
          array = []
          data.areas.each do |area|
            data.infos.each do |info|
              data.field_names.each do |field_name|
                array << [area.name, info.name, field_name.name]
              end
            end
          end
          array
        end
        
        public
        def initialize
          @area_ids = []
          get_area_ids(@area)
          @info_id = @info.attribute_get(:id)
          @data_model = @db.get_model(:data)
          
          data_collection = @data_model.all(@data_model.areas.id => @area_ids) & \
          @data_model.all(@data_model.infos.id => @info_id)
          if(@creator)
            data_collection = data_collection & @data_model.all(:creator => @creator)
          end
          
          @data_ids = []
          @gatherers = []
          get_data_ids(data_collection)
        end
      
        def gatherers
          @gatherers.collect do |gatherer|
            {
              gatherer.full_name => {
                :urls => gatherer.urls.collect { |url| url.value },
                :posts => gatherer.posts.collect { |post| { post.post_name => post.value } },
                :headers => gatherer.headers.collect { |header| { header.header_name => header.value } },
                :cookies => gatherer.cookies.collect { |cookie| { cookie.cookie_name => cookie.value } },
                :target_attributes => gatherer.target_datas.collect { |target_data| identify_data(target_data) }
              }
            }
          end
        end
        
        def publishes
          @info.publishes.collect  { |publish| publish.name }
        end

        def defaults
          @db.get_model(:default).all(@db.get_model(:default).areas.id => @area_ids).collect { |default| default.name }
        end

        def interpreters
          # @data_model.all(:id => @data_ids).collect do |data|
          #   data.interpreter_targets.each do |interpreter|
          @data_model.all(:id => @data_ids).interpreter_targets.collect do |interpreter|
            {
              interpreter.full_name => {
                :match_number => interpreter.match_number,
                :terminate_on_complete => interpreter.terminate_on_complete,
                :regexes => interpreter.patterns.collect { |pattern| pattern.regex },
                :source_attributes => interpreter.source_datas.collect { |source_data| identify_data(source_data) },
                :target_attributes => interpreter.target_datas.collect { |target_data| identify_data(target_data) },
                :gatherers => gatherers.collect { |gatherer| gatherer.full_name }
              }
            }
          end
        end

        def generators
          # @data_model.all(:id => @data_ids).collect do |data|
          #   data.generator_targets.each do |generator|
          @data_model.all(:id => @data_ids).generator_targets.collect do |generator|
            {
              generator.full_name => {
                :regexes => generator.patterns.collect { |pattern| pattern.regex },
                :source_attributes => generator.source_datas.collect { |source_data| identify_data(source_data) },
                :target_attributes => generator.target_datas.collect { |target_data| identify_data(target_data) },
                :gatherers => generator.gatherers.collect { |gatherer| gatherer.full_name }
              }
            }
          end
        end
        
        def to_json
          {
            :gatherers => gatherers,
            :interpreters => interpreters,
            :generators => generators,
            :defaults => defaults,
            :publishes => publishes
          }.to_json
        end
      end
    end
  end
end
