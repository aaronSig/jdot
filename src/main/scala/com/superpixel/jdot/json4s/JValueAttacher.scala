package com.superpixel.jdot.json4s

import com.superpixel.jdot._
import com.superpixel.jdot.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._


class JValueAttacher(attachmentPairs: Set[JPathPair], attachmentContext: AttachmentContext,
                     treatArraysAsList: Boolean, transformer: Option[JDotTransformer], nestedAttachers: List[JDotAttacher]) extends JDotAttacher {

  val attachBuilder = JValueBuilder()



  private def factorContextJValue(passedContext: () => JValue): JValue = {
    attachmentContext match {
      case PathAttachmentContext(path) => drillIntoContext(path, passedContext.apply())
      case OverrideAttachmentContext(cj) => parse(cj)
      case OverridePathAttachmentContext(cj, path) => drillIntoContext(path, parse(cj))
      case ListOverrideAttachmentContext(cjLs) => JArray(cjLs.map {jStr => parse(jStr) })
      case _ => passedContext.apply()
    }

  }

  private def drillIntoContext(jPath: JPath, jVal: JValue): JValue = {
    JValueAccessor.apply(jVal).getValue(jPath)
  }

  private def transformAndNestedAttachers(context: JValue): JValue = (transformer, context, treatArraysAsList) match {
    case (Some(trans: JValueTransformer), JArray(ls), true) => trans.transformJValueList(ls, nestedAttachers)
    case (Some(trans: JValueTransformer), _, _) => trans.transformJValue(context, nestedAttachers)
    case (Some(trans), JArray(ls), true) => parse(trans.transformList(ls.map(jv => compact(render(jv))), nestedAttachers))
    case (Some(trans), _, _) => parse(trans.transform(compact(render(context)), nestedAttachers))

    case (None, JArray(ls), true) => JArray(ls.map(jv => applyNestedAttachersVerbatim(jv)))
    case (None, _, _) => applyNestedAttachersVerbatim(context)
  }

  private def applyNestedAttachersVerbatim(jValue: JValue): JValue = {
    def recu(jv: JValue, attacherLs: List[JDotAttacher]): JValue = attacherLs match {
      case Nil => jValue
      case (hd: JValueAttacher) :: tl => recu(hd.attachJValue(jValue, jv), tl)
      case hd :: tl => recu(parse(hd.attach(compact(render(jv)), compact(render(jValue)))), tl)
    }
    recu(jValue, nestedAttachers)
  }



  
  private def _attachJValue(contextJson: () => JValue, attachToJson: JValue): JValue = {
    val postContext: JValue = factorContextJValue(contextJson)

    val postTransform: JValue = transformAndNestedAttachers(postContext)

    val accessor = JValueAccessor(postTransform)
    val attachee = attachBuilder.buildJValue(attachmentPairs.map{ jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from))});

    import JValueMerger.leftMerge
    //    val a = leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
    //    println("Attached:\n " + compact(render(a)))
    //    a
    leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
  }
  
  override def attach(contextJson: String, attachToJson: String): String = compact(render(_attachJValue(() => parse(contextJson), parse(attachToJson))))
  override def attachList(contextJsonList: List[String], attachToJson: String): String =
    compact(render(_attachJValue(() => {
      JArray(contextJsonList.map(parse(_)))
    },  parse(attachToJson))))
  
  def attachJValue(contextJson: JValue, attachToJson: JValue): JValue = _attachJValue(() => contextJson, attachToJson)
  def attachJValueList(contextJsonList: List[JValue], attachToJson: JValue): JValue = _attachJValue(() => JArray(contextJsonList), attachToJson)
  
}


object JValueAttacher {
  
  def apply(attachmentPairs: Set[JPathPair],
            attachmentContext: AttachmentContext = SimpleAttachmentContext,
            treatArraysAsList: Boolean = true,
            transformer: Option[JDotTransformer] = None,
            nestedAttachers: List[JDotAttacher] = Nil): JValueAttacher =
    new JValueAttacher(attachmentPairs, attachmentContext, treatArraysAsList, transformer, nestedAttachers)
  
}