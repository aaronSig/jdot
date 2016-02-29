package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._

import com.superpixel.advokit.json.lift.JValueTraverser.Continue
import com.superpixel.advokit.json.pathing._

class JValueAccessor(json: JValue, linkLamb: String=>Option[JValue]) {

	private val traverseForValue = JValueTraverser.traverse(linkLamb, JValueAccessor.notFoundLamb, JValueAccessor.endLamb)_

  def getValue(jPath: JPath): JValue = {
    try {
      traverseForValue(json, jPath).getOrElse(JNothing)
    } catch {
      case e: JsonTraversalException => {
        println(e.getMessage)
        e.printStackTrace()
        return JNothing
      }
    }
  }
   
}


object JValueAccessor {
  
  def apply(json: JValue, linkLamb: String=>Option[JValue] = (s)=>None): JValueAccessor = new JValueAccessor(json, linkLamb)
  
  private val notFoundLamb = (jVal: JValue, jPath: JPathElement) => {
    (JNothing, Continue)
  }
  private val endLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]], JValue] = 
    (JValueTraverser.jDefaultValueNoComplexEnd andThen { case s: String => JString(s) }) orElse 
    { case (jVal, _) => jVal }
}