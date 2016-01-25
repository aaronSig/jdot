package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.*;

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
	
	
	private JvContentSettingsBuilder addAttachment(Attachment attachment) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().add(attachment);
		return this;
	}
	public JvContentSettingsBuilder withSimpleAttachment(String jsonToAttach, JvContentAttacher attacher) {
		return addAttachment(new SimpleAttachment(jsonToAttach, attacher.getScAttacher()));
	}
	public JvContentSettingsBuilder withSimpleListAttachment(List<String> jsonListToAttach, JvContentAttacher attacher) {
		return addAttachment(new SimpleListAttachment(jvToScList(jsonListToAttach), attacher.getScAttacher()));
	}
	public JvContentSettingsBuilder withSimpleTransformAttachment(String jsonToAttach, JvContentTransformer transformer, JvContentAttacher attacher) {
		return addAttachment(new SimpleTransformAttachment(jsonToAttach, transformer.getScTransformer(), attacher.getScAttacher()));
	}
	public JvContentSettingsBuilder withSimpleTransformListAttachment(List<String> jsonListToAttach, JvContentTransformer transformer, JvContentAttacher attacher) {
		return addAttachment(new SimpleTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), attacher.getScAttacher()));
	}
	public JvContentSettingsBuilder withNestedTransformAttachment(String jsonToAttach, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return addAttachment(new NestedTransformAttachment(jsonToAttach, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher()));
	}
	public JvContentSettingsBuilder withNestedTransformListAttachment(List<String> jsonListToAttach, JvContentTransformer transformer, JvContentSettings settings, JvContentAttacher attacher) {
		return addAttachment(new NestedTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), settings.attachments, attacher.getScAttacher()));
	}
}
