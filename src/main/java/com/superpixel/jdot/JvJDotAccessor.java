package com.superpixel.jdot;

import static com.superpixel.jdot.util.ScalaConverters.jvToScMap;

import java.util.Map;
import java.util.Optional;

import com.superpixel.jdot.pathing.JPath;
import com.superpixel.jdot.pathing.JPath$;

import static com.superpixel.jdot.util.ScalaConverters.*;

public class JvJDotAccessor {

	private JDotAccessor scAccessor;
	
	public JvJDotAccessor(String json, Map<String, String> inclusions) {
		scAccessor = JDotAccessor$.MODULE$.apply(json, new FixedInclusions(jvToScMap(inclusions)));
	}
	
	public JvJDotAccessor(String json) {
		scAccessor = JDotAccessor$.MODULE$.apply(json);
	}

	public JDotAccessor getScAccessor() {
		return scAccessor;
	}
	
	public Optional<Number> getNumber(String jPath) {
		return scOptionToJvOptional(scAccessor.getNumber(stringToJPath(jPath)));
	}
	
	public Optional<Boolean> getBoolean(String jPath) {
		return scOptionBooleanToJvOptionalBoolean(scAccessor.getBoolean(stringToJPath(jPath)));
	}
	
	public Optional<String> getString(String jPath) {
		return scOptionToJvOptional(scAccessor.getString(stringToJPath(jPath)));
	}
	
	public Optional<String> getValueAsString(String jPath) {
		return scOptionToJvOptional(scAccessor.getValueAsString(stringToJPath(jPath)));
	}
	
	public Optional<String> getJsonString(String jPath) {
		return scOptionToJvOptional(scAccessor.getJsonString(stringToJPath(jPath)));
	}

	private JPath stringToJPath(String jPath) {
		return JPath$.MODULE$.fromString(jPath);
	}
	
}
