package com.superpixel.jdot

import com.superpixel.jdot.pathing.JPath
import com.superpixel.jdot.json4s.JValueBuilder

trait JsonContentBuilder {

  def build(pathVals: Set[(JPath, Any)]): String
  
}

object JsonContentBuilder { 
  
  def apply: JsonContentBuilder = JValueBuilder()
  
}