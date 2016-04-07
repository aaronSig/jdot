package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper._

class JValueTransformer(fieldMap: Set[JPathPair], merges: MergingJson, inclusions: Inclusions) extends JsonContentTransformer {

  val builder = JValueBuilder()
  val mergingJValues: (Seq[JValue], Seq[JValue]) = JValueMerger.transfromMergingJson(merges)
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
    
    val localMergingJValues = JValueMerger.transfromMergingJson(localMerges)
    
    import JValueMerger.MergeArraysOnIndex
    val leftMerge = JValueMerger.leftMerge(MergeArraysOnIndex)_
    val mergingFn = applyMerges(leftMerge)_
    
    
    val postInJV = mergingFn(j, localMergingJValues._1, mergingJValues._1)
      
    val builtJV  = builder.buildJValue((getValues(postInJV, additionalInclusions)))

    val preOutJV = try {
      JValueAttachment.applyAttachmentsToJValue(builtJV, attachments)
    } catch {
      case jte: JsonTraversalException => {
        println(s"ERROR: ${jte.getMessage}")
        builtJV
      }
    }
//      attachments.foldLeft(builtJV)(applyAttachment(additionalInclusions)_)
    
    val outJV    = mergingFn(preOutJV, localMergingJValues._2, mergingJValues._2)
    
    outJV
  }
  
  private def getValues(json: JValue, addInclusions: Inclusions): Set[(JPath, JValue)] = {
    val addInclusionsMap: Map[String, JValue] = parseInclusions(addInclusions)
	  val accessor = JValueAccessor(json, (s: String) => addInclusionsMap.get(s).orElse(inclusionsMap.get(s)))

    fieldMap.map { jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from)) }
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
}

object JValueTransformer {
  
  def apply(fieldMap: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions) = new JValueTransformer(fieldMap, merges, inclusions)
  
}