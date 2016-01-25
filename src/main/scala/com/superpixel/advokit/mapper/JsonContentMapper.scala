package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.pathing._

trait JsonContentMapper[T] {
  
  def map(jsonContent: String, attachments: List[Attachment] = Nil, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T
  
}