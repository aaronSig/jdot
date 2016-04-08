package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.jvStringMapToJPathPairSet;

import java.util.Map;

import com.superpixel.jdot.JsonContentAttacher;
import com.superpixel.jdot.JsonContentAttacher$;
import com.superpixel.jdot.pathing.JPathPair;

public class JvContentAttacherBuilder {

	

	public JvContentAttacher build(Map<String, String> attachmentMapping) {

		scala.collection.immutable.Set<JPathPair> scAttachmentMapping = jvStringMapToJPathPairSet(attachmentMapping);

		JsonContentAttacher scAttacher = JsonContentAttacher$.MODULE$.apply(scAttachmentMapping);
		return new JvContentAttacher(scAttacher);
	}

}
