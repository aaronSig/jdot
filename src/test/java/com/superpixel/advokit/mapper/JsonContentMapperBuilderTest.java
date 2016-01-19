package com.superpixel.advokit.mapper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

public class JsonContentMapperBuilderTest {
	
	static private List<String> jsonList = new LinkedList<>();
	
	@BeforeClass
	public static void beforeAll() {

		//Get file from resources folder
		ClassLoader classLoader = JsonContentMapperBuilderTest.class.getClassLoader();
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
		
		JvContentMapperBuilder builder = new JvContentMapperBuilder().withPathMapping(pathMap);
		
		JvContentMapper<SimpleMatchJava> mapper = builder.build(SimpleMatchJava.class);
		
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
		
		
		JvContentMapperBuilder builder = new JvContentMapperBuilder().withPathMapping(pathMap);
		JvContentMapper<GoalsHolder> mapper = builder.build(GoalsHolder.class);
		
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
		
		
		JvContentMapperBuilder builder = new JvContentMapperBuilder().withPathMapping(pathMap);
		JvContentMapper<GoalsHolder> mapper = builder.build(GoalsHolder.class);
		
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
}
