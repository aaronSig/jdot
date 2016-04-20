package com.superpixel.jdot.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import com.superpixel.jdot.JvJDotAttacher;
import com.superpixel.jdot.JvJDotTransformer;

public class AusGrandPrixJavaExample {
	
	private static String ausF1ShortArray;
	@SuppressWarnings("unused")
	private static String ausF1Simple;
	
	@BeforeClass
	public static void beforeAll() {

		ausF1ShortArray = fileToString("2016-aus-grandprix-result-shortarray.json");
		ausF1Simple = fileToString("2016-aus-grandprix-result-simple.json");
	
	}
	
	@Test
	public void transformerAndAttachExample() {
		
		Map<String, String> transformPairs = new HashMap<>();
		transformPairs.put("race.country",        "circuit.country");
		transformPairs.put("race.city",           "circuit.city");
		transformPairs.put("race.name",           "raceName");
		transformPairs.put("race.season",         "season");
		transformPairs.put("race.seasonRound",    "round");
		transformPairs.put("winner.code",         "results[0]");
		transformPairs.put("winner.name",         "podiumDetail[0].driverName");
		transformPairs.put("winner.team",         "podiumDetail[0].team");
		
		JvJDotTransformer transformer = JvJDotTransformer.builder()
														 .withPathMapping(transformPairs)
														 .build();
		String transformedJson = transformer.transform(ausF1ShortArray);
		System.out.println(transformedJson);
		//{
	    //	"race":{"name":"Australian Grand Prix","season":"2016","seasonRound":1,"city":"Melbourne","country":"Australia"},
		//	"winner":{"name":"Nico Rosberg","code":"ROS","team":"Mercedes"}
		//}
		
		Map<String, String> attachPairs = new HashMap<>();
		attachPairs.put("start.date",    "date");
		attachPairs.put("start.time",    "time");
		
		JvJDotAttacher attacher = JvJDotAttacher.builder()
												.withAttachmentMapping(attachPairs)
												.build();
		String attachedJson = attacher.attach("{\"time\":\"05:00:00Z\", \"date\":\"2016-03-20\"}", transformedJson);
		//{
	    //	"race":{...},
		//	"winner":{...},
		//	"start":{"time":"05:00:00Z", "date":"2016-03-20"}
		//}
		System.out.println(attachedJson);
	}
	
	

	private static String fileToString(String filename) {
		//Get file from resources folder
		ClassLoader classLoader = AusGrandPrixJavaExample.class.getClassLoader();
		File file = new File(classLoader.getResource(filename).getFile());
		try (Scanner scanner = new Scanner(file)) {
			StringBuilder result = new StringBuilder("");	
			while(scanner.hasNextLine()) {
				result.append(scanner.nextLine());
			}
			return result.toString();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Couldn't load JSON", e);
		}
	}
}
