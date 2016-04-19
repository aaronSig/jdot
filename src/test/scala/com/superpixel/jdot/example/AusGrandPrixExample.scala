package com.superpixel.jdot.example

import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import com.superpixel.jdot.pathing._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import scala.io.Source
import org.scalatest.FunSpec
import com.superpixel.jdot._
import scala.language.implicitConversions

class AusGrandPrixExample extends FunSpec with Matchers {

  val ausF1: String = {
      val buffSource = Source.fromURL(getClass.getResource("/2016-aus-grandprix-result.json"))
      val jsonLines = buffSource.getLines
      compact(render(parse(jsonLines.mkString)))
  }
  
  val ausF1Simple: String = {
      val buffSource = Source.fromURL(getClass.getResource("/2016-aus-grandprix-result-simple.json"))
      val jsonLines = buffSource.getLines
      compact(render(parse(jsonLines.mkString)))
  }
  
  val ausF1ShortArray: String = {
      val buffSource = Source.fromURL(getClass.getResource("/2016-aus-grandprix-result-shortarray.json"))
      val jsonLines = buffSource.getLines
      compact(render(parse(jsonLines.mkString)))
  }
  
  
  describe("Accessing json with simple JPath expressions") {
    
    //Set up accessor with our example json
    val accessor = JDotAccessor(ausF1ShortArray)
    
    it("can access immediate fields") {
      val racename = accessor.getString("raceName")
      assert(racename == Some("Australian Grand Prix"))
      
      val round = accessor.getNumber("round")
      assert(round == Some(1))
      
      val finished = accessor.getBoolean("finished")
      assert(finished == Some(true))
    }
    
    it("can access json objects") {
      val circuit = accessor.getJsonString("circuit")
      assert(circuit == Some("""{"name":"Albert Park Grand Prix Circuit","country":"Australia","city":"Melbourne"}"""))
    }
    
    it("can access json arrays") {
      val results = accessor.getJsonString("results")
      results.foreach { println(_) }
      assert(results == 
      Some( """[""" +
              """"ROS","HAM","VET","RIC","MAS","GRO","HUL","BOT","SAI","VES","PAL","MAG","PER","BUT","NAS","WEH","ERI","RAI","HAR","GUT","ALO","KVY"""" +
            """]"""))
    }
    
    it ("can access nested objects using javascript dot syntax") {
      val raceCity = accessor.getString("circuit.city")
      assert(raceCity == Some("Melbourne"))
    }
    
    it ("can access array elements using javascript square bracket syntax") {
      val winnerName = accessor.getString("results[0]")
      assert(winnerName == Some("ROS"))
      
      val secondPoints = accessor.getNumber("podiumDetail[1].points")
      assert(secondPoints == Some(18))
    }
  }
  
  describe("Building json with simple JPath expressions") {
    
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
    println(json)
    
    val expected = 
      ("title" -> "My F1 Profile") ~
      ("age" -> 26) ~
      ("favouriteRace" -> 
        ("year" -> "2011") ~
        ("name" -> "Canadian Grand Prix")
      ) ~
      ("topThreeDrivers" -> List("Jenson Button", "Kamui Kobayashi", "Stoffel Vandoorne"))
    
    assert(expected == parse(json))
  }
  
