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
  <tr><th>executable_id<th>id<th>name              <th>value</tr>
  <tr><td>0            <td>1 <td>simple-google.json<td><i>...Google HTML...</i></tr>
  <tr><td>1            <td>2 <td>what do we say after hello? <td>project</tr>
  <tr><td>1            <td>3 <td>what do we say after hello? <td>kitty</tr>
  <tr><td>1            <td>4 <td>what do we say after hello? <td>lyrics</tr>
  <tr><td>1            <td>5 <td>what do we say after hello? <td>lionel</tr>
  <tr><td>1            <td>6 <td>what do we say after hello? <td>kitty</tr>
  <tr><td>1            <td>7 <td>what do we say after hello? <td>beyonce</tr>
  <tr><td>1            <td>8 <td>what do we say after hello? <td>beyonce</tr>
  <tr><td>1            <td>9 <td>what do we say after hello? <td>glee</tr>
</table>

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  See [the examples](examples.md).
