package com.superpixel.advokit.mapper;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.superpixel.jdot.JdotExtractor;
import com.superpixel.jdot.JdotExtractor$;
import com.superpixel.jdot.JdotMapper;
import com.superpixel.jdot.JdotMapper$;
import com.superpixel.jdot.JdotTransformer;

import static com.superpixel.jdot.util.ScalaConverters.*;


public class JvJdotMapperBuilder {


	private JvJdotTransformerBuilder transformerBuilder = new JvJdotTransformerBuilder();
	
	private Optional<String> typeHintFieldName = Optional.empty();
	private Optional<java.util.List<Class<?>>> typeHintList = Optional.empty();
	
	/***
	 * Declares the paths for the json transformation.
	 * Format is Destination -> Source.
	 * Destinations paths are keys with source paths as values.
	 * 
	 * For example, if the target class has a String field called 'name'.
	 * Then mapping:
	 * 	'name' -> 'person.forename'
	 * Will assign the 'name' attribute with the value in 'forename' field in the 'person' object in the json.
	 * 
	 * @param pathMapping
	 * @return
	 */
	public JvJdotMapperBuilder withPathMapping(Map<String, String> pathMapping) {
		transformerBuilder.withPathMapping(pathMapping);
		return this;
	}
	
	/***
	 * Inclusions can be used by sepcifying a link in the path.
	 * The path 'person.id>.friendCount' will get the 'id' from the 'person' object in the json.
	 * It will look for this 'id' in the inclusions map, and then use the resulting json value for the rest of the path.
	 * 
	 * @param inclusionsMap
	 * @return
	 */
	public JvJdotMapperBuilder withInclusionsMap(Map<String, String> inclusionsMap) {
		transformerBuilder.withInclusionsMap(inclusionsMap);
		return this;
	}
	
	/***
	 * This json is merged with the passed in json BEFORE each transformation.
	 * The passed in json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as those passed IN for transformation)
	 * @param defaultInJson
	 * @return
	 */
	public JvJdotMapperBuilder withPreJsonMerging(String... preMergingJson) {
		transformerBuilder.withPreJsonMerging(preMergingJson);
		return this;
	}
	
	/***
	 * This json is merged with the resulting json AFTER each transformation.
	 * The result json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as the json that comes OUT of the transformation)
	 * @param defaultOutJson
	 * @return
	 */
	public JvJdotMapperBuilder withPostJsonMerging(String... postMergingJson) {
		transformerBuilder.withPostJsonMerging(postMergingJson);
		return this;
	}
	
	/***
	 * If you are building with build(Class<T> targetClass) and the targetClass you are extracting contains interface or abstract fields,
	 * you will need to add the implementing class to the typeHintList and ensure that the json corresponding to the field contains the typeHintFieldName,
	 * which should hold the name of the implementing class. For example with the classes:
	 * 
	 * public class Pet {
	 * 	 private Animal animal;
	 * 	 ...
	 * }
	 * 
	 * public class Dog implements Animal {
	 * 	 private String breed;
	 * 	 ...
	 * }
	 * 
	 * Dog.class must be added to typeHintList
	 * and the json (after transformation) must contain your typeHintFieldName (or the default if not set: "_t"} as follows:
	 * 
	 * {
	 * 	 "animal" : {
	 * 		"{yourTypeHintFieldNameHere}" : "Dog",
	 * 		"breed" : "Corgi"
	 *   }
	 * }
	 *  
	 * @param typeHintList
	 * @return
	 */
	public JvJdotMapperBuilder withTypeHintList(java.util.List<Class<?>> typeHintList) {
		this.typeHintList = Optional.ofNullable(typeHintList);
		return this;
	}
	
	/***
	 * Default typeHintFieldName is "_t"
	 * 
	 * If you are building with build(Class<T> targetClass) and the targetClass you are extracting contains interface or abstract fields,
	 * you will need to add the implementing class to the typeHintList and ensure that the json corresponding to the field contains the typeHintFieldName,
	 * which should hold the name of the implementing class. For example with the classes:
	 * 
	 * public class Pet {
	 * 	 private Animal animal;
	 * 	 ...
	 * }
	 * 
	 * public class Dog implements Animal {
	 * 	 private String breed;
	 * 	 ...
	 * }
	 * 
	 * Dog.class must be added to typeHintList
	 * and the json (after transformation) must contain your typeHintFieldName (or the default if not set: "_t"} as follows:
	 * 
	 * {
	 * 	 "animal" : {
	 * 		"{yourTypeHintFieldNameHere}" : "Dog",
	 * 		"breed" : "Corgi"
	 *   }
	 * }
	 * 
	 * @param typeHintList
	 * @return
	 */
	public JvJdotMapperBuilder withTypeHintFieldName(String typeHintFieldName) {
		this.typeHintFieldName = Optional.ofNullable(typeHintFieldName);
		return this;
	}
	
	public <T> JvJdotMapper<T> build(Function<String, T> extractFunction) {
		scala.Function1<String, T> f = new scala.runtime.AbstractFunction1<String, T>() {
		    public T apply(String json) {
		        return extractFunction.apply(json);
		    }
		};
		JdotExtractor<T> scExtractor = JdotExtractor$.MODULE$.apply(f);
		JdotTransformer scTransformer = this.buildTransformer();
		
		JdotMapper<T> scMapper = JdotMapper$.MODULE$.apply(scTransformer, scExtractor);
		return new JvJdotMapper<T>(scMapper);
	}
	
	public <T> JvJdotMapper<T> build(Class<T> targetClass) {

 		scala.collection.immutable.List<Class<?>> scTypeHintList;
 		if (typeHintList.isPresent()) {
 			scTypeHintList = jvToScList(typeHintList.get());
 		} else {
 			scTypeHintList = JdotExtractor$.MODULE$.forClass$default$2();
 		}
 		
 		scala.Option<String> scTypeHintFieldName;
 		if (typeHintFieldName.isPresent()) {
 			scTypeHintFieldName = scala.Option$.MODULE$.apply(typeHintFieldName.get());
 		} else {
 			scTypeHintFieldName = JdotExtractor$.MODULE$.forClass$default$3();
 		}

		JdotExtractor<T> scExtractor = JdotExtractor$.MODULE$.forClass(targetClass, scTypeHintList, scTypeHintFieldName);
		
		JdotTransformer scTransformer = this.buildTransformer();
		
		JdotMapper<T> scMapper = JdotMapper$.MODULE$.apply(scTransformer, scExtractor);
		return new JvJdotMapper<T>(scMapper);
	}
	
	private JdotTransformer buildTransformer() {
		return this.transformerBuilder.build().getScTransformer();
	}
}
