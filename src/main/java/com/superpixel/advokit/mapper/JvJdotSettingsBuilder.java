package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.jvListToScSeq;
import static com.superpixel.jdot.util.ScalaConverters.jvToScList;
import static com.superpixel.jdot.util.ScalaConverters.jvToScMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.superpixel.jdot.Attachment;
import com.superpixel.jdot.FixedInclusions;
import com.superpixel.jdot.Inclusions;
import com.superpixel.jdot.JsonArrayNestedTransformAttachment;
import com.superpixel.jdot.JsonArrayTransformAttachment;
import com.superpixel.jdot.MergingJson;
import com.superpixel.jdot.MergingJsonPost;
import com.superpixel.jdot.MergingJsonPre;
import com.superpixel.jdot.MergingJsonPrePost;
import com.superpixel.jdot.NestedTransformAttachment;
import com.superpixel.jdot.NestedTransformListAttachment;
import com.superpixel.jdot.NoInclusions$;
import com.superpixel.jdot.NoMerging$;
import com.superpixel.jdot.SimpleAttachment;
import com.superpixel.jdot.SimpleListAttachment;
import com.superpixel.jdot.SimpleTransformAttachment;
import com.superpixel.jdot.SimpleTransformListAttachment;

public class JvJDotSettingsBuilder {

	private Optional<Map<String, String>> inclusionsOpt = Optional.empty();
	private Optional<List<String>> preMergingJsonOpt = Optional.empty();
	private Optional<List<String>> postMergingJsonOpt = Optional.empty();
	private Optional<List<Attachment>> attachmentsOpt = Optional.empty();
	
	
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
	    
	    scala.collection.immutable.List<Attachment> scAtt;
	    if (attachmentsOpt.isPresent()) {
	    	scAtt = jvToScList(attachmentsOpt.get());
	    } else {
	    	scAtt = scala.collection.immutable.List$.MODULE$.empty();
	    }
	    
	    return new JvJDotSettings(scIncMap, scMergJson, scAtt);
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
	
	public JvJDotSettingsBuilder withAttachments(List<Attachment> attachments) {
		attachmentsOpt = Optional.of(attachments);
		return this;
	}
	public JvJDotSettingsBuilder addAttachments(List<Attachment> attachments) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().addAll(attachments);
		return this;
	}
	public JvJDotSettingsBuilder addAttachment(Attachment attachment) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().add(attachment);
		return this;
	}
	
	public static SimpleAttachment getSimpleAttachment(String jsonToAttach, JvJDotAttacher attacher) {
		return new SimpleAttachment(jsonToAttach, attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withSimpleAttachment(String jsonToAttach, JvJDotAttacher attacher) {
		return addAttachment(getSimpleAttachment(jsonToAttach, attacher));
	}
	
	public static SimpleListAttachment getSimpleListAttachment(List<String> jsonListToAttach, JvJDotAttacher attacher) {
		return new SimpleListAttachment(jvToScList(jsonListToAttach), attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withSimpleListAttachment(List<String> jsonListToAttach, JvJDotAttacher attacher) {
		return addAttachment(getSimpleListAttachment(jsonListToAttach, attacher));
	}
	
	public static SimpleTransformAttachment getSimpleTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
		return new SimpleTransformAttachment(jsonToAttach, transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withSimpleTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
		return addAttachment(getSimpleTransformAttachment(jsonToAttach, transformer, attacher));
	}
	
	public static SimpleTransformListAttachment getSimpleTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
		return new SimpleTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withSimpleTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotAttacher attacher) {
		return addAttachment(getSimpleTransformListAttachment(jsonListToAttach, transformer, attacher));
	}
	
	public  static JsonArrayTransformAttachment getJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotAttacher attacher) {
		return new JsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotAttacher attacher) {
		return addAttachment(getJsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer, attacher));
	}
	
	public static NestedTransformAttachment getNestedTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
		return new NestedTransformAttachment(jsonToAttach, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withNestedTransformAttachment(String jsonToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
		return addAttachment(getNestedTransformAttachment(jsonToAttach, transformer, settings, attacher));
	}
	
	public static NestedTransformListAttachment getNestedTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
		return new NestedTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withNestedTransformListAttachment(List<String> jsonListToAttach, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
		return addAttachment(getNestedTransformListAttachment(jsonListToAttach, transformer, settings, attacher));
	}
	
	public static JsonArrayNestedTransformAttachment getJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
		return new JsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvJDotSettingsBuilder withJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvJDotTransformer transformer, JvJDotSettings settings, JvJDotAttacher attacher) {
		return addAttachment(getJsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer, settings, attacher));
	}
}
