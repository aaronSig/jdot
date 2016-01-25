package com.superpixel.advokit.mapper;

import com.superpixel.advokit.json.lift.JValueAttachment;

public class JvContentAttachment {

	 public static String applyAttachments(String json, JvContentSettings settings) {
		  return JValueAttachment.applyAttachments(json, 
												  settings.attachments,
												  settings.mergingJson,
												  settings.inclusions);
	 }
	
}
