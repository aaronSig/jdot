package com.superpixel.advokit.mapper

trait JsonContentTransformer {

  def transform(json: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String
  
}