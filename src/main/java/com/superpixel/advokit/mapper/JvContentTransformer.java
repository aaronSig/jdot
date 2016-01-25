package com.superpixel.advokit.mapper;

import static com.superpixel.advokit.ScalaConverters.*;

import java.util.List;


public class JvContentTransformer {

	private JsonContentTransformer scTransformer;

	public JvContentTransformer(JsonContentTransformer scTransformer) {
		this.scTransformer = scTransformer;
	}

	public JsonContentTransformer getScTransformer() {
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


	public String transform(String json, JvContentSettings settings) {
		return scTransformer.transform(json,
				settings.attachments,
				settings.mergingJson,
				settings.inclusions);
	}
	
	public String transformList(List<String> jsonList, JvContentSettings settings) {
		return scTransformer.transformList(jvToScList(jsonList),
				settings.attachments,
				settings.mergingJson,
				settings.inclusions);
	}
}
