package com.superpixel.jdot;

import com.superpixel.jdot.json4s.JValueMerger;

public class JvJDotMerger {

	public String leftMergeWithArraysAsValues(String leftJson, String rightJson) {
		return JValueMerger.leftMergeStrings(JValueMerger.mergeArraysAsValues(), leftJson, rightJson);
	}

	public String leftMergeWithArraysOnIndex(String leftJson, String rightJson) {
		return JValueMerger.leftMergeStrings(JValueMerger.mergeArraysOnIndex(), leftJson, rightJson);
	}
}
