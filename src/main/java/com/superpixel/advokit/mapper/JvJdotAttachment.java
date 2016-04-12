package com.superpixel.advokit.mapper;

import com.superpixel.jdot.json4s.JValueAttachment;

public class JvJDotAttachment {

	 public static String applyAttachments(String json, JvJDotSettings settings) {
		  return JValueAttachment.applyAttachments(json, 
												  settings.attachments,
												  settings.mergingJson,
												  settings.inclusions);
	 }
	
}
