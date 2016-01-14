package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.*;

import java.util.List;
import java.util.Map;

public class JvContentAttacher {

	private JsonContentAttacher scAttacher;
	  
	  public JvContentAttacher(JsonContentAttacher scAttacher) {
	    this.scAttacher = scAttacher;
	  }
	  
	  public JsonContentAttacher getScAttacher() {
		  return scAttacher;
	  }

	  
	  public String attachList(List<String> jsonListToAttach, String jsonAttachee) {
		return scAttacher.attachList(jvToScList(jsonListToAttach), jsonAttachee, scAttacher.attachList$default$3(), scAttacher.attachList$default$4());
	  }
	  public String attach(String jsonToAttach, String jsonAttachee) {
	    return scAttacher.attach(jsonToAttach, jsonAttachee, scAttacher.attach$default$3(), scAttacher.attach$default$4());
	  }
	  

	public String attach(String jsonToAttach, String jsonAttachee, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
	    Inclusions scIncMap;
	    if (additionalInclusions != null) {
	      scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
	    } else {
	      scIncMap = scAttacher.attach$default$4();
	    }
	    MergingJson scDefJson;
	    if (preMergingJson == null && postMergingJson == null) {
	      scDefJson = scAttacher.attach$default$3();
	    } else if (preMergingJson == null) {
	      scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
	    } else if (postMergingJson == null) {
	      scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
	    } else {
	      scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
	    }

	    return scAttacher.attach(jsonToAttach, jsonAttachee, scDefJson, scIncMap);
	  }
	
	public String attachList(List<String> jsonListToAttach, String jsonAttachee, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
	    Inclusions scIncMap;
	    if (additionalInclusions != null) {
	      scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
	    } else {
	      scIncMap = scAttacher.attachList$default$4();
	    }
	    MergingJson scDefJson;
	    if (preMergingJson == null && postMergingJson == null) {
	      scDefJson = scAttacher.attachList$default$3();
	    } else if (preMergingJson == null) {
	      scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
	    } else if (postMergingJson == null) {
	      scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
	    } else {
	      scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
	    }

	    return scAttacher.attachList(jvToScList(jsonListToAttach), jsonAttachee, scDefJson, scIncMap);
	    
	  }
	  
}
