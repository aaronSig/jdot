package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.*;

import java.util.List;
import java.util.Map;

import com.superpixel.advokit.ScalaConverters;

public class JvContentTransformer {

  private JsonContentTransformer scTransformer;
  
  public JvContentTransformer(JsonContentTransformer scTransformer) {
    this.scTransformer = scTransformer;
  }
  
  public String transformList(List<String> jsonList) {
	return scTransformer.transformList(ScalaConverters.jvToScList(jsonList), scTransformer.transform$default$2(), scTransformer.transform$default$3());
  }
  public String transform(String json) {
    return scTransformer.transform(json, scTransformer.transform$default$2(), scTransformer.transform$default$3());
  }
  
  public JsonContentTransformer getScTransformer() {
	return scTransformer;
  }

public String transform(String json, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
    Inclusions scIncMap;
    if (additionalInclusions != null) {
      scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
    } else {
      scIncMap = scTransformer.transform$default$3();
    }
    MergingJson scDefJson;
    if (preMergingJson == null && postMergingJson == null) {
      scDefJson = scTransformer.transform$default$2();
    } else if (preMergingJson == null) {
      scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
    } else if (postMergingJson == null) {
      scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
    } else {
      scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
    }

    return scTransformer.transform(json, scDefJson, scIncMap);
  }
  public String transformList(List<String> jsonList, List<String> preMergingJson, List<String> postMergingJson, Map<String, String> additionalInclusions) {
	    Inclusions scIncMap;
	    if (additionalInclusions != null) {
	      scIncMap = new FixedInclusions(jvToScMap(additionalInclusions));
	    } else {
	      scIncMap = scTransformer.transformList$default$3();
	    }
	    MergingJson scDefJson;
	    if (preMergingJson == null && postMergingJson == null) {
	      scDefJson = scTransformer.transformList$default$2();
	    } else if (preMergingJson == null) {
	      scDefJson = new MergingJsonPost(jvListToScSeq(postMergingJson));
	    } else if (postMergingJson == null) {
	      scDefJson = new MergingJsonPre(jvListToScSeq(preMergingJson));
	    } else {
	      scDefJson = new MergingJsonPrePost(jvListToScSeq(preMergingJson), jvListToScSeq(postMergingJson));
	    }

	    return scTransformer.transformList(ScalaConverters.jvToScList(jsonList), scDefJson, scIncMap);
	  }

  public String transformWithInclusions(String json, Map<String, String> additionalInclusions) {
    return this.transform(json, null, null, additionalInclusions);
  }
  
  public String transformWithPreJsonMerging(String json, List<String> preMergingJson) {
    return this.transform(json, preMergingJson, null, null);
  }
  
  public String transformWithPostJsonMerging(String json, List<String> postMergingJson) {
    return this.transform(json, null, postMergingJson, null);
  }
  
  public String transformWithPrePostJsonMerging(String json, List<String> preMergingJson, List<String> postMergingJson) {
    return this.transform(json, preMergingJson, postMergingJson, null);
  }
  
}
