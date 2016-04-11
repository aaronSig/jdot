package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.*;

import java.util.List;

import com.superpixel.jdot.JdotAttacher;


public class JvJdotAttacher {

	private JdotAttacher scAttacher;

	public JvJdotAttacher(JdotAttacher scAttacher) {
		this.scAttacher = scAttacher;
	}

	public JdotAttacher getScAttacher() {
		return scAttacher;
	}

	public String attachList(List<String> jsonListToAttach, String jsonAttachTo) {
		return scAttacher.attachList(jvToScList(jsonListToAttach), jsonAttachTo);
	}

	public String attach(String jsonToAttach, String jsonAttachTo) {
		return scAttacher.attach(jsonToAttach, jsonAttachTo);
	}

}
