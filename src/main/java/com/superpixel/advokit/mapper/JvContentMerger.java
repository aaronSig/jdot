package com.superpixel.advokit.mapper;

import com.superpixel.advokit.json.lift.JValueMerger;

public class JvContentMerger {

	public String leftMergeWithArraysAsValues(String leftJson, String rightJson) {
		return JValueMerger.leftMergeWithArraysAsValues(leftJson, rightJson);
	}

	public String leftMergeWithArraysOnIndex(String leftJson, String rightJson) {
		return JValueMerger.leftMergeWithArraysOnIndex(leftJson, rightJson);
	}
}
