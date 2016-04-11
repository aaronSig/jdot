package com.superpixel.jdot

import com.superpixel.jdot.json4s.JValueMapper

trait JdotMapper[T] {
  
  def map(jsonContent: String, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T
  
}

object JdotMapper {
  
  def apply[T](transformer: JdotTransformer, extractor: JdotExtractor[T]): JdotMapper[T] = {
    JValueMapper[T](transformer, extractor)
  }
  
}