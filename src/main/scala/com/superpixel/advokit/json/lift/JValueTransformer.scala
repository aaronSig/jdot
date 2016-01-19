package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper._

class JValueTransformer(fieldMap: Set[JPathPair], merges: MergingJson, inclusions: Inclusions) extends JsonContentTransformer {

  val builder = JValueBuilder(fieldMap.map(_.to))
  val mergingJValues: (Seq[JValue], Seq[JValue]) = transformDefaults(merges)
  val inclusionsMap: Map[String, JValue] = parseInclusions(inclusions)
  
  override def transform(json: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String = {
    compact(render(transformJValue(parse(json), localMerges, additionalInclusions)))
  }
  
  override def transformList(jsonList: List[String], localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String = {
    compact(render(
          JArray(jsonList.map { jsonStr: String => transformJValue(parse(jsonStr), localMerges, additionalInclusions)})
          ))
  }
  
  def transformJValueList(jsonList: List[JValue], localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue = {
    JArray(jsonList.map {json: JValue => transformJValue(json, localMerges, additionalInclusions)})
  }
  def transformJValue(json: JValue, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue = {
    val localMergingJValues = transformDefaults(localMerges)
    
    import JValueMerger.MergeArraysAsValues
    
    val leftMerge = JValueMerger.leftMerge(MergeArraysAsValues)_
    
    val postInJV = (localMergingJValues._1, mergingJValues._1) match {
      case (Nil, Nil) => json
      case (sq, Nil) => json +: sq reduceLeft(leftMerge)
      case (Nil, sq) => json +: sq reduceLeft(leftMerge)
      case (sq1, sq2) => (json +: sq1 reduceLeft(leftMerge)) +: sq2 reduceLeft(leftMerge)
    } 
      
    val preOutJV = builder.build((getValues(postInJV, additionalInclusions)))
    
    (localMergingJValues._2, mergingJValues._2) match {
      case (Nil, Nil) => preOutJV
      case (sq, Nil) => {
        (preOutJV +: sq) reduceLeft(leftMerge)
      }
      case (Nil, sq) => {
        (preOutJV +: sq) reduceLeft(leftMerge)
      }
      case (sq1, sq2) => {
        (preOutJV +: sq1 reduceLeft(leftMerge)) +: sq2 reduceLeft(leftMerge)
      }
    } 
  }
  
  private def getValues(json: JValue, addInclusions: Inclusions): Set[(JPath, JValue)] = {
    val addInclusionsMap: Map[String, JValue] = parseInclusions(addInclusions)
	  val accessor = JValueAccessor(json, (s: String) => addInclusionsMap.get(s).orElse(inclusionsMap.get(s)))

    fieldMap.map { jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from)) }
  }
  
  
  
  private def transformDefaults(stringMerges: MergingJson): (Seq[JValue], Seq[JValue]) = stringMerges match {
    case NoMerging => (Nil, Nil)
    case MergingJsonPre(pre) => (pre.map{(s: String) => parse(s)} , Nil)
    case MergingJsonPost(post) => (Nil, post.map{(s: String) => parse(s)})
    case MergingJsonPrePost(pre, post) => (pre.map{(s: String) => parse(s)}, post.map{(s: String) => parse(s)})
  }
  
  private def parseInclusions(inc: Inclusions): Map[String, JValue] = inc match {
    case NoInclusions => Map()
    case FixedInclusions(m) => m.map {
      case (key, jsonStr) => (key, parse(jsonStr))
    } 
  }
}

object JValueTransformer {
  
  def apply(fieldMap: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions) = new JValueTransformer(fieldMap, merges, inclusions)
  
}