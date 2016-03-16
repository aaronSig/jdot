package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._

object JValueTraverser {

  val jDefaultValueNoComplexEnd: PartialFunction[Tuple2[JValue, Option[JPathElement]], String] = {
    case (_:JObject | _:JArray , Some(JDefaultValue(dVal))) => dVal
  }
  
  type TraverseFn[T] = (JValue,JPath) => Option[T] 
  
  sealed trait RouteChoice;
  case object Stop extends RouteChoice;
  case object Continue extends RouteChoice;
  
  val existsAndNotFalsePartial: TraverseFn[Boolean] = traverse((s: String) => None, 
                                                       (jVal: JValue, jPath: JPathElement) => (JNothing, Stop),
                                                       innerConditionJPathEndLamb)_
  
  def existsAndNotFalse(value: JValue, conditionPath: JPath): Boolean = existsAndNotFalsePartial(value, conditionPath).getOrElse(false);
  
  def existsAndNotFalse(value: String, conditionPath: String): Boolean = existsAndNotFalsePartial(parse(value), JPath.fromString(conditionPath)).getOrElse(false);

  @throws(classOf[JsonTraversalException])
  def traverse[T](linkLamb: String=>Option[JValue],
                  notFoundLamb: (JValue, JPathElement) => (JValue, RouteChoice),
                  endLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]],T])
                  (jVal: JValue, jPath: JPath): Option[T] = {
    
    
    def traverseRecu(value: JValue, pathSeq: Seq[JPathElement], prev: Option[JPathElement] = None): Option[T] = {
      def routeValueOption(valOpt: Option[JValue], hd: JPathElement, tl: JPath): Option[T] = {
          valOpt match {
          case None => notFoundLamb(value, hd) match {
            case (jV, Stop) => return endLamb.lift(jV, Some(hd))
            case (jV, Continue) => traverseRecu(jV, tl, Some(hd))
          } 
          case Some(jV) => traverseRecu(jV, tl, Some(hd))
          } 
      }
      
      pathSeq match {
        case Nil => endLamb.lift(value, prev)
        
        case JObjectPath(key) +: tl => routeValueOption(accessJObjectValue(value, key), pathSeq.head, tl)
        
        case JArrayPath(key) +: tl => routeValueOption(accessJArrayValue(value, key), pathSeq.head, tl)
        
        case JPathLink +: tl => 
          routeValueOption(
            value match {
              case JString(s) => linkLamb(s)
              case JInt(d) =>  linkLamb(d.toString)
              case _ => prev match {
                case Some(JDefaultValue(dVal)) => linkLamb(dVal)
                case _ => None
              }
            }, pathSeq.head, tl)
        
        case JDefaultValue(dVal) +: tl => value match {
          case JNothing | JNull => endLamb.lift(JString(dVal), Some(pathSeq.head))
          case jV => routeValueOption(Some(jV), pathSeq.head, tl)
        }
        
        // Potentially could be cleaner/more efficient by defining own endLamb and creating own curried traverse
        // This would include moving JDefaultValue logic in endLamb to own PartialFunction, so it could be included here
        case JStringFormat(formatSeq, valuePaths) +: tl => {
          val innerStringFormatJPathTraverse = traverse(linkLamb, notFoundLamb, innerStringFormatJPathEndLamb(jVal, jPath))_
          routeValueOption(
            Some(JString(StringFormat.formatToString(formatSeq, 
                valuePaths.map { (vP: JPath) =>  innerStringFormatJPathTraverse(value, vP) match {
                  case None => throw new JsonTraversalException(s"JPath in String formatting could not be followed, it must end in a stringable value: $jPath", jVal)
                  case Some(s) => s
                }
            }))),
            pathSeq.head,
            tl)
        }
        
        case JConditional(conditionPath, testPathOpt, truthPath, falsePath) +: tl => testPathOpt match {
          case None => {
            traverse(linkLamb, notFoundLamb, innerConditionJPathEndLamb)(value, conditionPath) match {
              case Some(true) => routeValueOption(Some(value), pathSeq.head, truthPath ++ tl)
              case _ => routeValueOption(Some(value), pathSeq.head, falsePath ++ tl)
            }
          }
          case Some(testPath) => {
            //Could use existing traverseRecu, or make one with JValue end
            val testTraverse: TraverseFn[JValue] = traverse(linkLamb, notFoundLamb, standardEndLamb)_
            testTraverse(value, conditionPath) match {
              case None | Some(JNothing) => routeValueOption(Some(value), pathSeq.head, falsePath ++ tl)
              case Some(x) => testTraverse(value, testPath) match {
                case None | Some(JNothing) => routeValueOption(Some(value), pathSeq.head, falsePath ++ tl)
                case Some(y) if x == y => routeValueOption(Some(value), pathSeq.head, truthPath ++ tl)
                case _ => routeValueOption(Some(value), pathSeq.head, falsePath ++ tl)
              }
            }
          }
        }
        
        case JTransmute(transmuteType, argument) +: tl => 
          try {
            routeValueOption(Some(JValueTransmuter.transmute(value, transmuteType, argument)), pathSeq.head, tl)
          } catch {
            case e: JsonTransmutingException =>
              throw new JsonTraversalException(e.message, e.jVal)
          }
        
      }
    }
    
    traverseRecu(jVal, jPath) 
  }
  
  private def innerStringFormatJPathEndLamb(jVal: JValue, jPath: JPath): PartialFunction[Tuple2[JValue, Option[JPathElement]], String] = 
    jDefaultValueNoComplexEnd orElse {
      case (_:JObject | _:JArray, _)  => throw new JsonTraversalException(s"JPath in String formatting ended in an Array or an Object, it must end in a stringable value: $jPath", jVal)
      case (JString(str), _) => str
      case (JDouble(db), _) => db.toString
      case (JDecimal(bd), _) => bd.toString
      case (JInt(int), _) => int.toString
      case (JLong(l), _) => l.toString
      case (JBool(bool), _) => bool.toString
    }
  
  private val innerConditionJPathEndLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]], Boolean] = {
    case (JBool(bool), _) => bool
    case (JString(str: String), _) => str.toLowerCase != "false"
    case (JInt(int), _) => int != 0
    case (JDouble(db), _) => db != 0
    case (JDecimal(bd), _) => bd != 0
    case (JLong(l), _) => l != 0
    case (JObject(fieldLs), _) => !fieldLs.isEmpty
    case (JArray(itemLs), _) => !itemLs.isEmpty
    case (JNothing | JNull, _) => false
  }
  
  val standardEndLamb: PartialFunction[Tuple2[JValue, Option[JPathElement]], JValue] = 
    (jDefaultValueNoComplexEnd andThen { case s: String => JString(s) }) orElse 
    { case (jVal, _) => jVal }
  
  private def accessJArrayValue(jVal: JValue, key: Int): Option[JValue] = jVal match {
    case JObject(fieldLs) => fieldLs.find { jFld => jFld._1 == key.toString } .map(_._2)
    case JArray(valueLs) => valueLs.lift(key)
    case _ => None
  }
  
  private def accessJObjectValue(jVal: JValue, key: String): Option[JValue] = jVal match {
    case JObject(fieldLs) => fieldLs.find { jFld => jFld._1 == key } .map(_._2)
    case _ => None
  }
}

class JsonTraversalException(message: String, jVal: JValue = JNull) 
  extends RuntimeException(s"$message. For Json:\n " + pretty(render(jVal)))