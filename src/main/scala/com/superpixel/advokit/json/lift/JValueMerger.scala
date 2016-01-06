package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._




object JValueMerger {
  
  type JMerge =  (JValue, JValue) => JValue
  type JArrayMerge = (List[JValue], List[JValue], JMerge) => List[JValue]
  type JObjectMerge = (List[JField], List[JField], JMerge) => List[JField]
  
  sealed trait ArrayMergeStrategy {
    def leftMerge: JArrayMerge
  }
  case object MergeArraysOnIndex extends ArrayMergeStrategy {
    def leftMerge = mergeArraysOnIndex;
  }
  def mergeArraysOnIndex(): ArrayMergeStrategy = MergeArraysOnIndex
  case object MergeArraysAsValues extends ArrayMergeStrategy {
    def leftMerge = leftMergeArraysAsValues
  };
  def mergeArraysAsValues(): ArrayMergeStrategy = MergeArraysAsValues
  
  def leftMergeStrings(arrayStrat: ArrayMergeStrategy)(leftJson: String, rightJson: String): String = {
    pretty(render(leftMerge(mergeFields, arrayStrat.leftMerge)(parse(leftJson), parse(rightJson))))
  }
  
  def leftMerge(arrayStrat: ArrayMergeStrategy)(leftJson: JValue, rightJson: JValue): JValue = {
    leftMerge(mergeFields, arrayStrat.leftMerge)(leftJson, rightJson)
  }
  
  
  private def leftMerge(objectStrat: JObjectMerge, arrayStrat: JArrayMerge): JMerge = {
    def inner(val1: JValue, val2: JValue): JValue = (val1, val2) match {
      case (JObject(xs), JObject(ys)) => JObject(objectStrat(xs, ys, inner))
      case (JArray(xs), JArray(ys)) => JArray(arrayStrat(xs, ys, inner))
      case (JNothing, x) => x
      case (x, _) => x
    }
    inner
  }
  
 
  private def mergeFields(vs1: List[JField], vs2: List[JField], outerMerge: JMerge): List[JField] = {
    def mergeRec(xleft: List[JField], yleft: List[JField]): List[JField] = xleft match {
      case Nil => yleft
      case (xn, xv) :: xs => yleft find (_._1 == xn) match {
        case Some(y @ (yn, yv)) =>
          JField(xn, outerMerge(xv, yv)) :: mergeRec(xs, yleft filterNot (_ == y))
        case None => JField(xn, xv) :: mergeRec(xs, yleft)
      }
    }

    mergeRec(vs1, vs2)
  }

  private def mergeArraysOnIndex(vs1: List[JValue], vs2: List[JValue], outerMerge: JMerge): List[JValue] = {
    def mergeRec(xleft: List[JValue], yleft: List[JValue]): List[JValue] = (xleft, yleft) match {
      case (Nil, ys) => ys
      case (xs, Nil) => xs
      case (x :: xs, y :: ys) => outerMerge(x, y) :: mergeRec(xs, ys)
    }

    mergeRec(vs1, vs2)
  }
  
  private def leftMergeArraysAsValues(vs1: List[JValue], vs2: List[JValue], outerMerge: JMerge): List[JValue] = (vs1, vs2) match {
    case (Nil, ys) => ys
    case (xs, _) => xs
  }
  
}