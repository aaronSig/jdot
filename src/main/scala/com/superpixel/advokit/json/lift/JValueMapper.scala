package com.superpixel.advokit.json.lift

import scala.reflect.Manifest

import com.superpixel.advokit.json.pathing.JPathPair
import com.superpixel.advokit.mapper._

import org.json4s._
import org.json4s.native.JsonMethods._
import muster._

class JValueMapper[T](transformer: JValueTransformer, extractor: JValueExtractor[T]) extends JsonContentMapper[T] {
  
  override def map(jsonContent: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T = {
    val jValContent = parse(jsonContent)
    mapJValue(jValContent, localMerges, additionalInclusions)
  }

  
  def mapJValue(json: JValue, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T = {
    val transformed = transformer.transformJValue(json, localMerges, additionalInclusions)
    extractor.extractFromJValue(transformed)
  }
  
  
  def withAttacher[S](attachmentClass: Class[S], attacher: JsonContentAttacher): JsonContentMapperWithAttacher[T] = {
    
    val attachmentTypeHintMerge: JObject = JObject(JValueExtractor.typeHintFieldForClass(attachmentClass))
    val mergeOptionForTypeHint = MergingJsonPost(Seq(compact(render(attachmentTypeHintMerge))))
    attacher match {
      case jva: JValueAttacher =>
        val tHExtractor = extractor.extractorJValueWithTypeHints(List(attachmentClass))
        new JsonContentMapperWithAttacher[T] {
            
          def mapWithAttachment(jsonToAttach: String, jsonAttachee: String, localAttacheeMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions) : T = {
            val transformedAttachee = transformer.transformJValue(parse(jsonAttachee), localAttacheeMerges, additionalInclusions)
            val fullToExtract = jva.attachJValue(parse(jsonToAttach), transformedAttachee, mergeOptionForTypeHint, additionalInclusions)
            tHExtractor(fullToExtract)
          }
          
          def mapWithListAttachment(jsonListToAttach: List[String], jsonAttachee: String, localAttacheeMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions) : T = {
        		val transformedAttachee = transformer.transformJValue(parse(jsonAttachee), localAttacheeMerges, additionalInclusions)
            val fullToExtract = jva.attachJValueList(jsonListToAttach.map{json => parse(json)}, transformedAttachee, mergeOptionForTypeHint, additionalInclusions)
            tHExtractor(fullToExtract)
          }
          
        }
      case _ => 
        val tHExtractor = extractor.extractorWithTypeHints(List(attachmentClass))
        new JsonContentMapperWithAttacher[T] {
          def mapWithAttachment(jsonToAttach: String, jsonAttachee: String, localAttacheeMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions) : T = {
            val transformedAttachee = transformer.transform(jsonAttachee, localAttacheeMerges, additionalInclusions)
            val fullToExtract = attacher.attach(jsonToAttach, transformedAttachee, mergeOptionForTypeHint, additionalInclusions)
            tHExtractor(fullToExtract)
          }
          
          def mapWithListAttachment(jsonListToAttach: List[String], jsonAttachee: String, localAttacheeMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions) : T = {
            val transformedAttachee = transformer.transform(jsonAttachee, localAttacheeMerges, additionalInclusions)
            val fullToExtract = attacher.attachList(jsonListToAttach, transformedAttachee, mergeOptionForTypeHint, additionalInclusions)
            tHExtractor(fullToExtract)  
          }
        }
    }
  }
}


object JValueMapper {

  def forTargetClass[T](targetClass: Class[T], pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions): JValueMapper[T] = {
    implicit val m: Manifest[T] = Manifest.classType(targetClass)
    apply[T](pathMapping, merges, inclusions)
  }
  
  
  def apply[T](pathMapping: Set[JPathPair], merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions)(implicit m: Manifest[T]): JValueMapper[T] = {
    val transformer = JValueTransformer(pathMapping, merges, inclusions)
    val extractor = JValueExtractor()
    new JValueMapper[T](transformer, extractor)
  } 
}