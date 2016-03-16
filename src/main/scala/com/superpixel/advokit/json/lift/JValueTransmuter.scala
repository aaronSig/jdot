package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._

object JValueTransmuter {
  
  @throws
  def transmute(value: JValue, transmuteType: String, argument: Option[String]): JValue = transmuteType match {
    case "b" => toBoolean(value, argument)
    case "n" => toNumber(value, argument)
//    case "s" => ??? //toString + operations e.g. .1u would take first char and uppercase it: simpson -> S
//    case "f" => ??? //numberFormater, follows printf rules
//    case "%" => ??? //converts to foat, then to percentage
//    case "d" => ??? //date formatter, takes dateString or timestamp and toString based on argument
    case _ => JString(value.toString)
  }
  
  private def toNumber(value: JValue, argument: Option[String]): JValue = value match {
    case JBool(bool) => if(bool) JInt(1) else JInt(0)
    case ji: JInt => ji
    case jdb: JDouble => jdb
    case jdc: JDecimal => jdc
    case jl: JLong => jl
    case _:JObject | _:JArray | JNothing | JNull => 
      throw new JsonTransmutingException("Cannot convert " + value.getClass.getName + " to a json number.", value)
    case JString(str: String) => ???
  }

  private def toBoolean(value: JValue, argument: Option[String]): JBool = value match {
    case jb: JBool => jb
    case JString(str: String) => 
      JBool(str.toLowerCase != "false" ||
            str.toLowerCase != "f")
    case JInt(int) => JBool(int != 0)
    case JDouble(db) => JBool(db != 0)
    case JDecimal(bd) => JBool(bd != 0)
    case JLong(l) => JBool(l != 0)
    case JObject(fieldLs) => JBool(!fieldLs.isEmpty)
    case JArray(itemLs) => JBool(!itemLs.isEmpty)
    case JNothing | JNull => JBool(false)
  }
}


class JsonTransmutingException(val message: String, val jVal: JValue) 
  extends RuntimeException(s"$message. For Json:\n " + pretty(render(jVal)))

//    case JBool(bool) => 
//    case JString(str: String) => 
//    case JInt(int) => 
//    case JDouble(db) => 
//    case JDecimal(bd) => 
//    case JLong(l) =>
//    case JObject(fieldLs) => 
//    case JArray(itemLs) => 
//    case JNothing | JNull => 