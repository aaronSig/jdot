package com.superpixel.advokit.mapper;

import com.superpixel.advokit.json.lift.JValueTraverser;

public class JvContentTraverser {

	public Boolean pathExistsOrIsNotFalse(String path, String content) {
		return JValueTraverser.existsOrNotFalse(content, path);
	}
	
}
