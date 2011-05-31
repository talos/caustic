# microscraper

cooperative scrapers for mobile apps

#### The format ####

Microscrapers are ultra lightweight, logic-free JSON objects that provide templated instructions for scraping data.

Here's a simple one:

    {
     "url" : "http://www.google.com/search?q={{query}}",
     "finds_many"  : [{
       "name"   : "what do we say after {{query}}?",
       "pattern"     : "{{query}}\\s+(\\w+)",
       "replacement" : "$1"
     }]
    }

Mapping <i>query</i> to "hello", the results look something like...

<table>
  <tr><th>name                        <th>value                   <th>uri                            <th>number  <th>source_uri         <th>source_number</tr>
  <tr><td><i>null</i>                 <td><i>...Google HTML...</i><td>simple-google.json#            <td>0       <td><i>null</i>        <td><i>null</i>  </tr>
  <tr><td>what do we say after hello? <td>project                 <td>simple-google.json#finds_many.0<td>0       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>kitty                   <td>simple-google.json#finds_many.0<td>1       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>lyrics                  <td>simple-google.json#finds_many.0<td>2       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>lionel                  <td>simple-google.json#finds_many.0<td>3       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>kitty                   <td>simple-google.json#finds_many.0<td>4       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>beyonce                 <td>simple-google.json#finds_many.0<td>5       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>beyonce                 <td>simple-google.json#finds_many.0<td>6       <td>simple-google.json#<td>0            </tr>
  <tr><td>what do we say after hello? <td>glee                    <td>simple-google.json#finds_many.0<td>7       <td>simple-google.json#<td>0            </tr>
</table>

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  See [the examples](examples.md).
