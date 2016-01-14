package com.superpixel.advokit.json.lift

import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper._

class JValueAttacherTest extends FlatSpec with Matchers with MockFactory {

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
  
  "JValueAttacher attachJValue" should "transform and attach json to a larger doc as specified by the pairs" in {
    
    val transFieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
    )
    
    val expected: JObject =
      ("hello" -> "world") ~
      ("when" -> "now") ~
      ("newVenue" -> "Stadium of Light")
    
    val attachTo: JObject = ("hello" -> "world") ~ ("when" -> "now")
    
    val attacherPairs = Set(JPathPair(JPath(JObjectPath("newVenue")), JPath(JObjectPath("venue"))))

    val attacher = JValueAttacher(JValueTransformer(transFieldMap), attacherPairs);
    
    assert(expected == attacher.attachJValue(jValsPremList(0), attachTo))
  }
  
  "JValueAttacher attachJValueList" should "transform and attach a list of json (treated as an array) to a larger doc" in {
    
    val transFieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
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
    
    val attacher = JValueAttacher(JValueTransformer(transFieldMap), attacherPairs);
    
    assert(expected == attacher.attachJValueList(jValsPremList, attachTo))
  }
  
  it should "be able to attach on array indexes" in {
    val transFieldMap = Set(
      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
    )
    
    val expected: JObject =
      ("hello" -> "world") ~
      ("when" -> "now") ~
      ("match1" -> ("game" -> "Sunderland vs. Stoke") ~ ("venue" -> "Stadium of Light") ~ ("score" -> "2 - 0") ~ ("winningTeam" -> "sunderland")) ~
      ("match2" -> ("game" -> "Man City vs. Southampton") ~ ("venue" -> "Etihad Stadium") ~ ("score" -> "3 - 1") ~ ("winningTeam" -> "man-city"))
    
    val attachTo: JObject = ("hello" -> "world") ~ ("when" -> "now")
    
    val attacherPairs = Set(JPathPair(JPath(JObjectPath("match1")), JPath(JArrayPath(0))), JPathPair(JPath(JObjectPath("match2")), JPath(JArrayPath(1))))
    
    val attacher = JValueAttacher(JValueTransformer(transFieldMap), attacherPairs);
    
    assert(expected == attacher.attachJValueList(jValsPremList, attachTo))
  }
}