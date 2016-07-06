package com.superpixel.jdot;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.superpixel.jdot.JDotExtractor;
import com.superpixel.jdot.JDotExtractor$;
import com.superpixel.jdot.JDotMapper;
import com.superpixel.jdot.JDotMapper$;
import com.superpixel.jdot.JDotTransformer;

import static com.superpixel.jdot.util.ScalaConverters.*;


public class JvJDotMapperBuilder {


	private JvJDotTransformerBuilder transformerBuilder = new JvJDotTransformerBuilder();
	
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
	public JvJDotMapperBuilder withPathMapping(Map<String, String> pathMapping) {
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
	public JvJDotMapperBuilder withInclusionsMap(Map<String, String> inclusionsMap) {
		transformerBuilder.withInclusionsMap(inclusionsMap);
		return this;
	}
	
	/***
	 * This json is merged with the passed in json BEFORE each transformation.
	 * The passed in json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as those passed IN for transformation)
	 * @param preMergingJson
	 * @return
	 */
	public JvJDotMapperBuilder withPreJsonMerging(String... preMergingJson) {
		transformerBuilder.withPreJsonMerging(preMergingJson);
		return this;
	}
	
	/***
	 * This json is merged with the resulting json AFTER each transformation.
	 * The result json is favoured, any missed fields are taken from the default
	 * (i.e. this json should be of the same format as the json that comes OUT of the transformation)
	 * @param postMergingJson
	 * @return
	 */
	public JvJDotMapperBuilder withPostJsonMerging(String... postMergingJson) {
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
	public JvJDotMapperBuilder withTypeHintList(java.util.List<Class<?>> typeHintList) {
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
	 * @param typeHintFieldName
	 * @return
	 */
	public JvJDotMapperBuilder withTypeHintFieldName(String typeHintFieldName) {
		this.typeHintFieldName = Optional.ofNullable(typeHintFieldName);
		return this;
	}
	
	public <T> JvJDotMapper<T> build(Function<String, T> extractFunction) {
		scala.Function1<String, T> f = new scala.runtime.AbstractFunction1<String, T>() {
		    public T apply(String json) {
		        return extractFunction.apply(json);
		    }
		};
		JDotExtractor<T> scExtractor = JDotExtractor$.MODULE$.apply(f);
		JDotTransformer scTransformer = this.buildTransformer();
		
		JDotMapper<T> scMapper = JDotMapper$.MODULE$.apply(scTransformer, scExtractor);
		return new JvJDotMapper<T>(scMapper);
	}
	
	public <T> JvJDotMapper<T> build(Class<T> targetClass) {

 		scala.collection.immutable.List<Class<?>> scTypeHintList;
 		if (typeHintList.isPresent()) {
 			scTypeHintList = jvToScList(typeHintList.get());
 		} else {
 			scTypeHintList = JDotExtractor$.MODULE$.forClass$default$2();
 		}
 		
 		scala.Option<String> scTypeHintFieldName;
 		if (typeHintFieldName.isPresent()) {
 			scTypeHintFieldName = scala.Option$.MODULE$.apply(typeHintFieldName.get());
 		} else {
 			scTypeHintFieldName = JDotExtractor$.MODULE$.forClass$default$3();
 		}

		JDotExtractor<T> scExtractor = JDotExtractor$.MODULE$.forClass(targetClass, scTypeHintList, scTypeHintFieldName);
		
		JDotTransformer scTransformer = this.buildTransformer();
		
		JDotMapper<T> scMapper = JDotMapper$.MODULE$.apply(scTransformer, scExtractor);
		return new JvJDotMapper<T>(scMapper);
	}
	
	private JDotTransformer buildTransformer() {
		return this.transformerBuilder.build().getScTransformer();
	}
}
