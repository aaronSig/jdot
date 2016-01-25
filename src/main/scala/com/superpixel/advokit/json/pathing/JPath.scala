package com.superpixel.advokit.json.pathing

import scala.util.matching.Regex
import java.util.regex.Pattern
import scala.language.implicitConversions

class JPath(val seq: Seq[JPathElement]) {
  
  def canEqual(a: Any) = (a.isInstanceOf[JPath] || a.isInstanceOf[Seq[_]])

  override def equals(that: Any): Boolean =
    that match {
      case that: JPath => that.canEqual(this) && this.hashCode == that.hashCode && that.seq == this.seq
      case that: Seq[Any] => that.canEqual(this) && this.hashCode == that.hashCode && that == this.seq
      case _ => false
   }

  override def hashCode:Int = {
    return seq.hashCode()
  }
  
  override def toString: String = {
    return seq.toString
  }
}

class JPathException(message: String, pathString: String, cause: Throwable = null) 
  extends RuntimeException(s"$message IN: '$pathString'", cause)


object JPath {
  def apply(jPathElements: JPathElement*): JPath = new JPath(jPathElements)
  
  implicit def seq2JPath(seq: Seq[JPathElement]): JPath = new JPath(seq)
  implicit def path2Seq(path: JPath): Seq[JPathElement] = path.seq
  
  val DELIMS = Seq(""".""", """[""", """]""", """>""", """(""", """)""", """|""", """$""", """{""", """}""")
  
  abstract sealed trait ExpressionType {
    final val keyStr = "key";
    def mustBeFinal: Boolean = false;
    def patterns: Seq[Regex];
  }
  case object StartAccessExpression extends ExpressionType {
    override val patterns = Seq(
      keyStr.r,
      "".r,
      ("""\[""" + keyStr + """\]""").r
    )
  }
  case object AccessExpression extends ExpressionType {
    override val patterns = Seq(
      ("""\.""" + keyStr).r,
      ("""\[""" + keyStr + """\]""").r
    )
  }
  case object LinkExpression extends ExpressionType {
    override val patterns = Seq(
      """\>""".r
    )
  }
  case object DefaultValueExpression extends ExpressionType {
    override val patterns = Seq(
      ("""\(""" + keyStr + """\)""").r    
    )
  }
  case object StringFormatExpression extends ExpressionType {
    override val mustBeFinal = true;
    override val patterns = Seq(
      ("""\|("""+keyStr+""")?(\$\{[^}]+\}("""+keyStr+""")?)*""").r
    )
  }
  
