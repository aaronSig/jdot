package com.superpixel.jdot;

import com.superpixel.jdot.JDotMapper;


public class JvJDotMapper<T> {

	private JDotMapper<T> scMapper;
	
	static public JvJDotMapperBuilder builder() {
		return new JvJDotMapperBuilder();
	}
	
	public JvJDotMapper(JDotMapper<T> scMapper) {
		this.scMapper = scMapper;
	}
	
	public T map(String json) {
		return scMapper.map(json, scMapper.map$default$2(), scMapper.map$default$3(), scMapper.map$default$4());
	}
	
	
	public T map(String json, JvJDotSettings settings) {
		return scMapper.map(json, settings.attachments, settings.mergingJson, settings.inclusions);
	}
}
