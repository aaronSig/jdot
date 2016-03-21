package com.superpixel.advokit.json.lift

import com.superpixel.advokit.json.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._
import java.text.NumberFormat
import com.github.nscala_time.time.Imports._

object JValueTransmuter {
  
  @throws(classOf[JsonTransmutingException])
  def transmute(value: JValue, transmuteType: String, argument: Option[String]): JValue = transmuteType match {
    case "b" => toBoolean(value, argument)
    case "n" => toNumber(value, argument)
    case "s" => toString(value, argument) //toString + operations e.g. .1:u would take first char and uppercase it: simpson -> S
    case "f" => toFormattedFloat(value, argument) //numberFormater, follows printf rules for 'f'
    case "i" => toInt(value, argument) //intFormater, follows printf rules for 'i'
    case "%" => toPercentage(value, argument) //converts to float, then to percentage
    case "d" => toDateFormat(value, argument) //date formatter, takes dateString or timestamp and toString based on argument
    case "ord" => toOrdinal(value, argument) //ordinal formatter 1 -> 1st, 4 - 4th
    case _ => JString(value.toString)
  }
  
  private def stringFormat(objArr: Array[_], op:String, arg: String, value: JValue): String = {
    try {
      String.format("%" + arg + op, objArr)
    } catch {
      case e : Throwable => throw new JsonTransmutingException(s"Did not understand transmutation arguments - $arg. Nested: ${e.getMessage}", value)
    }
  }
  
  private def toDateFormat(value: JValue, argument: Option[String]): JValue = {
    val dateOpt: Option[DateTime] = value match {
      case JBool(bool) => Some(DateTime.now)
      case JInt(int) => Some(new DateTime(int.toLong))
      case JDouble(db) => Some(new DateTime(db.toLong))
      case JDecimal(bd) => Some(new DateTime(bd.toLong))
      case JLong(l) => Some(new DateTime(l))
      case JString(str) => Some(DateTime.parse(str))
      case _ => None
    }
    dateOpt match {
      case None => throw new JsonTransmutingException("Cannot convert json value to date.", value)
      case Some(d: DateTime) => argument match {
        case None => JString(d.toString)
        case Some(arg: String) => JString(d.toString(arg))
      }
    }
    
  }
  
  val ordSuffix = Array("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th");
  val ordFullString = Array("Zeroth", "First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth", "Eleventh", "Twelth")
  private def toOrdinal(value: JValue, argument: Option[String]): JValue = {
    val iOpt: Option[Int] = value match {
      case JBool(bool) => Some(if (bool) 1 else 0)
      case JInt(int) => Some(int.toInt)
      case JDouble(db) => Some(db.toInt)
      case JDecimal(bd) => Some(bd.toInt)
      case JLong(l) => Some(l.toInt)
      case _ => None
    }
    iOpt.map { _.abs } match {
      case None => toOrdinal(toNumber(value, None), argument)
      case Some(i: Int) => argument.map{ _.toLowerCase } match {
        case None | Some("suffix") => JString(i.toString + ordSuffix(i % 10)) 
        case Some("full") if (i >= 0 && i < ordFullString.length) =>
          JString(ordFullString(i))
        case _ => JString(i.toString + ordSuffix(i % 10))
      }
    }
  }
  
  
  val numericRegex = """(\d)""".r
  private def toPercentage(value: JValue, argument: Option[String]): JValue = {
    val fOpt: Option[Double] = value match {
      case JBool(bool) => Some(if (bool) 1d else 0d)
      case JInt(int) => Some(int.toDouble)
      case JDouble(db) => Some(db)
      case JDecimal(bd) => Some(bd.toDouble)
      case JLong(l) => Some(l.toDouble)
      case _ => None
    }
    fOpt match {
      case None => toPercentage(toNumber(value, None), argument)
      case Some(d: Double) => argument.map { _.toLowerCase() } match {
        case None => JString(NumberFormat.getPercentInstance().format(d))
        case Some("!") | Some("c") => JString(NumberFormat.getPercentInstance().format(1.0d-d))
        case Some(numericRegex(of)) => try {
          JString(NumberFormat.getPercentInstance().format(d/of.toDouble))
        } catch {
          case e: Throwable => throw new JsonTransmutingException(s"Cannot convert $of to a double for percentage format: ${e.getMessage}", value)
        }
        case _ => JString(NumberFormat.getPercentInstance().format(d))
      }
    }
  }
  
  private def toInt(value: JValue, argument: Option[String]): JValue = {
    val iOpt: Option[Int] = value match {
      case JBool(bool) => Some(if (bool) 1 else 0)
      case JInt(int) => Some(int.toInt)
      case JDouble(db) => Some(db.toInt)
      case JDecimal(bd) => Some(bd.toInt)
      case JLong(l) => Some(l.toInt)
      case _ => None
    }
    iOpt match {
      case None => toInt(toNumber(value, None), argument)
      case Some(i: Int) => JString(stringFormat(Array(i), "i", argument.getOrElse(""), value))
    }
  }
  
  private def toFormattedFloat(value: JValue, argument: Option[String]): JValue = {
    val fOpt: Option[Double] = value match {
      case JBool(bool) => Some(if (bool) 1d else 0d)
      case JInt(int) => Some(int.toDouble)
      case JDouble(db) => Some(db)
      case JDecimal(bd) => Some(bd.toDouble)
      case JLong(l) => Some(l.toDouble)
      case _ => None
    }
    fOpt match {
      case None => toFormattedFloat(toNumber(value, None), argument)
      case Some(d: Double) => JString(stringFormat(Array(d), "f", argument.getOrElse(""), value))
    }
    
  }
  
