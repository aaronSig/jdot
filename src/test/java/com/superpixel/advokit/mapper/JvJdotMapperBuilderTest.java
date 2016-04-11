package com.superpixel.advokit.mapper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import com.superpixel.advokit.mapper.fixtures.GoalsHolder;
import com.superpixel.advokit.mapper.fixtures.MatchPairJava;
import com.superpixel.advokit.mapper.fixtures.MatchFixtureJava;
import com.superpixel.advokit.mapper.fixtures.SimpleMatchJava;

public class JvJdotMapperBuilderTest {
	
	static private List<String> jsonList = new LinkedList<>();
	
	@BeforeClass
	public static void beforeAll() {

		//Get file from resources folder
		ClassLoader classLoader = JvJdotMapperBuilderTest.class.getClassLoader();
		File file = new File(classLoader.getResource("pl-league-week-14.json").getFile());

		try (Scanner scanner = new Scanner(file)) {
			if (scanner.hasNextLine()) scanner.nextLine();
			
			int[] breakArray = {128, 259, 392, 525, 657, 786, 910, 1039, 1168, 1294};
			int breakIndex = 0;
			int lineIndex = 1;
			StringBuilder result = new StringBuilder("");
			
			while (scanner.hasNextLine()) {
				lineIndex++;
				String line = scanner.nextLine();
				if (scanner.hasNextLine()) {
					if (lineIndex == breakArray[breakIndex]) {
						result.append("}");
						jsonList.add(result.toString());
						breakIndex++;
						result = new StringBuilder("");
					} else {						
						result.append(line);
					}
				}
			}

			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void simpleMatchBuilderTest() {
		
		Map<String, String> pathMap = new HashMap<>();
		pathMap.put("game", "name");
		pathMap.put("venue", "metadata.venue");
		pathMap.put("score", "eventResult.metadata.score");
		pathMap.put("winningTeam", "eventResult.metadata.winnerCode");
		
		JvJdotMapperBuilder builder = new JvJdotMapperBuilder().withPathMapping(pathMap);
		
		JvJdotMapper<SimpleMatchJava> mapper = builder.build(SimpleMatchJava.class);
		
		System.out.println("WOOOO:" + mapper.map(jsonList.get(0)));
		assertEquals(new SimpleMatchJava("Sunderland vs. Stoke", Optional.of("Stadium of Light"), "2 - 0", "sunderland"), 
					mapper.map(jsonList.get(0)));
		assertEquals(new SimpleMatchJava("Man City vs. Southampton", Optional.of("Etihad Stadium"), "3 - 1", "man-city"), 
					mapper.map(jsonList.get(1)));
		assertEquals(new SimpleMatchJava("Crystal Palace vs. Newcastle", Optional.of("Selhurst Park"), "5 - 1", "crystal-palace"), 
					mapper.map(jsonList.get(2)));
		assertEquals(new SimpleMatchJava("Bournemouth vs. Everton", Optional.of("Vitality Stadium"), "3 - 3", "draw"), 
					mapper.map(jsonList.get(3)));
		assertEquals(new SimpleMatchJava("Aston Villa vs. Watford", Optional.of("Villa Park"), "2 - 3", "watford"), 
					mapper.map(jsonList.get(4)));
		assertEquals(new SimpleMatchJava("Leicester vs. Man Utd", Optional.of("King Power Stadium"), "1 - 1", "draw"), 
					mapper.map(jsonList.get(5)));
		assertEquals(new SimpleMatchJava("Tottenham vs. Chelsea", Optional.of("White Hart Lane"), "0 - 0", "draw"), 
					mapper.map(jsonList.get(6)));
		assertEquals(new SimpleMatchJava("West Ham vs. West Brom", Optional.of("Boleyn Ground"), "1 - 1", "draw"), 
					mapper.map(jsonList.get(7)));
		assertEquals(new SimpleMatchJava("Norwich vs. Arsenal", Optional.of("Carrow Road"), "1 - 1", "draw"), 
					mapper.map(jsonList.get(8)));
		assertEquals(new SimpleMatchJava("Liverpool vs. Swansea", Optional.of("Anfield"), "1 - 0", "liverpool"), 
					mapper.map(jsonList.get(9)));
		
		System.out.println(jsonList.get(0));
		System.out.println(mapper.map(jsonList.get(0)));
	}
	
	@Test
	public void simpleMatchLambdaTest() {
		
		Map<String, String> pathMap = new HashMap<>();
		pathMap.put("game", "name");
		pathMap.put("venue", "metadata.venue");
		pathMap.put("score", "eventResult.metadata.score");
		pathMap.put("winningTeam", "eventResult.metadata.winnerCode");
		
		JvJdotMapperBuilder builder = new JvJdotMapperBuilder().withPathMapping(pathMap);
		
		JvJdotMapper<SimpleMatchJava> mapper = builder.build((String json) -> {
			System.out.println("In lambda! : " + json);
			return new SimpleMatchJava("Sunderland vs. Stoke", Optional.of("Stadium of Light"), "2 - 0", "sunderland");
		});
		
		
		assertEquals(new SimpleMatchJava("Sunderland vs. Stoke", Optional.of("Stadium of Light"), "2 - 0", "sunderland"), 
				mapper.map(jsonList.get(0)));
	}
	
	
	@Test
	public void goalHolderBuilderTest() {
		Map<String, String> pathMap = new HashMap<>();
		//Match
		pathMap.put("match.game", "name");
		pathMap.put("match.venue", "metadata.venue");
		pathMap.put("match.score", "eventResult.metadata.score");
		pathMap.put("match.winningTeam", "eventResult.metadata.winnerCode");
		
		//AwayGoals
		pathMap.put("awayGoals", "eventResult.metadata.awayGoalMinutes");
		
		//FirstHomeGoal
		pathMap.put("firstHomeGoal", "eventResult.metadata.homeGoalMinutes[0](N/A)");
		
		//ScoreNames
		pathMap.put("scoreNames[0]", "eventResult.metadata.homeScorers");
		pathMap.put("scoreNames[1]", "eventResult.metadata.awayScorers");
		
		
		JvJdotMapperBuilder builder = new JvJdotMapperBuilder().withPathMapping(pathMap);
		JvJdotMapper<GoalsHolder> mapper = builder.build(GoalsHolder.class);
		
		List<String> ghag = new ArrayList<>();
		ghag.add("25");
		ghag.add("36");
		ghag.add("90+5");
		List<String> ghsn = new ArrayList<>();
		ghsn.add("Adam Smith (80) Junior Stanislas (87, 90+8)");
		ghsn.add("Ramiro Funes Mori (25) Romelu Lukaku (36) Ross Barkley (90+5)");
		GoalsHolder gh = new GoalsHolder(
				new SimpleMatchJava("Bournemouth vs. Everton", Optional.of("Vitality Stadium"), "3 - 3", "draw"),
				ghag,"80", ghsn);
		
		GoalsHolder ret = mapper.map(jsonList.get(3));
		
		assertEquals(gh, ret);
		
		System.out.println(jsonList.get(3));
		System.out.println(ret);
	
	}
	
	@Test
	public void goalHolderWithMissingFieldBuilderTest() {
		Map<String, String> pathMap = new HashMap<>();
		//Match
		pathMap.put("match.game", "name");
		//pathMap.put("match.venue", "metadata.venue");
		pathMap.put("match.score", "eventResult.metadata.score");
		pathMap.put("match.winningTeam", "eventResult.metadata.winnerCode");
		
		//AwayGoals
		pathMap.put("awayGoals", "eventResult.metadata.awayGoalMinutes");
		
		//FirstHomeGoal
		pathMap.put("firstHomeGoal", "eventResult.metadata.homeGoalMinutes[0](N/A)");
		
		//ScoreNames
		pathMap.put("scoreNames[0]", "eventResult.metadata.homeScorers");
		pathMap.put("scoreNames[1]", "eventResult.metadata.awayScorers");
		
		
		JvJdotMapperBuilder builder = new JvJdotMapperBuilder().withPathMapping(pathMap);
		JvJdotMapper<GoalsHolder> mapper = builder.build(GoalsHolder.class);
		
		List<String> ghag = new ArrayList<>();
		ghag.add("25");
		ghag.add("36");
		ghag.add("90+5");
		List<String> ghsn = new ArrayList<>();
		ghsn.add("Adam Smith (80) Junior Stanislas (87, 90+8)");
		ghsn.add("Ramiro Funes Mori (25) Romelu Lukaku (36) Ross Barkley (90+5)");
		GoalsHolder gh = new GoalsHolder(
				new SimpleMatchJava("Bournemouth vs. Everton", Optional.empty(), "3 - 3", "draw"),
				ghag,"80", ghsn);
		
		GoalsHolder ret = mapper.map(jsonList.get(3));
		
		assertEquals(gh, ret);
		
		System.out.println(jsonList.get(3));
		System.out.println(ret);
	
	}
	
	@Test
	public void matchPairTestFromTypeHint() {
		
		SimpleMatchJava matchOne = new SimpleMatchJava("Sunderland vs. Stoke", Optional.of("Stadium of Light"), "2 - 0", "sunderland");
		MatchFixtureJava matchTwo = new MatchFixtureJava("Man City vs. Southampton", "Etihad Stadium", "2015-11-28T15:00:00Z");
		MatchPairJava expected = new MatchPairJava(matchOne, matchTwo);
				
		List<Class<?>> typeHintList = new ArrayList<>();
		typeHintList.add(SimpleMatchJava.class);
		typeHintList.add(MatchFixtureJava.class);
		
		Map<String, String> pathMap = new HashMap<>();
		pathMap.put("matchOne._t", "(SimpleMatchJava)");
		pathMap.put("matchOne.game", "[0].name");
		pathMap.put("matchOne.venue", "[0].metadata.venue");
		pathMap.put("matchOne.score", "[0].eventResult.metadata.score");
		pathMap.put("matchOne.winningTeam", "[0].eventResult.metadata.winnerCode");
		
		pathMap.put("matchTwo._t", "(MatchFixtureJava)");
		pathMap.put("matchTwo.game", "[1].name");
		pathMap.put("matchTwo.venue", "[1].metadata.venue");
		pathMap.put("matchTwo.startDate", "[1].startDate");
		
		JvJdotMapperBuilder builder = new JvJdotMapperBuilder().withPathMapping(pathMap).withTypeHintList(typeHintList);
		JvJdotMapper<MatchPairJava> mapper = builder.build(MatchPairJava.class);
		
		String json = "[" + jsonList.get(0) + "," + jsonList.get(1) + "]";
		
		System.out.println(json);
		
		assertEquals(expected, mapper.map(json));
		
	}
}
