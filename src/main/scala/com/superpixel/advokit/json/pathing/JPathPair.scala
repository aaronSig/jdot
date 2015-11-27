package com.superpixel.advokit.json.pathing

import scala.collection.mutable.Map


class JPathPair(val from: JPath, val to: JPath);
class JPathStringPair(val from: String, val to: String);

object JPathMap {
  
  def fromMap(pathMap: Map[String, String]): Set[JPathPair] = {
    val entrySet = pathMap.toSet
    entrySet.map { case (to:String , from:String) => JPathMap(from, to) }
  }
    
  def apply(from: String, to: String): JPathPair = {
    new JPathPair(JPath.fromString(from), JPath.fromString(to));
  }
  
  implicit def strings2Paths(pathStringMap: JPathStringPair): JPathPair = {
    new JPathPair(JPath.fromString(pathStringMap.from), JPath.fromString(pathStringMap.to))
  }
  
}