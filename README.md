# **J.dot**
**A powerful, data controlled way to process json in Java or Scala.**

##### Publish to local maven repository:
* Ensure sbt is installed. sbt can be install with Homebrew `brew install sbt`
* Run `sbt publish-m2` in this directory

## Introduction
J.dot is a json utility library for Scala/Java, which allows for **data-only transformations**. All operations are configurable and controlled by simple, easily persisted, data structures. J.dot allows you to avoid hard coding transformation logic.  
A common use case for json based applications is translating json content into view-models. A common problem is that both content and view-models can change often. With J.dot, any alerations you need to make to your translation logic can be made in your datastore, avoiding the need to edit code and re-deploy.

#### JPath expressions
The basic building blocks of these operation are strings (called JPaths) that are used to pull/place values or json elements from/into a document. They are one-line expressions, which define a path through a json document, as well as some simple operations such as string format, if-else and number parsing. At their most basic they follow javascript dot/square-bracket syntax to access objects, arrays and fields.

#### J.dot operations
* Accessing fields of a json document using JPath-strings.
* Building json documents from JPath-string to value pairs.
* Creating a json document from another, defined by a set of JPath-string pairs (or a dictionary/map).
* Attaching (or merging) one json document onto another.
* Populating a Scala/Java object directly from a json document.
* Mapping a json document onto a Scala/Java document, using a set of JPath-string pairs.


## Simple Examples
Use of the library is best describe with some examples. These will demonstrate simple usage of the JPath expressions in accessing, building, transforming and attaching. The examples will be in Scala (through still relevant for Java users). These simple examples will use the following json:

```json
{
   "circuit":{
      "name":"Albert Park Grand Prix Circuit",
      "country":"Australia",
      "city":"Melbourne"
   },
   "raceName":"Australian Grand Prix",
   "season":"2016",
   "round":1,
   "finished":true,
   "results":[
      "ROS", "HAM", "VET", "RIC", "MAS", "GRO", "HUL", "BOT",
      "SAI", "VES", "PAL", "MAG", "PER", "BUT", "NAS", "WEH",
      "ERI", "RAI", "HAR", "GUT", "ALO", "KVY"
   ],
   "podiumDetail":[
      {"driverName":"Nico Rosberg","team":"Mercedes","points":25},
      {"driverName":"Lewis Hamilton","team":"Mercedes","points":18},
      {"driverName":"Sebastian Vettel","team":"Ferrari","points":15}
   ]
}
```
#### JDotAccessor
We can access the fields of this document using an accessor and JPath strings. First we set up a JDotAccessor with our json:
```scala
val json: String = ... //json as above
val accessor = JDotAccessor(json)
```
We can access strings, numbers, booleans, json arrays and json objects:
```scala
val racename: Option[String] = accessor.getString("raceName") //Some("Australian Grand Prix")
val round: Option[Number] = accessor.getNumber("round") //Some(1)
val finished: Option[Boolean] = accessor.getBoolean("finished") //Some(true)

val circuit: Option[String] = accessor.getJsonString("circuit")
//Some("""{"name":"Albert Park Grand Prix Circuit","country":"Australia","city":"Melbourne"}"""))
val results: Option[String] = accessor.getJsonString("results")
//Some( """["ROS","HAM","VET","RIC","MAS","GRO","HUL","BOT","SAI","VES","PAL","MAG","PER","BUT","NAS","WEH","ERI","RAI","HAR","GUT","ALO","KVY"]"""))
```
We can drill down into nested objects using javascript style dot syntax:
```scala
val raceCity = accessor.getString("circuit.city") //Some("Melbourne")
```
We can access arrays with square bracket syntax
```scala
val winnerName = accessor.getString("results[0]") //Some("ROS")
val secondPoints = accessor.getNumber("podiumDetail[1].points") //Some(18)
```

