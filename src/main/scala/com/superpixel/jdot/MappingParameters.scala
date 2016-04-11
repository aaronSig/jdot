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


sealed trait Attachment {
  def attacher: JdotAttacher;
};
case class SimpleAttachment(jsonToAttach: String, attacher: JdotAttacher) extends Attachment
case class SimpleListAttachment(jsonListToAttach: List[String], attacher: JdotAttacher) extends Attachment
case class SimpleTransformAttachment(jsonToTransformAttach: String, transformer: JdotTransformer, attacher: JdotAttacher) extends Attachment
case class SimpleTransformListAttachment(jsonListToTransformAttach: List[String], transformer: JdotTransformer, attacher: JdotAttacher) extends Attachment
case class JsonArrayTransformAttachment(jPathToArray: String, jsonContainingArray: String, transformer: JdotTransformer, attacher: JdotAttacher) extends Attachment
case class NestedTransformAttachment(jsonToTransformAttach: String, transformer: JdotTransformer, transformAttachments: List[Attachment], attacher: JdotAttacher) extends Attachment
case class NestedTransformListAttachment(jsonListToTransformAttach: List[String], transformer: JdotTransformer, transformAttachments: List[Attachment], attacher: JdotAttacher) extends Attachment
case class JsonArrayNestedTransformAttachment(jPathToArray: String, jsonContainingArray: String, transformer: JdotTransformer, transformAttachments: List[Attachment], attacher: JdotAttacher) extends Attachment
