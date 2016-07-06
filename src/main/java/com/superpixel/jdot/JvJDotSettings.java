package com.superpixel.jdot;


import java.util.List;

public class JvJDotSettings {

	Inclusions inclusions;
	MergingJson mergingJson;
	scala.collection.immutable.List<JDotAttacher> attachers;
	
	static public JvJDotSettingsBuilder builder() {
		return new JvJDotSettingsBuilder();
	}
	
	JvJDotSettings(Inclusions inclusions, MergingJson mergingJson,
				   scala.collection.immutable.List<JDotAttacher> scAttachments) {
		super();
		this.inclusions = inclusions;
		this.mergingJson = mergingJson;
		this.attachers = scAttachments;
	}
}
