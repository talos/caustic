# microscraper

cooperative scrapers for mobile apps

#### Usage ####

The easiest way to try out microscraper is the precompiled utility. Run

    $ utility/microscraper '{"load":"http://www.google.com","then":{"find":"[\\w]*\\sLucky"}}'

in the terminal of your choice.  This executes the JSON instruction

    {
      "load"  : "http://www.google.com",
      "then" : {
        "find" : "[\\w]*\\sLucky"
      }
    }

and sends the results to stdout

<table>
  <tr><th>id  <th>source_id <th>name                  <th>value
  <tr><td>0   <td>          <td>http://www.google.com <td>
  <tr><td>1   <td>0         <td>[\w]*\sLucky          <td>Feeling Lucky
</table>

First, microscraper loads the URL in *load*.  As this is the first part of the instruction executed, it received the *id* "0".  Loading
the page did not depend on another part of the instruction, so its *source_id* is blank.  No *name* is assigned to this part of
the instruction, so url is assigned automatically.  Page values are not saved by default, so its *value* is blank.

Next, microscraper searched for the first instance of *find*.  *id* has incremented to "1", and *source_id* tells us that 
*find* is matching against the google url.  The *find* is assigned as *name*  automatically, and the result of *find* 
is stored in *value*.

#### The instruction format ####

Microscrapers instructions are logic-free JSON objects that provide very dynamic templated instructions for scraping data.
Substitutions are done for text inside double-curlies *{{}}*, kind of like [mustache](http://mustache.github.com/).

Here's a simple instruction, which is one of the [fixtures](microscraper-client/blob/master/fixtures/json/simple-google.json):

    {
     "load" : "http://www.google.com/search?q={{query}}",
     "then"  : {
       "find"    : "{{query}}\\s+(\\w+)",
       "replace" : "I say '$1'!",
       "name"    : "what do you say after '{{query}}'?"
     }
    }

For microscraper to execute this instruction, it needs a value to substitute for *{{query}}*.  Run the following

    $ utility/microscraper fixtures/json/simple-google.json --defaults="query=hello"

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

Not only is google queried for *hello*, but the substitution affects the *name* and *replace* of *find*.

Here we see that *find* will match against any number of pattern matches from *load*.

We can use backreferences from *$0* to *$9* in *replace*.

#### Advanced substitutions ####

Substitutions are a powerful tool because they develop over the course of execution.  Any *name* that appears in 
curlies will be substituted once a *value* has been found for it.

This [fixture](microscraper-client/blob/master/fixtures/json/complex-google.json)

    {
      "load" : "http://www.google.com/search?q={{query}}",
      "then"  : {
        "find"    : "{{query}}\\s+(\\w+)",
        "replace" : "$1",
        "name"    : "after",
        "then" : {
          "load" : "http://www.google.com/search?q={{after}}",
          "then" : {
            "find"    : "{{query}}\\s+(\\w+)",
            "replace" : "I say '$1'!",
            "name"    : "what do you say after '{{after}}'?"
          }
        }
      }
    }

takes advantage of dynamic substitution, along with the ability to place any number of *load* or *find* instructions inside *then*.  It launches a whole new series of queries!

Try it with

    $ utility/microscraper fixtures/json/complex-google.json --defaults="query=hello"

You'll see that this results in quite a few dozen rows, but here are some highlights:

<table>
  <tr><th>id     <th>source_id <th>name                       <th>value
  <tr><td>48     <td>14  <td>what do you say after 'beyonce'? <td>I say 'wedding'!
  <tr><td>49     <td>14  <td>what do you say after 'beyonce'? <td>I say 'songs'!
  <tr><td>50     <td>14  <td>what do you say after 'beyonce'? <td>I say 'youtube'!
  <tr><td>51     <td>14  <td>what do you say after 'beyonce'? <td>I say 'jay'!
  <tr><td>52     <td>14  <td>what do you say after 'beyonce'? <td>I say 'diet'!
  <tr><td>53     <td>14  <td>what do you say after 'beyonce'? <td>I say 'albums'!
  <tr><td>54     <td>14  <td>what do you say after 'beyonce'? <td>I say 'biography'!
  <tr><td>55     <td>14  <td>what do you say after 'beyonce'? <td>I say 'lyrics'!
  <tr><td>56     <td>15  <td>what do you say after 'glee'?    <td>I say 'episodes'!
  <tr><td>57     <td>15  <td>what do you say after 'glee'?    <td>I say 'tv'!
  <tr><td>58     <td>15  <td>what do you say after 'glee'?    <td>I say 'spoilers'!
  <tr><td>59     <td>15  <td>what do you say after 'glee'?    <td>I say 'songs'!
  <tr><td>60     <td>15  <td>what do you say after 'glee'?    <td>I say 'soundtrack'!
  <tr><td>61     <td>15  <td>what do you say after 'glee'?    <td>I say 'cast'!
  <tr><td>62     <td>15  <td>what do you say after 'glee'?    <td>I say 'wiki'!
  <tr><td>63     <td>16  <td>what do you say after 'movie'?   <td>I say 'download'!
</table>

Note that the *source_id* column links each *find* result back to its source *load*.

#### References ####

You probably noticed that interior portion of the last fixture was basically copy-and-pasted from the fixture
before it.  Wouldn't it be nice if we could reuse instruction components?

This [fixture](microscraper-client/blob/master/fixtures/json/reference-google.json) does just that

    {
      "load" : "http://www.google.com/search?q={{query}}",
      "then"  : {
        "find"    : "{{query}}\\s+(\\w+)",
        "replace" : "$1",
        "name"    : "after",
        "then"    : "simple-google.json"
      }
    }

Running

    $ utility/microscraper fixtures/json/complex-google.json --defaults="query=hello"

should give you the same results as before.

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.
