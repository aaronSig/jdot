package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.jvStringMapToJPathPairSet;

import java.util.Map;

import com.superpixel.jdot.JDotAttacher;
import com.superpixel.jdot.JDotAttacher$;
import com.superpixel.jdot.pathing.JPathPair;

public class JvJDotAttacherBuilder {

	/***
	 * Format Destination -> Source. Destinations paths are keys with source
	 * paths as values.
	 */
	private Map<String, String> attachmentMapping;

	/***
	 * Declares the paths for the json attachment. Format is Destination ->
	 * Source. Destinations paths are keys with source paths as values.
	 * 
	 * @param pathMapping
	 * @return
	 */
	public JvJDotAttacherBuilder withAttachmentMapping(
			Map<String, String> attachmentMapping) {
		this.attachmentMapping = attachmentMapping;
		return this;
	}

	public JvJDotAttacher build() {

		scala.collection.immutable.Set<JPathPair> scAttachmentMapping = jvStringMapToJPathPairSet(attachmentMapping);

		JDotAttacher scAttacher = JDotAttacher$.MODULE$
				.apply(scAttachmentMapping);
		return new JvJDotAttacher(scAttacher);
	}

}
