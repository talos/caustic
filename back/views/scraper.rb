
      # # Collect associated areas non-redundantly.
      # area_ids = []
      # def get_area_ids(check_area, area_ids)
      #   area_ids << check_area.attribute_get(:id)
      #   if area_ids.length == area_ids.uniq.length
      #     check_area.follow_areas.each { |assoc_area| get_area_ids(assoc_area, area_ids) }
      #   else
      #     area_ids.uniq!
      #   end
      # end
      # get_area_ids(area, area_ids)

      # info_id = info.attribute_get(:id)
      
      # data_collection = SimpleScraper::Data.all(SimpleScraper::Data.areas.id => area_ids) & \
      # SimpleScraper::Data.all(SimpleScraper::Data.infos.id => info_id)
      # if(params[:creator])
      #   data_collection = data_collection & SimpleScraper::Data.all(:creator_id => params[:creator])
      # end

      # data_ids, gatherers = [], []
      # def get_data_ids (check_datas, data_ids, gatherers)
      #   check_data_ids = check_datas.collect { |check_data| check_data.attribute_get(:id) } - data_ids 
      #   data_ids.push(*check_data_ids)
      #   #check_datas.collect { |check_data| puts check_data.describe.to_json }
      #   #if check_datas.length > 0
        
      #   # Not sure why this is necessary. Something involving loading??
      #   check_datas[0].interpreter_targets #.to_a.to_json
      #   check_datas[0].generator_targets   #.to_a.to_json
      #   #end
      #   interpreters = check_datas.collect { |check_data| check_data.interpreter_targets.all.to_a }.flatten
      #   generators   = check_datas.collect { |check_data| check_data.generator_targets.all.to_a   }.flatten
      #   gatherers.push(*interpreters.collect { |interpreter| interpreter.gatherers.all.to_a }.flatten )
      #   gatherers.push(*generators.collect   { |generator|   generator.gatherers.all.to_a   }.flatten )
      #   gatherers.uniq!
      #   additional_datas = []
      #   additional_datas.push(*interpreters.collect { |interpreter| interpreter.source_datas.all.to_a }.flatten)
      #   additional_datas.push(*generators.collect   { |generator|   generator.source_datas.all.to_a   }.flatten)
        
      #   if additional_datas.length > 0
      #     get_data_ids additional_datas, data_ids, gatherers
      #   end
      # end
      # get_data_ids data_collection, data_ids, gatherers

      # object = {
      #   :publishes    => info.publishes.collect  { |publish| publish.name },
      #   :defaults     => SimpleScraper::Default.all(SimpleScraper::Default.areas.id => area_ids).collect { |default| default.name },
      #   :gatherers    => {},
      #   :interpreters => {},
      #   :generators   => {}
      # }
      
      # gatherers.each do |gatherer|
      #   object[:gatherers][gatherer.full_name] = gatherer.to_scraper
      # end
      
      # SimpleScraper::Data.all(:id => data_ids).each do |data|
      #   data.interpreter_targets.each do |interpreter|
      #     object[:interpreters][interpreter.full_name] = interpreter.to_scraper
      #   end
      # end
      
      # SimpleScraper::Data.all(:id => data_ids).each do |data|
      #   data.generator_targets.each do |generator|
      #     object[:generators][generator.full_name] = generator.to_scraper
      #   end
      # end
      
      # to_output object
