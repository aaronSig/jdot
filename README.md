## Publish to local maven repository:
* Ensure sbt is installed. sbt can be install with Homebrew `brew install sbt`
* Run `sbt publish-m2` in this directory


# **J&middot;Dot**

## Introduction
J&middot;Dot is a json utility library for Scala/Java, which allows for **data-only transformations**. All operations are configurable and controlled by simple, easily persisted, data structures, mainly string sets and string dictionaries/maps. This allows for data lead access, building, transformations, object extraction and object mapping.

#### JPath expressions
The basic building blocks of these operation are strings (called JPaths) that are used to pull/place values or json elements from/into a document. They are one-line expressions, which define a path through a json document, as well as some simple operations such as string format, if-else and number parsing. At their most basic they follow javascript dot/square-bracket syntax to access objects, arrays and fields.

#### JDot Operation classes
* **JDotAccessor**: for accessing fields of a json document using JPath strings.
* **JDotBuilder**: for building json documents from JPath string to value pairs.
* **JDotTransformer**: for creating a json document from another, defined by a set of JPath string pairs (or a dictionary/map).
* **JDotAttacher**: for attaching (or merging) one json document onto another.
* **JDotExtractor**: for populating a Scala/Java object directly from a json document.
* **JDotMapper**: for mapping a json document onto a Scala/Java document, using a set of JPath string pairs.

## Simple Examples
Use of the library is best describe with some examples. These will demonstrate simple usage of the JPath expressions in accessing, building, transforming and attaching. The examples will be in Scala (through still relevant for Java users) and all will use the following json:

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
We can access the fields of this document using an accessor. First we set up a JDotAccessor with our json:
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





