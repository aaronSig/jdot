package com.superpixel.jdot;

import static com.superpixel.jdot.util.ScalaConverters.*;

import java.util.List;
import java.util.stream.Collectors;

import com.superpixel.jdot.JDotAttacher;


public class JvJDotAttacher {
	
	private JDotAttacher scAttacher;

	static public JvJDotAttacherBuilder builder() {
		return new JvJDotAttacherBuilder();
	}

	static public String applyAttachers(String context, String attachTo, List<JvJDotAttacher> attachers) {
		return JDotAttacher$.MODULE$.applyAttachers(context, attachTo, jvToScList(attachers.stream().map(a -> a.getScAttacher()).collect(Collectors.toList())));
	}

	public JvJDotAttacher(JDotAttacher scAttacher) {
		this.scAttacher = scAttacher;
	}

	public JDotAttacher getScAttacher() {
		return scAttacher;
	}

	public String attachList(List<String> contextJsonList, String jsonAttachTo) {
		return scAttacher.attachList(jvToScList(contextJsonList), jsonAttachTo);
	}

	public String attach(String contextJsonList, String jsonAttachTo) {
		return scAttacher.attach(contextJsonList, jsonAttachTo);
	}

}
