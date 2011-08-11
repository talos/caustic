# microscraper

cooperative scrapers for mobile apps

#### Usage ####

The easiest way to try out microscraper is the precompiled utility. Run

    $ utility/microscraper -e '{"url":"http://www.google.com","finds_one":{"pattern":"[\\w]*\\sLucky"}}' --output-stdout

in the terminal of your choice.  This executes the inline (-e) JSON instruction

    {
      "url" : "http://www.google.com",
      "finds_one" : {
        "pattern" : "[\\w]*\\sLucky"
      }
    }

and pipes the results to stdout (--output-stdout)

<table>
  <tr><th>id  <th>source_id <th>name                  <th>value
  <tr><td>0   <td>          <td>http://www.google.com <td>
  <tr><td>1   <td>0         <td>[\w]*\sLucky          <td>Feeling Lucky
</table>

First, microscraper loaded the *url*.  As this is the first part of the instruction executed, it received the *id* "0".  Loading
the page did not depend on another part of the instruction, so its *source_id* is blank.  No *name* is assigned to this part of
the instruction, so *url* is assigned automatically.  Page values are not saved by default, so its *value* is blank.

Next, microscraper searched for the first instance of *pattern*.  *id* has incremented to "1", and *source_id* tells us that 
*pattern* is matching against the google url.  The *pattern* is assigned as *name*  automatically, and the result of *pattern* 
is stored in *value*.

#### The instruction format ####

Microscrapers instructions are logic-free JSON objects that provide very dynamic templated instructions for scraping data.
Substitutions are done for text inside double-curlies *{{}}*, kind of like [mustache](http://mustache.github.com/).

Here's a simple instruction, which is one of the [fixtures](microscraper-client/blob/master/utility/fixtures/json/simple-google.json):

    {
     "url" : "http://www.google.com/search?q={{query}}",
     "finds_many"  : {
       "name"   : "what do you say after '{{query}}'?",
       "pattern"     : "{{query}}\\s+(\\w+)",
       "replacement" : "I say '$1'!"
     }
    }

For microscraper to execute this instruction, it needs a value to substitute for *{{query}}*.  Run the following

  $ utility/microscraper fixtures/json/simple-google.json --defaults="query=hello" --output-stdout

to replace *{{query}}* with *hello*.  We get the following

<table>
  <tr><th>id<th>source_id<th>name<th>value</tr>
  <tr><td>0 <td>         <td>http://www.google.com/search?q=hello <td>       </tr>
  <tr><td>1 <td>0        <td>what do you say after 'hello'?          <td>I say 'kitty'!
  <tr><td>2 <td>0        <td>what do you say after 'hello'?          <td>I say 'lyrics'!
  <tr><td>3 <td>0        <td>what do you say after 'hello'?          <td>I say 'lionel'!
  <tr><td>4 <td>0        <td>what do you say after 'hello'?          <td>I say 'kitty'!
  <tr><td>5 <td>0        <td>what do you say after 'hello'?          <td>I say 'beyonce'!
  <tr><td>6 <td>0        <td>what do you say after 'hello'?          <td>I say 'beyonce'!
  <tr><td>7 <td>0        <td>what do you say after 'hello'?          <td>I say 'glee'!   
  <tr><td>8 <td>0        <td>what do you say after 'hello'?          <td>I say 'movie'!  
</table>

Not only is google queried for *hello*, but the substitution affects the *name* and *replacement* of *finds_many*.

Here we see that *finds_many* will match against any number of pattern matches from the *url*.

We can use backreferences from *$0* to *$9* in  *replacement*.

Substitutions are a powerful tool because they develop over the course of execution.  Any *name* that appears in 
curlies will be substituted once a *value* has been found for it.



#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.  See [the examples](examples.md).
