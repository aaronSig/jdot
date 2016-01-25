package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper._

object JValueAttachment {

  def applyAttachments(json: String, attachments: List[Attachment], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions): String = 
    compact(render(applyAttachmentsToJValue(parse(json), attachments, merges, inclusions)))
    
  def applyAttachmentsToJValue(json: JValue, attachments: List[Attachment], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions): JValue = {
    val mergingJValues = JValueMerger.transfromMergingJson(merges)
    
    import JValueMerger.MergeArraysAsValues
    val leftMerge = JValueMerger.leftMerge(MergeArraysAsValues)_
    
    val postInJV = mergingJValues._1 match {
      case Nil => json
      case seq => (json +: seq) reduceLeft(leftMerge)
    }
      
    val preOutJV = attachments.foldLeft(postInJV)(applyAttachmentToJValue(inclusions)_)
    
    val outJV    = mergingJValues._2 match {
      case Nil => preOutJV
      case seq => (preOutJV +: seq) reduceLeft(leftMerge)
    }
    outJV
  }
  
  def applyAttachment(inclusions: Inclusions)(json: String, attachment: Attachment): String = 
    compact(render(applyAttachmentToJValue(inclusions)(parse(json), attachment)))
  def applyAttachmentToJValue(inclusions: Inclusions)(json: JValue, attachment: Attachment): JValue = attachment match {
      case SimpleAttachment(jStr, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(jStr), json)
      case SimpleAttachment(jStr, attacher) =>
        parse(attacher.attach(jStr, compact(render(json))))
        
      case SimpleListAttachment(jStrLs, attacher: JValueAttacher) =>
        attacher.attachJValueList(jStrLs.map{parse(_)}, json)
      case SimpleListAttachment(jStrLs, attacher) =>
        parse(attacher.attachList(jStrLs, compact(render(json))))
        
      case SimpleTransformAttachment(jStr, transformer: JValueTransformer, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer.transformJValue(parse(jStr), Nil, NoMerging, inclusions), json)
      case SimpleTransformAttachment(jStr, transformer, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transform(jStr, additionalInclusions=inclusions)), json)
      case SimpleTransformAttachment(jStr, transformer, attacher) =>
        parse(attacher.attach(transformer.transform(jStr, additionalInclusions=inclusions), compact(render(json))))
      
      case SimpleTransformListAttachment(jStrLs, transformer: JValueTransformer, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer.transformJValueList(jStrLs.map { (s: String) => parse(s) }, Nil, NoMerging, inclusions), json)
      case SimpleTransformListAttachment(jStrLs, transformer, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transformList(jStrLs, additionalInclusions=inclusions)), json)
      case SimpleTransformListAttachment(jStrLs, transformer, attacher) =>
        parse(attacher.attach(transformer.transformList(jStrLs, additionalInclusions=inclusions), compact(render(json))))
        
      case NestedTransformAttachment(jStr, transformer: JValueTransformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer.transformJValue(parse(jStr), transformAttachments, NoMerging, inclusions), json)
      case NestedTransformAttachment(jStr, transformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transform(jStr, transformAttachments, additionalInclusions=inclusions)), json)
      case NestedTransformAttachment(jStr, transformer, transformAttachments, attacher) =>
        parse(attacher.attach(transformer.transform(jStr, transformAttachments, additionalInclusions=inclusions), compact(render(json))))
        
      case NestedTransformListAttachment(jStrLs, transformer: JValueTransformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer.transformJValueList(jStrLs.map { (s: String) => parse(s) }, transformAttachments, NoMerging, inclusions), json)
      case NestedTransformListAttachment(jStrLs, transformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transformList(jStrLs, transformAttachments, additionalInclusions=inclusions)), json)
      case NestedTransformListAttachment(jStrLs, transformer, transformAttachments, attacher) =>
        parse(attacher.attach(transformer.transformList(jStrLs, transformAttachments, additionalInclusions=inclusions), compact(render(json))))
  }
  
}