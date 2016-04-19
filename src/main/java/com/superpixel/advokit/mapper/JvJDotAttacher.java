package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.*;

import java.util.List;

import com.superpixel.jdot.JDotAttacher;


public class JvJDotAttacher {

	private JDotAttacher scAttacher;

	public JvJDotAttacher(JDotAttacher scAttacher) {
		this.scAttacher = scAttacher;
	}

	public JDotAttacher getScAttacher() {
		return scAttacher;
	}

	public String attachList(List<String> jsonListToAttach, String jsonAttachTo) {
		return scAttacher.attachList(jvToScList(jsonListToAttach), jsonAttachTo);
	}

	public String attach(String jsonToAttach, String jsonAttachTo) {
		return scAttacher.attach(jsonToAttach, jsonAttachTo);
	}

}
