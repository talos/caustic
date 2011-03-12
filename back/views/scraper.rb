require 'json'

module SimpleScraper
  class Application
    module Views
      class Scraper < Layout
        def area_name
          @area.name
        end
        
        def defaults
          @scraper.defaults.to_a.collect do |default|
            default.substitutes_for_interpreters.collect do |interpreter|
              { interpreter.full_name => default.value }
            end
          end.flatten
        end
        
        def gatherers
          @scraper.gatherers.collect do |gatherer|
            {
              gatherer.full_name => {
                :urls => gatherer.urls.collect { |url| url.value },
                :posts => gatherer.posts.collect { |post| { post.post_name => post.value } },
                :headers => gatherer.headers.collect { |header| { header.header_name => header.value } },
                :cookies => gatherer.cookie_headers.collect { |cookie| { cookie.cookie_name => cookie.value } },
                :terminates => gatherer.terminates.collect { |terminate| { terminate.name => terminate.regex } }
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
                :regexes => interpreter.regex,
                :match_number => interpreter.match_number,
                :publish => interpreter.publish,
                :source_interpreters => interpreter.interpreter_sources.collect { |source| source.full_name },
                :gatherers => interpreter.gatherers.collect { |gatherer| gatherer.full_name }
              }
            }
          end
        end
        
        def to_json
          {
            :gatherers => gatherers,
            :interpreters => interpreters,
            :defaults => defaults
          }.to_json
        end
      end
    end
  end
end
