/*
 * Turn raw results table from nyc-incentives-simple-extended.json and make it
 * into something usable. 
 */

CREATE UNIQUE INDEX IF NOT EXISTS uri_number ON results (uri, number);
CREATE INDEX IF NOT EXISTS source_uri_source_number ON results (source_uri, source_number);
CREATE INDEX IF NOT EXISTS name ON results (name);

--------
SELECT
  source.number id,
  address.value `Address`,
  base_year.value `Base Year`,
  base_year_assessed_value.value `Base Year Assessed Value`,
  benefit_amount.value `Benefit Amount`,
  benefit_end_date.value `Benefit End Date`,
  benefit_name.value `Benefit Name`,
  benefit_start_date.value `Benefit Start Date`,
  benefit_type.value `Benefit Type`,
  benefit_for_tax_year.value `Benefit for Tax Year`,
  block.value Block,
  borough.value Borough,
  borough_number.value `Borough Number`,
  building_class.value `Building Class`,
  corner.value `Corner`,
  current_benefit_year.value `Current Benefit Year`,
  current_tax_year.value `Current Tax Year`,
  government_financed.value `Government Financed`,
  ineligible_commercial.value `Ineligible Commercial %`,
  ineligible_residential.value `Ineligible Residential %`,
  lot.value `Lot`,
  number_of_benefit_years.value `Number of Benefit Years`,
  number_of_buildings.value `Number of Buildings`,
  number_of_units.value `Number of Units`,
  project_start_date.value `Project Start Date`,
  tax_class.value `Tax Class`,
  tax_rate.value `Tax Rate`,
  year_built.value `Year Built`,
  zip_code.value `Zip Code`
FROM
  results source
  LEFT JOIN (SELECT * FROM results WHERE name = 'Address') address ON
    source.uri = address.source_uri AND source.number = address.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Base Year') base_year ON
    source.uri = base_year.source_uri AND source.number = base_year.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Base Year Assessed Value') base_year_assessed_value ON
    source.uri = base_year_assessed_value.source_uri AND source.number = base_year_assessed_value.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Benefit Amount') benefit_amount ON
    source.uri = benefit_amount.source_uri AND source.number = benefit_amount.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Benefit End Date') benefit_end_date ON
    source.uri = benefit_end_date.source_uri AND source.number = benefit_end_date.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Benefit Name') benefit_name ON
    source.uri = benefit_name.source_uri AND source.number = benefit_name.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Benefit Start Date') benefit_start_date ON
    source.uri = benefit_start_date.source_uri AND source.number = benefit_start_date.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Benefit Type') benefit_type ON
    source.uri = benefit_type.source_uri AND source.number = benefit_type.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Benefit for Tax Year') benefit_for_tax_year ON
    source.uri = benefit_for_tax_year.source_uri AND source.number = benefit_for_tax_year.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Block') block ON
    source.uri = block.source_uri AND source.number = block.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Borough') borough ON
    source.uri = borough.source_uri AND source.number = borough.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Borough Number') borough_number ON
    source.uri = borough_number.source_uri AND source.number = borough_number.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Building Class') building_class ON
    source.uri = building_class.source_uri AND source.number = building_class.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Corner') corner ON
    source.uri = corner.source_uri AND source.number = corner.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Current Benefit Year') current_benefit_year ON
    source.uri = current_benefit_year.source_uri AND source.number = current_benefit_year.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Current Tax Year') current_tax_year ON
    source.uri = current_tax_year.source_uri AND source.number = current_tax_year.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Government Financed') government_financed ON
    source.uri = government_financed.source_uri AND source.number = government_financed.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Ineligible Commercial %') ineligible_commercial ON
    source.uri = ineligible_commercial.source_uri AND source.number = ineligible_commercial.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Ineligible Residential %') ineligible_residential ON
    source.uri = ineligible_residential.source_uri AND source.number = ineligible_residential.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Lot') lot ON
    source.uri = lot.source_uri AND source.number = lot.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Number of Benefit Years') number_of_benefit_years ON
    source.uri = number_of_benefit_years.source_uri AND source.number = number_of_benefit_years.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Number of Buildings') number_of_buildings ON
    source.uri = number_of_buildings.source_uri AND source.number = number_of_buildings.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Number of Units') number_of_units ON
    source.uri = number_of_units.source_uri AND source.number = number_of_units.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Project Start Date') project_start_date ON
    source.uri = project_start_date.source_uri AND source.number = project_start_date.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Tax Class') tax_class ON
    source.uri = tax_class.source_uri AND source.number = tax_class.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Tax Rate') tax_rate ON
    source.uri = tax_rate.source_uri AND source.number = tax_rate.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Year Built') year_built ON
    source.uri = year_built.source_uri AND source.number = year_built.source_number
  LEFT JOIN (SELECT * FROM results WHERE name = 'Zip Code') zip_code ON
    source.uri = zip_code.source_uri AND source.number = zip_code.source_number
  WHERE
    source.uri = 'nyc-incentives-simple-extended.json#/then/0'

