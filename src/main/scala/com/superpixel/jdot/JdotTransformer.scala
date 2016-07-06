package com.superpixel.jdot

import com.superpixel.jdot.json4s.JValueTransformer
import com.superpixel.jdot.pathing.JPathPair

trait JDotTransformer {

  def transform(json: String, attachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String
  
  def transformList(jsonList: List[String], attachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String
  
}

object JDotTransformer {
  
  def apply(fieldMap: Set[JPathPair], attachers: List[JDotAttacher] = Nil, merges: MergingJson = NoMerging, inclusions: Inclusions = NoInclusions): JDotTransformer = JValueTransformer(fieldMap, attachers, merges, inclusions)
  
}