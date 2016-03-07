package com.superpixel.advokit.mapper;

import com.superpixel.advokit.json.lift.JValueTraverser;

public class JvContentTraverser {

	public Boolean pathExistsAndIsNotFalse(String path, String content) {
		return JValueTraverser.existsAndNotFalse(content, path);
	}
	
}