#### JDotBuilder
JPaths can also be used to build a json document. This is done with a list of pairs, JPath to Value:
```scala
val builder = JDotBuilder.default
val buildPairs: Set[(JPath, Any)] = Set(
  ("title",               "My F1 Profile"),
  ("age",                 26),
  ("favouriteRace.year",  "2011"),
  ("favouriteRace.name",  "Canadian Grand Prix"),
  ("topThreeDrivers[0]",  "Jenson Button"),
  ("topThreeDrivers[1]",  "Kamui Kobayashi"),
  ("topThreeDrivers[2]",  "Stoffel Vandoorne")
)

val json: String = builder.build(buildPairs)
//{
//  "title":"My F1 Profile",
//  "age":26,
//  "favouriteRace":{"name":"Canadian Grand Prix","year":"2011"}
//  "topThreeDrivers":["Jenson Button","Kamui Kobayashi","Stoffel Vandoorne"],
//}
```

#### JDotTransformer
Transformers are a combination of accessor and builder. They pull values (or arrays/objects) from a given json document and build a new json document from them. They take a json document and a set of JPath pairs:
```scala
val transformPairs: Set[JPathPair] = Set(  //Set[JPathPair] expands to Set[(JPath, JPath)]
//  JPathPair format:
//        TO                      FROM
      ("race.country",        "circuit.country"),
      ("race.city",           "circuit.city"),
      ("race.name",           "raceName"),
      ("race.season",         "season"),
      ("race.seasonRound",    "round"),
      ("winner.code",         "results[0]"),
      ("winner.name",         "podiumDetail[0].driverName"),
      ("winner.team",         "podiumDetail[0].team")
    )
val transformer = JDotTransformer(transformPairs)
val transformedJson = transformer.transform(json)
// {
//  "race":{
//    "name":"Australian Grand Prix",
//    "season":"2016",
//    "seasonRound":1,
//    "city":"Melbourne",
//    "country":"Australia"
//  },
//  "winner":{
//    "name":"Nico Rosberg",
//    "code":"ROS",
//    "team":"Mercedes"
//  }
//}
```
By setting up the transformer with the transform pairs we can reuse it for other json document which follow that same format e.g. the results of other races in the season.

The previous examples utilise implicit conversions for syntactic sugar. The following all declare the same JPathPair:
```scala
val shortDeclaration: JPathPair = ("winner.name", "podiumDetail[0].driverName")

val noSugarDeclaration: JPathPair = 
    JPathPair(JPath.fromString("winner.name"), JPath.fromString("podiumDetail[0].driverName"))

val fullDeclaration: JPathPair = 
      JPathPair(
        JPath(JObjectPath("winner"), JObjectPath("name")),
        JPath(JObjectPath("podiumDetail"), JArrayPath(0), JObjectPath("driverName"))
      )
```

#### JDotAttacher
Attachers allow you to add to (or merge into) an existing json document (in this case the transformed json from above). 

```scala
val attachPairs: Set[JPathPair] = Set(
      ("start.date",      "date"),
      ("start.time",      "time")  
    )
val attacher = JDotAttacher(attachPairs)
val startJson = """{"time":"05:00:00Z", "date":"2016-03-20"}"""
val attachedJson = attacher.attach(startJson, transformedJson)
//{
//  "race":{...},
//  "winner":{...},
//  "start":{"time":"05:00:00Z", "date":"2016-03-20"}
//}
```
Attachers can also be used to merge into existing json objects:
```scala
val attachPairs: Set[JPathPair] = Set(
        ("winner.streak",      "rosbergWinStreak") 
    )
val attacher = JDotAttacher(attachPairs)
val streakJson = """{"rosbergWinStreak":4}"""
val attachedJson = attacher.attach(streakJson, transformedJson)
//{
//  "race":{...},
//  "winner":{"streak":4, "name":"Nico Rosberg", "code":"ROS", "team":"Mercedes"}
//}
```

