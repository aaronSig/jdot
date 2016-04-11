package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.jvStringMapToJPathPairSet;

import java.util.Map;

import com.superpixel.jdot.JdotAttacher;
import com.superpixel.jdot.JdotAttacher$;
import com.superpixel.jdot.pathing.JPathPair;

public class JvJdotAttacherBuilder {

	

	public JvJdotAttacher build(Map<String, String> attachmentMapping) {

		scala.collection.immutable.Set<JPathPair> scAttachmentMapping = jvStringMapToJPathPairSet(attachmentMapping);

		JdotAttacher scAttacher = JdotAttacher$.MODULE$.apply(scAttachmentMapping);
		return new JvJdotAttacher(scAttacher);
	}

}
