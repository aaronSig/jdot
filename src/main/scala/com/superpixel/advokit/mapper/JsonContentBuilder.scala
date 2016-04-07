package com.superpixel.advokit.mapper

import com.superpixel.advokit.json.pathing.JPath
import com.superpixel.advokit.json.lift.JValueBuilder

trait JsonContentBuilder {

  def build(pathVals: Set[(JPath, Any)]): String
  
}

object JsonContentBuilder { 
  
  def apply: JsonContentBuilder = JValueBuilder()
  
}