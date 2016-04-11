package com.superpixel.advokit.mapper;

import com.superpixel.jdot.json4s.JValueTraverser;

public class JvJdotTraverser {

	public Boolean pathExistsAndIsNotFalse(String path, String content) {
		return JValueTraverser.existsAndNotFalse(content, path);
	}
	
}
