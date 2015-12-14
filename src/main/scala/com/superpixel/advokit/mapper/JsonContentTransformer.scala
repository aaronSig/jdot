package com.superpixel.advokit.mapper

trait JsonContentTransformer {

  def transform(json: String, defaults: DefaultJson = NoDefaultJson, additionalInclusions: Inclusions = NoInclusions): String
  
}