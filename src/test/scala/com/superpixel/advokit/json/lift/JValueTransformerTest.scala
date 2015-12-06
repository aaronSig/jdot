package com.superpixel.advokit.json.lift

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._


class JValueTransformerTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {


  val jValsList: List[JValue] = {
      val buffSource = Source.fromURL(getClass.getResource("/pl-league-week-14.json"))
      val jsonLines = buffSource.getLines
      val temp = parse(jsonLines.mkString)
      buffSource.close()
      temp match {
        case JArray(ls) => ls
        case _ => List()
      }
  }
  
  val inclusionsMap: Map[String, JValue] = Map(
      "man-city" ->       ("leaguePosition" -> 1),
      "leicester" ->      ("leaguePosition" -> 2),
      "man-utd" ->        ("leaguePosition" -> 3),
      "arsenal" ->        ("leaguePosition" -> 4),
      "tottenham" ->      ("leaguePosition" -> 5),
      "liverpool" ->      ("leaguePosition" -> 6),
      "crystal-palace" -> ("leaguePosition" -> 7),
      "west-ham" ->       ("leaguePosition" -> 8),
      "everton" ->        ("leaguePosition" -> 9),
      "southampton" ->    ("leaguePosition" -> 10),
      "watford" ->        ("leaguePosition" -> 11),
      "stoke" ->          ("leaguePosition" -> 12),
      "west-brom" ->      ("leaguePosition" -> 13),
      "chelsea" ->        ("leaguePosition" -> 14),
      "swansea" ->        ("leaguePosition" -> 15),
      "norwich" ->        ("leaguePosition" -> 16),
      "sunderland" ->     ("leaguePosition" -> 17),
      "bournemouth" ->    ("leaguePosition" -> 18),
      "newcastle" ->      ("leaguePosition" -> 19),
      "aston-villa" ->    ("leaguePosition" -> 20)
  )
  
  "JValueTransformer transform" should "transform to simple object based on fieldMap" in {
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
    
    val returned = jValsList.map { jv => transfromer.transform(jv) }
    
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
    
    val transfromer = JValueTransformer(fieldMap)
    
//    val ret1 = transfromer.transform(jValsList(1))
//    assert(expected(0) == ret1)
//    println(pretty(render(ret1)))
//    
//    val ret2 = transfromer.transform(jValsList(2))
//    assert(expected(1) == ret2)
//    println(pretty(render(ret2)))
//    
//    val ret3 = transfromer.transform(jValsList(3))
//    assert(expected(2) == ret3)
//    println(pretty(render(ret3)))
//
//    val ret4 = transfromer.transform(jValsList(4))
//    assert(expected(3) == ret4)
//    println(pretty(render(ret4)))
    
    val returned = jValsList.map { jv => transfromer.transform(jv) }
    
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
    
    val transfromer = JValueTransformer(fieldMap, inclusionsMap)
    
    val returned = jValsList.map { jv => transfromer.transform(jv) }
    
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
    
    val inclusionsMapLocal: Map[String, JValue] = Map(
      "west-brom" ->      ("leaguePosition" -> -1),
      "chelsea" ->        ("leaguePosition" -> 100)
    )
    
    val transfromer = JValueTransformer(fieldMap, inclusionsMap)
    
    val returned = jValsList.map { jv => transfromer.transform(jv, inclusionsMapLocal) }
    
    returned.zipWithIndex.foreach { 
      case (retJv, index) => {
        assert(retJv == expected(index))
        println(pretty(render(retJv)))
      }
    }
    
  }
  
}