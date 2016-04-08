package com.superpixel.advokit.mapper;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.superpixel.jdot.JsonContentExtractor;
import com.superpixel.jdot.JsonContentExtractor$;
import com.superpixel.jdot.JsonContentMapper;
import com.superpixel.jdot.JsonContentMapper$;
import com.superpixel.jdot.JsonContentTransformer;

import static com.superpixel.jdot.util.ScalaConverters.*;


public class JvContentMapperBuilder {


	private JvContentTransformerBuilder transformerBuilder = new JvContentTransformerBuilder();
	
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
	public JvContentMapperBuilder withPathMapping(Map<String, String> pathMapping) {
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
	public JvContentMapperBuilder withInclusionsMap(Map<String, String> inclusionsMap) {
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
	public JvContentMapperBuilder withPreJsonMerging(String... preMergingJson) {
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
	public JvContentMapperBuilder withPostJsonMerging(String... postMergingJson) {
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
	public JvContentMapperBuilder withTypeHintList(java.util.List<Class<?>> typeHintList) {
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
	public JvContentMapperBuilder withTypeHintFieldName(String typeHintFieldName) {
		this.typeHintFieldName = Optional.ofNullable(typeHintFieldName);
		return this;
	}
	
	public <T> JvContentMapper<T> build(Function<String, T> extractFunction) {
		scala.Function1<String, T> f = new scala.runtime.AbstractFunction1<String, T>() {
		    public T apply(String json) {
		        return extractFunction.apply(json);
		    }
		};
		JsonContentExtractor<T> scExtractor = JsonContentExtractor$.MODULE$.apply(f);
		JsonContentTransformer scTransformer = this.buildTransformer();
		
		JsonContentMapper<T> scMapper = JsonContentMapper$.MODULE$.apply(scTransformer, scExtractor);
		return new JvContentMapper<T>(scMapper);
	}
	
	public <T> JvContentMapper<T> build(Class<T> targetClass) {

 		scala.collection.immutable.List<Class<?>> scTypeHintList;
 		if (typeHintList.isPresent()) {
 			scTypeHintList = jvToScList(typeHintList.get());
 		} else {
 			scTypeHintList = JsonContentExtractor$.MODULE$.forClass$default$2();
 		}
 		
 		scala.Option<String> scTypeHintFieldName;
 		if (typeHintFieldName.isPresent()) {
 			scTypeHintFieldName = scala.Option$.MODULE$.apply(typeHintFieldName.get());
 		} else {
 			scTypeHintFieldName = JsonContentExtractor$.MODULE$.forClass$default$3();
 		}

		JsonContentExtractor<T> scExtractor = JsonContentExtractor$.MODULE$.forClass(targetClass, scTypeHintList, scTypeHintFieldName);
		
		JsonContentTransformer scTransformer = this.buildTransformer();
		
		JsonContentMapper<T> scMapper = JsonContentMapper$.MODULE$.apply(scTransformer, scExtractor);
		return new JvContentMapper<T>(scMapper);
	}
	
	private JsonContentTransformer buildTransformer() {
		return this.transformerBuilder.build().getScTransformer();
	}
}
