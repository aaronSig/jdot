package com.superpixel.jdot;

import static com.superpixel.jdot.util.ScalaConverters.*;

import java.util.List;

import com.superpixel.jdot.JDotTransformer;


public class JvJDotTransformer {

	private JDotTransformer scTransformer;
	
	static public JvJDotTransformerBuilder builder() {
		return new JvJDotTransformerBuilder();
	}

	public JvJDotTransformer(JDotTransformer scTransformer) {
		this.scTransformer = scTransformer;
	}

	public JDotTransformer getScTransformer() {
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


	public String transform(String json, JvJDotSettings settings) {
		return scTransformer.transform(json,
				settings.attachers,
				settings.mergingJson,
				settings.inclusions);
	}
	
	public String transformList(List<String> jsonList, JvJDotSettings settings) {
		return scTransformer.transformList(jvToScList(jsonList),
				settings.attachers,
				settings.mergingJson,
				settings.inclusions);
	}
}
