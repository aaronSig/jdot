package com.superpixel.advokit.json.lift

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import scala.io.Source
import com.superpixel.advokit.json.pathing._

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



class JValueMapperTest extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {
  
  val jsonList: List[String] = {
	    val buffSource = Source.fromURL(getClass.getResource("/pl-league-week-14.json"))

      import org.json4s._
      import org.json4s.native.JsonMethods._

    
      val jsonLines = buffSource.getLines
      val temp = parse(jsonLines.mkString)
      buffSource.close()
      temp match {
        case JArray(ls) => ls.map { ja => compact(render(ja))}
        case _ => List()
      }
  }
  
  val compRoundJson = """ {
            "id": 34,
            "code": "week-14",
            "name": "Week 14",
            "startDate": "2015-11-27T00:00:00Z",
            "endDate": "2015-12-03T23:59:59Z"
        } """
  
  val inclusionsMap: Map[String, String] = Map(
      "man-city" ->       """{"leaguePosition": 1}""",
      "leicester" ->      """{"leaguePosition": 2}""",
      "man-utd" ->        """{"leaguePosition": 3}""",
      "arsenal" ->        """{"leaguePosition": 4}""",
      "tottenham" ->      """{"leaguePosition": 5}""",
      "liverpool" ->      """{"leaguePosition": 6}""",
      "crystal-palace" -> """{"leaguePosition": 7}""",
      "west-ham" ->       """{"leaguePosition": 8}""",
      "everton" ->        """{"leaguePosition": 9}""",
      "southampton" ->    """{"leaguePosition" :10}""",
      "watford" ->        """{"leaguePosition" :11}""",
      "stoke" ->          """{"leaguePosition" :12}""",
      "west-brom" ->      """{"leaguePosition" :13}""",
      "chelsea" ->        """{"leaguePosition" :14}""",
      "swansea" ->        """{"leaguePosition" :15}""",
      "norwich" ->        """{"leaguePosition" :16}""",
      "sunderland" ->     """{"leaguePosition" :17}""",
      "bournemouth" ->    """{"leaguePosition" :18}""",
      "newcastle" ->      """{"leaguePosition" :19}""",
      "aston-villa" ->    """{"leaguePosition" :20}"""
  )
  
  "JValueMapper map" should "map to a simple class based on fieldMap" in {
    
      val fieldMap = Set(
        JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
        JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
        JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
        JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
      )
      
      val expected = List(
        new MatchWithResult("Sunderland vs. Stoke", "Stadium of Light", "2 - 0", "sunderland"),
        new MatchWithResult("Man City vs. Southampton", "Etihad Stadium", "3 - 1", "man-city"),
        new MatchWithResult("Crystal Palace vs. Newcastle", "Selhurst Park", "5 - 1", "crystal-palace"),
        new MatchWithResult("Bournemouth vs. Everton", "Vitality Stadium", "3 - 3", "draw"),
        new MatchWithResult("Aston Villa vs. Watford", "Villa Park", "2 - 3", "watford"),
        new MatchWithResult("Leicester vs. Man Utd", "King Power Stadium", "1 - 1", "draw"),
        new MatchWithResult("Tottenham vs. Chelsea", "White Hart Lane", "0 - 0", "draw"),
        new MatchWithResult("West Ham vs. West Brom", "Boleyn Ground", "1 - 1", "draw"),
        new MatchWithResult("Norwich vs. Arsenal", "Carrow Road", "1 - 1", "draw"),
        new MatchWithResult("Liverpool vs. Swansea", "Anfield", "1 - 0", "liverpool")    
      )
      

      val mapper = JValueMapper[MatchWithResult](fieldMap)
      
      val returned = jsonList.map { json => mapper.map(json) }
      
      returned.zipWithIndex.foreach { 
        case (ret, index) => {
          assert(ret == expected(index))
          println(ret)
          println(expected(index))
        }
      }
  }
  
//  "JValueMapper mapWithAttachment" should "return an interface with will attach and extract" in {
//    
//    val compRoundMapping = Set(
//      JPathPair(JPath(JObjectPath("weekName")), JPath(JObjectPath("name")))
//    )
//    
//    val matchResultMapping = Set(
//      JPathPair(JPath(JObjectPath("game")), JPath(JObjectPath("name"))),
//      JPathPair(JPath(JObjectPath("venue")), JPath(JObjectPath("metadata"), JObjectPath("venue"))),
//      JPathPair(JPath(JObjectPath("score")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("score"))),
//      JPathPair(JPath(JObjectPath("winningTeam")), JPath(JObjectPath("eventResult"), JObjectPath("metadata"), JObjectPath("winnerCode")))
//    )
//    
//    val attacherPairs = Set(
//      JPathPair(JPath(JObjectPath("matchList")), JPath())
//    )
//      
//    val expected = new Weekend("Week 14", List(
//      new MatchWithResult("Sunderland vs. Stoke", "Stadium of Light", "2 - 0", "sunderland"),
//      new MatchWithResult("Man City vs. Southampton", "Etihad Stadium", "3 - 1", "man-city"),
//      new MatchWithResult("Crystal Palace vs. Newcastle", "Selhurst Park", "5 - 1", "crystal-palace"),
//      new MatchWithResult("Bournemouth vs. Everton", "Vitality Stadium", "3 - 3", "draw"),
//      new MatchWithResult("Aston Villa vs. Watford", "Villa Park", "2 - 3", "watford"),
//      new MatchWithResult("Leicester vs. Man Utd", "King Power Stadium", "1 - 1", "draw"),
//      new MatchWithResult("Tottenham vs. Chelsea", "White Hart Lane", "0 - 0", "draw"),
//      new MatchWithResult("West Ham vs. West Brom", "Boleyn Ground", "1 - 1", "draw"),
//      new MatchWithResult("Norwich vs. Arsenal", "Carrow Road", "1 - 1", "draw"),
//      new MatchWithResult("Liverpool vs. Swansea", "Anfield", "1 - 0", "liverpool")    
//    ))
//    
//    val mapper = JValueMapper[Weekend](compRoundMapping)
//    val smAttacher = JValueAttacher(JValueTransformer(matchResultMapping), attacherPairs)
//    
//    val mapperWithAttachment: JsonContentMapperWithAttacher[Weekend] = mapper.withAttacher(classOf[MatchWithResult], smAttacher)
//    
//    val returned = mapperWithAttachment.mapWithListAttachment(jsonList, compRoundJson)
//    
//    println(returned)
//    assert(returned == expected)
//    
//  }
  
  
  
}