package com.superpixel.jdot;

import static com.superpixel.jdot.util.ScalaConverters.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class JvJDotSettingsBuilder {

	private Optional<Map<String, String>> inclusionsOpt = Optional.empty();
	private Optional<List<String>> preMergingJsonOpt = Optional.empty();
	private Optional<List<String>> postMergingJsonOpt = Optional.empty();
	private List<JvJDotAttacher> attachers = new ArrayList<>();
		
	public Optional<Map<String, String>> getInclusions() {
		return inclusionsOpt;
	}

	public Optional<List<String>> getPreMergingJson() {
		return preMergingJsonOpt;
	}

	public Optional<List<String>> getPostMergingJson() {
		return postMergingJsonOpt;
	}

	public List<JvJDotAttacher> getAttachments() {
		return attachers;
	}


	public JvJDotSettings build() {	    
	    Inclusions scIncMap;
	    if (inclusionsOpt.isPresent()) {
	      scIncMap = new FixedInclusions(jvToScMap(inclusionsOpt.get()));
	    } else {
	      scIncMap = NoInclusions$.MODULE$;
	    }
	    
	    MergingJson scMergJson;
	    if (preMergingJsonOpt.isPresent() && postMergingJsonOpt.isPresent()) {
	      scMergJson = new MergingJsonPrePost(jvListToScSeq(preMergingJsonOpt.get()), jvListToScSeq(postMergingJsonOpt.get()));
	    } else if (preMergingJsonOpt.isPresent()) {
	      scMergJson = new MergingJsonPre(jvListToScSeq(preMergingJsonOpt.get()));
	    } else if (postMergingJsonOpt.isPresent()) {
	      scMergJson = new MergingJsonPost(jvListToScSeq(postMergingJsonOpt.get()));
	    } else {
	      scMergJson = NoMerging$.MODULE$;
	    }

		scala.collection.immutable.List scAttachers;
		if (!attachers.isEmpty()) {
			scAttachers = jvToScList(attachers.stream().map(na -> na.getScAttacher()).collect(Collectors.toList()));
		} else {
			scAttachers = scala.collection.immutable.Nil$.MODULE$;
		}
	    
	    return new JvJDotSettings(scIncMap, scMergJson, scAttachers);
	}
	
	
	public JvJDotSettingsBuilder withInclusion(String key, String jsonValue) {
		if (!inclusionsOpt.isPresent()) {
			inclusionsOpt = Optional.of(new HashMap<>());
		}
		inclusionsOpt.get().put(key, jsonValue);
		return this;
	}
	public JvJDotSettingsBuilder withInclusions(Map<String, String> inclusions) {
		if (!inclusionsOpt.isPresent()) {
			inclusionsOpt = Optional.of(new HashMap<>());
		}
		inclusionsOpt.get().putAll(inclusions);
		return this;
	}
	
	public JvJDotSettingsBuilder withPreMergingJson(String... preMergingJson) {
		if (!preMergingJsonOpt.isPresent()) {
			preMergingJsonOpt = Optional.of(new ArrayList<>());
		}
		Collections.addAll(preMergingJsonOpt.get(), preMergingJson);
		return this;
	}
	public JvJDotSettingsBuilder withPostMergingJson(String... postMergingJson) {
		if (!postMergingJsonOpt.isPresent()) {
			postMergingJsonOpt = Optional.of(new ArrayList<>());
		}
		Collections.addAll(postMergingJsonOpt.get(), postMergingJson);
		return this;
	}

	public JvJDotSettingsBuilder withAttachers(JvJDotAttacher... attachers) {
		Collections.addAll(this.attachers, attachers);
		return this;
	}

    public JvJDotSettingsBuilder withAttachers(List<JvJDotAttacher> attachers) {
        this.attachers.addAll(attachers);
        return this;
    }
	
//	public static SimpleAttachment getSimpleAttachment(String jsonToAttach, JvJDotAttacher attacher) {
//		return new SimpleAttachment(jsonToAttach, attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withSimpleAttachment(String jsonToAttach, JvJDotAttacher attacher) {
//		return addAttachment(getSimpleAttachment(jsonToAttach, attacher));
//	}
//
//	public static SimpleListAttachment getSimpleListAttachment(List<String> jsonListToAttach, JvJDotAttacher attacher) {
//		return new SimpleListAttachment(jvToScList(jsonListToAttach), attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withSimpleListAttachment(List<String> jsonListToAttach, JvJDotAttacher attacher) {
//		return addAttachment(getSimpleListAttachment(jsonListToAttach, attacher));
//	}
//
//	public static SimpleTransformAttachment getSimpleTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
//		return new SimpleTransformAttachment(jsonToAttach, transformer.getScTransformer(), attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withSimpleTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
//		return addAttachment(getSimpleTransformAttachment(jsonToAttach, transformer, attacher));
//	}
//
//	public static SimpleTransformListAttachment getSimpleTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
//		return new SimpleTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withSimpleTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
//		return addAttachment(getSimpleTransformListAttachment(jsonListToAttach, transformer, attacher));
//	}
//
//	public  static JsonArrayTransformAttachment getJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotAttacher attacher) {
//		return new JsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotAttacher attacher) {
//		return addAttachment(getJsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer, attacher));
//	}
//
//	public static NestedTransformAttachment getNestedTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
//		return new NestedTransformAttachment(jsonToAttach, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withNestedTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
//		return addAttachment(getNestedTransformAttachment(jsonToAttach, transformer, settings, attacher));
//	}
//
//	public static NestedTransformListAttachment getNestedTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
//		return new NestedTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withNestedTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
//		return addAttachment(getNestedTransformListAttachment(jsonListToAttach, transformer, settings, attacher));
//	}
//
//	public static JsonArrayNestedTransformAttachment getJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
//		return new JsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
//	}
//	public JvJDotSettingsBuilder withJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
//		return addAttachment(getJsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer, settings, attacher));
//	}
}
