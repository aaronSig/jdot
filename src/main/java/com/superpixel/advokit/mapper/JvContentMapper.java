package com.superpixel.advokit.mapper;

import java.util.Map;

import com.superpixel.advokit.ScalaConverters;

public class JvContentMapper<T> {

	private JsonContentMapper<T> scMapper;
	
	public JvContentMapper(JsonContentMapper<T> scMapper) {
		this.scMapper = scMapper;
	}
	
	public T map(String json) {
		return scMapper.map(json, scMapper.map$default$2(), scMapper.map$default$3());
	}
	
	
	public T map(String json, String defaultInJson, String defaultOutJson, Map<String, String> additionalInclusions) {
		Inclusions scIncMap;
		if (additionalInclusions != null) {
			scIncMap = new FixedInclusions(ScalaConverters.jvToScMap(additionalInclusions));
		} else {
			scIncMap = scMapper.map$default$3();
		}
		DefaultJson scDefJson;
		if (defaultInJson == null && defaultOutJson == null) {
			scDefJson = scMapper.map$default$2();
		} else if (defaultInJson == null) {
			scDefJson = new DefaultJsonOut(defaultOutJson);
		} else if (defaultOutJson == null) {
			scDefJson = new DefaultJsonIn(defaultInJson);
		} else {
			scDefJson = new DefaultJsonInOut(defaultInJson, defaultOutJson);
		}

		return scMapper.map(json, scDefJson, scIncMap);
	}

	public T mapWithInclusions(String json, Map<String, String> additionalInclusions) {
		return this.map(json, null, null, additionalInclusions);
	}
	
	public T mapWithDefaultInJson(String json, String defaultInJson) {
		return this.map(json, defaultInJson, null, null);
	}
	
	public T mapWithDefaultOutJson(String json, String defaultOutJson) {
		return this.map(json, null, defaultOutJson, null);
	}
	
	public T mapWithDefaultInAndOutJson(String json, String defaultInJson, String defaultOutJson) {
		return this.map(json, defaultInJson, defaultOutJson, null);
	}
}
