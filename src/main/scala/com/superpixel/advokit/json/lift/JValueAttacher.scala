package com.superpixel.advokit.json.lift

import com.superpixel.advokit.mapper._
import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._


class JValueAttacher(transformer: JValueTransformer, attachmentPairs: Set[JPathPair]) extends JsonContentAttacher {

  val attachBuilder = JValueBuilder(attachmentPairs.map(_.to))
  
  def attach(transformAttachJson: String, attachToJson: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String = {
    compact(render(attachJValue(parse(transformAttachJson), parse(attachToJson), localMerges, additionalInclusions)))
  }
  
  def attachList(transformAttachJsonList: List[String], attachToJson: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String = {
    compact(render(attachJValueList(transformAttachJsonList.map{s => parse(s)}, parse(attachToJson), localMerges, additionalInclusions)))
  }
  
  def attachJValue(transformAttachJson: JValue, attachToJson: JValue, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue = {
    val transformed = transformer.transformJValue(transformAttachJson, localMerges, additionalInclusions);
    postTransform(transformed, attachToJson);
  }
  
  def attachJValueList(transformAttachJsonList: List[JValue], attachToJson: JValue, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue = {
    val transformed = transformer.transformJValueList(transformAttachJsonList, localMerges, additionalInclusions);
    postTransform(transformed, attachToJson);
  }
  
  private def postTransform(attachJson: JValue, attachToJson: JValue): JValue = {
    
    val accessor = JValueAccessor(attachJson)
    val attachee = attachBuilder.build(attachmentPairs.map{ jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from))});
    
//    println("Attachee:\n " + compact(render(attachee)))
//    println("AttachTo:\n " + compact(render(attachToJson)))
    
    
    import JValueMerger.{MergeArraysOnIndex, leftMerge}
//    val a = leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
//    println("Attached:\n " + compact(render(a)))
//    a
    leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
  }

}


object JValueAttacher {
  
  def apply(transformer: JValueTransformer, attachmentPairs: Set[JPathPair]): JValueAttacher = new JValueAttacher(transformer, attachmentPairs)
  
}