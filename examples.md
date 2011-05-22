# microscraper

cooperative scrapers for mobile apps

#### Examples ####

###### The three types of objects:

*/scraper.json*

    {
     "source"    : { "$ref" : "/page.json" },
     "findMany"  : [{
       "name"   : "what you were querying",
       "parser" : { "$ref" : "/parser.json" }
     }]
    }

*/page.json*

    {
      "url" : "http://www.google.com?q={{query}}"
    }

*/parser.json*

    {
      "pattern"     : "{{query}}",
      "replacement" : "$0"
    }

###### Dynamic substitution [coming soon]

###### Hidden data

*/mockup_property.json*

    {
      "source" : {
	 "url"  : "http://gis.localgovernment.gov/parcel_applet.aspx",
	 "posts": {
	    "streetName": "{{street name}}",
	    "streetNum" : "{{street number}}",
	    "city"      : "{{city}}"
	 },
	 "cookies": {
	    "iClickedThrough":"ofCourse"
	 }
      },
      "findOne": [
	{
	   "name"        : "parcel number",
	   "parser"      : "parcel_num=(\\d+)",
	   "replacement" : "$1"
	}
      ],
      "then" : {
	"url"  : "http://finance.localgovernment.gov/history.aspx",
	"posts": {
	  "parcel" : "{{parcel number}}"
	},
	"findOne": [{
	 "name"       : "owner",
	 "pattern"    : "owner:\\s+(.*?)</td>",
	 "replacement": "$1"
	}]
      }
    }
