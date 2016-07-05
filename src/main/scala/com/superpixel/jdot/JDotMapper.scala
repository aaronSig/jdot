package com.superpixel.jdot

import com.superpixel.jdot.json4s.JValueMapper

trait JDotMapper[T] {
  
  def map(jsonContent: String, atttachers: List[JDotAttacher] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T
  
}

object JDotMapper {
  
  def apply[T](transformer: JDotTransformer, extractor: JDotExtractor[T]): JDotMapper[T] = {
    JValueMapper[T](transformer, extractor)
  }
  
}