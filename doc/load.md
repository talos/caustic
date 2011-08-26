# microscraper

cooperative scrapers for mobile apps

## load ##

### the basics ###

---

The *load* object extends [instruction](instruction.md), and permits all of its attributes.  Take a look at the [schema](../schema/json/load) for technical specifics.

*Load* requests a URL and obtains the body of the response.  The simplest valid *load* object looks like this:

    {
      "load" : "http://www.template.com"
    }

This would request "http://www.template.com".  If *load* also has a name, then the response will be saved to the database and used for substitutions.  If it has *then* children, each child will be run with the response as a source.

*Load* is a template, and substitutions are performed upon it before it is used as a URL.

    {
      "load" : "http://www.google.com?q={{query}}"
    }

would load the page querying google for whatever substitutes for "query".  Substitutions within *load* are URL-encoded.  If "query" were "a bunch of words", the request would evaluate to be "http://www.google.com?q=a+whole+bunch+of+words".

### optional attributes ###

---

#### method ####

A string HTTP method, either "get", "post", or "head".  Defaults to "get" unless *posts* are defined, in which case it defaults to "post" and should not be defined otherwise.

The "head" method is useful for obtaining session cookies without waiting to load an entire page.  After running

    {
      "load"   : "http://www.nytimes.com",
      "method" : "head"
    }

microscraper would use cookies from the Times's "Set-Cookie" header response.  Any *then* children from a "head" *load* will run without a source string, since no response is loaded.

---

#### posts ####

Either a string of post data, or an object mapping post names to values.  This is undefined by default.  If *posts* is defined, *method* defaults to "post".  Whether it is a string or an object, *posts* is evaluated for substitutions and form encoded.

For example, 

    {
      "load"  : "http://www.site.com",
      "posts" : "A payload of some {{key}}"
    }

would obtain the response from an HTTP post to site.com.  If "key" mapped to "value", the post uploaded would be "A%20payload%20of%20some%20value".

Many forms post using a name-value format.  This is mimicked easily using an object inside *post*

    {
      "load"  : "http://www.site.com",
      "posts" : {
        "a + b = c"      : "x & y & z",
        "{{name}}" : "{{value}}"
      }
    }

were "name" mapped to "mary" and "value" mapped to "a little lamb", the post uploaded would be "a%20%2B%20b%20%3D%20c=x%20%26%20y%20%26%20z&mary=a%20little%20lamb".

---

#### headers ####

An object mapping header names to values.  This is undefined by default.  Both the name and value are evaluated for substitutions, but are not form encoded.

    {
      "load"  : "http://www.site.com",
      "headers" : {
        "User-Agent" : "Mozilla/{{version}} ({{OS}})"
      }
    }

could replace microscraper's default "User-Agent" header with something like "User-Agent: Mozilla/5.0 (Macintosh)"

---

#### cookies ####

An object mapping cookie names to values.  This is undefined by default.  Both the name and value are evaluated for substitutions and are form encoded.  The cookies are added to microscraper's cookie store for the host in *load*, and will be reused in future requests to matching hosts unless overwritten.

    {
      "load"    : "http://www.site.com",
      "cookies" : {
        "abc" : "xyz",
        "several words" : "are encoded"
      },
      "then" : { "load" : "http://www.site.com/path/to/page" }
    }

The "Cookie" request header for both "http://www.site.com" and "http://www.site.com/path/to/page" will be "abc=xyz; several%20words=are%20encoded;", in addition to any cookies set by "Set-Cookie" responses from either *load*.
