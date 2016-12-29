package com.superpixel.jdot.json4s

import com.superpixel.jdot._
import com.superpixel.jdot.pathing._
import org.json4s._
import org.json4s.native.JsonMethods._


class JValueAttacher(attachmentPairs: Set[JPathPair],
                     additionalContext: Option[ContextAlteration],
                     transformer: Option[JDotTransformer],
                     nestedAttachers: List[JDotAttacher]) extends JDotAttacher {

  val attachBuilder = JValueBuilder()


  private def factorContextJValue(rootContext: Either[JValue, List[JValue]]): Either[JValue, List[JValue]] = {
    additionalContext match {
      case None => return rootContext
      case Some(sc) => sc match {
        case PathFollowAlteration(path) => rootContext match {
          case Left(rc) => JValueAccessor.apply(rc).getValue(path) match {
              case JArray(ls) => Right(ls.map(alt => attachRootToAltered(alt, rc)))
              case alt => Left(attachRootToAltered(alt, rc))
            }
          case Right(rcLs) => Right(rcLs.map { rc =>
            attachRootToAltered(JValueAccessor.apply(rc).getValue(path), rc)
            })
          }
        case AlterJsonContext(alt) => rootContext match {
          case Left(rc) => Left(attachRootToAltered(alt, rc))
          case Right(rcLs) => Right(rcLs.map { rc =>
            attachRootToAltered(alt, rc)
            })
        }
        case AlterJsonListContext(ls) => rootContext match {
          case Left(rc) => Right(ls.map(alt => attachRootToAltered(alt, rc)))
          case Right(rcLs) => Right(ls.map(alt => attachRootToAltered(alt, JArray(rcLs))))
        }
      }
    }
  }

  private def attachRootToAltered(alt: JValue, root: JValue): JValue = alt match {
    case jo: JObject => JValueAttacher.rootAttacher.attachJValue(root, jo)
    case _ => alt
  }

  private def transformAndNestedAttachers(context: JValue): JValue = transformer match {
    case None => applyJustNestedAttachers(context)
    case Some(jValTrans: JValueTransformer) => jValTrans.transformJValue(context, nestedAttachers)
    case Some(trans) => parse(trans.transform(compact(render(context)), nestedAttachers))
  }

  private def transformAndNestedAttachers(contextList: List[JValue]): JValue = transformer match {
    case None => JArray(contextList.map(applyJustNestedAttachers(_)))
    case Some(jValTrans: JValueTransformer) => jValTrans.transformJValueList(contextList, nestedAttachers)
    case Some(trans) => parse(trans.transformList(contextList.map(jv => compact(render(jv))), nestedAttachers))
  }


  private def applyJustNestedAttachers(jValue: JValue): JValue = JValueAttacher.applyAttachers(jValue, jValue, nestedAttachers);

  
  private def _attachJValue(contextJson: Either[JValue, List[JValue]], attachToJson: JValue): JValue = {
    val postTransform: JValue = factorContextJValue(contextJson) match {
      case Left(JNothing) => JNothing
      case Left(alteredContext) => transformAndNestedAttachers(alteredContext)
      case Right(alteredContextList) => transformAndNestedAttachers(alteredContextList)
    }

    val accessor = JValueAccessor(postTransform)
    val attachee = attachBuilder.buildJValue(attachmentPairs.map{ jpm: JPathPair => (jpm.to, accessor.getValue(jpm.from))});

    import JValueMerger.leftMerge
    //    val a = leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
    //    println("Attached:\n " + compact(render(a)))
    //    a
    leftMerge(MergeArraysOnIndex)(attachee, attachToJson)
  }
  
  override def attach(contextJson: String, attachToJson: String): String =
    compact(render(_attachJValue(Left(parse(contextJson)), parse(attachToJson))))
  override def attachList(contextJsonList: List[String], attachToJson: String): String =
    compact(render(_attachJValue(Right(contextJsonList.map(parse(_))),  parse(attachToJson))))
  
  def attachJValue(contextJson: JValue, attachToJson: JValue): JValue =
    _attachJValue(Left(contextJson), attachToJson)
  def attachJValueList(contextJsonList: List[JValue], attachToJson: JValue): JValue =
    _attachJValue(Right(contextJsonList), attachToJson)
  
}


object JValueAttacher {

  private val rootAttacher = JValueAttacher(Set((JDotAttacher.originalContextKey, "")))
  
  def apply(attachmentPairs: Set[JPathPair],
            contextPath: Option[JPath] = None,
            transformer: Option[JDotTransformer] = None,
            nestedAttachers: List[JDotAttacher] = Nil): JValueAttacher =
    new JValueAttacher(attachmentPairs, contextPath.map(jp => PathFollowAlteration(jp)), transformer, nestedAttachers)

  def withAdditionalJson(attachmentPairs: Set[JPathPair],
                         additionalContextJson: JValue,
                         transformer: Option[JDotTransformer] = None,
                         nestedAttachers: List[JDotAttacher] = Nil): JValueAttacher =
    new JValueAttacher(attachmentPairs, Some(AlterJsonContext(additionalContextJson)), transformer, nestedAttachers)

  def withAdditionalJsonList(attachmentPairs: Set[JPathPair],
                             additionalContextJsonList: List[JValue],
                             transformer: Option[JDotTransformer] = None,
                             nestedAttachers: List[JDotAttacher] = Nil): JValueAttacher =
    new JValueAttacher(attachmentPairs, Some(AlterJsonListContext(additionalContextJsonList)), transformer, nestedAttachers)


  def applyAttachers(context:JValue, applyTo: JValue, atts: List[JDotAttacher]): JValue = {
    def recu(jValue: JValue, attacherLs: List[JDotAttacher]): JValue = attacherLs match {
      case Nil => jValue
      case (hd: JValueAttacher) :: tl => recu(hd.attachJValue(context, jValue), tl)
      case hd :: tl => {
        recu(parse(hd.attach(compact(render(context)), compact(render(jValue)))), tl)
      }
    }
    recu(applyTo, atts)
  }
}

sealed trait ContextAlteration;

case class PathFollowAlteration(contextPath: JPath) extends ContextAlteration;
case class AlterJsonContext(contextJson: JValue) extends ContextAlteration;
case class AlterJsonListContext(contextJson: List[JValue]) extends ContextAlteration;

