# This lets us extract the variables referred to in a Mustache template.

require 'rubygems'
require 'mustache'

class Mustache
  module SimpleScraper
    module Template
      private
      def process_tokens *tokens
        variables = []
        first = tokens.shift
        second = tokens.shift
        if first == :mustache and second == :fetch
          variables.push(tokens.flatten.first)
        else
          tokens.each { |token| variables.push(*process_tokens(*token)) }
        end
        variables.uniq
      end
      
      public
      def extract_variables
        process_tokens(*tokens)
      end
    end
    
    def self.extract_variables (string)
      template = Mustache.templateify(string)
      template.extend(Template)
      template.extract_variables
    end
  end
end
