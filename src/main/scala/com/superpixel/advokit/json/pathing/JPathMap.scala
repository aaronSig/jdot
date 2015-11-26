package com.superpixel.advokit.json.pathing

import scala.collection.mutable.Map


class JPathMap(val from: JPath, val to: JPath);
class JPathStringMap(val from: String, val to: String);

object JPathMap {
  
  def fromMap(pathMap: Map[String, String]): Set[JPathMap] = {
    val entrySet = pathMap.toSet
    entrySet.map { case (to:String , from:String) => JPathMap(from, to) }
  }
    
  def apply(from: String, to: String): JPathMap = {
    new JPathMap(JPath(from), JPath(to));
  }
  
  implicit def strings2Paths(pathStringMap: JPathStringMap): JPathMap = {
    new JPathMap(JPath(pathStringMap.from), JPath(pathStringMap.to))
  }
  
}