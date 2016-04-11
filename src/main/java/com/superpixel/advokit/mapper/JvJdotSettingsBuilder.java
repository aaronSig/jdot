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

public class JvJdotSettingsBuilder {

	private Optional<Map<String, String>> inclusionsOpt = Optional.empty();
	private Optional<List<String>> preMergingJsonOpt = Optional.empty();
	private Optional<List<String>> postMergingJsonOpt = Optional.empty();
	private Optional<List<Attachment>> attachmentsOpt = Optional.empty();
	
	
	public JvJdotSettings build() {	    
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
	    
	    return new JvJdotSettings(scIncMap, scMergJson, scAtt);
	}
	
	
	public JvJdotSettingsBuilder withInclusion(String key, String jsonValue) {
		if (!inclusionsOpt.isPresent()) {
			inclusionsOpt = Optional.of(new HashMap<>());
		}
		inclusionsOpt.get().put(key, jsonValue);
		return this;
	}
	public JvJdotSettingsBuilder withInclusions(Map<String, String> inclusions) {
		if (!inclusionsOpt.isPresent()) {
			inclusionsOpt = Optional.of(new HashMap<>());
		}
		inclusionsOpt.get().putAll(inclusions);
		return this;
	}
	
	public JvJdotSettingsBuilder withPreMergingJson(String... preMergingJson) {
		if (!preMergingJsonOpt.isPresent()) {
			preMergingJsonOpt = Optional.of(new ArrayList<>());
		}
		Collections.addAll(preMergingJsonOpt.get(), preMergingJson);
		return this;
	}
	public JvJdotSettingsBuilder withPostMergingJson(String... postMergingJson) {
		if (!postMergingJsonOpt.isPresent()) {
			postMergingJsonOpt = Optional.of(new ArrayList<>());
		}
		Collections.addAll(postMergingJsonOpt.get(), postMergingJson);
		return this;
	}
	
	public JvJdotSettingsBuilder withAttachments(List<Attachment> attachments) {
		attachmentsOpt = Optional.of(attachments);
		return this;
	}
	public JvJdotSettingsBuilder addAttachments(List<Attachment> attachments) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().addAll(attachments);
		return this;
	}
	public JvJdotSettingsBuilder addAttachment(Attachment attachment) {
		if (!attachmentsOpt.isPresent()) {
			attachmentsOpt = Optional.of(new ArrayList<>());
		}
		attachmentsOpt.get().add(attachment);
		return this;
	}
	
	public static SimpleAttachment getSimpleAttachment(String jsonToAttach, JvJdotAttacher attacher) {
		return new SimpleAttachment(jsonToAttach, attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withSimpleAttachment(String jsonToAttach, JvJdotAttacher attacher) {
		return addAttachment(getSimpleAttachment(jsonToAttach, attacher));
	}
	
	public static SimpleListAttachment getSimpleListAttachment(List<String> jsonListToAttach, JvJdotAttacher attacher) {
		return new SimpleListAttachment(jvToScList(jsonListToAttach), attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withSimpleListAttachment(List<String> jsonListToAttach, JvJdotAttacher attacher) {
		return addAttachment(getSimpleListAttachment(jsonListToAttach, attacher));
	}
	
	public static SimpleTransformAttachment getSimpleTransformAttachment(String jsonToAttach, JvJdotTransformer transformer, JvJdotAttacher attacher) {
		return new SimpleTransformAttachment(jsonToAttach, transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withSimpleTransformAttachment(String jsonToAttach, JvJdotTransformer transformer, JvJdotAttacher attacher) {
		return addAttachment(getSimpleTransformAttachment(jsonToAttach, transformer, attacher));
	}
	
	public static SimpleTransformListAttachment getSimpleTransformListAttachment(List<String> jsonListToAttach, JvJdotTransformer transformer, JvJdotAttacher attacher) {
		return new SimpleTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withSimpleTransformListAttachment(List<String> jsonListToAttach, JvJdotTransformer transformer, JvJdotAttacher attacher) {
		return addAttachment(getSimpleTransformListAttachment(jsonListToAttach, transformer, attacher));
	}
	
	public  static JsonArrayTransformAttachment getJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvJdotTransformer transformer, JvJdotAttacher attacher) {
		return new JsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withJsonArrayTransformAttachment(String jPathToArray, String arrayContainerJson, JvJdotTransformer transformer, JvJdotAttacher attacher) {
		return addAttachment(getJsonArrayTransformAttachment(jPathToArray, arrayContainerJson, transformer, attacher));
	}
	
	public static NestedTransformAttachment getNestedTransformAttachment(String jsonToAttach, JvJdotTransformer transformer, JvJdotSettings settings, JvJdotAttacher attacher) {
		return new NestedTransformAttachment(jsonToAttach, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withNestedTransformAttachment(String jsonToAttach, JvJdotTransformer transformer, JvJdotSettings settings, JvJdotAttacher attacher) {
		return addAttachment(getNestedTransformAttachment(jsonToAttach, transformer, settings, attacher));
	}
	
	public static NestedTransformListAttachment getNestedTransformListAttachment(List<String> jsonListToAttach, JvJdotTransformer transformer, JvJdotSettings settings, JvJdotAttacher attacher) {
		return new NestedTransformListAttachment(jvToScList(jsonListToAttach), transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withNestedTransformListAttachment(List<String> jsonListToAttach, JvJdotTransformer transformer, JvJdotSettings settings, JvJdotAttacher attacher) {
		return addAttachment(getNestedTransformListAttachment(jsonListToAttach, transformer, settings, attacher));
	}
	
	public static JsonArrayNestedTransformAttachment getJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvJdotTransformer transformer, JvJdotSettings settings, JvJdotAttacher attacher) {
		return new JsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer.getScTransformer(), settings.attachments, attacher.getScAttacher());
	}
	public JvJdotSettingsBuilder withJsonArrayNestedTransformAttachment(String jPathToArray, String arrayContainerJson, JvJdotTransformer transformer, JvJdotSettings settings, JvJdotAttacher attacher) {
		return addAttachment(getJsonArrayNestedTransformAttachment(jPathToArray, arrayContainerJson, transformer, settings, attacher));
	}
}
