package com.superpixel.jdot.json4s

import org.json4s._
import org.json4s.native.JsonMethods._


import com.superpixel.jdot.pathing._
import com.superpixel.jdot._

class JValueTransformer(fieldMap: Set[JPathPair], merges: MergingJson, inclusions: Inclusions) extends JDotTransformer {

  val builder = JValueBuilder()
  val mergingJValues: (Seq[JValue], Seq[JValue]) = JValueMerger.transfromMergingJson(merges)
  val inclusionsMap: Map[String, JValue] = parseInclusions(inclusions)
  
  override def transform(json: String, attachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String =
    compact(render(_transform(Left(json), attachers, localMerges, additionalInclusions)))
  override def transformList(jsonList: List[String], attachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String =
    compact(render(_transformList(Left(jsonList), attachers, localMerges, additionalInclusions)))
  
  def transformJValue(json: JValue, attachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue =
    _transform(Right(json), attachers, localMerges, additionalInclusions)
  def transformJValueList(jsonList: List[JValue], attachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): JValue =
    _transformList(Right(jsonList), attachers, localMerges, additionalInclusions)
    
    
  private def _transformList(jsonList: Either[List[String], List[JValue]], attachers: List[JDotAttacher], localMerges: MergingJson, additionalInclusions: Inclusions): JValue = {
    JArray(
      jsonList match {
        case Left(strLs) => strLs.map {str: String => _transform(Left(str), attachers, localMerges, additionalInclusions)}
        case Right(jsonLs) => jsonLs.map {json: JValue => _transform(Right(json), attachers, localMerges, additionalInclusions)}
      }
    )
  }
  
  private def _transform(json: Either[String, JValue], attachers: List[JDotAttacher], localMerges: MergingJson, additionalInclusions: Inclusions): JValue = {
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
      this.applyAttachers(postInJV, builtJV, attachers)
    } catch {
      case jte: JsonTraversalException => {
        println(s"ERROR: ${jte.getMessage}")
        builtJV
      }
    }
    
    val outJV = mergingFn(preOutJV, localMergingJValues._2, mergingJValues._2)
    
    outJV
  }

  private def applyAttachers(context:JValue, applyTo: JValue, attachers: List[JDotAttacher]): JValue = {
    def recu(jValue: JValue, attacherLs: List[JDotAttacher]): JValue = attacherLs match {
      case Nil => jValue
      case (hd: JValueAttacher) :: tl => recu(hd.attachJValue(context, jValue), tl)
      case hd :: tl => {
        recu(parse(hd.attach(compact(render(context)), compact(render(jValue)))), tl)
      }
    }
    recu(applyTo, attachers)
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