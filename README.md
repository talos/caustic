# microscraper

cooperative scrapers for mobile apps

#### The format ####

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

1. what do we say after hello? | Kitty
2. what do we say after hello? | to
3. what do we say after hello? | World

...and so on.

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  See [the examples](examples.md).
