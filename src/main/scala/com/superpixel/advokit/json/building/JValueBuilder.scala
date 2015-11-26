package com.superpixel.advokit.json.building

import com.superpixel.advokit.json.pathing._
import net.liftweb.json._

class JValueBuilder(paths: Set[JPath]) {

  def build(pathVals: Set[(JPath, JValue)]): JValue = {
    def buildLinearPath(revPath: Seq[JPathElement], acc: JValue): JValue = revPath match {
      case Nil => acc
      case JObjectPath(key) +: tl => 
        buildLinearPath(tl, JObject(List(JField(key, acc))))
      case JArrayPath(key) +: tl =>
        buildLinearPath(tl, JArray(List.tabulate(key+1){
            n:Int => if (n==key) acc else JNothing
        }))
      case JPathLink +: tl =>
        throw new JsonBuildingException("Json build paths cannot include JLinks.")
    }
    
    
    pathVals.map {
      case (jp, jv) => buildLinearPath(jp reverse, jv)
    } reduceLeft(_ merge _)
  }
  
}

class JsonBuildingException(message: String) extends RuntimeException(message)

object JValueBuilder {
  
  def apply(paths: Set[JPath]): JValueBuilder = new JValueBuilder(paths)
  
}