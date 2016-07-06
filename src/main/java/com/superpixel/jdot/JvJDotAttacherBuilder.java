package com.superpixel.jdot;

import static com.superpixel.jdot.util.ScalaConverters.jvStringMapToJPathPairSet;

import java.util.*;
import java.util.stream.Collectors;

import com.superpixel.jdot.JDotAttacher;
import com.superpixel.jdot.JDotAttacher$;
import com.superpixel.jdot.pathing.JPathPair;
import static com.superpixel.jdot.util.ScalaConverters.*;

public class JvJDotAttacherBuilder {

	/***
	 * Format Destination -> Source. Destinations paths are keys with source
	 * paths as values.
	 */
	private Map<String, String> attachmentMapping;

	private Optional<Boolean> treatArraysAsLists = Optional.empty();

	private Optional<AttachmentContext> attachmentContext = Optional.empty();

	private Optional<JvJDotTransformer> transformer = Optional.empty();

	private List<JvJDotAttacher> nestedAttachers = new ArrayList<>();

	/***
	 * Declares the paths for the json attachment. Format is Destination ->
	 * Source. Destinations paths are keys with source paths as values.
	 * 
	 * @param attachmentMapping
	 * @return
	 */
	public JvJDotAttacherBuilder withAttachmentMapping(
			Map<String, String> attachmentMapping) {
		this.attachmentMapping = attachmentMapping;
		return this;
	}

	public JvJDotAttacherBuilder withTreatArraysAsList(Boolean bool) {
		this.treatArraysAsLists = Optional.ofNullable(bool);
		return this;
	}

	//TODO must annotate these, flow is still unintuitive

	public JvJDotAttacherBuilder withOverrideJsonContext(String json) {
		this.attachmentContext = Optional.of(new OverrideAttachmentContext(json));
		return this;
	}

	public JvJDotAttacherBuilder withPathContext(String jPath) {
		this.attachmentContext = Optional.of(new PathAttachmentContext(jPath));
		return this;
	}

	public JvJDotAttacherBuilder withOverrideJsonPathContext(String json, String jPath) {
		this.attachmentContext = Optional.of(new OverridePathAttachmentContext(json, jPath));
		return this;
	}

	public JvJDotAttacherBuilder withOverrideJsonListContext(List<String> jsonList) {
		this.attachmentContext = Optional.of(new ListOverrideAttachmentContext(jvToScList(jsonList)));
		return this;
	}

	public JvJDotAttacherBuilder withTransformer(JvJDotTransformer transformer) {
		this.transformer = Optional.of(transformer);
		return this;
	}

	public JvJDotAttacherBuilder withNestedAttachers(JvJDotAttacher... attachers) {
		Collections.addAll(this.nestedAttachers, attachers);
		return this;
	}

	public JvJDotAttacher build() {

		scala.collection.immutable.Set<JPathPair> scAttachmentMapping = jvStringMapToJPathPairSet(attachmentMapping);

		if (!attachmentContext.isPresent()) {
			attachmentContext = Optional.of(JDotAttacher$.MODULE$.apply$default$2());
		}

		scala.Option<JDotTransformer> scTransformer = jvOptionalToScOption(transformer.map(t -> t.getScTransformer()));
		if (!scTransformer.isDefined()) {
			scTransformer = JDotAttacher$.MODULE$.apply$default$3();
		}

		scala.collection.immutable.List<JDotAttacher> scAttachers;
		if (!nestedAttachers.isEmpty()) {
			scAttachers = jvToScList(nestedAttachers.stream().map(na -> na.getScAttacher()).collect(Collectors.toList()));
		} else {
			scAttachers = JDotAttacher$.MODULE$.apply$default$4();
		}

		if (!treatArraysAsLists.isPresent()) {
			treatArraysAsLists = Optional.of(JDotAttacher$.MODULE$.apply$default$5());
		}

		JDotAttacher scAttacher = JDotAttacher$.MODULE$
				.apply(scAttachmentMapping,
						attachmentContext.get(),
						scTransformer,
						scAttachers,
						treatArraysAsLists.get());
		return new JvJDotAttacher(scAttacher);
	}

}
