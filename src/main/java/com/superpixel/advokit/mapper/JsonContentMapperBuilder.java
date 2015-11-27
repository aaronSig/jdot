package com.superpixel.advokit.mapper;

import java.util.Map;

import com.superpixel.advokit.json.lift.JValueMapper;

public class JsonContentMapperBuilder {

	private Map<String, String> pathMapping;
	private Map<String, String> inclusions;
	
	/***
	 * Specifies the transformation that the json content should go through.
	 * Map keys should be the 'to' path and map values should be the 'from' path.
	 * 
	 * Path format follows javascript object notation. More details can be found in the JPath class file.
	 * 
	 * @param pathMapping
	 * @return
	 */
	public JsonContentMapperBuilder withPathMapping(Map<String, String> pathMapping) {
		this.pathMapping = pathMapping;
		return this;
	}
	
	/***
	 * Specifies inclusions for the content.
	 * Map keys should be inclusion ids and values should be JSON.
	 * 
	 * @param inclusions
	 * @return
	 */
	public JsonContentMapperBuilder withInclusions(Map<String, String> inclusions) {
		this.inclusions = inclusions;
		return this;
	}
	
	public <T> JsonContentMapper<T> build(Class<T> targetClass) {
		JValueMapper.apply(targetClass, JPathPair.fromMap(pathMapping), inclusions)
	}
}
