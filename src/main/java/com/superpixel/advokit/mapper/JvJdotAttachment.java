package com.superpixel.advokit.mapper;

import com.superpixel.jdot.json4s.JValueAttachment;

public class JvJdotAttachment {

	 public static String applyAttachments(String json, JvJdotSettings settings) {
		  return JValueAttachment.applyAttachments(json, 
												  settings.attachments,
												  settings.mergingJson,
												  settings.inclusions);
	 }
	
}
