package com.superpixel.advokit
import scala.collection.JavaConverters._;
import com.superpixel.advokit.json.pathing.JPathPair


object ScalaConverters {

  def jvToScMap[U, V](jvMap: java.util.Map[U, V]): scala.collection.immutable.Map[U, V] = {
    if (jvMap == null) {
      scala.collection.immutable.Map[U, V]()
    } else {
      jvMap.asScala.toMap
    }
  }
  
  def jvStringMapToJPathPairSet(jvMap: java.util.Map[String, String]): scala.collection.immutable.Set[JPathPair] = {
    if (jvMap == null) {
      scala.collection.immutable.Set[JPathPair]();
    } else {
      val entrySet: collection.immutable.Set[(String, String)] = jvMap.asScala.toSet
      entrySet.map { case (to:String , from:String) => JPathPair.fromStrings(to, from) }
    }
  }

}