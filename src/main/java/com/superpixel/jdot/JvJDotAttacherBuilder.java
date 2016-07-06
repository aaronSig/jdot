package com.superpixel.jdot;

import static com.superpixel.jdot.util.ScalaConverters.jvStringMapToJPathPairSet;

import java.util.*;
import java.util.stream.Collectors;

import com.superpixel.jdot.pathing.JPathPair;
import static com.superpixel.jdot.util.ScalaConverters.*;

public class JvJDotAttacherBuilder {

	/***
	 * Format Destination -> Source. Destinations paths are keys with source
	 * paths as values.
	 */
	private Map<String, String> attachmentMapping;

	private Optional<String> contextJPath = Optional.empty();

	private Optional<String> contextJson = Optional.empty();

	private Optional<List<String>> contextJsonList = Optional.empty();

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


	//TODO must annotate these, flow is still unintuitive

	public JvJDotAttacherBuilder withPathContext(String jPath) {
		this.contextJPath = Optional.of(jPath);
		this.contextJson = Optional.empty();
		this.contextJsonList = Optional.empty();
		return this;
	}

	public JvJDotAttacherBuilder withAdditionJsonContext(String json) {
		this.contextJson = Optional.of(json);
		this.contextJsonList = Optional.empty();
		this.contextJPath = Optional.empty();
		return this;
	}

	public JvJDotAttacherBuilder withAdditionJsonListContext(List<String> jsonList) {
		this.contextJsonList = Optional.of(jsonList);
		this.contextJson = Optional.empty();
		this.contextJPath = Optional.empty();
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

		JDotAttacher scAttacher;
		if (!this.contextJson.isPresent() && !this.contextJsonList.isPresent()) {
			scAttacher = JDotAttacher$.MODULE$
					.apply(scAttachmentMapping,
							jvOptionalToScOption(this.contextJPath),
							scTransformer,
							scAttachers);

		} else if (this.contextJson.isPresent()) {
			scAttacher = JDotAttacher$.MODULE$
					.withAdditionalJson(scAttachmentMapping,
										contextJson.get(),
										scTransformer,
										scAttachers);
		} else {
			scAttacher = JDotAttacher$.MODULE$
					.withAdditionalJsonList(scAttachmentMapping,
											jvToScList(contextJsonList.get()),
											scTransformer,
											scAttachers);
		}

		return new JvJDotAttacher(scAttacher);
	}

}