  describe("Transforming json with simple JPath expressions") {
    
    val transformPairs: Set[JPathPair] = Set(  //Set[JPathPair] == Set[(JPath, JPath)]
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
    
    it ("can create a new format for existing json doc") {
      val transformedJson = transformer.transform(ausF1ShortArray)
      
      println(transformedJson)
      val expected = 
        ("race" ->  ("country" -> "Australia") ~ ("city" -> "Melbourne") ~ ("name" -> "Australian Grand Prix") ~ ("season" -> "2016") ~ ("seasonRound" -> 1)) ~
        ("winner" -> ("code" -> "ROS") ~ ("name" -> "Nico Rosberg") ~ ("team" -> "Mercedes"))
        
      assert(expected == parse(transformedJson))
    }
    
    it ("can be declared without syntactic sugar") {
      val shortDeclaration: JPathPair = ("winner.name", "podiumDetail[0].driverName")
      
      val noSugarDeclaration: JPathPair = JPathPair(JPath.fromString("winner.name"), JPath.fromString("podiumDetail[0].driverName"))
      
      val fullDeclaration: JPathPair = 
        JPathPair(
          JPath(JObjectPath("winner"), JObjectPath("name")),
          JPath(JObjectPath("podiumDetail"), JArrayPath(0), JObjectPath("driverName"))
        )
  //    assert(shortDeclaration == noSugarDeclaration)
  //    assert(shortDeclaration == fullDeclaration)
    }
  }
  
  describe("Attaching json with simple JPath expressions") {
    
    val jsonJV = 
        ("race" ->  ("country" -> "Australia") ~ ("city" -> "Melbourne") ~ ("name" -> "Australian Grand Prix") ~ ("season" -> "2016") ~ ("seasonRound" -> 1)) ~
        ("winner" -> ("code" -> "ROS") ~ ("name" -> "Nico Rosberg") ~ ("team" -> "Mercedes"))
    val json = compact(render(jsonJV))
    
    it ("can attach to a new nested object") {
      val attachPairs: Set[JPathPair] = Set(
        ("start.date",      "date"),
        ("start.time",      "time")  
      )
      
      val attacher = JDotAttacher(attachPairs)
      val attachedJson = attacher.attach("""{"time":"05:00:00Z", "date":"2016-03-20"}""", json)
      
      println(attachedJson)
      val expected: JObject = parse(json) match {
        case JObject(fieldArr) => {
          val start:JValue = ("time" -> "05:00:00Z") ~ ("date" -> "2016-03-20")
      	  JObject(JField("start", start) +: fieldArr)
        }        
        case _ => fail
      }
      assert(expected == parse(attachedJson))
    }
    
    it ("can merge into an existing object") {
      val attachPairs: Set[JPathPair] = Set(
        ("winner.streak",      "rosbergStreak") 
      )
      
      val attacher = JDotAttacher(attachPairs)
      val attachedJson = attacher.attach("""{"rosbergStreak":4}""", json)
      
      println(attachedJson)
      val expected: JObject = 
        ("race" ->  ("country" -> "Australia") ~ ("city" -> "Melbourne") ~ ("name" -> "Australian Grand Prix") ~ ("season" -> "2016") ~ ("seasonRound" -> 1)) ~
        ("winner" -> ("code" -> "ROS") ~ ("streak" -> 4) ~ ("name" -> "Nico Rosberg") ~ ("team" -> "Mercedes"))
      assert(expected == parse(attachedJson))
    }
    
  }
  
  describe("Empty JPaths are allowed") {
    
	  val jsonJV = 
			  ("race" ->  ("country" -> "Australia") ~ ("city" -> "Melbourne") ~ ("name" -> "Australian Grand Prix") ~ ("season" -> "2016") ~ ("seasonRound" -> 1)) ~
			  ("winner" -> ("code" -> "ROS") ~ ("name" -> "Nico Rosberg") ~ ("team" -> "Mercedes"))
	  val json = compact(render(jsonJV))
    
    it ("can be used in accessors to get the entire object") {
      
      val accessor = JDotAccessor(json)

      val baseWinnerName = accessor.getJsonString("winner.name") 
      //"Nico Rosberg"
      
      val baseWinner = accessor.getJsonString("winner")
      //{"name":"Nico Rosberg", "code":"ROS", "team":"Mercedes"}
      
      val base = accessor.getJsonString("").getOrElse("")
      assert(parse(json) == parse(base))
    }
    
    it ("can be used in attachers on the right") {
      val attachPairsEmptyRight: Set[JPathPair] = Set(
        ("start",      "")
      )
      val attacherEmptyRight = JDotAttacher(attachPairsEmptyRight)
      val attachedJsonEmptyRight = attacherEmptyRight.attach("""{"time":"05:00:00Z", "date":"2016-03-20"}""", json)
      
      println(attachedJsonEmptyRight)
      val expected: JObject = parse(json) match {
        case JObject(fieldArr) => {
          val start:JValue = ("time" -> "05:00:00Z") ~ ("date" -> "2016-03-20")
          JObject(JField("start", start) +: fieldArr)
        }        
        case _ => fail
      }
      assert(expected == parse(attachedJsonEmptyRight))
    }
    
    
    it ("can be used in builders") {
      val builder = JDotBuilder.default
      val buildPairs: Set[(JPath, Any)] = Set(
        ("",               """{"name":"Nico Rosberg", "team":"Mercedes"}"""),
        ("position",       1)
      )
      val built: String = builder.build(buildPairs)
      println(built)
      val expected = ("name" -> "Nico Rosberg") ~ ("position" -> 1) ~ ("team" -> "Mercedes")
      assert(expected == parse(built))
    }
    
    
    it ("can be used in attachers on the left") {
      val attachPairsEmptyLeft: Set[JPathPair] = Set(
        ("",      "start")
      )
      val attacherEmptyLeft = JDotAttacher(attachPairsEmptyLeft)
      val attachedJsonEmptyLeft = attacherEmptyLeft.attach("""{"start":{"time":"05:00:00Z", "date":"2016-03-20"}}""", json)
      
      println(attachedJsonEmptyLeft)
      val expected: JObject = parse(json) match {
        case JObject(fieldArr) => {
          JObject(JField("time", "05:00:00Z") +: JField("date", "2016-03-20") +: fieldArr)
        }        
        case _ => fail
      }
      assert(expected == parse(attachedJsonEmptyLeft))
    }
    
  }
  
  
  describe("Accessing json with embedded, default values") {
    
    //Set up accessor with our example json
    val accessor = JDotAccessor(ausF1Simple)
    
    it("can access values with a fallback default using round brackets") {
      val racename = accessor.getString("raceName(Unknown Race)")
      assert(racename == Some("Australian Grand Prix"))
      
      val racenameTypo = accessor.getString("raxeName(Unknown Race)")
      assert(racenameTypo == Some("Unknown Race"))
    }
    
    it("can be used to manage differing fields in an array of objects") {
      //Winner has field "finishLap"
      val winnerFinishLap = accessor.getString("results[0].finishLap(DNF)")
      assert(winnerFinishLap == Some("Lead Lap"))
      
      //Seventeenth element of array does not have field "finishLap"
      val seventeenthFinishLap = accessor.getString("results[16].finishLap(DNF)")
      assert(seventeenthFinishLap == Some("DNF"))
    }
    
    it("can be used multiple times in a path to intercept missing elements") {
      //Winner has field "finishLap"
      val winnerFinishLap = accessor.getString("results[0](No entry).finishLap(DNF)")
      assert(winnerFinishLap == Some("Lead Lap"))
      
      //Seventeenth element of "results" array does not have field "finishLap"
      val seventeenthFinishLap = accessor.getString("results[16](No entry).finishLap(DNF)")
      assert(seventeenthFinishLap == Some("DNF"))
      
      //There is no 30th element of the "results" array
      val thirtiethFinishLap = accessor.getString("results[29](No entry).finishLap(DNF)")
      assert(thirtiethFinishLap == Some("No entry"))
    }
  }
  
  
  describe("Accessing just values (no json interaction) with round brackets") {
    
    //Set up accessor with our example json
    val accessor = JDotAccessor(ausF1Simple)
    
    it("can be used to access a value defined purely in the path") {
      val justValue = accessor.getString("(hello world)")
      assert(justValue == Some("hello world"))
    }
    
    it("can be used in a transformer") {
      val transformPairs: Set[JPathPair] = Set(
        ("sport",    "(F1)")    
      )
      val transformer = JDotTransformer(transformPairs)
      val transformed = transformer.transform(ausF1Simple)
      assert(parse("""{"sport":"F1"}""") == parse(transformed))
    }
    
  }
  
  
  describe("String format expressions") {
    
    //Set up accessor with our example json
    val accessor = JDotAccessor(ausF1Simple)
    
    it ("can be used to create formatted strings") {
      val raceIntro = accessor.getString("|Round {round} of the {season} season.")
      assert(Some("Round 1 of the 2016 season.") == raceIntro)
      val winnerText = accessor.getString("|And the winner is {results[0].driver.forename} {results[0].driver.surname}!")
      assert(Some("And the winner is Nico Rosberg!") == winnerText)
    }
    
    it ("can come after a path") {
      val winnerText = accessor.getString("results[0].driver|And the winner is {forename} {surname}!")
      assert(Some("And the winner is Nico Rosberg!") == winnerText)
    }
    
    it ("can include any path between the curly braces (nested paths)") {
      val winnerText = accessor.getString("results[0]|{driver.forename} {driver.surname} {status} due to {dnfReason(good driving!)}")
      assert(Some("Nico Rosberg Finished due to good driving!") == winnerText)
      
      val seventeenthText = accessor.getString("results[16]|{driver.forename} {driver.surname} {status} due to {dnfReason(good driving!)}")
      assert(Some("Marcus Ericsson DNF due to Engine") == seventeenthText)
      
      val seventeenthText2 = accessor.getString("results[16]|{driver|{forename} {surname}} {status} due to {dnfReason(good driving!)}")
      assert(Some("Marcus Ericsson DNF due to Engine") == seventeenthText2)

    }
    
    it("allows escaping of certain characters") {
      val escapeMessage = accessor.getString("""results[0].driver|{forename} says: 'You must escape \(, \), \{, \} and \^'""")
      assert(Some("Nico says: 'You must escape (, ), {, } and ^'") == escapeMessage)
    }
    
    it("can include round brackets to provide clarity") {
      val raceIntro2 = accessor.getString("|(Round {round} of the {season} season.)")
      assert(Some("Round 1 of the 2016 season.") == raceIntro2)
      
      val raceIntro3 = accessor.getString("|((Round ){round}( of the ){season}( season.))")
      assert(Some("Round 1 of the 2016 season.") == raceIntro3)
      
      val raceIntro4 = accessor.getString("|((Round ){round}( of {^} the ){season}( season.))")
      assert(Some("Round 1 of {^} the 2016 season.") == raceIntro4)
      
      val seventeenthText2 = accessor.getString("results[16]|({driver|({forename} {surname})} {status} due to {dnfReason(good driving!)})")
      assert(Some("Marcus Ericsson DNF due to Engine") == seventeenthText2)
      
      //"results[16]
      //  |(
      //    {driver|(
      //              {forename} {surname}
      //           )}
      //    {status}
      //    due to 
      //    {dnfReason(good driving!)}
      //  )
    }
  }
  
  
  describe("Conditional Expressions") {
    
    //Set up accessor with our example json
    val accessor = JDotAccessor(ausF1Simple)
    
    it ("can be used to define output on whether a field is present") {
      //No datetime field so use date field
      val raceDate = accessor.getString("~datetime?datetime:date")
      assert(Some("2016-03-20") == raceDate)
    }
    
    it ("can be used after a path") {
      //if car has dnfreason then return it, if not return status
      val finished = accessor.getString("results[0]~dnfReason?dnfReason:status")
      assert(Some("Finished") == finished)
      
      val notFinished = accessor.getString("results[20]~dnfReason?dnfReason:status")
      assert(Some("Collision") == notFinished)
    }
    
    it ("can be used to check values relative to each other") {
      //if finished with same position as grind then return team, otherwise return gridPosition
      val gridEqual = accessor.getString("results[2]~position=gridPosition?team:position")
      assert(Some("Ferrari") == gridEqual)

      val gridNEqual = accessor.getString("results[10]~position=gridPosition?team:gridPosition")
      assert(Some("13") == gridNEqual)
    }
    
    it ("can be used to check values with value paths") {
      //If driver had position equal to string "1" then return "Winner" otherwise "Not winner"
    	val winner = accessor.getString("results[0]~position=(1)?(Winner):(Not winner)")
    	assert(Some("Winner") == winner)
    			
    	val notWinner = accessor.getString("results[10]~position=(1)?(Winner):(Not winner)")
    	assert(Some("Not winner") == notWinner)
    }

    it ("can contain nested paths") {
      //if driver qualified on pole then return race country, otherwise return winning team
      val forWinner = accessor.getString("~results[0].gridPosition=(1)?circuit.country:results[0].team")
      assert(Some("Mercedes") == forWinner)
      
      val forSecond = accessor.getString("~results[1].gridPosition=(1)?circuit.country:results[1].team")
      assert(Some("Australia") == forSecond)
    }
    
    it ("can use curly braces for clarity") {
      val raceDate2 = accessor.getString("~{datetime?datetime:date}")
      assert(Some("2016-03-20") == raceDate2)
      
      val raceDate3 = accessor.getString("~{{datetime}?{datetime}:{date}}")
      assert(Some("2016-03-20") == raceDate3)
      
      val forSecond2 = accessor.getString("~{{results[1].gridPosition}=(1)?{circuit.country}:{results[1].team}}")
      assert(Some("Australia") == forSecond2)
    }
    
    it ("can contain other conditionals") {
      //If driver had position equal to string "1" then return "Winner"
      //otherwise if point equals "0" then "Not in Top 10", otherwise "In Top 10"
      
      //clarifying the nested conditional
      val winner = accessor.getString("results[0]~position=(1)?(Winner):~{points=(0)?(Not in Top 10):(In Top 10)}")
      assert(Some("Winner") == winner)
          
      val top10 = accessor.getString("results[6]~position=(1)?(Winner):~{points=(0)?(Not in Top 10):(In Top 10)}")
      assert(Some("In Top 10") == top10)
      
      val notInTop10 = accessor.getString("results[12]~position=(1)?(Winner):~{points=(0)?(Not in Top 10):(In Top 10)}")
      assert(Some("Not in Top 10") == notInTop10)
      
      //clarifying the whole of the nested false path
      val winner2 = accessor.getString("results[0]~position=(1)?(Winner):{~points=(0)?(Not in Top 10):(In Top 10)}")
      assert(Some("Winner") == winner2)
          
      val top102 = accessor.getString("results[6]~position=(1)?(Winner):{~points=(0)?(Not in Top 10):(In Top 10)}")
      assert(Some("In Top 10") == top102)
      
      val notInTop102 = accessor.getString("results[12]~position=(1)?(Winner):{~points=(0)?(Not in Top 10):(In Top 10)}")
      assert(Some("Not in Top 10") == notInTop102)
    }
  }
  
  
  describe("Simple Transmutations") {
    
    it ("can turn a string or number into a boolean") {
      val boolJson = """{"trueField":"true", "falseField":0}"""
      val boolAccessor = JDotAccessor(boolJson)
      
      val t = boolAccessor.getBoolean("trueField^b")
      assert(Some(true) == t)
      val f = boolAccessor.getBoolean("falseField^b")
      assert(Some(false) == f)
    }
    
    it ("can turn a string or boolean into a number") {
      val numJson = """{"intField":"214", "floatField":"3.14", "boolField":true}"""
      val numAccessor = JDotAccessor(numJson)
      
      val i = numAccessor.getNumber("intField^n")
      assert(Some(214) == i)
      val f = numAccessor.getNumber("floatField^n")
      assert(Some(3.14) == f)
      val b = numAccessor.getNumber("boolField^n")
      assert(Some(1) == b)
    }
    
    it ("can turn a number or boolean into a string") {
      val strJson = """{"intField":3, "boolField":false}"""
      val strAccessor = JDotAccessor(strJson)
      
      val iStr = strAccessor.getString("intField^s")
      assert(Some("3") == iStr)
      val bStr = strAccessor.getString("boolField^s")
      assert(Some("false") == bStr)
    }
    
    it ("can take arguments") {
      val json = """{"floatStrField":"3.14", "boolStrField":"false"}"""
      val accessor = JDotAccessor(json)
      
      val i = accessor.getNumber("floatStrField^n<(i)")
      assert(Some(3) == i)
      val b = accessor.getBoolean("boolStrField^b<(!)")
      assert(Some(true) == b)
    }
    
    it ("is mainly useful in transformations") {
      val json = """{ "falseStrField":  "false", 
                      "zeroField":       0,
                      "intStrField":    "214.0", 
                      "floatStrField":  "3.14", 
                      "boolField":      false,
                      "intField":       3, 
                      "floatField":     1.61}"""
     
      val transformer = JDotTransformer.apply(Set(
        ("falseField",       "falseStrField^b"),
        ("trueField",        "zeroField^b<(!)"),
        ("intField",         "intStrField^n<(i)"),
        ("floatField",    "floatStrField^n"),
        ("zeroField",        "boolField^n"),
        ("intStringField",   "intField^s"),
        ("floatStringField", "floatField^s")
      ))
      
      val transformed = transformer.transform(json)
      
      val expected = """{ "falseField":       false, 
                          "trueField":        true,
                          "intField":         214, 
                          "floatField":       3.14, 
                          "zeroField":        0,
                          "intStringField":   "3", 
                          "floatStringField": "1.61"}"""
      
      assert(parse(expected) == parse(transformed))
    }
  }
  
  describe("Simple Formatted String transmutations") {
    //f, i, d, ord, s with args
    
    
    
  }
  
  
  describe("Simplified ausF1 json") {

    //     TO                  FROM
    val jcTrans = JDotTransformer(
      Set(
        ("season",             "season"),
        ("round",              "round^n"),
        ("raceName",           "raceName"),
        ("circuit.name",       "Circuit.circuitName"),
        ("circuit.city",       "Circuit.Location.locality"),
        ("circuit.country",    "Circuit.Location.country"),
        ("date",               "date"),
        ("time",               "time")
      )    
    )
    
    val resultTrans = JDotTransformer(
      Set(
        ("driver.forename",     "Driver.givenName"),
        ("driver.surname",      "Driver.familyName"),
        ("driver.nationality",  "Driver.nationality"),
        ("team",                "Constructor.name"),
        ("position",            "position"),
        ("carNumber",           "number"),
        ("gridPosition",        "grid"),
        ("points",              "points"),
        ("time",                "Time.time"),
        ("fastestLap",          "FastestLap.Time.time"),
        ("finishLap",           "~{status=(Finished)?(Lead Lap):{~status=(+1 Lap)?status:nothing}}"),
        ("dnfReason",           "~{status=(Finished)?nothing:{~status=(+1 Lap)?nothing:status}}"),
        ("status",              "~{status=(Finished)?(Finished):{~status=(+1 Lap)?(Finished):(DNF)}}")
      )    
    )
    
    val attacher = JDotAttacher(Set(("results", "")))
    
    it("should be equals to ausF1Simple") {
      val transformed = jcTrans.transform(ausF1, List(JsonArrayTransformAttachment("Results", ausF1, resultTrans, attacher)));
      println(transformed)
      assert(parse(ausF1Simple) == parse(transformed));
    }
    
    val jcTrans2 = JDotTransformer(
      Set(
        ("season",             "season"),
        ("round",              "round^n"),
        ("finished",           "(true)^b"),    
        ("raceName",           "raceName"),
        ("circuit.name",       "Circuit.circuitName"),
        ("circuit.city",       "Circuit.Location.locality"),
        ("circuit.country",    "Circuit.Location.country"),
        ("podiumDetail[0].driverName", "Results[0].Driver|{givenName} {familyName}"),
        ("podiumDetail[0].points", "Results[0].points^n"),
        ("podiumDetail[0].team", "Results[0].Constructor.name"),
        ("podiumDetail[1].driverName", "Results[1].Driver|{givenName} {familyName}"),
        ("podiumDetail[1].points", "Results[1].points^n"),
        ("podiumDetail[1].team", "Results[1].Constructor.name"),
        ("podiumDetail[2].driverName", "Results[2].Driver|{givenName} {familyName}"),
        ("podiumDetail[2].points", "Results[2].points^n"),
        ("podiumDetail[2].team", "Results[2].Constructor.name")
      )    
    )
    
    val resultTrans2 = JDotTransformer(Set(("", "Driver.code")))
    
    it("should make an even simpler version") {
      val transformed = jcTrans2.transform(ausF1, List(JsonArrayTransformAttachment("Results", ausF1, resultTrans2, attacher)));
      println(transformed)
      assert(parse(ausF1ShortArray) == parse(transformed));
    }
  }
  
}