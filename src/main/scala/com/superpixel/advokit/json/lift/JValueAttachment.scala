package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._

import com.superpixel.advokit.json.lift.JValueTraverser.Stop
import com.superpixel.advokit.json.pathing._
import com.superpixel.advokit.mapper._

object JValueAttachment {
  
  private val jArrayNotFoundLamb = (jVal: JValue, jPathEl: JPathElement) => {
    (JNothing, Stop)
  }
  
  private val jArrayEndLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]], List[JValue]] = { 
    case (JArray(ls), _) => ls 
  } 
  
  private def linkLamb(inc: Inclusions): String => Option[JValue] = inc match {
    case NoInclusions => s => None
    case FixedInclusions(mp) => s => mp.get(s).map { parse(_) };
  }
  
  private def getJArray(jPath: JPath, fromJson: String, inclusions: Inclusions): List[JValue] = {
    val jValueArray = parse(fromJson);
    JValueTraverser.traverse(linkLamb(inclusions), jArrayNotFoundLamb, jArrayEndLamb)(jValueArray, jPath)
                   .getOrElse(throw new JsonTraversalException("JsonArray Transform Attacher couldn't find json array in json with the given path: " + jPath, jValueArray));
  }
  

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
  
  private def simpleAttach(attachJson: Either[Either[String, JValue], List[String]], 
                           attachToJson: JValue, 
                           attacher: JsonContentAttacher): JValue = (attachJson, attacher) match {
    case (Left(Left(jsonStr)), atchr: JValueAttacher) => atchr.attachJValue(parse(jsonStr), attachToJson)
    case (Left(Right(jVal)), atchr: JValueAttacher) => atchr.attachJValue(jVal, attachToJson)
    case (Left(Left(jsonStr)), atchr) => parse(atchr.attach(jsonStr, compact(render(attachToJson))))
    case (Left(Right(jVal)), atchr) => parse(atchr.attach(compact(render(jVal)), compact(render(attachToJson))))
    case (Right(jsonStrLs), atchr: JValueAttacher) => atchr.attachJValueList(jsonStrLs.map{parse(_)}, attachToJson)
    case (Right(jsonStrLs), atchr) => parse(atchr.attachList(jsonStrLs, compact(render(attachToJson))))
  }
  
  
  private def attachWithTransformer(attachJson: Either[Either[String, JValue], Either[List[String], List[JValue]]], 
                                    attachToJson: JValue, 
                                    transformer: JsonContentTransformer,
                                    attacher: JsonContentAttacher,
                                    inclusions: Inclusions = NoInclusions,
                                    transformAttachments: List[Attachment] = Nil): JValue = {
    simpleAttach(
      Left(
        (attachJson, transformer) match {
          case (Left(Left(jsonStr)), trsfm: JValueTransformer) => 
            Right(trsfm.transformJValue(parse(jsonStr), attachments=transformAttachments, additionalInclusions=inclusions))
          case (Left(Right(jVal)), trsfm: JValueTransformer) => 
            Right(trsfm.transformJValue(jVal, attachments=transformAttachments, additionalInclusions=inclusions))
          case (Right(Right(jValLs)), trsfm: JValueTransformer) => 
            Right(trsfm.transformJValueList(jValLs, attachments=transformAttachments, additionalInclusions=inclusions))
          case (Right(Left(jsonStrLs)), trsfm: JValueTransformer) =>
            Right(trsfm.transformJValueList(jsonStrLs.map { parse(_) }, attachments=transformAttachments, additionalInclusions=inclusions))
          case (Left(Left(jsonStr)), trsfm) => 
            Left(trsfm.transform(jsonStr, attachments=transformAttachments, additionalInclusions=inclusions))
          case (Left(Right(jVal)), trsfm) => 
            Left(trsfm.transform(compact(render(jVal)), attachments=transformAttachments, additionalInclusions=inclusions))
          case (Right(Left(jsonStrLs)), trsfm) => 
            Left(trsfm.transformList(jsonStrLs, attachments=transformAttachments, additionalInclusions=inclusions))
          case (Right(Right(jsonValLs)), trsfm) => 
            Left(trsfm.transformList(jsonValLs.map { (j: JValue) => compact(render(j)) }, attachments=transformAttachments, additionalInclusions=inclusions))
        }    
      ),
      attachToJson,
      attacher
    )
  }
  
  def applyAttachment(inclusions: Inclusions)(json: String, attachment: Attachment): String = 
    compact(render(applyAttachmentToJValue(inclusions)(parse(json), attachment)))
  def applyAttachmentToJValue(inclusions: Inclusions)(json: JValue, attachment: Attachment): JValue = attachment match {
      case SimpleAttachment(jStr, attacher) =>
        simpleAttach(Left(Left(jStr)), json, attacher)
      
      case SimpleListAttachment(jStrLs, attacher) =>
        simpleAttach(Right(jStrLs), json, attacher)
        
      case SimpleTransformAttachment(jStr, transformer, attacher) =>
        attachWithTransformer(Left(Left(jStr)), json, transformer, attacher, inclusions)
      
      case SimpleTransformListAttachment(jStrLs, transformer, attacher) =>
        attachWithTransformer(Right(Left(jStrLs)), json, transformer, attacher, inclusions)
        
      case JsonArrayTransformAttachment(jPath, jArrayContainer, transformer, attacher) => {
        attachWithTransformer(Right(Right(getJArray(JPath.fromString(jPath), jArrayContainer, inclusions))), json, transformer, attacher, inclusions)
      }

      case NestedTransformAttachment(jStr, transformer, transformAttachments, attacher) =>
        attachWithTransformer(Left(Left(jStr)), json, transformer, attacher, inclusions, transformAttachments)
        
      case NestedTransformListAttachment(jStrLs, transformer, transformAttachments, attacher) =>
        attachWithTransformer(Right(Left(jStrLs)), json, transformer, attacher, inclusions, transformAttachments)
        
      case JsonArrayNestedTransformAttachment(jPath, jArrayContainer, transformer, transformAttachments, attacher) => {
        attachWithTransformer(Right(Right(getJArray(JPath.fromString(jPath), jArrayContainer, inclusions))), json, transformer, attacher, inclusions, transformAttachments)
      }
  }
  
}