package com.superpixel.jdot.json4s

import org.json4s._
import org.json4s.native.JsonMethods._


import com.superpixel.jdot.pathing._
import com.superpixel.jdot._

class JValueTransformer(fieldMap: Set[JPathPair], attachers: List[JDotAttacher], merges: MergingJson, inclusions: Inclusions) extends JDotTransformer {

  val builder = JValueBuilder()
  val mergingJValues: (Seq[JValue], Seq[JValue]) = JValueMerger.transfromMergingJson(merges)
  val inclusionsMap: Map[String, JValue] = parseInclusions(inclusions)
  
  override def transform(json: String, addAttachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String =
    compact(render(_transform(Left(json), addAttachers, localMerges, additionalInclusions)))
  override def transformList(jsonList: List[String], addAttachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String =
    compact(render(_transformList(Left(jsonList), addAttachers, localMerges, additionalInclusions)))
  
  def transformJValue(json: JValue, addAttachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue =
    _transform(Right(json), addAttachers, localMerges, additionalInclusions)
  def transformJValueList(jsonList: List[JValue], addAttachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue =
    _transformList(Right(jsonList), addAttachers, localMerges, additionalInclusions)
    
    
  private def _transformList(jsonList: Either[List[String], List[JValue]], addAttachers: List[JDotAttacher], localMerges: MergingJson, additionalInclusions: Inclusions): JValue = {
    JArray(
      jsonList match {
        case Left(strLs) => strLs.map {str: String => _transform(Left(str), addAttachers, localMerges, additionalInclusions)}
        case Right(jsonLs) => jsonLs.map {json: JValue => _transform(Right(json), addAttachers, localMerges, additionalInclusions)}
      }
    )
  }
  
  private def _transform(json: Either[String, JValue], addAttachers: List[JDotAttacher], localMerges: MergingJson, additionalInclusions: Inclusions): JValue = {
    val initial = json match {
      case Left(str) => parse(str)
      case Right(json) => json
    }
    
    val localMergingJValues = JValueMerger.transfromMergingJson(localMerges)

    val leftMerge = JValueMerger.leftMerge(MergeArraysOnIndex)_
    val mergingFn = applyMerges(leftMerge)_
    
    
    val postInJV = mergingFn(initial, localMergingJValues._1, mergingJValues._1)
      
    val builtJV  = builder.buildJValue((getValues(postInJV, additionalInclusions)))

    val preOutJV = try {
      this.applyAttachers(postInJV, builtJV, addAttachers ++ attachers)
    } catch {
      case jte: JsonTraversalException => {
        println(s"ERROR: ${jte.getMessage}")
        builtJV
      }
    }
    
    val outJV = mergingFn(preOutJV, localMergingJValues._2, mergingJValues._2)
    
    outJV
  }

  private def applyAttachers(context:JValue, applyTo: JValue, atts: List[JDotAttacher]): JValue = {
    def recu(jValue: JValue, attacherLs: List[JDotAttacher]): JValue = attacherLs match {
      case Nil => jValue
      case (hd: JValueAttacher) :: tl => recu(hd.attachJValue(context, jValue), tl)
      case hd :: tl => {
        recu(parse(hd.attach(compact(render(context)), compact(render(jValue)))), tl)
      }
    }
    recu(applyTo, atts)
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
  
  def apply(fieldMap: Set[JPathPair], attachers: List[JDotAttacher] = Nil, merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions) =
    new JValueTransformer(fieldMap, attachers, merges, inclusions)
  
}