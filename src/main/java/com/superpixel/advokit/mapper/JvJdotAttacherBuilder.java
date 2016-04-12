package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.jvStringMapToJPathPairSet;

import java.util.Map;

import com.superpixel.jdot.JDotAttacher;
import com.superpixel.jdot.JDotAttacher$;
import com.superpixel.jdot.pathing.JPathPair;

public class JvJDotAttacherBuilder {

	

	public JvJDotAttacher build(Map<String, String> attachmentMapping) {

		scala.collection.immutable.Set<JPathPair> scAttachmentMapping = jvStringMapToJPathPairSet(attachmentMapping);

		JDotAttacher scAttacher = JDotAttacher$.MODULE$.apply(scAttachmentMapping);
		return new JvJDotAttacher(scAttacher);
	}

}
