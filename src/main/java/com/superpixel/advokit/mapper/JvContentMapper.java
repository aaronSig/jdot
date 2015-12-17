package com.superpixel.advokit.mapper;

import java.util.List;
import java.util.Map;

import static com.superpixel.advokit.ScalaConverters.*;

public class JvContentMapper<T> {

	private JsonContentMapper<T> scMapper;
	
	public JvContentMapper(JsonContentMapper<T> scMapper) {
		this.scMapper = scMapper;
	}
	
	public T map(String json) {
		return scMapper.map(json, scMapper.map$default$2(), scMapper.map$default$3());
	}
	
	
	public T map(String json, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
		Inclusions scIncMap;
		if (additionalInclusions != null) {
			scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
		} else {
			scIncMap = scMapper.map$default$3();
		}
		MergingJson scDefJson;
		if (preMergingJson == null && postMergingJson == null) {
			scDefJson = scMapper.map$default$2();
		} else if (preMergingJson == null) {
			scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
		} else if (postMergingJson == null) {
			scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
		} else {
			scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
		}

		return scMapper.map(json, scDefJson, scIncMap);
	}

	public T mapWithInclusions(String json, Map<String, String> additionalInclusions) {
		return this.map(json, null, null, additionalInclusions);
	}
	
	public T mapWithPreJsonMerging(String json, List<String> preMergingJson) {
		return this.map(json, preMergingJson, null, null);
	}
	
	public T mapWithPostJsonMerging(String json, List<String> postMergingJson) {
		return this.map(json, null, postMergingJson, null);
	}
	
	public T mapWithPrePostJsonMerging(String json, List<String> preMergingJson, List<String> postMergingJson) {
		return this.map(json, preMergingJson, postMergingJson, null);
	}
}
