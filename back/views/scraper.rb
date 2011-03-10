require 'json'

module SimpleScraper
  class Application
    module Views
      class Scraper < Layout
        private
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
        def area_name
          @area.name
        end
        
        def defaults
          @scraper.defaults.collect { |default| default.name }
        end
      
        def gatherers
          @scraper.gatherers.collect do |gatherer|
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

        def generators
          @scraper.generators.collect do |generator|
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
        
        def info_name
          @info.name
        end

        def info_name_starts_with_vowel
          ['a','e','i','o','u'].include? @info.name.slice(0,1).downcase
        end

        def interpreters
          @scraper.interpreters.collect do |interpreter|
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

        def publishes
          @scraper.publishes.collect  { |publish| publish.name }
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
