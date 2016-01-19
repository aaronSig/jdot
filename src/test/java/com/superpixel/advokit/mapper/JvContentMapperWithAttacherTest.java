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

public class JvContentMapperWithAttacherTest {

	static private List<String> jsonList = new LinkedList<>();

	static private String compRoundJson = "{\"id\": 34,\"code\": \"week-14\",\"name\": \"Week 14\",\"startDate\": \"2015-11-27T00:00:00Z\",\"endDate\": \"2015-12-03T23:59:59Z\"}";

	@BeforeClass
	public static void beforeAll() {

		// Get file from resources folder
		ClassLoader classLoader = JsonContentMapperBuilderTest.class
				.getClassLoader();
		File file = new File(classLoader.getResource("pl-league-week-14.json")
				.getFile());

		try (Scanner scanner = new Scanner(file)) {
			if (scanner.hasNextLine())
				scanner.nextLine();

			int[] breakArray = { 128, 259, 392, 525, 657, 786, 910, 1039, 1168,
					1294 };
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
	public void WeekendBuildAttacherMapTest() {

		Map<String, String> simpleMatchMapping = new HashMap<>();
		simpleMatchMapping.put("game", "name");
		simpleMatchMapping.put("venue", "metadata.venue");
		simpleMatchMapping.put("score", "eventResult.metadata.score");
		simpleMatchMapping.put("winningTeam", "eventResult.metadata.winnerCode");
		
		Map<String, String> weekendMapping = new HashMap<>();
		weekendMapping.put("weekName", "name");
		
		Map<String, String> attachmentMapping = new HashMap<>();
		attachmentMapping.put("matchList", "");

		JvContentMapper<WeekendJava> mapper = new JvContentMapperBuilder()
				.withPathMapping(weekendMapping)
				.build(WeekendJava.class);
		
		JvContentAttacher smAttacher = new JvContentAttacherBuilder()
				.withPathMapping(simpleMatchMapping)
				.build(attachmentMapping);
		
		JvContentMapperWithAttacher<WeekendJava> mapAtt = mapper.withAttacher(SimpleMatchJava.class, smAttacher);
		
		List<MatchJava> matchList = new ArrayList<>();

		matchList.add(new SimpleMatchJava("Sunderland vs. Stoke", Optional.of("Stadium of Light"), "2 - 0", "sunderland"));
		matchList.add(new SimpleMatchJava("Man City vs. Southampton", Optional.of("Etihad Stadium"), "3 - 1", "man-city"));
		matchList.add(new SimpleMatchJava("Crystal Palace vs. Newcastle", Optional.of("Selhurst Park"), "5 - 1", "crystal-palace"));
		matchList.add(new SimpleMatchJava("Bournemouth vs. Everton", Optional.of("Vitality Stadium"), "3 - 3", "draw"));
		matchList.add(new SimpleMatchJava("Aston Villa vs. Watford", Optional.of("Villa Park"), "2 - 3", "watford"));
		matchList.add(new SimpleMatchJava("Leicester vs. Man Utd", Optional.of("King Power Stadium"), "1 - 1", "draw"));
		matchList.add(new SimpleMatchJava("Tottenham vs. Chelsea", Optional.of("White Hart Lane"), "0 - 0", "draw"));
		matchList.add(new SimpleMatchJava("West Ham vs. West Brom", Optional.of("Boleyn Ground"), "1 - 1", "draw"));
		matchList.add(new SimpleMatchJava("Norwich vs. Arsenal", Optional.of("Carrow Road"), "1 - 1", "draw"));
		matchList.add(new SimpleMatchJava("Liverpool vs. Swansea", Optional.of("Anfield"), "1 - 0", "liverpool"));

		WeekendJava expected = new WeekendJava("Week 14", matchList);
		
		
		assertEquals(expected, mapAtt.mapWithListAttachment(jsonList, compRoundJson));
	}
}
