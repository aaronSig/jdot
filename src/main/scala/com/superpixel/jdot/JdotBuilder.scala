package com.superpixel.jdot

import com.superpixel.jdot.pathing.JPath
import com.superpixel.jdot.json4s.JValueBuilder

trait JdotBuilder {

  def build(pathVals: Set[(JPath, Any)]): String
  
}

object JdotBuilder { 
  
  def apply: JdotBuilder = JValueBuilder()
  
}