package com.superpixel.jdot;

import java.util.*;
import com.google.common.base.Optional;

import com.superpixel.jdot.json4s.JValueTransformer;
import com.superpixel.jdot.pathing.JPathPair;

import static com.superpixel.jdot.util.ScalaConverters.*;

public class JvJDotTransformerBuilder {

  private Optional<Map<String, String>> inclusionsMap = Optional.absent();
  
  private Optional<List<String>> preMergingJsonOpt = Optional.absent();
  private Optional<List<String>> postMergingJsonOpt = Optional.absent();

  private List<JvJDotAttacher> attachers = new ArrayList<>();
  
  /***
   * Format Destination -> Source.
   * Destinations paths are keys with source paths as values.
   */
  private Map<String, String> pathMapping;
  
  /***
   * Declares the paths for the json transformation.
   * Format is Destination -> Source.
   * Destinations paths are keys with source paths as values.
   * 
   * For example, if the target class has a String field called 'name'.
   * Then mapping:
   *  'name' -> 'person.forename'
   * Will assign the 'name' attribute with the value in 'forename' field in the 'person' object in the json.
   * 
   * @param pathMapping
   * @return
   */
  public JvJDotTransformerBuilder withPathMapping(Map<String, String> pathMapping) {
    this.pathMapping = pathMapping;
    return this;
  }
  
  
  /***
   * Inclusions can be used by specifying a link in the path.
   * The path 'person.id>.friendCount' will get the 'id' from the 'person' object in the json.
   * It will look for this 'id' in the inclusions map, and then use the resulting json value for the rest of the path.
   * 
   * @param inclusionsMap
   * @return
   */
  public JvJDotTransformerBuilder withInclusionsMap(Map<String, String> inclusionsMap) {
    this.inclusionsMap = Optional.of(inclusionsMap);
    return this;
  }
  
  /***
   * This json is merged with the passed in json BEFORE each transformation.
   * The passed in json is favoured, any missed fields are taken from the default
   * (i.e. this json should be of the same format as those passed IN for transformation)
   * @param preMergingJson
   * @return
   */
  public JvJDotTransformerBuilder withPreJsonMerging(String... preMergingJson) {
    if (!preMergingJsonOpt.isPresent()) {
      preMergingJsonOpt = Optional.of((List<String>)new ArrayList<String>());
    }
    Collections.addAll(preMergingJsonOpt.get(), preMergingJson);
    return this;
  }
  
  /***
   * This json is merged with the resulting json AFTER each transformation.
   * The result json is favoured, any missed fields are taken from the default
   * (i.e. this json should be of the same format as the json that comes OUT of the transformation)
   * @param postMergingJson
   * @return
   */
  public JvJDotTransformerBuilder withPostJsonMerging(String... postMergingJson) {
    if (!postMergingJsonOpt.isPresent()) {
      postMergingJsonOpt = Optional.of((List<String>)new ArrayList<String>());
    }
    Collections.addAll(postMergingJsonOpt.get(), postMergingJson);
    return this;
  }

  public JvJDotTransformerBuilder withAttachers(JvJDotAttacher... attachers) {
    Collections.addAll(this.attachers, attachers);
    return this;
  }

  public JvJDotTransformerBuilder addAttacher(JvJDotAttacher attacher) {
    attachers.add(attacher);
    return this;
  }
  
  /***
   * Extracts from the settings the inclusions, preJsonMerging and postMergingJson.
   * Ignores any attachment settings.
   * 
   * 
   * 
   * @param settings
   * @return
   */
  public JvJDotTransformer buildWithSettings(JvJDotSettings settings) {
	scala.collection.immutable.Set<JPathPair> scPathMapping = jvStringMapToJPathPairSet(pathMapping);
	    
    Inclusions scIncMap;
    if (inclusionsMap.isPresent()) {
      scIncMap = MappingParameters.combineInclusions(new FixedInclusions(jvToScMap(inclusionsMap.get())), settings.inclusions);
    } else {
      scIncMap = settings.inclusions;
    }
    MergingJson scMergJson;
    if (preMergingJsonOpt.isPresent() && postMergingJsonOpt.isPresent()) {
      scMergJson = MappingParameters.combineMergingJson(new MergingJsonPrePost(jvListToScSeq(preMergingJsonOpt.get()), jvListToScSeq(postMergingJsonOpt.get())), settings.mergingJson);
    } else if (preMergingJsonOpt.isPresent()) {
      scMergJson = MappingParameters.combineMergingJson(new MergingJsonPre(jvListToScSeq(preMergingJsonOpt.get())), settings.mergingJson);
    } else if (postMergingJsonOpt.isPresent()) {
      scMergJson = MappingParameters.combineMergingJson(new MergingJsonPost(jvListToScSeq(postMergingJsonOpt.get())), settings.mergingJson);
    } else {
      scMergJson = settings.mergingJson;
    }

    scala.collection.immutable.List scAttachers;
    if (!attachers.isEmpty()) {
      List<JDotAttacher> scAttachersJv = new ArrayList<>();
      for (JvJDotAttacher att : attachers) scAttachersJv.add(att.getScAttacher());

      scala.collection.immutable.List scA = jvToScList(scAttachersJv);
      if (!settings.attachers.isEmpty()) {
        scAttachers = settings.attachers.$colon$colon$colon(scA);
      } else {
        scAttachers = scA;
      }
    } else {
      scAttachers = settings.attachers;
    }

    
    JDotTransformer scTransformer = JDotTransformer$.MODULE$.apply(scPathMapping, scAttachers, scMergJson, scIncMap);
    return new JvJDotTransformer(scTransformer);
  }
  
  public JvJDotTransformer build() {

    scala.collection.immutable.Set<JPathPair> scPathMapping = jvStringMapToJPathPairSet(pathMapping);
    
    Inclusions scIncMap;
    if (inclusionsMap.isPresent()) {
      scIncMap = new FixedInclusions(jvToScMap(inclusionsMap.get()));
    } else {
      scIncMap = JDotTransformer$.MODULE$.apply$default$4();
    }
    MergingJson scMergJson;
    if (preMergingJsonOpt.isPresent() && postMergingJsonOpt.isPresent()) {
      scMergJson = new MergingJsonPrePost(jvListToScSeq(preMergingJsonOpt.get()), jvListToScSeq(postMergingJsonOpt.get()));
    } else if (preMergingJsonOpt.isPresent()) {
      scMergJson = new MergingJsonPre(jvListToScSeq(preMergingJsonOpt.get()));
    } else if (postMergingJsonOpt.isPresent()) {
      scMergJson = new MergingJsonPost(jvListToScSeq(postMergingJsonOpt.get()));
    } else {
      scMergJson = JDotTransformer$.MODULE$.apply$default$3();
    }

    scala.collection.immutable.List scAttachers;
    if (!attachers.isEmpty()) {
      List<JDotAttacher> scAttachersJv = new ArrayList<>();
      for (JvJDotAttacher att : attachers) scAttachersJv.add(att.getScAttacher());

      scAttachers = jvToScList(scAttachersJv);
    } else {
      scAttachers = JDotTransformer$.MODULE$.apply$default$2();
    }
    
    JDotTransformer scTransformer = JDotTransformer$.MODULE$.apply(scPathMapping, scAttachers, scMergJson, scIncMap);
    return new JvJDotTransformer(scTransformer);
  }
}