#### Empty JPaths
Empty JPaths are also allowed. An empty JPath signifies to take the entire json document. This follows logically if you work backwards from a longer path:
```scala
//{
//  "race":{...},
//  "winner":{"name":"Nico Rosberg", "code":"ROS", "team":"Mercedes"}
//}
val accessor = JDotAccessor(transformedJson)

val baseWinnerName = accessor.getJsonString("winner.name") 
//"Nico Rosberg"

val baseWinner = accessor.getJsonString("winner")
//{"name":"Nico Rosberg", "code":"ROS", "team":"Mercedes"}

val base = accessor.getJsonString("")
//{
//  "race":{...},
//  "winner":{"name":"Nico Rosberg", "code":"ROS", "team":"Mercedes"}
//}
```
When applied to attachers, an empty JPath on the right allows you to attach the whole json document:
```scala
val attachPairsEmptyRight: Set[JPathPair] = Set(
        ("start",      "")
      )
val attacherEmptyRight = JDotAttacher(attachPairsEmptyRight)
val attachedJsonEmptyRight = attacherEmptyRight.attach("""{"time":"05:00:00Z", "date":"2016-03-20"}""", ausF1ShortArray)
//{
//  "race":{...},
//  "winner":{...},
//  "start":{"time":"05:00:00Z", "date":"2016-03-20"}
//}
```
Empty JPaths also make sense for builders. They declare that the associated value be merged into the base of the resulting json document:
```scala
val builder = JDotBuilder.default
val buildPairs = Set(
  ("",               """{"name":"Nico Rosberg", "team":"Mercedes"}"""),
  ("position",       1)
)
val built: String = builder.build(buildPairs)
//{"name":"Nico Rosberg", "position":1, "team":"Mercedes"}
```
Applying this to attachs, an empty JPath on the left declares that the accessed value be merged into the base target json:
```scala
val attachPairsEmptyLeft: Set[JPathPair] = Set(
        ("",      "start")
      )
val attacherEmptyLeft = JDotAttacher(attachPairsEmptyLeft)
val attachedJsonEmptyLeft = attacherEmptyLeft.attach("""{"start":{"time":"05:00:00Z", "date":"2016-03-20"}}""", ausF1ShortArray)
//{
//  "race":{...},
//  "winner":{...},
//  "date":"2016-03-20",
//  "time":"05:00:00Z"
//}
```
## Simple Java Examples
To simplify use of the library from Java, adapter classes are provided. Below are some of the previous examples converted to Java:
#### JvJDotTransformer
We use the builder pattern to attain a transformer (due to the many parameters that a transformer can take, which will be described later).
```java
Map<String, String> transformPairs = new HashMap<>();
transformPairs.put("race.country",        "circuit.country");
transformPairs.put("race.city",           "circuit.city");
transformPairs.put("race.name",           "raceName");
transformPairs.put("race.season",         "season");
transformPairs.put("race.seasonRound",    "round");
transformPairs.put("winner.code",         "results[0]");
transformPairs.put("winner.name",         "podiumDetail[0].driverName");
transformPairs.put("winner.team",         "podiumDetail[0].team");

JvJDotTransformer transformer = JvJDotTransformer.builder()
                                                .withPathMapping(transformPairs)
                                                .build();
                                  
String transformedJson = transformer.transform(json);
// {
//  "race":{"name":"Australian Grand Prix","season":"2016","seasonRound":1,"city":"Melbourne","country":"Australia"},
//  "winner":{"name":"Nico Rosberg","code":"ROS","team":"Mercedes"}
//}
```
#### JvJDotAttacher
```java
Map<String, String> attachPairs = new HashMap<>();
attachPairs.put("start.date",    "date");
attachPairs.put("start.time",    "time");

JvJDotAttacher attacher = JvJDotAttacher.builder()
                                        .withAttachmentMapping(attachPairs)
                                      .build();
String attachedJson = attacher.attach("{\"time\":\"05:00:00Z\", \"date\":\"2016-03-20\"}", transformedJson);
//{
//  "race":{...},
//  "winner":{...},
//  "start":{"time":"05:00:00Z", "date":"2016-03-20"}
//}
```

## More JPath Expressions

So far we've only seen the simplest JPath expressions. Below are some examples using an accessor to show off what JPaths can do. For this we will use a new json document, which expands on the previous one: [exampleJson](https://raw.githubusercontent.com/superpixelhq/jdot/master/src/test/resources/2016-aus-grandprix-result-simple.json)

