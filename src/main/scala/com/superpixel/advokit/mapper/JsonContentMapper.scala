package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.lift.JValueMapper

trait JsonContentMapper[T] {
  
  def map(jsonContent: String, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T
  
}

object JsonContentMapper {
  
  def apply[T](transformer: JsonContentTransformer, extractor: JsonContentExtractor[T]): JsonContentMapper[T] = {
    JValueMapper[T](transformer, extractor)
  }
  
}