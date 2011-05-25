# microscraper

cooperative scrapers for mobile apps

#### The format ####

Microscrapers are ultra lightweight, logic-free JSON objects that provide templated instructions for scraping data.

Here's a simple one:

    {
     "source" : {
       "url" : "http://www.google.com/search?q={{query}}",
     },
     "finds_many"  : [{
       "name"   : "what do we say after {{query}}?",
       "pattern"     : "{{query}}\\s+(\\w+)",
       "replacement" : "$1"
     }]
    }

Mapping <i>query</i> to "hello", the results look something like...

<table>
  <tr><td>what do we say after hello? <td>Kitty</tr>
  <tr><td>what do we say after hello? <td>to   </tr>
  <tr><td>what do we say after hello? <td>World</tr>
</table>

...and so on.

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  See [the examples](examples.md).
