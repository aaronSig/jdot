package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper._

class JValueTransformer(fieldMap: Set[JPathPair], merges: MergingJson, inclusions: Inclusions) extends JsonContentTransformer {

  val builder = JValueBuilder(fieldMap.map(_.to))
  val mergingJValues: (Seq[JValue], Seq[JValue]) = transformDefaults(merges)
  val inclusionsMap: Map[String, JValue] = parseInclusions(inclusions)
  
  override def transform(json: String, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String = 
    compact(render(_transform(Left(json), attachments, localMerges, additionalInclusions)))
  override def transformList(jsonList: List[String], attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String = 
    compact(render(_transformList(Left(jsonList), attachments, localMerges, additionalInclusions)))
  
  def transformJValue(json: JValue, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue = 
    _transform(Right(json), attachments, localMerges, additionalInclusions)
  def transformJValueList(jsonList: List[JValue], attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue = 
    _transformList(Right(jsonList), attachments, localMerges, additionalInclusions)
    
    
  private def _transformList(jsonList: Either[List[String], List[JValue]], attachments: List[Attachment], localMerges: MergingJson, additionalInclusions: Inclusions): JValue = {
    JArray(
      jsonList match {
        case Left(strLs) => strLs.map {str: String => _transform(Left(str), attachments, localMerges, additionalInclusions)}
        case Right(jsonLs) => jsonLs.map {json: JValue => _transform(Right(json), attachments, localMerges, additionalInclusions)}
      }
    )
  }
  
  private def _transform(json: Either[String, JValue], attachments: List[Attachment], localMerges: MergingJson, additionalInclusions: Inclusions): JValue = {
    val j = json match {
      case Left(str) => parse(str)
      case Right(json) => json
    }
    
    val localMergingJValues = transformDefaults(localMerges)
    
    import JValueMerger.MergeArraysAsValues
    val leftMerge = JValueMerger.leftMerge(MergeArraysAsValues)_
    val mergingFn = applyMerges(leftMerge)_
    
    
    val postInJV = mergingFn(j, localMergingJValues._1, mergingJValues._1)
      
    val builtJV  = builder.build((getValues(postInJV, additionalInclusions)))

    val preOutJV = attachments.foldLeft(builtJV)(applyAttachment(additionalInclusions)_)
    
    val outJV    = mergingFn(preOutJV, localMergingJValues._2, mergingJValues._2)
    
    outJV
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
  
  private def applyMerges(merge: (JValue, JValue) => JValue)(json: JValue, local: Seq[JValue], classWide: Seq[JValue]): JValue = {
    (local, classWide) match {
      case (Nil, Nil) => json
      case (sq, Nil) => (json +: sq) reduceLeft(merge)
      case (Nil, sq) => (json +: sq) reduceLeft(merge)
      case (sq1, sq2) => (json +: sq1 reduceLeft(merge)) +: sq2 reduceLeft(merge)
    }
  }
  
  private def applyAttachment(inclusions: Inclusions)(json: JValue, attachment: Attachment): JValue = attachment match {
      case SimpleAttachment(jStr, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(jStr), json)
      case SimpleAttachment(jStr, attacher) =>
        parse(attacher.attach(jStr, compact(render(json))))
        
      case SimpleListAttachment(jStrLs, attacher: JValueAttacher) =>
        attacher.attachJValueList(jStrLs.map{parse(_)}, json)
      case SimpleListAttachment(jStrLs, attacher) =>
        parse(attacher.attachList(jStrLs, compact(render(json))))
        
      case SimpleTransformAttachment(jStr, transformer: JValueTransformer, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer._transform(Left(jStr), Nil, NoMerging, inclusions), json)
      case SimpleTransformAttachment(jStr, transformer, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transform(jStr, additionalInclusions=inclusions)), json)
      case SimpleTransformAttachment(jStr, transformer, attacher) =>
        parse(attacher.attach(transformer.transform(jStr, additionalInclusions=inclusions), compact(render(json))))
      
      case SimpleTransformListAttachment(jStrLs, transformer: JValueTransformer, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer._transformList(Left(jStrLs), Nil, NoMerging, inclusions), json)
      case SimpleTransformListAttachment(jStrLs, transformer, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transformList(jStrLs, additionalInclusions=inclusions)), json)
      case SimpleTransformListAttachment(jStrLs, transformer, attacher) =>
        parse(attacher.attach(transformer.transformList(jStrLs, additionalInclusions=inclusions), compact(render(json))))
        
      case NestedTransformAttachment(jStr, transformer: JValueTransformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer._transform(Left(jStr), transformAttachments, NoMerging, inclusions), json)
      case NestedTransformAttachment(jStr, transformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transform(jStr, transformAttachments, additionalInclusions=inclusions)), json)
      case NestedTransformAttachment(jStr, transformer, transformAttachments, attacher) =>
        parse(attacher.attach(transformer.transform(jStr, transformAttachments, additionalInclusions=inclusions), compact(render(json))))
        
      case NestedTransformListAttachment(jStrLs, transformer: JValueTransformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(transformer._transformList(Left(jStrLs), transformAttachments, NoMerging, inclusions), json)
      case NestedTransformListAttachment(jStrLs, transformer, transformAttachments, attacher: JValueAttacher) =>
        attacher.attachJValue(parse(transformer.transformList(jStrLs, transformAttachments, additionalInclusions=inclusions)), json)
      case NestedTransformListAttachment(jStrLs, transformer, transformAttachments, attacher) =>
        parse(attacher.attach(transformer.transformList(jStrLs, transformAttachments, additionalInclusions=inclusions), compact(render(json))))
  }
}

object JValueTransformer {
  
  def apply(fieldMap: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions) = new JValueTransformer(fieldMap, merges, inclusions)
  
}