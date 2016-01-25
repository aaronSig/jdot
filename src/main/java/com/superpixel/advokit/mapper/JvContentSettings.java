package com.superpixel.advokit.mapper;

import scala.collection.immutable.List;

public class JvContentSettings {

	Inclusions inclusions;
	MergingJson mergingJson;
	scala.collection.immutable.List<Attachment> attachments;
	
	JvContentSettings(Inclusions inclusions, MergingJson mergingJson,
			List<Attachment> attachments) {
		super();
		this.inclusions = inclusions;
		this.mergingJson = mergingJson;
		this.attachments = attachments;
	}
}
