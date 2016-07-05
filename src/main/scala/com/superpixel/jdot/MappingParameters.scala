package com.superpixel.jdot

import com.superpixel.jdot.pathing.JPath


sealed trait Inclusions;
case class FixedInclusions(inc: Map[String, String]) extends Inclusions;
case object NoInclusions extends Inclusions;


sealed trait MergingJson;
case class MergingJsonPre(pre: Seq[String]) extends MergingJson
case class MergingJsonPost(post: Seq[String]) extends MergingJson
case class MergingJsonPrePost(pre: Seq[String], post: Seq[String]) extends MergingJson
case object NoMerging extends MergingJson



sealed trait AttachmentContext;

/**
  * Using context json as passed in, no overriding or drilling.
  */
case object SimpleAttachmentContext extends AttachmentContext;

/**
  * Takes the context json as passed into the attach/attachList call and drills into it using the jPath to fetch context json
  * @param jPathToContext
  */
case class PathAttachmentContext(jPathToContext: String) extends AttachmentContext;

case class OverrideAttachmentContext(contextJson: String) extends AttachmentContext;

case class ListOverrideAttachmentContext(contextJsonList: List[String]) extends AttachmentContext;

case class OverridePathAttachmentContext(contextJson: String, jPathToContext: String) extends AttachmentContext;



object MappingParameters {
  
  def combineInclusions(left: Inclusions, right: Inclusions): Inclusions = (left, right) match {
    case (l, NoInclusions) => l
    case (NoInclusions, r) => r
    case (FixedInclusions(l), FixedInclusions(r)) => FixedInclusions(r ++ l)
  }
  
  def combineMergingJson(left: MergingJson, right: MergingJson): MergingJson = (left, right) match {
    case (l, NoMerging) => l
    case (NoMerging, r) => r
    
    case (MergingJsonPre(pre1), MergingJsonPre(pre2)) => MergingJsonPre(pre1 ++ pre2);
    case (MergingJsonPost(post1), MergingJsonPost(post2)) => MergingJsonPost(post1 ++ post2);

    case (MergingJsonPost(post), MergingJsonPre(pre)) => MergingJsonPrePost(pre, post)
    case (MergingJsonPre(pre), MergingJsonPost(post)) => MergingJsonPrePost(pre, post)
    
    case (MergingJsonPrePost(pre1, post), MergingJsonPre(pre2)) => MergingJsonPrePost(pre1 ++ pre2, post)
    case (MergingJsonPrePost(pre, post1), MergingJsonPost(post2)) => MergingJsonPrePost(pre, post1 ++ post2)
      
    case (MergingJsonPre(pre1), MergingJsonPrePost(pre2, post)) => MergingJsonPrePost(pre1 ++ pre2, post)
    case (MergingJsonPost(post1), MergingJsonPrePost(pre, post2)) => MergingJsonPrePost(pre, post1 ++ post2)
      
    case (MergingJsonPrePost(pre1, post1), MergingJsonPrePost(pre2, post2)) => MergingJsonPrePost(pre1 ++ pre2, post1 ++ post2)
  }
}