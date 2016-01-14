package com.superpixel.advokit.mapper

trait JsonContentAttacher {

  def attach(transformAttachJson: String, attachToJson: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String
  
  def attachList(transformAttachJsonList: List[String], attachToJson: String, localMerges: MergingJson = NoMerging, additionalInclusions: Inclusions = NoInclusions): String
  
}