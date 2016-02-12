package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.jvArrayToScSeq;
import static com.superpixel.advokit.ScalaConverters.jvToScList;
import static com.superpixel.advokit.ScalaConverters.jvToScMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JvContentSettingsBuilder {

	private Optional<Map<String, String>> inclusionsOpt = Optional.empty();
	private Optional<List<String>> preMergingJsonOpt = Optional.empty();
	private Optional<List<String>> postMergingJsonOpt = Optional.empty();
	private Optional<List<Attachment>> attachmentsOpt = Optional.empty();
	
	
	public JvContentSettings build() {	    
	    Inclusions scIncMap;
	    if (inclusionsOpt.isPresent()) {
	      scIncMap = new FixedInclusions(jvToScMap(inclusionsOpt.get()));
	    } else {
	      scIncMap = NoInclusions$.MODULE$;
	    }
	    
	    MergingJson scMergJson;
	    if (preMergingJsonOpt.isPresent() && postMergingJsonOpt.isPresent()) {
	      scMergJson = new MergingJsonPrePost(jvArrayToScSeq(preMergingJsonOpt.get()), jvArrayToScSeq(postMergingJsonOpt.get()));
	    } else if (preMergingJsonOpt.isPresent()) {
	      scMergJson = new MergingJsonPre(jvArrayToScSeq(preMergingJsonOpt.get()));
	    } else if (postMergingJsonOpt.isPresent()) {
	      scMergJson = new MergingJsonPost(jvArrayToScSeq(postMergingJsonOpt.get()));
	    } else {
	      scMergJson = NoMerging$.MODULE$;
	    }
	    
	    scala.collection.immutable.List<Attachment> scAtt;
	    if (attachmentsOpt.isPresent()) {
	    	scAtt = jvToScList(attachmentsOpt.get());
	    } else {
	    	scAtt = scala.collection.immutable.List$.MODULE$.empty();
	    }
	    
	    return new JvContentSettings(scIncMap, scMergJson, scAtt);
	}
	
	
	public JvContentSettingsBuilder withInclusion(String key, String jsonValue) {
		if (!inclusionsOpt.isPresent()) {
			inclusionsOpt = Optional.of(new HashMap<>());
		}
		inclusionsOpt.get().put(key, jsonValue);
		return this;
	}
	public JvContentSettingsBuilder withInclusions(Map<String, String> inclusions) {
		if (!inclusionsOpt.isPresent()) {
			inclusionsOpt = Optional.of(new HashMap<>());
		}
		inclusionsOpt.get().putAll(inclusions);
		return this;
	}
	
	public JvContentSettingsBuilder withPreMergingJson(String... preMergingJson) {
		if (!preMergingJsonOpt.isPresent()) {
			preMergingJsonOpt = Optional.of(new ArrayList<>());
		}
		Collections.addAll(preMergingJsonOpt.get(), preMergingJson);
		return this;
	}
	public JvContentSettingsBuilder withPostMergingJson(String... postMergingJson) {
		if (!postMergingJsonOpt.isPresent()) {
			postMergingJsonOpt = Optional.of(new ArrayList<>());
		}
		Collections.addAll(postMergingJsonOpt.get(), postMergingJson);
		return this;
	}
	
	public JvContentSettingsBuilder withAttachments(List<Attachment> attachments) {
		attachmentsOpt = Optional.of(attachments);
		return this;
	}
	public JvContentSettingsBuilder addAttachments(List<Attachment> attachments) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().addAll(attachments);
		return this;
	}
	public JvContentSettingsBuilder addAttachment(Attachment attachment) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().add(attachment);
		return this;
	}
	
	public static SimpleAttachment getSimpleAttachment(String jsonToAttach, JvContentAttacher attacher) {
		return new SimpleAttachment(jsonToAttach, attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withSimpleAttachment(String jsonToAttach, JvContentAttacher attacher) {
		return addAttachment(getSimpleAttachment(jsonToAttach, attacher));
	}
	
	public static SimpleListAttachment getSimpleListAttachment(List<String> jsonListToAttach, JvContentAttacher attacher) {
		return new SimpleListAttachment(jvToScList(jsonListToAttach), attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withSimpleListAttachment(List<String> jsonListToAttach, JvContentAttacher attacher) {
		return addAttachment(getSimpleListAttachment(jsonListToAttach, attacher));
	}
	
	public static SimpleTransformAttachment getSimpleTransformAttachment(String jsonToAttach, JvContentTransformer transformer, JvContentAttacher attacher) {
		return new SimpleTransformAttachment(jsonToAttach, transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withSimpleTransformAttachment(String jsonToAttach, JvContentTransformer transformer, JvContentAttacher attacher) {
		return addAttachment(getSimpleTransformAttachment(jsonToAttach, transformer, attacher));
	}
	
	public static SimpleTransformListAttachment getSimpleTransformListAttachment(List<String> jsonListToAttach, JvContentTransformer transformer, JvContentAttacher attacher) {
		return new SimpleTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withSimpleTransformListAttachment(List<String> jsonListToAttach, JvContentTransformer transformer, JvContentAttacher attacher) {
		return addAttachment(getSimpleTransformListAttachment(jsonListToAttach, transformer, attacher));
	}
	
	public  static JsonArrayTransformAttachment getJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvContentTransformer transformer, JvContentAttacher attacher) {
		return new JsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvContentTransformer transformer, JvContentAttacher attacher) {
		return addAttachment(getJsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer, attacher));
	}
	
	public static NestedTransformAttachment getNestedTransformAttachment(String jsonToAttach, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return new NestedTransformAttachment(jsonToAttach, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withNestedTransformAttachment(String jsonToAttach, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return addAttachment(getNestedTransformAttachment(jsonToAttach, transformer, settings, attacher));
	}
	
	public static NestedTransformListAttachment getNestedTransformListAttachment(List<String> jsonListToAttach, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return new NestedTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withNestedTransformListAttachment(List<String> jsonListToAttach, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return addAttachment(getNestedTransformListAttachment(jsonListToAttach, transformer, settings, attacher));
	}
	
	public static JsonArrayNestedTransformAttachment getJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return new JsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvContentSettingsBuilder withJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return addAttachment(getJsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer, settings, attacher));
	}
}