#### Default Values
Round brackets can be used in a JPath to define a default value. If the path doesn't exist then the default value will be returned instead.
```scala
val accessor = JDotAccessor(exampleJson)
//field "raceName" exists
accessor.getString("raceName(Unknown Race)")  //Some("Australian Grand Prix")
//no field called "raXeName"
accessor.getString("raXeName")                //None
accessor.getString("raXeName(Unknown Race)")  //Some("Unknown Race")
```
One common use case is to manage optional fields in arrays (e.g. the `"finishLap"` field in the `"results"` array in our  [exampleJson](https://raw.githubusercontent.com/superpixelhq/jdot/master/src/test/resources/2016-aus-grandprix-result-simple.json)): 
```scala
//Winner has field "finishLap"
accessor.getString("results[0].finishLap(DNF)")     //Some("Lead Lap")
//17th element of "results" array does not have field "finishLap"
accessor.getString("results[16].finishLap(DNF)")    //Some("DNF")
```
They can also be used in the middle of a path, to capture where the json is missing:
```scala
//Winner has field "finishLap"
accessor.getString("results[0](No entry).finishLap(DNF)")       //Some("Lead Lap")
//17th element of "results" array does not have field "finishLap"
accessor.getString("results[16](No entry).finishLap(DNF)")      //Some("DNF")
//There is no 30th element of the "results" array
accessor.getString("results[29](No entry).finishLap(DNF)")      //Some("No entry")
```

#### Pure Values
It is sometimes useful to just have a JPath return a value independent of the json, for example adding a static field during transformations. This is achieved with a path consisting of only a default value (round brackets) expression:
```scala
val transformPairs: Set[JPathPair] = Set(
    ("sport",    "(F1)")    
)
val transformer = JDotTransformer(transformPairs)
transformer.transform(ausF1Simple)
// {"sport":"F1"}
```

#### String Format
Using a String Format expression allows you to combine fields and literal values to form a single string. The expression starts with a `"|"` and then follows a similar syntax to Scala's string interpolation. Everything after the `"|"` will appear as is. Fields can be accessed by wrapping the access JPath in curly braces (`"{"`, `"}"`):
```scala
accessor.getString("|Round {round} of the {season} season.")
//Some("Round 1 of the 2016 season.")
accessor.getString("|And the winner is {results[0].driver.forename} {results[0].driver.surname}!")
//Some("And the winner is Nico Rosberg!")
```
You can also start a String Format expression after a path. The path defines the base json object for the string format. This allows us to simplify the example above:
```scala
accessor.getString("results[0].driver|And the winner is {forename} {surname}!")
//Some("And the winner is Nico Rosberg!")
```
Any JPath expression can be placed inside the curly braces (`"{"`, `"}"`), including another string format:
```scala
//Winner finished the race so has no dnfReason
accessor.getString("results[0]|{driver.forename} {driver.surname} {status} due to {dnfReason(good driving!)}")
//Some("Nico Rosberg Finished due to good driving!")

//Seventeenth position DNF (did not finish) because of an engine problem
accessor.getString("results[16]|{driver.forename} {driver.surname} {status} due to {dnfReason(good driving!)}")
//Some("Marcus Ericsson DNF due to Engine")

//String format inside a string format to build driver name
accessor.getString("results[16]|{driver|{forename} {surname}} {status} due to {dnfReason(good driving!)}")
//Some("Marcus Ericsson DNF due to Engine")
```
Some characters must be escaped if you want them to included in the resulting string:
```scala
accessor.getString("""results[0].driver|{forename} says: 'You must escape \(, \), \{, \} and \^'""")
//Some("Nico says: 'You must escape (, ), {, } and ^'")
```
Round brackets (`"("`,`")"`) can be included to clarify String Format expressions. They also provide a way to escape characters as anything inside round brackets is treated as literal:
```scala
//Can clarify whole string format with format "|(...)"
accessor.getString("|(Round {round} of the {season} season.)")
//Some("Round 1 of the 2016 season.")

//Can clarify each literal string individually
accessor.getString("|((Round ){round}( of the ){season}( season.))")
//Some("Round 1 of the 2016 season.")

//Which allows us to save from escaping characters
accessor.getString("|((Round ){round}( of {^} the ){season}( season.))")
//Some("Round 1 of {^} the 2016 season.")

//Here both string format expression are clarified with format "|(...)"
accessor.getString("results[16]|({driver|({forename} {surname})} {status} due to {dnfReason(good driving!)})")
//Some("Marcus Ericsson DNF due to Engine")
```

#### Conditional Expressions
Conditional expression follow the syntax and logic of Java ternary operators. They can be used to check field existence or compare field values. The expression starts with a `"~"`. The field to test comes first. If you are testing existence then it is of the form `"`_`<jpath>`_`?"` (where _<jpath>_ is replaces by another JPath expression). If you are doing a comparison then: `"`_`<jpath1>`_`=`_`<jpath2>`_`?"`. Next comes the true/false values: `"`_`<trueJPath>`_`:`_`<falseJPath>`_`"`. So the full expression follows this format: `"~`_`<jpath1>`_`=`_`<jpath2>`_`?`_`<trueJPath>`_`:`_`<falseJPath>`_`"`.
```scala
//No datetime field so use date field
accessor.getString("~datetime?datetime:date")       //Some("2016-03-20")

//If car has dnfReason field then return it, if not then return status
accessor.getString("results[0]~dnfReason?dnfReason:status")     //Some("Finished")
accessor.getString("results[20]~dnfReason?dnfReason:status")    //Some("Collision")

//if finished with same position as grind then return team, otherwise return gridPosition
accessor.getString("results[2]~position=gridPosition?team:gridPosition")    //Some("Ferrari")
accessor.getString("results[10]~position=gridPosition?team:gridPosition")   //Some("13")
```
The JPath in each of the four operator spots can be a full JPath. Pure value expression are particularly useful here:

```scala
//If driver had position equal to string "1" then return "Winner" otherwise "Not winner"
accessor.getString("results[0]~position=(1)?(Winner):(Not winner)")     //Some("Winner")
accessor.getString("results[10]~position=(1)?(Winner):(Not winner)")    //Some("Not winner")

//if driver qualified on pole then return race country, otherwise return winning team
accessor.getString("~results[0].gridPosition=(1)?circuit.country:results[0].team")  //Some("Mercedes")
accessor.getString("~results[1].gridPosition=(1)?circuit.country:results[0].team")  //Some("Australia")
```
Curly braces (`"{"`, `"}"`) can be used to help clarify expressions:
```scala
//Can clarify the whole expression with format "~{...(=...)?...:...}"
accessor.getString("~{datetime?datetime:date}")             //Some("2016-03-20")

//Can clarify each nested path with format "~{{...}={...}?{...}:{...}}"
accessor.getString("~{{datetime}?{datetime}:{date}}")       //Some("2016-03-20")

//You can mix and match in this regard
accessor.getString("~{datetime?{datetime}:date}")       //Some("2016-03-20")

accessor.getString("~{{results[1].gridPosition}=(1)?{circuit.country}:{results[1].team}}")
//Some("Australia")
```
This allows us to nest conditionals in other conditionals:
```scala
//If driver had position equal to string "1" then return "Winner"
//otherwise if points field equals "0" then "Not in Top 10", otherwise "In Top 10"

//Clarifying just the nested conditional ("{" after "~")
accessor.getString("results[0]~position=(1)?(Winner):~{points=(0)?(Not in Top 10):(In Top 10)}")  
//Some("Winner")
accessor.getString("results[6]~position=(1)?(Winner):~{points=(0)?(Not in Top 10):(In Top 10)}")  
//Some("In Top 10")
accessor.getString("results[12]~position=(1)?(Winner):~{points=(0)?(Not in Top 10):(In Top 10)}") 
//Some("Not in Top 10")

//Clarifying the whole of the nested false path ("{" before "~")
accessor.getString("results[0]~position=(1)?(Winner):{~points=(0)?(Not in Top 10):(In Top 10)}")  
//Some("Winner")
accessor.getString("results[6]~position=(1)?(Winner):{~points=(0)?(Not in Top 10):(In Top 10)}")  
//Some("In Top 10")
accessor.getString("results[12]~position=(1)?(Winner):{~points=(0)?(Not in Top 10):(In Top 10)}") 
//Some("Not in Top 10")
```
In fact, curly braces (`"{"`, `"}"`) can be used to clarify any nested JPath (as we saw with String Format expressions).    
Similarly round brackets (`"("`,`")"`) can be used to clarify any part of path that is literal, similar to Default/Pure Values (which have to have them anyway) and String Format expressions.

#### Transmutation expressions

Transmutation expressions allow you to change the returning value. You can specify a specific json type or format the value. Think type casting meets C-style _printf_ operations. For example, you can use transmutations to: change a numeric string into a number, format a float to a string with 2 decimal places, format a date string, and turn a number value into a formatted currency string.

The expressions take the following form `"`_`jpath`_`^`_`transmuteType`_`<(`_`argument`_`)"`. The _transmuteType_ denotes what operation to perform and the _argument_ is a single literal string which can augment this operation. For example to parse a value into an integer the following JPath would be used: `"path.to.numberString^n<(i)"` where `^n` denotes that we are transmuting into a number and `<(i)` denotes that the number should be an integer.

Below are some examples of simple, casting style transmutations that change json value types (boolean, number, string). `^b` transmutes into a boolean, `^n` transmutes into a number, and `^s` transmutes into a string:
```scala
val json = """{   "falseStrField":  "false", 
                  "oneField":       1,
                  "intStrField":    "214", 
                  "floatStrField":  "3.14", 
                  "boolField":      false,
                  "intField":       3, 
                  "floatField":     1.61    }"""
     
val transformer = JDotTransformer(Set(
    ("falseField",       "falseStrField^b"),        //Transmutes string to boolean
    ("trueField",        "oneField^b"),             //Transmutes number to boolean
    ("notTrueField",     "oneField^b<(!)"),         //Transmutes number to boolean
                                                    // argument "!" performs a NOT on the result
    ("intField",         "intStrField^n"),          //Transmutes numeric string to number
    ("floatField",       "floatStrField^n"),        //Transmutes numeric string to number
    ("floatToIntField",  "floatStrField^n<(i)"),    //Transmutes numeric string to number
                                                    // argument "i" ensures result is an integer
    ("zeroField",        "boolField^n"),            //Transmutes boolean to number
    ("intStringField",   "intField^s"),             //Transmutes number to string
    ("floatStringField", "floatField^s")            //Transmutes number to string
))
      
val transformed = transformer.transform(json)
//{ "falseField":       false, 
//  "trueField":        true,
//  "notTrueField":     false,
//  "intField":         214, 
//  "floatField":       3.14,
//  "floatToIntField":  3, 
//  "zeroField":        0,
//  "intStringField":   "3", 
//  "floatStringField": "1.61"  }
```

Below are some example of printf style Transmutations. `^f` formats floats strings, `^i` and `^d` (identical) format integer strings, `^%` formats into a percentage string, `^ord` formats into a number with ordinal suffix, and `^s` takes arguments which perform basic string transformations (e.g. substring, uppercase):
```scala
val json = """{  "longFloatField":   3.14159265,
                 "shortFloatField":  0.34,
                 "lowIntField":      5,
                 "highIntField":     5321,
                 "lcString":         "transmute",
                 "ucString":         "JDOT"
                 "name":             "joe dottington"  }"""
    
val transformer = JDotTransformer(Set(
    ("twoDP",                 "longFloatField^f<(.2)"),     //printf format float ("%f"), 
                                                            // argument ".2" specifies 2 decimal places
    ("extraZeroes",           "shortFloatField^f<(06.3)"),  //printf format float ("%f"),  
                                                            // argument "06.3" specifies lead with zeroes,
                                                            // 6 character total length, 3 decimal places
    ("percentage",            "shortFloatField^%"),         //transmutes float between 0 and 1 to percentage
    ("formattedInt",          "lowIntField^i<(+03)"),       //printf format integer ("%d"),
                                                            // argument "+03" specifies show sign,
                                                            // lead with zeroes, 3 character length
    ("intWithCommas",         "highIntField^d<(,)"),        //printf format integer ("%d"),
                                                            // argument "," specifies add clarity commas 
    ("ordinal",               "highIntField^ord"),          //transmutes int to append ordinal suffix
    ("ordinalFullString",     "lowIntField^ord<(full)"),    //argument "full" transmutes to full 
                                                            // ordinal word (up to 12)
    ("uppercase",             "lcString^s<(u)"),            //argument "u" performs uppercase function
    ("substring",             "lcString^s<(1.4)"),          //argument "1.4" performs substring function
                                                            // from (inc) index 1 to (exc) index 4
    ("capitalised",           "lcString^s<(1u)"),           //argument "1u" uppercases the first character
    ("lowercase",             "ucString^s<(l)"),            //argument "l" performs lowercase function
    ("minusSubstring",        "ucString^s<(.-3)"),          //argument ".-3" performs substring function
                                                            // which takes up to the last 3 characters
    ("surname",               "name^s<(4.:1u)")             //colon in "^s" argument allows chaining:
                                                            // applies "4." then "1u"
))
    
val transformed = transformer.transform(json)
//{   "twoDP":               "3.14",
//    "extraZeroes":         "00.340",
//    "percentage":          "34%",
//    "formattedInt":        "+05",
//    "intWithCommas":       "5,321",
//    "ordinal":             "5321st",
//    "ordinalFullString":   "Fifth",
//    "uppercase":           "TRANSMUTE",
//    "substring":           "ran",
//    "capitalised":         "Transmute",
//    "lowercase":           "jdot",
//    "minusSubstring":      "J",
//    "surname":             "Dottington"  }
```

You can also use Transmutations for date and currency formatting, using the `^date` and `^cur` types. Currency formatting transmutations accept a number and take a locale string, currency code or currency symbol (£, $, €, ¥ supported) as an argument and outputs a formatted currency string:
```scala
val json = """{  "small": 19.99,
                 "subunits": 3020,
                 "large": 1500.00 }"""
    
val transformer = JDotTransformer(Set(
    ("irelandEuros",           "small^cur<en-IE"),          //Formats for english speaking Irish locale
    ("frenchEuros",            "small^cur<fr-FR"),          //Formats for French locale
    ("dkkLarge",               "large^cur<da-DK"),          //Formats for Danish locale
    ("usdDollars",             "small^cur<USD"),            //Formats for USD currency
    ("symbolDollars",          "small^cur<$"),              //Formats with "$" symbol
    ("centsToDollars",         "subunits^cur<_en-US"),      //Adding "_" to argument ensures value
                                                            // is treated as subunits, instead of units
    ("gbpNoPennies",           "large^cur<0GBP")            //Adding "0" to argument removes subunits
))                                                          // from output (think decimal places)
    
val transformed = transformer.transform(json)
//{   "irelandEuros":        "€19.99",
//    "frenchEuros":         "19,99 €",
//    "dkkLarge":            "kr 1.500,00",
//    "usdDollars":          "USD19.99",
//    "symbolDollars":       "$19.99",
//    "centsToDollars":      "$30.20",
//    "gbpNoPennies":        "£1,500"  }
```

Date formatting accepts an ISO 8601 date string, a timestamp integer (seconds since 1970-01-01), or a keyword (such as _"now"_) and takes a date format (e.g. _" as an argument (or a keyword, such as _"pretty"_ or _"epoch"_). It outputs a string based on that format. For date format rules please look [here](http://www.joda.org/joda-time/key_format.html).

```scala
val json = """{  "justDate":  "2016-02-28"
                 "time":      "T05:00:00Z"
                 "datetime":  "2016-04-20T05:00:00Z" 
                 "timestamp": 1456617600  }"""
    
// Date of example: 2016-04-25
val transformer = JDotTransformer(Set(
    ("simpleDate",      "justDate^date<(dd MMM yyyy)"),         //Formats to date format
    ("ordinalDate",     "justDate^date<(do 'of' MMMM)"),        //'do' renders an ordinal day of month
    ("simpleTime",      "time^date<(hh)"),                      //Formats to time format
    ("simpleDatetime",  "datetime^date<(hh:mm, dd MMM yy)"),    //Can take any form of ISO 8601
    ("prettyTime",      "datetime^date<(pretty)"),              //Supports pretty time with 'pretty' argument
    ("now",             "(now)^date<(yyyy-MM-dd)"),             //Using a pure value JPath "(now)"
                                                                // to render current datetime.
    ("fromTimestamp",   "timestamp^date<(yyyy:MM:dd)"),         //Takes a number as a timestamp
    ("toTimestamp",     "justDate^date<(timestamp)")            //Can turn a date string into a timestamp
))

val transformed = transformer.transform(json)
//{   "simpleDate":        "28 Feb 2016",
//    "ordinalDate":       "28th of February",
//    "simpleTime":        "05",
//    "simpleDatetime":    "05:00, 20 Apr 16",
//    "prettyTime":        "5 days ago",
//    "now":               "2016-04-25",
//    "fromTimestamp":     "2016:02:28",
//    "toTimestamp":       1456617600  }
```
