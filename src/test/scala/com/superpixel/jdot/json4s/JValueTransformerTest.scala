package com.superpixel.jdot.json4s

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.superpixel.jdot.pathing._
import com.superpixel.jdot._


class JValueTransformerTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {


  val jValsPremList: List[JValue] = {
      val buffSource = Source.fromURL(getClass.getResource("/pl-league-week-14.json"))
      val jsonLines = buffSource.getLines
      val temp = parse(jsonLines.mkString)
      buffSource.close()
      temp match {
        case JArray(ls) => ls
        case _ => List()
      }
  }
  
  val jValsChampLeagueList: List[JValue] = {
      val buffSource = Source.fromURL(getClass.getResource("/champ-league-dec9.json"))
      val jsonLines = buffSource.getLines
      val temp = parse(jsonLines.mkString)
      buffSource.close()
      temp match {
        case JArray(ls) => ls
        case _ => List()
      }
  }
  
  val inclusionsMap: Inclusions = FixedInclusions(Map(
      "man-city" ->       """{"leaguePosition": 1}""",
      "leicester" ->      """{"leaguePosition": 2}""",
      "man-utd" ->        """{"leaguePosition": 3}""",
      "arsenal" ->        """{"leaguePosition": 4}""",
      "tottenham" ->      """{"leaguePosition": 5}""",
      "liverpool" ->      """{"leaguePosition": 6}""",
      "crystal-palace" -> """{"leaguePosition": 7}""",
      "west-ham" ->       """{"leaguePosition": 8}""",
      "everton" ->        """{"leaguePosition": 9}""",
      "southampton" ->    """{"leaguePosition": 10}""",
      "watford" ->        """{"leaguePosition": 11}""",
      "stoke" ->          """{"leaguePosition": 12}""",
      "west-brom" ->      """{"leaguePosition": 13}""",
      "chelsea" ->        """{"leaguePosition": 14}""",
      "swansea" ->        """{"leaguePosition": 15}""",
      "norwich" ->        """{"leaguePosition": 16}""",
      "sunderland" ->     """{"leaguePosition": 17}""",
      "bournemouth" ->    """{"leaguePosition": 18}""",
      "newcastle" ->      """{"leaguePosition": 19}""",
      "aston-villa" ->    """{"leaguePosition": 20}"""
  ))
  
  "JValueTransformer transformJValue" should "transform to simple object based on fieldMap" in {
    val fieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
    )
    
