package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.pathing._

trait JsonContentMapper[T] {
  
  def map(jsonContent: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T
  
  def withAttacher[S](attachmentClass: Class[S], attacher: JsonContentAttacher): JsonContentMapperWithAttacher[T]
}

trait JsonContentMapperWithAttacher[T] {
  
  def mapWithAttachment(jsonToAttach: String, jsonAttachee: String, localAttacheeMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions) : T
  
  def mapWithListAttachment(jsonListToAttach: List[String], jsonAttachee: String, localAttacheeMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions) : T
  
}
