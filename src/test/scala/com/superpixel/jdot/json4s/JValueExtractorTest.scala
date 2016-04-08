package com.superpixel.jdot.json4s

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

trait Match

class MatchWithResult(val game: String, val venue: String, val score: String, val winningTeam: String) extends Match {
  override def equals(that: Any): Boolean = that match {
    case that: MatchWithResult => 
      this.game == that.game &&
      this.venue == that.venue &&
      this.score == that.score &&
      this.winningTeam == that.winningTeam      
  }
  override def toString: String = s"Game: $game, Venue: $venue, Score: $score, Winning Team: $winningTeam."
}

class MatchFixture(val game: String, val venue: String, val startDate: String) extends Match {
  override def equals(that: Any): Boolean = that match {
    case that: MatchFixture => 
      this.game == that.game &&
      this.venue == that.venue &&
      this.startDate == that.startDate
  }
  override def toString: String = s"Game: $game, Venue: $venue, Start Date: $startDate."
}

class Weekend(val weekName: String, val matchList: List[Match]) {
  override def equals(that: Any): Boolean = that match {
    case that: Weekend => 
      this.matchList == that.matchList &&
      this.weekName == that.weekName
  }
  override def toString: String = s"Week Name: $weekName, Match List: " + matchList.mkString("\n");
}

class MatchPair(val matchOne: Match, val matchTwo: Match) {
  override def equals(that: Any): Boolean = that match {
    case that: MatchPair => 
      this.matchOne == that.matchOne &&
      this.matchTwo == that.matchTwo    
  }
  override def toString: String = s"Match One: $matchOne, Match Two: $matchTwo."
}

class JValueExtractorTest extends FlatSpec with Matchers {
  

  "JValueExtractor lambda apply" should "take a function that extracts from json" in {

import com.superpixel.jdot.json4s.JValueExtractor;

import scala.language.implicitConversions
	  implicit def stringFromJValue(json: JValue): String = json match {
	  case JString(s) => s
	  case _ => compact(render(json))
	  }
    
    val extract = (json: JValue) => json match {
      case jObj: JObject => {
        new MatchFixture(jObj \ "game", jObj \ "venue", "2017-03-02")
      }
      case _ => new MatchFixture("UNKNOWN", "UNKNOWN", "UNKNOWN");
    }
    
    val extractor = JValueExtractor(extract)
    
    val expected = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val jVal = ("game" -> expected.game) ~ ("venue" -> expected.venue) ~ ("date" -> "???")
    
    assert(expected == extractor.extractFromJValue(jVal))
  }
  
  "JValueExtractor default apply" should "be able to extract on a field identity basis" in {
    
    val extractor = JValueExtractor[MatchFixture]()
    
    val expected = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val jVal = ("game" -> expected.game) ~ ("venue" -> expected.venue) ~ ("startDate" -> expected.startDate)
    
    assert(expected == extractor.extractFromJValue(jVal))
  }
  
  it should "be able to extract on a field identity basis with type hints" in {
    
    val extractor = JValueExtractor[MatchPair](List(classOf[MatchFixture], classOf[MatchWithResult]))
    
    val matchOne = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val matchTwo = new MatchWithResult("Chelsea vs Arsenal", "Chelsea", "1-0", "Chelsea")
    val expected = new MatchPair(matchOne, matchTwo)
    val jVal = ("matchOne" -> (("_t" -> matchOne.getClass.getSimpleName) ~ ("game" -> matchOne.game) ~ ("venue" -> matchOne.venue) ~ ("startDate" -> matchOne.startDate))) ~
               ("matchTwo" -> (("_t" -> matchTwo.getClass.getSimpleName) ~ ("game" -> matchTwo.game) ~ ("venue" -> matchTwo.venue) ~ ("score" -> matchTwo.score) ~ ("winningTeam" -> matchTwo.winningTeam)))
    
    assert(expected == extractor.extractFromJValue(jVal))
  }
  