  /***
   * Takes path string, validates and converts in to JPathElement tree.
   * 
   * apply method can be called from an object as follows
   *  JParser.fromString(pathString)
   */
  @throws(classOf[JPathException])
  def fromString(pathString: String): JPath = {
    
    /***
     * Takes expression type tuples in reverse order and adds them tail recursively 
     * into a JPathElement tree.
     * Expression type is mapped to a JPathElement class, extracting the relevent information 
     * from the expression sequence included.
     * 
     */
    def jsonPathConstructor(ls: Seq[(ExpressionType, Seq[String])], acc: Seq[JPathElement]): Seq[JPathElement] = {
      ls match { 
        case Nil => acc
        case (StartAccessExpression, Nil) +: Nil => acc
        case (StartAccessExpression, exprSeq) +: Nil => 
          jsonPathConstructor(Nil, JObjectPath(extractFirstFieldKey(exprSeq)) +: acc)
        case (_, exprSeq) +: Nil => 
          throw new JPathException("J stringPath does not start correctly, the first expression must be a json key", pathString)
        
        case (AccessExpression, exprSeq) +: tl => exprSeq match {
          case Seq("[", IS_NUMERIC_REGEX(idx), "]") => 
            jsonPathConstructor(tl, JArrayPath(idx.toInt) +: acc)
          case _ =>
            jsonPathConstructor(tl, JObjectPath(extractFirstFieldKey(exprSeq)) +: acc)            
        }
        
        case (LinkExpression, exprSeq) +: tl => 
          jsonPathConstructor(tl, JPathLink +: acc)
          
        case (DefaultValueExpression, exprSeq) +: tl =>
          jsonPathConstructor(tl, JDefaultValue(extractFirstFieldKey(exprSeq)) +: acc)
          
        case (StringFormatExpression, exprSeq) +: tl =>
          jsonPathConstructor(tl, parseStringFromatExpression(exprSeq) +: acc)
        
        case _ => throw new JPathException("JPath semantic error.", pathString)
      }
    }
    
    def extractFirstFieldKey(exprSeq: Seq[String]): String = {
      exprSeq.find { case IS_DELIMITER_REGEX(_*) => false case _ => true } match {
        case Some(key) => unescapeJKey(key)
        case None => throw new JPathException("No field found in expression sequence " + exprSeq.mkString, pathString)
      }
    }
    
    def parseStringFromatExpression(exprSeq: Seq[String]) : JStringFormat = {
      def inner(expr: Seq[String], accFormatString: Seq[StringFormat], accValuePaths: Seq[JPath]): JStringFormat = expr match {
        case Nil => JStringFormat(accFormatString.reverse, accValuePaths.reverse)
        case (hd +: Nil) => JStringFormat((FormatLiteral(hd) +:accFormatString).reverse, accValuePaths.reverse)
        case (hd +: (tl @ (hd2 +: tl2))) => (hd, hd2) match {
          case ("$", "{") => tl2.span { (s: String) => s != "}" } match {
            case (innerExprSeq, brackEnd +: remainder) => {
              if (brackEnd != "}") throw new JPathException("Unterminated '${' in String Format expression: " + exprSeq.mkString, pathString)
              else inner(remainder, 
                         ReplaceHolder +: accFormatString, 
                         //TRANSFORM innerExprSeq to JPATH +: accValuePaths)
                         new JPath(jsonPathConstructor(transformToExpressions(innerExprSeq), Nil)) +: accValuePaths
                         )
            }
          }
          //ESCAPE '|' in hd
          case _ => inner(tl, FormatLiteral(hd) +: accFormatString, accValuePaths)
        }
      }
      exprSeq.head match {
        case "|" => inner(exprSeq.tail, Nil, Nil)
        case _ => throw new JPathException("String format expression does not start with '|': " + exprSeq.mkString, pathString)
      }
    }
    
    new JPath(
        jsonPathConstructor(
            transformToExpressions(
                splitToDelimeterSequence(pathString)), Nil))
  }
  
  def validate(pathString: String): Boolean = {
    try {
      transformToExpressions(splitToDelimeterSequence(pathString)) match {
        case Nil => false;
        case _ => true;
      }
    } catch {
      case jpe: JPathException =>  false;
    }
  }
  
  
  
  private val IS_NUMERIC_REGEX = """([0-9]+)""".r
  
  private val IS_DELIMITER_STR = """[""" + DELIMS.map(Pattern.quote(_)).mkString + """]"""
  private val IS_DELIMITER_REGEX = (IS_DELIMITER_STR).r
  private val UNESCAPED_DELIMS_STR = """((?<=(?<!\\)(?:\\{2}){0,10})[""" + DELIMS.map(Pattern.quote(_)).mkString + """])"""
  
  private val ESCAPED_DELIM_REGEX = ("""(\\)(""" + IS_DELIMITER_STR + """)""").r
  private val UNESCAPED_DELIM_REGEX = UNESCAPED_DELIMS_STR.r
  private val WITH_DELIMITER_REGEX_STR = {
    """((?<=""" + UNESCAPED_DELIMS_STR + """)|(?=""" + UNESCAPED_DELIMS_STR + """))"""
  }
  
  def unescapeJKey(key: String): String = {
    ESCAPED_DELIM_REGEX.replaceAllIn(key, """$2""")
  }
  
  def escapeJKey(key: String): String = {
    UNESCAPED_DELIM_REGEX.replaceAllIn(key, """\\$1""")
  }
  
  
  /***
   * Returns a sequence of strings, which splits the string (and includes) by delimiters
   * 
   * For example:
   * "one.two[three].four>.five"   
   * 
   *   transforms into -
   *
   *  Seq("one", ".", "two", "[", "three", "]", ".", "four", ">", ".", "five")
   *  
   */
 
  private def splitToDelimeterSequence(pathString: String): Seq[String] = {
    
    // Splits the pathString, separating delimiters from keys.
    // For example "one.two[three].four>.five" is split into
    // Seq("one", ".", "two", "[", "three", "]" , ".", "four", ">", ".", "five")
    pathString.split(WITH_DELIMITER_REGEX_STR).toSeq.filter { _ != "" }

  }
  
