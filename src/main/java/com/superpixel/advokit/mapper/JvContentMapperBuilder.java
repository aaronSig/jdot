package com.superpixel.advokit.mapper;

import java.util.Map;
import java.util.Optional;

import com.superpixel.advokit.ScalaConverters;
import com.superpixel.advokit.json.lift.JValueMapper;
import com.superpixel.advokit.json.pathing.JPathPair;

public class JvContentMapperBuilder {

	private Optional<Map<String, String>> inclusionsMap = Optional.empty();
	
	private Optional<String> defaultInJson = Optional.empty();
	private Optional<String> defaultOutJson = Optional.empty();
	
	/***
	 * Format Destination -> Source.
	 * Destinations paths are keys with source paths as values.
	 */
	private Map<String, String> pathMapping;
	
	/***
	 * Declares the paths for the json transformation.
	 * Format is Destination -> Source.
	 * Destinations paths are keys with source paths as values.
	 * 
	 * For example, if the target class has a String field called 'name'.
	 * Then mapping:
	 * 	'name' -> 'person.forename'
	 * Will assign the 'name' attribute with the value in 'forename' field in the 'person' object in the json.
	 * 
	 * @param pathMapping
	 * @return
	 */
	public JvContentMapperBuilder withPathMapping(Map<String, String> pathMapping) {
		this.pathMapping = pathMapping;
		return this;
	}
	
	/***
	 * Inclusions can be used by sepcifying a link in the path.
	 * The path 'person.id>.friendCount' will get the 'id' from the 'person' object in the json.
	 * It will look for this 'id' in the inclusions map, and then use the resulting json value for the rest of the path.
	 * 
	 * @param inclusionsMap
	 * @return
	 */
	public JvContentMapperBuilder withInclusionsMap(Map<String, String> inclusionsMap) {
		this.inclusionsMap = Optional.of(inclusionsMap);
		return this;
	}
	
	/***
	 * This json is merged with the passed in json BEFORE each transformation.
	 * The passed in json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as those passed IN for transformation)
	 * @param defaultInJson
	 * @return
	 */
	public JvContentMapperBuilder withDefaultInJson(String defaultInJson) {
		this.defaultInJson = Optional.of(defaultInJson);
		return this;
	}
	
	/***
	 * This json is merged with the resulting json AFTER each transformation.
	 * The result json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as the json that comes OUT of the transformation)
	 * @param defaultOutJson
	 * @return
	 */
	public JvContentMapperBuilder withDefaultOutJson(String defaultOutJson) {
		this.defaultOutJson = Optional.of(defaultOutJson);
		return this;
	}
	
	public <T> JvContentMapper<T> build(Class<T> targetClass) {

		scala.collection.immutable.Set<JPathPair> scPathMapping = ScalaConverters.jvStringMapToJPathPairSet(pathMapping);
 		
		Inclusions scIncMap;
		if (inclusionsMap.isPresent()) {
			scIncMap = new FixedInclusions(ScalaConverters.jvToScMap(inclusionsMap.get()));
		} else {
			scIncMap = JValueMapper.forTargetClass$default$4();
		}
		DefaultJson scDefJson;
		if (defaultInJson.isPresent() && defaultOutJson.isPresent()) {
			scDefJson = new DefaultJsonInOut(defaultInJson.get(), defaultOutJson.get());
		} else if (defaultInJson.isPresent()) {
			scDefJson = new DefaultJsonIn(defaultInJson.get());
		} else if (defaultOutJson.isPresent()) {
			scDefJson = new DefaultJsonOut(defaultOutJson.get());
		} else {
			scDefJson = JValueMapper.forTargetClass$default$3();
		}
		
		JsonContentMapper<T> scMapper = JValueMapper.forTargetClass(targetClass, scPathMapping, scDefJson, scIncMap);
		return new JvContentMapper<T>(scMapper);
	}
}
