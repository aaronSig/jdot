package com.superpixel.jdot

import com.superpixel.jdot.pathing.JPath
import com.superpixel.jdot.json4s.JValueBuilder

trait JDotBuilder {

  def build(pathVals: Set[(JPath, Any)]): String
  
}

object JDotBuilder { 
  
  def default = apply
  def apply: JDotBuilder = JValueBuilder()
  
}