  val substringRegex = """([0-9]*)\.([0-9]*)""".r
  private def toString(value: JValue, argument: Option[String]): JString = {
    def argApply(args: Seq[String], string: String): String = args match {
      case Nil => string
      case "u" +: tl => argApply(tl, string.toUpperCase())
      case "l" +: tl => argApply(tl, string.toLowerCase())
      case "1u" +: tl => argApply(tl, string.capitalize)
      case substringRegex(ss) +: tl => {
        ss.split("\\.") match {
          case Array("", d) => argApply(tl, string.take(d.toInt))
          case Array(d, "") => argApply(tl, string.drop(d.toInt))
          case Array(d1, d2) => argApply(tl, string.substring(d1.toInt, d2.toInt))
          case _ => argApply(tl, string)
        }
      }
      case arg +: tl => try {
        argApply(tl, String.format("%" + arg + "s", string))
      } catch {
        case e : Throwable => argApply(tl, string)
      }
    }
    
    val str = value.toString();
    
    JString(argument.map { s => s.toLowerCase().split(":") } match {
      case None => str
      case Some(arr) => argApply(arr, str)
    })
  }
  
  private def toNumber(value: JValue, argument: Option[String]): JValue = {
    val argTup = argument.map(str => {
      val arr = str.split(":") 
      if (arr.length == 1) {
        (arr(0), arr(0))
      } else {
        (arr(0), arr(1))
      }
    })
    val num: JValue = value match {
      case JBool(bool) => if(bool) JInt(1) else JInt(0)
      case ji: JInt => ji
      case jdb: JDouble => jdb
      case jdc: JDecimal => jdc
      case jl: JLong => jl
      case _:JObject | _:JArray | JNothing | JNull => 
        throw new JsonTransmutingException("Cannot convert " + value.getClass.getName + " to a json number.", value)
      case JString(str: String) => argTup match {
        case Some((base, _)) if base == "2" || base == "8" || base == "16" => string2Number(str, Some(base.toInt))
        case _ => toNumber(string2Number(str), argument)
      }
    }
    
    argTup match {
      case None => num
      case Some((_,"l")) => num match {
        case jl: JLong => jl
        case JInt(i) => JLong(i.toLong)
        case JDouble(db) => JLong(db.toLong)
        case JDecimal(dc) => JLong(dc.toLong)
        case _ => num
      }
      case Some((_,"d")) | Some((_,"f")) => num match {
        case jdb: JDouble => jdb
        case JInt(i) => JDouble(i.toDouble)
        case JLong(l) => JDouble(l.toDouble)
        case JDecimal(dc) => JDouble(dc.toDouble)
        case _ => num
      }
      case Some((_,"i")) => num match {
        case ji: JInt => ji
        case JLong(l) => JInt(l.toInt)
        case JDouble(db) => JInt(db.toInt)
        case JDecimal(dc) => JInt(dc.toInt)
        case _ => num
      }
      case Some(_) => num
    }
}
  
  val hexCharRegex = "[a-fA-F]".r
  private def string2Number(str: String, base: Option[Int] = None): JValue = try {
    if (str.take(2) == "0x") {
      JLong(Integer.parseInt(str.drop(2), 16))
    } else if (str.take(2) == "0b") {
      JLong(Integer.parseInt(str.drop(2), 2))
    } else if (base != None) {
      JLong(Integer.parseInt(str, base.get))
    } else str.toLowerCase().splitAt(str.length - 1) match {
      
      case (l, "l") => JLong(l.toLong)
      case (d, "d") => JDouble(d.toDouble)
      case (f, "f") => JDouble(f.toFloat)
      case (i, "i") => JInt(i.toInt)
      case _ => if (str.contains(".")) {
          JDouble(str.toDouble)
        } else {
          JInt(str.toInt)
        }
    }
  } catch {
    case e: Throwable => throw new JsonTransmutingException(s"Could not convert string $str to number: ${e.getMessage}", JString(str))
  }

  private def toBoolean(value: JValue, argument: Option[String]): JBool = {
    val bool: Boolean = value match {
      case jb: JBool => jb.value
      case JString(str: String) => 
        !(str.toLowerCase == "false" ||
          str.toLowerCase == "f" ||
          str.toLowerCase == "n")
      case JInt(int) => int != 0
      case JDouble(db) => db != 0
      case JDecimal(bd) => bd != 0
      case JLong(l) => l != 0
      case JObject(fieldLs) => !fieldLs.isEmpty
      case JArray(itemLs) => !itemLs.isEmpty
      case JNothing | JNull => false
    }
    
    JBool(argument.map { s => s.toLowerCase() } match {
      case None => bool
      case Some("!") | Some("not") => !bool
      case _ => bool
    })
  }
}


class JsonTransmutingException(val message: String, val jVal: JValue) 
  extends RuntimeException(s"$message. For Json:\n " + (if (jVal == JNothing) "nothing" else pretty(render(jVal))))

//    case JBool(bool) => 
//    case JString(str: String) => 
//    case JInt(int) => 
//    case JDouble(db) => 
//    case JDecimal(bd) => 
//    case JLong(l) =>
//    case JObject(fieldLs) => 
//    case JArray(itemLs) => 
//    case JNothing | JNull => 