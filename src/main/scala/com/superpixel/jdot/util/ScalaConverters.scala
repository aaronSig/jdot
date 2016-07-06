package com.superpixel.jdot.util
import scala.collection.JavaConverters._;
import com.superpixel.jdot.pathing.JPathPair


object ScalaConverters {
  
  def scOptionToJvOptional[T](scOption: scala.Option[T]): java.util.Optional[T] =  scOption match {
    case None => java.util.Optional.empty();
    case Some(t) => java.util.Optional.of(t);
  }

  def jvOptionalToScOption[T](jvOptional: java.util.Optional[T]): scala.Option[T] = {
    if (jvOptional.isPresent) Some(jvOptional.get())
    else None
  }

  def scOptionBooleanToJvOptionalBoolean(scOption: scala.Option[scala.Boolean]): java.util.Optional[java.lang.Boolean] =  scOption match {
    case None => java.util.Optional.empty();
    case Some(b) => java.util.Optional.of(scTojvBoolean(b));
  }

  def scTojvBoolean(b: scala.Boolean): java.lang.Boolean = {
    return new java.lang.Boolean(b);
  }

  def jvToScMap[U, V](jvMap: java.util.Map[U, V]): scala.collection.immutable.Map[U, V] = {
    if (jvMap == null) {
      scala.collection.immutable.Map[U, V]()
    } else {
      jvMap.asScala.toMap
    }
  }
  
  def jvListToScSeq[T](jvList: java.util.List[T]): scala.collection.Seq[T] = {
    if (jvList == null) {
      Nil
    } else {
      jvList.asScala.toSeq
    }
  }
  
  def jvToScList[T](jvList: java.util.List[T]): scala.collection.immutable.List[T] = {
    if (jvList == null) {
      Nil
    } else {
      jvList.asScala.toList
    }
  }
  
  def jvArrayToScSeq[T](jvArray: Array[T]): scala.collection.Seq[T] = {
    jvArray
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