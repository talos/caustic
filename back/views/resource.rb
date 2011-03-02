module SimpleScraper
  module Views
    class Resource < Mustache
      def attributes
        @resource.attributes
      end
      def relationships
        relationships = {}
        @resource.class.tag_names.each do |tag_name|
          desc[tag_name.to_s + '/'] = []
          send(tag_name).all.each do |tag|
            desc[tag_name.to_s + '/'] << {
              :name  => tag.full_name,
              :id    => tag.attribute_get(:id),
              :model => tag.model.raw_name
            }
          end
        end
        relationships
      end
    end
  end
end
