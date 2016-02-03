package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.*;

import java.util.List;


public class JvContentAttacher {

	private JsonContentAttacher scAttacher;

	public JvContentAttacher(JsonContentAttacher scAttacher) {
		this.scAttacher = scAttacher;
	}

	public JsonContentAttacher getScAttacher() {
		return scAttacher;
	}

	public String attachList(List<String> jsonListToAttach, String jsonAttachTo) {
		return scAttacher.attachList(jvToScList(jsonListToAttach), jsonAttachTo);
	}

	public String attach(String jsonToAttach, String jsonAttachTo) {
		return scAttacher.attach(jsonToAttach, jsonAttachTo);
	}

}