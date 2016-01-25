package com.superpixel.advokit.mapper


sealed trait Inclusions;
case class FixedInclusions(inc: Map[String, String]) extends Inclusions;
case object NoInclusions extends Inclusions;


sealed trait MergingJson;
case class MergingJsonPre(pre: Seq[String]) extends MergingJson
case class MergingJsonPost(post: Seq[String]) extends MergingJson
case class MergingJsonPrePost(pre: Seq[String], post: Seq[String]) extends MergingJson
case object NoMerging extends MergingJson


sealed trait Attachment {
  def attacher: JsonContentAttacher;
};
case class SimpleAttachment(jsonToAttach: String, attacher: JsonContentAttacher) extends Attachment
case class SimpleListAttachment(jsonListToAttach: List[String], attacher: JsonContentAttacher) extends Attachment
case class SimpleTransformAttachment(jsonToTransformAttach: String, transformer: JsonContentTransformer, attacher: JsonContentAttacher) extends Attachment
case class SimpleTransformListAttachment(jsonListToTransformAttach: List[String], transformer: JsonContentTransformer, attacher: JsonContentAttacher) extends Attachment
case class NestedTransformAttachment(jsonToTransformAttach: String, transformer: JsonContentTransformer, transformAttachments: List[Attachment], attacher: JsonContentAttacher) extends Attachment
case class NestedTransformListAttachment(jsonListToTransformAttach: List[String], transformer: JsonContentTransformer, transformAttachments: List[Attachment], attacher: JsonContentAttacher) extends Attachment

