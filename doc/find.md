# microscraper

cooperative scrapers for mobile apps

## Find ##

The *find* object extends [instruction](instruction.md), and permits all of its attributes.  Take a look at the [JSON schema](../schema/json/find).

*Find* searches within a piece of source text provided by its enclosing instruction.  The simplest valid find object looks like this:

    {
      "find" : "template regexp"
    }

This would search for all matches of "tempalte regexp" within the text provided by its enclosing expression.  If *find* also has a name, each match will be saved to the database and used for substitutions.  If it has children, each child will be run once with every match.

*Find* is a template, and substitutions are performed upon it before it is used as a regular expression.

    {
      "find" : "{{key}}"
    }

would find matches of the text substituting for "key" when *find* is run.  Substitutions are performed before the regular expression is compiled.  Regex wildcards and other special characters can be part of a substitution.  If the substitution for "key" were "b.t", the find could return "bot", "bat", etc.

---

### Substitution & Scope ###

While any [instruction](instruction.md) can have a *name* and have its results be used in substitutions, only *find* instructions can populate a *name* with multiple result values.

Microscraper handles this many-to-one situation by providing every instruction launched from one of several matches with a separate scope for substitution.

    {
      "name" : "query", 
      "find" : ".....ing",
      "then" : { "load" : "http://www.google.com?q={{query}}" }
    }

Runs the *load* once for each match.  It would query Google for "troll", "grift" and "crack" if the source string were "trolling, grifting, and cracking".

Let's look at some more complex examples.

    {
      "name" : "query", 
      "find" : ".....ing",
      "then" : [
        { "load" : "http://www.google.com?q={{substring}}" },
        {
          "name" : "substring",
          "find" : "....ing"
        }
      ]
    }

Using the same source string, this would save three strings into the database -- "rolling", "rifting", and "racking".  However, it would never query google for these words.  Since the *find* "query" matches more than once, its instructions do not share their values with one another for substitution.  If the *load* instruction were located inside the *find* "substring", Google would be queried for the substrings.

    {
      "name" : "query", 
      "find" : "^.....ing",
      "then" : [
        { "load" : "http://www.google.com?q={{substring}}" },
        {
          "name" : "substring",
          "find" : "....ing"
        }
      ]
    }

With the addition of a "^", the *find* "query" now only matches "trolling".  Since it is one-to-one, its one-to-one children can share substitutions.  "substring", too,  matches exactly once against "trolling", so Google would be queried for "rolling".

Use the optional *match* attribute to force one-to-one matching by *find*, if you want to ensure deep sharing of substitutions.

    {
      "name" : "parent",
      "find" : ".*",
      "match": 0,
      "then" : [{
        "name"  : "child",
        "find"  : "\\w+"
        "match" : 2,
        "then"  : {
          "name" : "grandchild",
          "find" : "^."
          "match": 0
        }
      },{
        "name" : "cousin",
        "find" : "{{grandchild}}"
      }]
    }

If the source string for "parent" were "rats, ribbons and rhizomes", the following substitutions would be be assigned:

    parent = "rats, ribbons and rhizomes"
    child  = "rhizomes"
    grandchild = "r"

Although nested within several *then*s, each *find* leading to "grandchild" is one-to-one.  This means that "grandchild" is within scope for "cousin" *find*.  It will match three times, once for each "r" in the source string.

---

### Optional Attributes ###

#### case_insensitive ####

A boolean value, false by default.

    {
      "find"             : "hello world!",
      "case_insensitive" : true
    }

Would match once against the input "Hello world!", but would not match at all if case_insensitive were not specified.

---

#### multiline ####

A boolean value, false by default.  If specified to be true, "^" will match before the start of every line, in addition to before the beginning of the whole string.  Likewise, "$" will match after the end of every line, in addition to after the end of the whole string.

    {
      "find" :      "^<tr>",
      "multiline" : true
    }

Would match every table row element at the very beginning of every line, in addition to one at the very beginning of the source string.

---

#### dot_matches_all ####

A boolean value, true by default.  If specified to be false, "." will *not* match newlines.

    {
      "find"            : "<td>.*?</td>"
      "dot_matches_all" : false
    }

Would not match a cell where the intervening text had any linebreaks.

---

#### replace ####

A string, "$0" by default.  This replacement is performed upon every match from find before each is saved to the database and/or used as a source for other instructions.

Additional, or replacement, text can be used in replace, in addition to template substitutions.

    {
      "find"    : "cat",
      "replace" : "A $0 is a $0"
    }

Would run every dependent instruction with the source text "A cat is a cat".

    {
      "find"    : "cat",
      "replace" : "A $0 is not an {{animal}}"
    }

Would run every dependent instruction with the source text "A cat is not a giraffe", were "animal" keyed to giraffe.

---

#### match ####

An integer, undefined by default.  When specified, find matches once instead of as many times as possible.  This attribute should not be specified if *max* or *min* are specified.

Counting is done forwards from "0" and backwards from "-1".

    {
      "find"    : ".",
      "match"   : 0
    }

Would match only the first character in the source string.  "1" would match the second character, "2" the third, and so on.

    {
      "find"    : ".",
      "match"   : -1
    }

Would match only the last character in the source string.  "-2" would match the second to last, "-3" the third to last, and so on.

---

#### min ####

An integer, "0" by default.  When specified, find's results do not include the specified number of matches from the beginning.  This attribute should not be specified if *match* is specified, and it should not define a minimum match that would always come before *max*.  "1" would be an invalid *min* were *max* "0", since there cannot be matches before the second and after the first.  Likewise, "-1" would be an invalid *min* were *max* "-2".

Counting is done forwards from "0" and backwards from "-1".

    {
      "find" : ".",
      "min"  : 3
    }

Would match separately for every character in the source string including and after the fourth.

    {
      "find"  : ".",
      "min"   : -3
    }

Would match separately for the last three characters of the source string.

---

#### max ####

An integer, "-1" by default.  When specified, *find*'s results do not include the specified number of matches until the end.  The same restrictions apply to *max* as to *min*, above.

Counting is done forwards from "0" and backwards from "-1".

    {
      "find" : ".",
      "max"  : 3
    }

Would match separately for every character in the source string from the first to the fourth, inclusive.

    {
      "find" : ".",
      "max"  : -3
    }

Would match separately for every character in the source string excepting the last two.