  it should "be able to extract on a field identity basis with type hints and custom typeHintFieldName" in {
    
    val extractor = JValueExtractor[MatchPair](List(classOf[MatchFixture], classOf[MatchWithResult]), typeHintFieldName = "type")
    
    val matchOne = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val matchTwo = new MatchWithResult("Chelsea vs Arsenal", "Chelsea", "1-0", "Chelsea")
    val expected = new MatchPair(matchOne, matchTwo)
    val jVal = ("matchOne" -> (("type" -> matchOne.getClass.getSimpleName) ~ ("game" -> matchOne.game) ~ ("venue" -> matchOne.venue) ~ ("startDate" -> matchOne.startDate))) ~
               ("matchTwo" -> (("type" -> matchTwo.getClass.getSimpleName) ~ ("game" -> matchTwo.game) ~ ("venue" -> matchTwo.venue) ~ ("score" -> matchTwo.score) ~ ("winningTeam" -> matchTwo.winningTeam)))
    
    assert(expected == extractor.extractFromJValue(jVal))
    
    val expectedRev = new MatchPair(matchTwo, matchOne)
    val jValRev = ("matchTwo" -> (("type" -> matchOne.getClass.getSimpleName) ~ ("game" -> matchOne.game) ~ ("venue" -> matchOne.venue) ~ ("startDate" -> matchOne.startDate))) ~
               ("matchOne" -> (("type" -> matchTwo.getClass.getSimpleName) ~ ("game" -> matchTwo.game) ~ ("venue" -> matchTwo.venue) ~ ("score" -> matchTwo.score) ~ ("winningTeam" -> matchTwo.winningTeam)))
    
    assert(expectedRev == extractor.extractFromJValue(jValRev))
  }
  
  "JValueExtractor forClass" should "be able to extract on a field identity basis" in {
    
    val extractor = JValueExtractor.forClass(classOf[MatchFixture])
    
    val expected = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val jVal = ("game" -> expected.game) ~ ("venue" -> expected.venue) ~ ("startDate" -> expected.startDate)
    
    assert(expected == extractor.extractFromJValue(jVal))
  }
  
  it should "be able to extract on a field identity basis with type hints" in {
    
    val extractor = JValueExtractor.forClass(classOf[MatchPair], List(classOf[MatchFixture], classOf[MatchWithResult]))
    
    val matchOne = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val matchTwo = new MatchWithResult("Chelsea vs Arsenal", "Chelsea", "1-0", "Chelsea")
    val expected = new MatchPair(matchOne, matchTwo)
    val jVal = ("matchOne" -> (("_t" -> matchOne.getClass.getSimpleName) ~ ("game" -> matchOne.game) ~ ("venue" -> matchOne.venue) ~ ("startDate" -> matchOne.startDate))) ~
               ("matchTwo" -> (("_t" -> matchTwo.getClass.getSimpleName) ~ ("game" -> matchTwo.game) ~ ("venue" -> matchTwo.venue) ~ ("score" -> matchTwo.score) ~ ("winningTeam" -> matchTwo.winningTeam)))
    
    assert(expected == extractor.extractFromJValue(jVal))
  }
  
  it should "be able to extract on a field identity basis with type hints and custom typeHintFieldName" in {
    
    val extractor = JValueExtractor.forClass(classOf[MatchPair], List(classOf[MatchFixture], classOf[MatchWithResult]), typeHintFieldName = "type")
    
    val matchOne = new MatchFixture("Man United vs Man City", "Man U", "2017-03-02")
    val matchTwo = new MatchWithResult("Chelsea vs Arsenal", "Chelsea", "1-0", "Chelsea")
    val expected = new MatchPair(matchOne, matchTwo)
    val jVal = ("matchOne" -> (("type" -> matchOne.getClass.getSimpleName) ~ ("game" -> matchOne.game) ~ ("venue" -> matchOne.venue) ~ ("startDate" -> matchOne.startDate))) ~
               ("matchTwo" -> (("type" -> matchTwo.getClass.getSimpleName) ~ ("game" -> matchTwo.game) ~ ("venue" -> matchTwo.venue) ~ ("score" -> matchTwo.score) ~ ("winningTeam" -> matchTwo.winningTeam)))
    
    assert(expected == extractor.extractFromJValue(jVal))
    
    val expectedRev = new MatchPair(matchTwo, matchOne)
    val jValRev = ("matchTwo" -> (("type" -> matchOne.getClass.getSimpleName) ~ ("game" -> matchOne.game) ~ ("venue" -> matchOne.venue) ~ ("startDate" -> matchOne.startDate))) ~
               ("matchOne" -> (("type" -> matchTwo.getClass.getSimpleName) ~ ("game" -> matchTwo.game) ~ ("venue" -> matchTwo.venue) ~ ("score" -> matchTwo.score) ~ ("winningTeam" -> matchTwo.winningTeam)))
    
    assert(expectedRev == extractor.extractFromJValue(jValRev))
  }
  
}