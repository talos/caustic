# microscraper

cooperative scrapers for mobile apps

## The format

Microscrapers are ultra lightweight, logic-free JSON objects that provide a Mustache-like template insructions for scraping data.

Here's a real simple one:
   {
     "source"    : { "url" : "http://www.google.com?q={{query}}" },
     "findMany"  : [{
       "name"   : "what do we say after {{query}}?",
       "pattern"     : "{{query}}\\s+(\\w+)",
       "replacement" : "$1"
     }]
   }

If we map <i>query</i> were to "hello", the results look something like...
   what do we say after hello? | Kitty
   what do we say after hello? | to
   what do we say after hello? | World
...and so on.

## FAQ


## Examples


The pattern, too, accepts dynamic substitutions.

There are three types of objects:
## /scraper.json ##
{
  "source"    : { "$ref" : "/page.json" },
  "findMany"  : [{
     "name"   : "what you were querying",
     "parser" : { "$ref" : "/parser.json" }
  }]
}

## /page.json ##
{
  "url" : "http://www.google.com?q={{query}}"
}

## /parser.json ##
{
  "pattern"     : "{{query}}",
  "replacement" : "$0"
}


As the microscraper executes, the variables are populated with the scraped data.  If part of the microscraper insructions can't be used early on, they will be tried again once more variables have been populated from elsewhere in the scraper.  To wit:
## /c_c_c_changes.json ##
{
  "source"   : { "url" : "http://www.google.com?q=bowie" },
  "findOne"  : [{
     "name" : "
}

---

What for?

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  To wit:

## Examples
**/mockup_property.json**
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

Let's say we substitute "742" for <i>street number</i>, "Evergreen" for <i>street name</i>, and 
could produce
0 | 1 | evergreen