package com.superpixel.jdot.json4s

import com.superpixel.jdot.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._
import com.superpixel.jdot.JDotBuilder

class JValueBuilder extends JDotBuilder {

  def buildJValue(pathVals: Set[(JPath, JValue)]): JValue = {
    def buildLinearPath(revPath: Seq[JPathElement], acc: JValue): JValue = revPath match {
      case Nil => acc
      case JObjectPath(objKey) +: tl => objKey match {
        case LiteralKey(key) => buildLinearPath(tl, JObject(List(JField(key, acc))))
        case KeyFromPath(_) => throw new JsonBuildingException(s"JPaths cannot contain nested path arguments for object path elements in a building context: ${JObjectPath(objKey)}")
      }
        
      case JArrayPath(arrIdx) +: tl => arrIdx match {
        case LiteralIndex(idx) => 
          buildLinearPath(tl, JArray(List.tabulate(idx+1){
          	n:Int => if (n==idx) acc else JNothing
          }))
        case IndexFromPath(_) => throw new JsonBuildingException(s"JPaths cannot contain nested path arguments for array index elements in a building context: ${JArrayPath(arrIdx)}")
      }
      case JDefaultValue(s, transmute) +: tl => acc match {
        case JNothing | JNull => transmute match {
          case None => buildLinearPath(tl, JString(s))
          case Some(JTransmute(func, arg)) => arg match {
            case None => buildLinearPath(tl, JValueTransmuter.transmute(JString(s), func, None))
            case Some(LiteralArgument(argStr)) => buildLinearPath(tl, JValueTransmuter.transmute(JString(s), func, Some(argStr)))
            case Some(NestedArgument(_)) => throw new JsonBuildingException(s"JPaths cannot contain nested path arguments for transmutations elements in a building context: ${JDefaultValue(s, transmute)}")
          }
        }
        case _ => buildLinearPath(tl, acc)
      }
      case jpe +: _ => 
        throw new JsonBuildingException(s"JPaths cannot contain ${jpe.getClass.getName} elements in a building context: $jpe")
    }
    
    import JValueMerger.{MergeArraysOnIndex, leftMerge}
    
    pathVals match {
      case empty if empty.isEmpty => JNothing
      case notEmpty => notEmpty.map {
                         case (jp, jv) => buildLinearPath(jp reverse, jv)
                       } reduceLeft(leftMerge(MergeArraysOnIndex)_)
    }
  }
  
  override def build(pathVals: Set[(JPath, Any)]): String = {
    return compact(render(buildJValue(pathVals.map{
      case (jp, s: String) => {
        if (!s.isEmpty && (s(0) == '{' || s(0) == '[')) {
          try {
            (jp, parse(s))
          } catch {
            case _: ParserUtil.ParseException =>
              (jp, JString(s))
          } 
        } else {
          (jp, JString(s))
        }
      }
      case (jp, n: Number) => {
        n match {
          case i: java.lang.Integer => (jp, JInt(i.intValue()))
          case d: java.lang.Double => (jp, JDouble(d))
          case l: java.lang.Long => (jp, JLong(l))
          case f: java.lang.Float => (jp, JDouble(f.doubleValue()))
          case _ => (jp, getNumberValue(n))
        }
      }
      case (jp, b: Boolean) => (jp, JBool(b))
      case (jp, o) => (jp, JString(o.toString()))
    })))
  }
  
  def getNumberValue[A](x: A)(implicit num: Numeric[A] = null): JValue = Option(num) match {
    case Some(num) => JDouble(num.toDouble(x))
    case None => JNothing
  }
  
}

class JsonBuildingException(message: String) extends RuntimeException(message)

object JValueBuilder {
  
  def apply(): JValueBuilder = new JValueBuilder()
  
}