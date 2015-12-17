package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.pathing._

trait JsonContentMapper[T] {
  
  def map(jsonContent: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): T
  
}
