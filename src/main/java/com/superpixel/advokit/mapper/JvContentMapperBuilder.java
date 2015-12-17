package com.superpixel.advokit.mapper;

import java.util.Map;
import java.util.Optional;

import static com.superpixel.advokit.ScalaConverters.*;
import com.superpixel.advokit.json.lift.JValueMapper;
import com.superpixel.advokit.json.pathing.JPathPair;

public class JvContentMapperBuilder {

	private Optional<Map<String, String>> inclusionsMap = Optional.empty();
	
	private Optional<String[]> preMergingJson = Optional.empty();
	private Optional<String[]> postMergingJson = Optional.empty();
	
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
	public JvContentMapperBuilder withPreJsonMerging(String... preMergingJson) {
		this.preMergingJson = Optional.of(preMergingJson);
		return this;
	}
	
	/***
	 * This json is merged with the resulting json AFTER each transformation.
	 * The result json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as the json that comes OUT of the transformation)
	 * @param defaultOutJson
	 * @return
	 */
	public JvContentMapperBuilder withPostJsonMerging(String... postMergingJson) {
		this.postMergingJson = Optional.of(postMergingJson);
		return this;
	}
	
	public <T> JvContentMapper<T> build(Class<T> targetClass) {

		scala.collection.immutable.Set<JPathPair> scPathMapping = jvStringMapToJPathPairSet(pathMapping);
 		
		Inclusions scIncMap;
		if (inclusionsMap.isPresent()) {
			scIncMap = new FixedInclusions(jvToScMap(inclusionsMap.get()));
		} else {
			scIncMap = JValueMapper.forTargetClass$default$4();
		}
		MergingJson scMergJson;
		if (preMergingJson.isPresent() && postMergingJson.isPresent()) {
			scMergJson = new MergingJsonPrePost(jvArrayToScSeq(preMergingJson.get()), jvArrayToScSeq(postMergingJson.get()));
		} else if (preMergingJson.isPresent()) {
			scMergJson = new MergingJsonPre(jvArrayToScSeq(preMergingJson.get()));
		} else if (postMergingJson.isPresent()) {
			scMergJson = new MergingJsonPost(jvArrayToScSeq(postMergingJson.get()));
		} else {
			scMergJson = JValueMapper.forTargetClass$default$3();
		}
		
		JsonContentMapper<T> scMapper = JValueMapper.forTargetClass(targetClass, scPathMapping, scMergJson, scIncMap);
		return new JvContentMapper<T>(scMapper);
	}
}
