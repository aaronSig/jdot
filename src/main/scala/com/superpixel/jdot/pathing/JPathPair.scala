package com.superpixel.jdot.pathing


import collection.mutable.Map
import scala.language.implicitConversions

class JPathPair(val to: JPath, val from: JPath);
class JPathStringPair(val to: String, val from: String);

object JPathPair {
    
  def fromStrings(to: String, from: String): JPathPair = {
    new JPathPair(JPath.fromString(to), JPath.fromString(from));
  }
  
  def apply(to: JPath, from: JPath): JPathPair = {
    new JPathPair(to , from)
  }
  
  implicit def tupleToJPathPair(tuple: (JPath, JPath)): JPathPair = JPathPair(tuple._1, tuple._2)
  implicit def stringTupleToJPathPair(tuple: (String, String)): JPathPair = JPathPair(tuple._1, tuple._2)
  
  implicit def strings2Paths(pathStringMap: JPathStringPair): JPathPair = {
    new JPathPair(JPath.fromString(pathStringMap.from), JPath.fromString(pathStringMap.to))
  }
  
}