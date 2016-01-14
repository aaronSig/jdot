package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.*;

import java.util.List;
import java.util.Map;

public class JvContentMapperWithAttacher<T> {
	
	private JsonContentMapperWithAttacher<T> scMapper;
	
	public JvContentMapperWithAttacher(JsonContentMapperWithAttacher<T> scMapper) {
		this.scMapper = scMapper;
	}
	
	public T mapWithAttachment(String jsonToAttach, String jsonAttachee) {
		return scMapper.mapWithAttachment(jsonToAttach, jsonAttachee, scMapper.mapWithAttachment$default$3(), scMapper.mapWithAttachment$default$4());
	}
	
	public T mapWithListAttachment(List<String> jsonListToAttach, String jsonAttachee) {
		return scMapper.mapWithListAttachment(jvToScList(jsonListToAttach), jsonAttachee, scMapper.mapWithListAttachment$default$3(), scMapper.mapWithListAttachment$default$4());
	}
	
	
	public T mapWithAttachment(String jsonToAttach, String jsonAttachee, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
		Inclusions scIncMap;
		if (additionalInclusions != null) {
			scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
		} else {
			scIncMap = scMapper.mapWithAttachment$default$4();
		}
		MergingJson scDefJson;
		if (preMergingJson == null && postMergingJson == null) {
			scDefJson = scMapper.mapWithAttachment$default$3();
		} else if (preMergingJson == null) {
			scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
		} else if (postMergingJson == null) {
			scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
		} else {
			scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
		}

		return scMapper.mapWithAttachment(jsonToAttach, jsonAttachee, scDefJson, scIncMap);
	}
	
	public T mapWithListAttachment(List<String> jsonListToAttach, String jsonAttachee, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
		Inclusions scIncMap;
		if (additionalInclusions != null) {
			scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
		} else {
			scIncMap = scMapper.mapWithListAttachment$default$4();
		}
		MergingJson scDefJson;
		if (preMergingJson == null && postMergingJson == null) {
			scDefJson = scMapper.mapWithListAttachment$default$3();
		} else if (preMergingJson == null) {
			scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
		} else if (postMergingJson == null) {
			scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
		} else {
			scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
		}

		return scMapper.mapWithListAttachment(jvToScList(jsonListToAttach), jsonAttachee, scDefJson, scIncMap);
	}

	
}
