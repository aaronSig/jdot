package com.superpixel.jdot;

import com.superpixel.jdot.Attachment;
import com.superpixel.jdot.Inclusions;
import com.superpixel.jdot.MergingJson;

import scala.collection.immutable.List;

public class JvJDotSettings {

	Inclusions inclusions;
	MergingJson mergingJson;
	scala.collection.immutable.List<Attachment> attachments;
	
	static public JvJDotSettingsBuilder builder() {
		return new JvJDotSettingsBuilder();
	}
	
	JvJDotSettings(Inclusions inclusions, MergingJson mergingJson,
			List<Attachment> attachments) {
		super();
		this.inclusions = inclusions;
		this.mergingJson = mergingJson;
		this.attachments = attachments;
	}
}
