package com.superpixel.jdot;

import com.superpixel.jdot.json4s.JValueAttachmentApplier;

public class JvJDotAttachment {

	 public static String applyAttachments(String json, JvJDotSettings settings) {
		  return JValueAttachmentApplier.applyAttachments(json,
												  settings.attachments,
												  settings.mergingJson,
												  settings.inclusions);
	 }
	
}
