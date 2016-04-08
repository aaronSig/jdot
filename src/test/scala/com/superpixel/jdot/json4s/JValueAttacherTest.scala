package com.superpixel.jdot.json4s

import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.superpixel.jdot.pathing._

class JValueAttacherTest extends FlatSpec with Matchers with MockFactory {

  
  "JValueAttacher attachJValue" should "attach json to a larger doc as specified by the pairs" in {
    
    val joining: JObject = 
      ("one" -> "two") ~
      ("three" -> 
        ("four" -> "five") ~ 
        ("six" -> "seven")
      )
    val expected: JObject =
      ("hello" -> "world") ~
      ("when" -> "now") ~
      ("join" -> 
        ("four" -> "five") ~ 
        ("six" -> "seven")
      )
    
    val attachTo: JObject = ("hello" -> "world") ~ ("when" -> "now")
    
    val attacherPairs = Set(JPathPair(JPath(JObjectPath("join")), JPath(JObjectPath("three"))))

    val attacher = JValueAttacher(attacherPairs);
    
    assert(expected == attacher.attachJValue(joining, attachTo))
  }
  
  "JValueAttacher attachJValueList" should "attach a list of json (treated as an array) to a larger doc" in {
    
    val ls: List[JValue] = List(
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
    
    val expected: JObject =
      ("hello" -> "world") ~
      ("when" -> "now") ~
      ("children" -> List(
        ("game" -> "Sunderland vs. Stoke") ~ ("venue" -> "Stadium of Light") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "sunderland"),
        ("game" -> "Man City vs. Southampton") ~ ("venue" -> "Etihad Stadium") ~ ("score" -> "3 - 1") ~ ("winningTeam" -> "man-city"),
        ("game" -> "Crystal Palace vs. Newcastle") ~ ("venue" -> "Selhurst Park") ~ ("score" -> "5 - 1") ~ ("winningTeam" -> "crystal-palace"),
        ("game" -> "Bournemouth vs. Everton") ~ ("venue" -> "Vitality Stadium") ~ ("score" -> "3 - 3") ~ ("winningTeam" -> "draw"),
        ("game" -> "Aston Villa vs. Watford") ~ ("venue" -> "Villa Park") ~ ("score" -> "2 - 3") ~ ("winningTeam" -> "watford"),
        ("game" -> "Leicester vs. Man Utd") ~ ("venue" -> "King Power Stadium") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
        ("game" -> "Tottenham vs. Chelsea") ~ ("venue" -> "White Hart Lane") ~ ("score" -> "0 - 0") ~ ("winningTeam" -> "draw"),
        ("game" -> "West Ham vs. West Brom") ~ ("venue" -> "Boleyn Ground") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
        ("game" -> "Norwich vs. Arsenal") ~ ("venue" -> "Carrow Road") ~ ("score" -> "1 - 1") ~ ("winningTeam" -> "draw"),
        ("game" -> "Liverpool vs. Swansea") ~ ("venue" -> "Anfield") ~ ("score" -> "1 - 0") ~ ("winningTeam" -> "liverpool"))
      )
    
    val attachTo: JObject = ("hello" -> "world") ~ ("when" -> "now")
    
    val attacherPairs = Set(JPathPair(JPath(JObjectPath("children")), JPath()))
    
    val attacher = JValueAttacher(attacherPairs);
    
    assert(expected == attacher.attachJValueList(ls, attachTo))
  }
  
  it should "be able to attach on array indexes" in {
    val ls: List[JValue] = List(
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
    
    val expected: JObject =
      ("hello" -> "world") ~
      ("when" -> "now") ~
      ("match1" -> ("game" -> "Sunderland vs. Stoke") ~ ("venue" -> "Stadium of Light") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "sunderland")) ~
      ("match2" -> ("game" -> "Man City vs. Southampton") ~ ("venue" -> "Etihad Stadium") ~ ("score" -> "3 - 1") ~ ("winningTeam" -> "man-city"))
    
    val attachTo: JObject = ("hello" -> "world") ~ ("when" -> "now")
    
    val attacherPairs = Set(JPathPair(JPath(JObjectPath("match1")), JPath(JArrayPath(0))), JPathPair(JPath(JObjectPath("match2")), JPath(JArrayPath(1))))
    
    val attacher = JValueAttacher(attacherPairs);
    
    assert(expected == attacher.attachJValueList(ls, attachTo))
  }
}