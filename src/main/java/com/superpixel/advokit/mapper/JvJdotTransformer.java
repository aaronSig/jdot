package com.superpixel.advokit.mapper;

import static com.superpixel.jdot.util.ScalaConverters.*;

import java.util.List;

import com.superpixel.jdot.JdotTransformer;


public class JvJdotTransformer {

	private JdotTransformer scTransformer;

	public JvJdotTransformer(JdotTransformer scTransformer) {
		this.scTransformer = scTransformer;
	}

	public JdotTransformer getScTransformer() {
		return scTransformer;
	}

	public String transform(String json) {
		return scTransformer.transform(json,
				scTransformer.transform$default$2(),
				scTransformer.transform$default$3(),
				scTransformer.transform$default$4());
	}

	public String transformList(List<String> jsonList) {
		return scTransformer.transformList(jvToScList(jsonList),
				scTransformer.transform$default$2(),
				scTransformer.transform$default$3(),
				scTransformer.transform$default$4());
	}


	public String transform(String json, JvJdotSettings settings) {
		return scTransformer.transform(json,
				settings.attachments,
				settings.mergingJson,
				settings.inclusions);
	}
	
	public String transformList(List<String> jsonList, JvJdotSettings settings) {
		return scTransformer.transformList(jvToScList(jsonList),
				settings.attachments,
				settings.mergingJson,
				settings.inclusions);
	}
}
