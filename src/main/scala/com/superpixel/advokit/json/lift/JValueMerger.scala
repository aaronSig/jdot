package com.superpixel.advokit.json.lift

import org.json4s._
import org.json4s.native.JsonMethods._

object JValueMerger {

  def mergeFlats(val1: JValue, val2: JValue): JValue = (val1, val2) match {
    case (JObject(xs), JObject(ys)) => JObject(mergeFields(xs, ys))
    case (JArray(xs), JArray(ys)) => JArray(mergeVals(xs, ys))
    case (JNothing, x) => x
    case (x, JNothing) => x
    case (_, y) => y
  }

  private def mergeFields(vs1: List[JField], vs2: List[JField]): List[JField] = {
    def mergeRec(xleft: List[JField], yleft: List[JField]): List[JField] = xleft match {
      case Nil => yleft
      case (xn, xv) :: xs => yleft find (_._1 == xn) match {
        case Some(y @ (yn, yv)) =>
          JField(xn, mergeFlats(xv, yv)) :: mergeRec(xs, yleft filterNot (_ == y))
        case None => JField(xn, xv) :: mergeRec(xs, yleft)
      }
    }

    mergeRec(vs1, vs2)
  }

  private def mergeVals(vs1: List[JValue], vs2: List[JValue]): List[JValue] = {
    def mergeRec(xleft: List[JValue], yleft: List[JValue]): List[JValue] = (xleft, yleft) match {
      case (Nil, ys) => ys
      case (xs, Nil) => xs
      case (x :: xs, y :: ys) => mergeFlats(x, y) :: mergeRec(xs, ys)
    }

    mergeRec(vs1, vs2)
  }
  
}