    val expected: List[JValue] = List(
      ("game" -> "Sunderland vs. Stoke") ~ ("venue" -> "Stadium of Light") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "sunderland"),
      ("game" -> "Man City vs. Southampton") ~ ("venue" -> "Etihad Stadium") ~ ("score" -> "3 - 1") ~ ("winningTeam" -> "man-city"),
      ("game" -> "Crystal Palace vs. Newcastle") ~ ("venue" -> "Selhurst Park") ~ ("score" -> "5 - 1") ~ ("winningTeam" -> "crystal-palace"),
      ("game" -> "Bournemouth vs. Everton") ~ ("venue" -> "Vitality Stadium") ~ ("score" -> "3 - 3") ~ ("winningTeam" -> "draw"),
      ("game" -> "Aston Villa vs. Watford") ~ ("venue" -> "Villa Park") ~ ("score" -> "2 - 3") ~ ("winningTeam" -> "watford"),
      ("game" -> "Leicester vs. Man Utd") ~ ("venue" -> "King Power Stadium") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
      ("game" -> "Tottenham vs. Chelsea") ~ ("venue" -> "White Hart Lane") ~ ("score" -> "0 - 0") ~ ("winningTeam" -> "draw"),
      ("game" -> "West Ham vs. West Brom") ~ ("venue" -> "Boleyn Ground") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
      ("game" -> "Norwich vs. Arsenal") ~ ("venue" -> "Carrow Road") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
      ("game" -> "Liverpool vs. Swansea") ~ ("venue" -> "Anfield") ~ ("score" -> "1 - 0") ~ ("winningTeam" -> "liverpool")
    )
    
    val transfromer = JValueTransformer(fieldMap)
    
    val returned = jValsPremList.map { jv => transfromer.transformJValue(jv) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }
    
  }
  
  it should "be able to process arrays" in {
    val fieldMap = Set(
      JPathPair(JPath(JObjectPath("firstHomeGoal")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("homeGoalMinutes"), JArrayPath(0), JDefaultValue("N/A"))),
      JPathPair(JPath(JObjectPath("awayGoals")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("awayGoalMinutes"))),
      JPathPair(JPath(JObjectPath("teams"), JArrayPath(0)), JPath(JObjectPath("metadata"), JObjectPath("homeTeamName"))),
      JPathPair(JPath(JObjectPath("teams"), JArrayPath(1)), JPath(JObjectPath("metadata"), JObjectPath("awayTeamName")))
    )
    
    val expected: List[JValue] = List(
      ("firstHomeGoal" -> "82") ~ ("awayGoals" -> JNothing) ~ ("teams" -> List("Sunderland", "Stoke")),
      ("firstHomeGoal" -> "9") ~ ("awayGoals" -> List("49")) ~ ("teams" -> List("Man City", "Southampton")),
      ("firstHomeGoal" -> "14") ~ ("awayGoals" -> List("10")) ~ ("teams" -> List("Crystal Palace", "Newcastle")),
      ("firstHomeGoal" -> "80") ~ ("awayGoals" -> List("25", "36", "90+5")) ~ ("teams" -> List("Bournemouth", "Everton")),
      ("firstHomeGoal" -> "41") ~ ("awayGoals" -> List("17", "69", "85")) ~ ("teams" -> List("Aston Villa", "Watford")),
      ("firstHomeGoal" -> "24") ~ ("awayGoals" -> List("45+1")) ~ ("teams" -> List("Leicester", "Man Utd")),
      ("firstHomeGoal" -> "N/A") ~ ("awayGoals" -> JNothing) ~ ("teams" -> List("Tottenham", "Chelsea")),
      ("firstHomeGoal" -> "17") ~ ("awayGoals" -> List("50")) ~ ("teams" -> List("West Ham", "West Brom")),
      ("firstHomeGoal" -> "43") ~ ("awayGoals" -> List("30")) ~ ("teams" -> List("Norwich", "Arsenal")),
      ("firstHomeGoal" -> "62") ~ ("awayGoals" -> JNothing) ~ ("teams" -> List("Liverpool", "Swansea"))
    )
    
    val transfromer = JValueTransformer(fieldMap)

    
    val returned = jValsPremList.map { jv => transfromer.transformJValue(jv) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }

  }
  
  it should "be able to process arrays, with defaults" in {
    val defaultInJson = """{"eventResult": {"metadata": {"awayGoalMinutes":[]}}}"""
    val defaultOutJson = """{"firstHomeGoal":"N/A"}"""
    
    val fieldMap = Set(
      JPathPair(JPath(JObjectPath("firstHomeGoal")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("homeGoalMinutes"), JArrayPath(0))),
      JPathPair(JPath(JObjectPath("awayGoals")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("awayGoalMinutes"))),
      JPathPair(JPath(JObjectPath("teams"), JArrayPath(0)), JPath(JObjectPath("metadata"), JObjectPath("homeTeamName"))),
      JPathPair(JPath(JObjectPath("teams"), JArrayPath(1)), JPath(JObjectPath("metadata"), JObjectPath("awayTeamName")))
    )
    
    val expected: List[JValue] = List(
      ("firstHomeGoal" -> "82") ~ ("awayGoals" -> JArray(List())) ~ ("teams" -> List("Sunderland", "Stoke")),
      ("firstHomeGoal" -> "9") ~ ("awayGoals" -> List("49")) ~ ("teams" -> List("Man City", "Southampton")),
      ("firstHomeGoal" -> "14") ~ ("awayGoals" -> List("10")) ~ ("teams" -> List("Crystal Palace", "Newcastle")),
      ("firstHomeGoal" -> "80") ~ ("awayGoals" -> List("25", "36", "90+5")) ~ ("teams" -> List("Bournemouth", "Everton")),
      ("firstHomeGoal" -> "41") ~ ("awayGoals" -> List("17", "69", "85")) ~ ("teams" -> List("Aston Villa", "Watford")),
      ("firstHomeGoal" -> "24") ~ ("awayGoals" -> List("45+1")) ~ ("teams" -> List("Leicester", "Man Utd")),
      ("firstHomeGoal" -> "N/A") ~ ("awayGoals" -> JArray(List())) ~ ("teams" -> List("Tottenham", "Chelsea")),
      ("firstHomeGoal" -> "17") ~ ("awayGoals" -> List("50")) ~ ("teams" -> List("West Ham", "West Brom")),
      ("firstHomeGoal" -> "43") ~ ("awayGoals" -> List("30")) ~ ("teams" -> List("Norwich", "Arsenal")),
      ("firstHomeGoal" -> "62") ~ ("awayGoals" -> JArray(List())) ~ ("teams" -> List("Liverpool", "Swansea"))
    )
    
    val transfromer = JValueTransformer(fieldMap, MergingJsonPrePost(Seq(defaultInJson), Seq(defaultOutJson)))

    
    val returned = jValsPremList.map { jv => transfromer.transformJValue(jv) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }

  }
  
  it should "follow links to inclusions" in {
    val fieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode"))),
      JPathPair(JPath(JObjectPath("awayPositionAfter")), JPath(JObjectPath("metadata"), JObjectPath("awayTeamCode"), JPathLink, JObjectPath("leaguePosition")))
    )
    
    val expected: List[JValue] = List(
      ("game" -> "Sunderland vs. Stoke") ~ ("venue" -> "Stadium of Light") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "sunderland") ~ ("awayPositionAfter" -> 12),
      ("game" -> "Man City vs. Southampton") ~ ("venue" -> "Etihad Stadium") ~ ("score" -> "3 - 1") ~ ("winningTeam" -> "man-city") ~ ("awayPositionAfter" -> 10),
      ("game" -> "Crystal Palace vs. Newcastle") ~ ("venue" -> "Selhurst Park") ~ ("score" -> "5 - 1") ~ ("winningTeam" -> "crystal-palace") ~ ("awayPositionAfter" -> 19),
      ("game" -> "Bournemouth vs. Everton") ~ ("venue" -> "Vitality Stadium") ~ ("score" -> "3 - 3") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 9),
      ("game" -> "Aston Villa vs. Watford") ~ ("venue" -> "Villa Park") ~ ("score" -> "2 - 3") ~ ("winningTeam" -> "watford") ~ ("awayPositionAfter" -> 11),
      ("game" -> "Leicester vs. Man Utd") ~ ("venue" -> "King Power Stadium") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 3),
      ("game" -> "Tottenham vs. Chelsea") ~ ("venue" -> "White Hart Lane") ~ ("score" -> "0 - 0") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 14),
      ("game" -> "West Ham vs. West Brom") ~ ("venue" -> "Boleyn Ground") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 13),
      ("game" -> "Norwich vs. Arsenal") ~ ("venue" -> "Carrow Road") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 4),
      ("game" -> "Liverpool vs. Swansea") ~ ("venue" -> "Anfield") ~ ("score" -> "1 - 0") ~ ("winningTeam" -> "liverpool") ~ ("awayPositionAfter" -> 15)
    )
    
    val transfromer = JValueTransformer(fieldMap, inclusions=inclusionsMap)
    
    val returned = jValsPremList.map { jv => transfromer.transformJValue(jv) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }
    
  }
  
  it should "follow links to passed inclusions first" in {
    val fieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode"))),
      JPathPair(JPath(JObjectPath("awayPositionAfter")), JPath(JObjectPath("metadata"), JObjectPath("awayTeamCode"), JPathLink, JObjectPath("leaguePosition")))
    )
    
    val expected: List[JValue] = List(
      ("game" -> "Sunderland vs. Stoke") ~ ("venue" -> "Stadium of Light") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "sunderland") ~ ("awayPositionAfter" -> 12),
      ("game" -> "Man City vs. Southampton") ~ ("venue" -> "Etihad Stadium") ~ ("score" -> "3 - 1") ~ ("winningTeam" -> "man-city") ~ ("awayPositionAfter" -> 10),
      ("game" -> "Crystal Palace vs. Newcastle") ~ ("venue" -> "Selhurst Park") ~ ("score" -> "5 - 1") ~ ("winningTeam" -> "crystal-palace") ~ ("awayPositionAfter" -> 19),
      ("game" -> "Bournemouth vs. Everton") ~ ("venue" -> "Vitality Stadium") ~ ("score" -> "3 - 3") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 9),
      ("game" -> "Aston Villa vs. Watford") ~ ("venue" -> "Villa Park") ~ ("score" -> "2 - 3") ~ ("winningTeam" -> "watford") ~ ("awayPositionAfter" -> 11),
      ("game" -> "Leicester vs. Man Utd") ~ ("venue" -> "King Power Stadium") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 3),
      ("game" -> "Tottenham vs. Chelsea") ~ ("venue" -> "White Hart Lane") ~ ("score" -> "0 - 0") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 100),
      ("game" -> "West Ham vs. West Brom") ~ ("venue" -> "Boleyn Ground") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> -1),
      ("game" -> "Norwich vs. Arsenal") ~ ("venue" -> "Carrow Road") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw") ~ ("awayPositionAfter" -> 4),
      ("game" -> "Liverpool vs. Swansea") ~ ("venue" -> "Anfield") ~ ("score" -> "1 - 0") ~ ("winningTeam" -> "liverpool") ~ ("awayPositionAfter" -> 15)
    )
    
    val inclusionsMapLocal: Inclusions = FixedInclusions(Map(
      "west-brom" ->      """{"leaguePosition": -1}""",
      "chelsea" ->        """{"leaguePosition": 100}"""
    ))
    
    val transfromer = JValueTransformer(fieldMap, inclusions=inclusionsMap)
    
    val returned = jValsPremList.map { jv => transfromer.transformJValue(jv, additionalInclusions=inclusionsMapLocal) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }
    
  }
  
  it should "merge in defaults" in {
    val jsonInDefault = 
      """{   
          "eventResult": {
              "resultDate": "2015-12-09T22:35:00Z",
              "eventVoid": false,
              "finalResult": false,
              "metadata": {
                  "delayedScore": true,
                  "half": "FT",
                  "loserCode": "unknown",
                  "matchMinute": 90,
                  "draw": false,
                  "homeGoals": -1,
                  "winnerCode": "unknown",
                  "score": "unknown",
                  "awayWin": false,
                  "mmliveid": "unknown",
                  "fulltime": true,
                  "homeWin": false,
                  "awayGoals": -1
              }
            }
      }"""
    
    val jsonOutDefault = """{"venue": "unknown"}"""
    
     val fieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
    )
    
    val expected: List[JValue] = List(
      ("game" -> "Dynamo Kiev vs. M'bi Tel-Aviv") ~ ("venue" -> "unknown") ~ ("score" -> "unknown") ~ ("winningTeam" -> "unknown"),
      ("game" -> "Chelsea vs. FC Porto") ~ ("venue" -> "Stamford Bridge") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "chelsea"),
      ("game" -> "Olympiakos vs. Arsenal") ~ ("venue" -> "unknown") ~ ("score" -> "0 - 3") ~ ("winningTeam" -> "arsenal"),
      ("game" -> "Dinamo Zagreb vs. Bayern Mun") ~ ("venue" -> "unknown") ~ ("score" -> "0 - 2") ~ ("winningTeam" -> "bayern-mun"),
      ("game" -> "Roma vs. BATE Bor") ~ ("venue" -> "Stadio Olimpico") ~ ("score" -> "unknown") ~ ("winningTeam" -> "unknown"),
      ("game" -> "Bayer Levkn vs. Barcelona") ~ ("venue" -> "BayArena") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
      ("game" -> "Valencia vs. Lyon") ~ ("venue" -> "unknown") ~ ("score" -> "0 - 2") ~ ("winningTeam" -> "lyon"),
      ("game" -> "KAA Gent vs. Zenit St P") ~ ("venue" -> "unknown") ~ ("score" -> "2 - 1") ~ ("winningTeam" -> "kaa-gent")
    )
    
    val transfromer = JValueTransformer(fieldMap, MergingJsonPrePost(Seq(jsonInDefault), Seq(jsonOutDefault)))
    
    val returned = jValsChampLeagueList.map { jv => transfromer.transformJValue(jv) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }
  }
  
  it should "merge in local defaults before global defaults" in {
    val jsonInDefault = 
      """{   
          "eventResult": {
              "resultDate": "2015-12-09T22:35:00Z",
              "eventVoid": false,
              "finalResult": false,
              "metadata": {
                  "delayedScore": true,
                  "half": "FT",
                  "matchMinute": 90,
                  "draw": false,
                  "homeGoals": -1,
                  "score": "unknown",
                  "awayWin": false,
                  "mmliveid": "unknown",
                  "fulltime": true,
                  "homeWin": false,
                  "awayGoals": -1
              }
            }
      }"""
    
    val jsonOutDefault = """{"venue": "unknown", "winningTeam":"unknown"}"""
    
    val localJsonInDefault = List(
      """{"eventResult":{"metadata":{"score":"1 - 0"}}}""",
      """{"eventResult":{"metadata":{"score":"thisiswrong"}}}""",
      """{"eventResult":{"metadata":{"score":"thisiswrong"}}}""",
      """{"eventResult":{"metadata":{"score":"thisiswrong"}}}""",
      """{"eventResult":{"metadata":{"score":"0 - 0"}}}""",
      """{"eventResult":{"metadata":{"score":"thisiswrong"}}}""",
      """{"eventResult":{"metadata":{"score":"thisiswrong"}}}""",
      """{"eventResult":{"metadata":{"score":"thisiswrong"}}}"""
    )
    
    
    val localJsonOutDefault = List(
      """{"winningTeam":"dynamo-kiev"}""",
      """{"winningTeam":"thisiswrong"}""",
      """{"winningTeam":"thisiswrong"}""",
      """{"winningTeam":"thisiswrong"}""",
      """{"winningTeam":"draw"}""",
      """{"winningTeam":"thisiswrong"}""",
      """{"winningTeam":"thisiswrong"}""",
      """{"winningTeam":"thisiswrong"}"""
    )
    
    val fieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
    )
    
    val expected: List[JValue] = List(
      ("game" -> "Dynamo Kiev vs. M'bi Tel-Aviv") ~ ("venue" -> "unknown") ~ ("score" -> "1 - 0") ~ ("winningTeam" -> "dynamo-kiev"),
      ("game" -> "Chelsea vs. FC Porto") ~ ("venue" -> "Stamford Bridge") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "chelsea"),
      ("game" -> "Olympiakos vs. Arsenal") ~ ("venue" -> "unknown") ~ ("score" -> "0 - 3") ~ ("winningTeam" -> "arsenal"),
      ("game" -> "Dinamo Zagreb vs. Bayern Mun") ~ ("venue" -> "unknown") ~ ("score" -> "0 - 2") ~ ("winningTeam" -> "bayern-mun"),
      ("game" -> "Roma vs. BATE Bor") ~ ("venue" -> "Stadio Olimpico") ~ ("score" -> "0 - 0") ~ ("winningTeam" -> "draw"),
      ("game" -> "Bayer Levkn vs. Barcelona") ~ ("venue" -> "BayArena") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
      ("game" -> "Valencia vs. Lyon") ~ ("venue" -> "unknown") ~ ("score" -> "0 - 2") ~ ("winningTeam" -> "lyon"),
      ("game" -> "KAA Gent vs. Zenit St P") ~ ("venue" -> "unknown") ~ ("score" -> "2 - 1") ~ ("winningTeam" -> "kaa-gent")
    )
    
    val transfromer = JValueTransformer(fieldMap, MergingJsonPrePost(Seq(jsonInDefault), Seq(jsonOutDefault)))
    
    val returned = jValsChampLeagueList.zipWithIndex.map { case (jv, i) => 
      transfromer.transformJValue(jv, localMerges=MergingJsonPrePost(Seq(localJsonInDefault(i)), Seq(localJsonOutDefault(i)))) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }
  }
  
}