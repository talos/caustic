# microscraper

cooperative scrapers for mobile apps

#### Usage ####

The easiest way to try out microscraper is the precompiled utility. Run

    $ utility/microscraper '{"load":"http://www.google.com","then":{"find":"Feeling\\s[\\w]*","name":"Feeling?"}}'

in the terminal of your choice.  This executes the JSON instruction

    {
      "load"  : "http://www.google.com",
      "then" : {
        "find" : "Feeling\\s[\\w]*",
        "name" : "Feeling?"
      }
    }

and sends the results to stdout

<table>
  <tr><th>id  <th>source_id <th>name     <th>value
  <tr><td>1   <td>0         <td>Feeling? <td>Feeling Lucky
</table>

First, microscraper loads the URL in *load*.  Then it looks for the regular expression in *find*, and saves the match.

#### The instruction format ####

Microscrapers instructions are logic-free JSON objects that provide very dynamic templated instructions for scraping data.
By default, substitutions are done for text inside double-curlies *{{}}*, kind of like [mustache](http://mustache.github.com/).

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
  <tr><td>1 <td>0        <td>query                                   <td>hello
  <tr><td>2 <td>0        <td>what do you say after 'hello'?          <td>I say 'kitty'!
  <tr><td>3 <td>0        <td>what do you say after 'hello'?          <td>I say 'lyrics'!
  <tr><td>4 <td>0        <td>what do you say after 'hello'?          <td>I say 'lionel'!
  <tr><td>5 <td>0        <td>what do you say after 'hello'?          <td>I say 'kitty'!
  <tr><td>6 <td>0        <td>what do you say after 'hello'?          <td>I say 'beyonce'!
  <tr><td>7 <td>0        <td>what do you say after 'hello'?          <td>I say 'beyonce'!
  <tr><td>8 <td>0        <td>what do you say after 'hello'?          <td>I say 'glee'!   
  <tr><td>9 <td>0        <td>what do you say after 'hello'?          <td>I say 'movie'!  
</table>

Not only is google queried for *hello*, but the substitution affects the *name* and *replace* of *find*.

We can also see that *find* can match multiple times.

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

Note that the *source_id* column links each *find* result back to the result it depends upon.

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

should give you the same results as before.  Any string appearing inside *then* will be evaulated as a reference.

#### Recursion ####

What if you want a scraper to run itself?  No problem!

    {
      "load"  : "http://www.google.com/search?q={{query}}",
      "then" : {
        "find"     : "{{query}}\\s+(\\w+)",
        "replace" : "$1",
        "name"   : "query",
        "then"   : "$this"
      }
    }

When inside *then*, *$this* evaluates to be the entire object.  This evaluation is only performed when *then*
operates.

#### Why? ####

Microscraper is designed to give wider access to obscure public data.  The microscraper format makes it easy to quickly design and test a scraper that extracts a few pieces of information from behind several layers of obfuscation.