  /***
   * Returns a sequence of tuples, which contain the expression type and the corresponding sequence of strings in REVERSE ORDER.
   * 
   * For example:
   * Seq("one", ".", "two", "[", "three", "]", ".", "four", ">", ".", "five")   
   * 
   *   transforms into -
   *
   *  Seq(
   *    (AccessExpression, Seq(".", "five"))
   *    (LinkExpression, Seq(">")),
   *    (AccessExpression, Seq(".", "four")),
   *    (AccessExpression, Seq("[", "three", "]")),
   *    (AccessExpression, Seq(".", "two")),
   *    (StartAccessExpression, Seq("one")),
   *  )
   */
  @throws(classOf[JPathException])
  private def transformToExpressions(exprSeq: Seq[String]): Seq[(ExpressionType, Seq[String])] = {
    
    /***
     * Tail recurses through the full expression sequence extracting expressions.
     * Returns a sequence of tuples, which contain the expression type and the corresponding sequence of strings.
     * This sequence is in reverse order, which allows for the AST to be build leaf up.
     * 
     * For example:
     *  Seq("one", ".", "two", "[", "three", "]" , ".", "four", ">", ".", "five")
     *   transforms into -
     *  
     *  Seq(
     *    (AccessExpression, Seq(".", "five"))
     *    (LinkExpression, Seq(">")),
     *    (AccessExpression, Seq(".", "four")),
     *    (AccessExpression, Seq("[", "three", "]")),
     *    (AccessExpression, Seq(".", "two")),
     *    (StartAccessExpression, Seq("one")),
     *  )
     */
    def inner(exprSeq: Seq[String], acc: Seq[(ExpressionType, Seq[String])]): Seq[(ExpressionType, Seq[String])] = exprSeq match {
      case Nil => acc
      case seq => {
        val retTup = extractNextExpression(seq, testForExpressionType(AccessExpression, LinkExpression, DefaultValueExpression, StringFormatExpression))
        inner(retTup._2, retTup._1 +: acc)
      }
    }
    
    /***
     * Applies the expression test to all sub-sequences of the expression sequence that start at index 0
     * If the expression test returns an expression type, it is returned with the sub-sequence that matched it in a tuple.
     * This tuple is nested in another, the second element of which is the leftover sub-sequence.
     * 
     * If no sub-sequence passes the expression test an exception is thrown.
     */
    @throws(classOf[JPathException])
    def extractNextExpression(exprSeq: Seq[String], expressionTest: (Seq[String], Boolean)=>Option[ExpressionType]): ((ExpressionType, Seq[String]), Seq[String]) = exprSeq match {
      case Nil => expressionTest(Nil, true) match {
        case Some(exprType) => return ((exprType, Nil), Nil)
        case None => throw new JPathException("Empty expression sequence does not match to any allowed expression type.", exprSeq.mkString)
      }
      case _ => {
        for (i <- 1 to exprSeq.size) {
          exprSeq.splitAt(i) match {
            case (expr, remainder) => expressionTest(expr, remainder.isEmpty) match {
              case Some(exprType) => return ((exprType, expr), remainder)
              case None => 
            }
          }
        }
        throw new JPathException("String path of invalid format at " + exprSeq.mkString, exprSeq.mkString)
      }
    }
    
    /***
     * Takes the expression types that should be tested for and constructs a lambda
     * that returns the expression type if a sequence of strings matches it.
     * Lambda also takes a boolean to denote whether the sequence ends at the string/line end.
     * 
     * Any none delimiters in the sequence of strings is replaces by a 'key' placeholder and then
     * concatenated to a single string.
     * This allows for easy matching and simple declaration of the expression patterns.
     */
    def testForExpressionType(exprTypes: ExpressionType*): (Seq[String], Boolean)=>Option[ExpressionType] = {
      (strSeq: Seq[String], end: Boolean) => {
        val exprStr = strSeq.map { s => s match {
          case IS_DELIMITER_REGEX(_*) => s
          case _ => exprTypes.head.keyStr
        }}.mkString
        exprTypes.find { x => x.patterns.exists { (p: Regex) => exprStr match {
          case p(_*) => true && !(x.mustBeFinal && !end)
          case _ => false
        }}}
      }
    }
    
    //Pulls out the starting expression, usually just a key name ("one"), throws a JPathException
    //if the expression doesn't start properly.
    // In our example This tuple looks like:
    // ((StartAccessExpression, Seq("one")), Seq(".", "two", "[", "three", "]" , ".", "four", ">", ".", "five"))
    val startingTuple = extractNextExpression(exprSeq, testForExpressionType(StartAccessExpression, DefaultValueExpression))
    
    //Pulls out the remaining expressions tail recursively and returns them
    return inner(startingTuple._2, Seq(startingTuple._1));
  }
}