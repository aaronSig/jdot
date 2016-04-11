package com.superpixel.advokit.mapper;

import com.superpixel.jdot.JdotMapper;


public class JvJdotMapper<T> {

	private JdotMapper<T> scMapper;
	
	public JvJdotMapper(JdotMapper<T> scMapper) {
		this.scMapper = scMapper;
	}
	
	public T map(String json) {
		return scMapper.map(json, scMapper.map$default$2(), scMapper.map$default$3(), scMapper.map$default$4());
	}
	
	
	public T map(String json, JvJdotSettings settings) {
		return scMapper.map(json, settings.attachments, settings.mergingJson, settings.inclusions);
	}
}
