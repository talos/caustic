# microscraper

cooperative scrapers for mobile apps

#### The format ####

Microscrapers are ultra lightweight, logic-free JSON objects that provide very dynamic templated instructions for scraping data.

Here's a [simple one](microscraper-client/utility/fixtures/simple-google.json):

    {
     "url" : "http://www.google.com/search?q={{query}}",
     "finds_many"  : {
       "name"   : "what do we say after {{query}}?",
       "pattern"     : "{{query}}\\s+(\\w+)",
       "replacement" : "$1"
     }
    }

For microscraper to execute this instruction, it needs a value to substitute for <i>{{query}}</i>.  Given "hello", the results look like...

<table>
  <tr><th>id<th>source_id<th>name<th>value</tr>
  <tr><td>0 <td> <td>fixtures/json/simple-google.json#/<td>       </tr>
  <tr><td>1 <td>0<td>what do we say after hello?       <td>kitty  </tr>
  <tr><td>2 <td>0<td>what do we say after hello?       <td>lyrics </tr>
  <tr><td>3 <td>0<td>what do we say after hello?       <td>lionel </tr>
  <tr><td>4 <td>0<td>what do we say after hello?       <td>kitty  </tr>
  <tr><td>5 <td>0<td>what do we say after hello?       <td>beyonce</tr>
  <tr><td>6 <td>0<td>what do we say after hello?       <td>beyonce</tr>
  <tr><td>7 <td>0<td>what do we say after hello?       <td>glee   </tr>
  <tr><td>8 <td>0<td>what do we say after hello?       <td>movie  </tr>
</table>

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  See [the examples](examples.md).
