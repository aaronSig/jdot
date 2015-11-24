package com.superpixel.advokit

import scala.collection.mutable.Map;

class JsonPathMap(val from: JsonPath, val to: JsonPath);
class JsonPathStringMap(val from: String, val to: String);

object JsonPathMap {
  
  def fromMap(pathMap: Map[String, String]): Set[JsonPathMap] = {
    val entrySet = pathMap.toSet
    entrySet.map { case (to:String , from:String) => JsonPathMap(from, to) }
  }
    
  def apply(from: String, to: String): JsonPathMap = {
    new JsonPathMap(JsonPath(from), JsonPath(to));
  }
  
  implicit def strings2Paths(pathStringMap: JsonPathStringMap): JsonPathMap = {
    new JsonPathMap(JsonPath(pathStringMap.from), JsonPath(pathStringMap.to))
  }
  
}