package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper.JsonContentTransformer
import com.superpixel.advokit.mapper.NoInclusions
import com.superpixel.advokit.mapper.NoDefaultJson
import com.superpixel.advokit.mapper.DefaultJson
import com.superpixel.advokit.mapper.Inclusions
import com.superpixel.advokit.mapper.DefaultJsonIn
import com.superpixel.advokit.mapper.DefaultJsonOut
import com.superpixel.advokit.mapper.DefaultJsonInOut
import com.superpixel.advokit.mapper.FixedInclusions

class JValueTransformer(fieldMap: Set[JPathPair], defaults: DefaultJson, inclusions: Inclusions) extends JsonContentTransformer {

  val builder = JValueBuilder(fieldMap.map(_.to))
  val defaultJValues: (JValue, JValue) = transformDefaults(defaults)
  val inclusionsMap: Map[String, JValue] = parseInclusions(inclusions)
  
  override def transform(json: String, localDefaults: DefaultJson = NoDefaultJson, additionalInclusions: Inclusions = NoInclusions): String = {
    pretty(render(transformJValue(parse(json), localDefaults, additionalInclusions)))
  }
  
  def transformJValue(json: JValue, localDefaults: DefaultJson = NoDefaultJson, additionalInclusions: Inclusions = NoInclusions): JValue = {
    val localDefaultJValues = transformDefaults(localDefaults)
    
    import JValueMerger.{leftMergeWithArraysAsValues => leftMerge}
    
    val postInJV = Seq(json, localDefaultJValues._1, defaultJValues._1) reduceLeft(leftMerge)
    val preOutJV = builder.build((getValues(postInJV, additionalInclusions)))
    Seq(preOutJV, localDefaultJValues._2, defaultJValues._2) reduceLeft(leftMerge)
  }
  
  private def getValues(json: JValue, addInclusions: Inclusions): Set[(JPath, JValue)] = {
    val addInclusionsMap: Map[String, JValue] = parseInclusions(addInclusions)
	  val accessor = JValueAccessor(json, (s: String) => addInclusionsMap.get(s).orElse(inclusionsMap.get(s)))

    fieldMap.map { jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from)) }
  }
  
  
  
  private def transformDefaults(stringDefaults: DefaultJson): (JValue, JValue) = stringDefaults match {
    case NoDefaultJson => (JNothing, JNothing)
    case DefaultJsonIn(in) => (parse(in), JNothing)
    case DefaultJsonOut(out) => (JNothing, parse(out))
    case DefaultJsonInOut(in, out) => (parse(in), parse(out))
  }
  
  private def parseInclusions(inc: Inclusions): Map[String, JValue] = inc match {
    case NoInclusions => Map()
    case FixedInclusions(m) => m.map {
      case (key, jsonStr) => (key, parse(jsonStr))
    } 
  }
}

object JValueTransformer {
  
  def apply(fieldMap: Set[JPathPair], defaults: DefaultJson = NoDefaultJson, inclusions: Inclusions = NoInclusions) = new JValueTransformer(fieldMap, defaults, inclusions)
  
}