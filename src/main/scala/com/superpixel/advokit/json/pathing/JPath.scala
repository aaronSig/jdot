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
  
  val ESCAPE = """\"""
  val DELIMS = Seq(""".""", """[""", """]""", """>""", """(""", """)""", """|""", """$""", """{""", """}""")
  val LITERAL_BETWEEN_DELIMS = Map("""(""" -> """)""", """|""" -> """$""", """}""" -> """$""")
  
  /***
   * ExpressionTypes are determined from a sequence of ExpressionElements via Regex.
   * 
   * Any JsonKeys are replaced by the keyStr, any Literals are replaced by literalStr before regex matching.
   * This allows for simple declaration of regex patterns.
   */
  sealed trait ExpressionType {
    final val keyStr = "key";
    final val literalStr = "lit";
    protected def nestedPathWithoutChar(c: String): String = """[^""" + Pattern.quote(c) + """]+"""
    
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
      ("""\(""" + literalStr + """\)""").r    
    )
  }
  case object StringFormatExpression extends ExpressionType {
    override val mustBeFinal = true;
    override val patterns = Seq(
      ("""\|("""+ literalStr +""")?(\$\{"""+ nestedPathWithoutChar("}") +"""\}("""+ literalStr +""")?)*""").r
    )
  }
  
  /***
   * Helper trait for splitting and identifying pathString elements
   */
  sealed trait ExpressionElement {
    def str: String;
  }

  case class JsonKey(str: String) extends ExpressionElement
  case class Literal(str: String) extends ExpressionElement
  case class Delimiter(str: String) extends ExpressionElement
  
  /***
   * Takes path string, validates and converts in to JPathElement tree.
   * 
   * apply method can be called from an object as follows
   *  JParser.fromString(pathString)
   */
  @throws(classOf[JPathException])
  def fromString(pathString: String): JPath = {
    
    /***
     * Takes expression type tuples in reverse order and adds them tail recursively into a JPathElement tree.
     * Expression type is mapped to a JPathElement case class, extracting the relevant information 
     * from the expression sequence included.
     * 
     */
    def jsonPathConstructor(ls: Seq[(ExpressionType, Seq[ExpressionElement])], acc: Seq[JPathElement]): Seq[JPathElement] = {
      ls match { 
        case Nil => acc
        case (StartAccessExpression, Nil) +: Nil => acc
        case (StartAccessExpression, exprSeq) +: Nil => 
          exprSeq filter {!_.isInstanceOf[Delimiter]} match {
            case JsonKey(key) +: Nil => jsonPathConstructor(Nil, JObjectPath(key) +: acc)
            case _ => throw new JPathException(s"Cannot find Json Key expression element in StartAccessExpression: $exprSeq", pathString)
          }
        
        case (DefaultValueExpression, exprSeq) +: tl =>
          exprSeq filter {!_.isInstanceOf[Delimiter]} match {
            case Literal(lit) +: Nil => jsonPathConstructor(tl, JDefaultValue(lit) +: acc)
            case _ => throw new JPathException(s"Cannot find Literal expression element in DefaultValueExpression: $exprSeq", pathString)
          }
        
        //Only the above are allowed to start the pathString (which ends the expressionType sequence)
        case (_, exprSeq) +: Nil => 
          throw new JPathException("JPath String does not start correctly, the first expression must be a json key or a string literal in brackets.", pathString)
        
        case (AccessExpression, exprSeq) +: tl => exprSeq match {
          case Seq(Delimiter("["), JsonKey(IS_NUMERIC_REGEX(idx)), Delimiter("]")) => 
            jsonPathConstructor(tl, JArrayPath(idx.toInt) +: acc)
          case _ =>  
            exprSeq filter {!_.isInstanceOf[Delimiter]} match {
              case JsonKey(key) +: Nil => jsonPathConstructor(tl, JObjectPath(key) +: acc)
              case _ => throw new JPathException(s"Cannot find Json Key expression element in AccessExpression: $exprSeq", pathString)
            }
          
        }
        
        case (LinkExpression, exprSeq) +: tl => 
          jsonPathConstructor(tl, JPathLink +: acc)
          
          
        case (StringFormatExpression, exprSeq) +: tl =>
          jsonPathConstructor(tl, parseStringFromatExpression(exprSeq) +: acc)
        
        case _ => throw new JPathException("JPath semantic error.", pathString)
      }
    }
    
    /***
     * Helper method to parse StringFormatExpression sequences.
     * 
     */
    def parseStringFromatExpression(exprSeq: Seq[ExpressionElement]) : JStringFormat = {
      def inner(expr: Seq[ExpressionElement], accFormatString: Seq[StringFormat], accValuePaths: Seq[JPath]): JStringFormat = expr match {
        case Nil => JStringFormat(accFormatString.reverse, accValuePaths.reverse)
        case (Literal(lit) +: Nil) => JStringFormat((FormatLiteral(lit) +:accFormatString).reverse, accValuePaths.reverse)
        case (hd +: (tl @ (hd2 +: tl2))) => (hd, hd2) match {
          case (Delimiter("$"), Delimiter("{")) => tl2.span { (s: ExpressionElement) => s != Delimiter("}") } match {
            case (innerExprSeq, brackEnd +: remainder) => {
              if (brackEnd != Delimiter("}")) throw new JPathException("Unterminated '${' in String Format expression: " + exprSeq.mkString, pathString)
              else inner(remainder, 
                         ReplaceHolder +: accFormatString, 
                         //TRANSFORM innerExprSeq to JPATH +: accValuePaths)
                         new JPath(jsonPathConstructor(transformToExpressions(innerExprSeq), Nil)) +: accValuePaths
                         )
            }
          }
          case (Literal(lit), _) => inner(tl, FormatLiteral(lit) +: accFormatString, accValuePaths)
          case _ => throw new JPathException("String format path parsing error in: " + exprSeq.mkString, pathString)
        }
        case _ => throw new JPathException("String format path parsing error in: " + exprSeq.mkString, pathString)
      }
      exprSeq.head match {
        case Delimiter("|") => inner(exprSeq.tail, Nil, Nil)
        case _ => throw new JPathException("String format expression does not start with '|': " + exprSeq.mkString, pathString)
      }
    }
    

    jsonPathConstructor(
        transformToExpressions(
            splitToDelimiterSequence(pathString)), Nil)
  }
  
  
  /***
   * Attempts to form a JPath from the pathString,
   * If an error is throw, it's message is return with false
   * Else true is returned. 
   */
  def validate(pathString: String): (Boolean, Option[String]) = {
    try {
      fromString(pathString)
      (true, None)
    } catch {
      case jpe: JPathException =>  (false, Some(jpe.getMessage));
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
  
  def unescapeJsonKey(key: String): String = {
    ESCAPED_DELIM_REGEX.replaceAllIn(key, """$2""")
  }
  
  def escapeJsonKey(key: String): String = {
    UNESCAPED_DELIM_REGEX.replaceAllIn(key, """\\$1""")
  }
  
  
  /***
   * Returns a sequence of strings, which splits the string into ExpressionElements
   * 
   * For example:
   * "one.two[three].four>.five"   
   * 
   *   transforms into -
   *
   *  Seq(JsonKey("one"),
   *      Delimiter("."), 
   *      JsonKey("two"), 
   *      Delimiter("["), 
   *      JsonKey("three"), 
   *      Delimiter("]"), 
   *      Delimiter("."), 
   *      JsonKey("four"), 
   *      Delimiter(">"),
   *      Delimiter("."),
   *      JsonKey("five"))
   *  
   *  And:
   *    "one.two|web.start?id=${three(1.234)}"
   *
   *  transforms into -
   *  
   *  Seq(JsonKey("one"),
   *      Delimiter("."),
   *      JsonKey("two"), 
   *      Delimiter("|"), 
   *      Literal("web.start?id="), 
   *      Delimiter("$"), 
   *      Delimiter("{"), 
   *      JsonKey("two"), 
   *      Delimiter("("), 
   *      Literal("1.234"), 
   *      Delimiter(")"), 
   *      Delimiter("}"))
   * 
   */
  private def splitToDelimiterSequence(pathString: String): Seq[ExpressionElement] = {
    val length = pathString.length();
    def inner(start: Int, check: Int, acc: Seq[ExpressionElement], literalUntil: Option[String] = None): Seq[ExpressionElement] = {
      if (start >= length) acc filter { _.str != "" } reverse;
      else if (check >= length) literalUntil match {
        case Some(_) => inner(check+1, check+1, (Literal(pathString.substring(start, length)) +: acc)) 
        case None => inner(check+1, check+1, (JsonKey(unescapeJsonKey(pathString.substring(start, length))) +: acc))
      }
      else {
        (pathString.charAt(check).toString(), literalUntil) match {
          case ("", _) => throw new JPathException(s"String format expression error. Check: $check Length: $length", pathString);
          case (ESCAPE, _) => inner(start, check+2, acc, literalUntil)
          case (c, Some(s)) if (s == c) => 
            inner(check+1, check+1, 
                  Delimiter(c) +: Literal(pathString.substring(start, check)) +: acc, 
                  LITERAL_BETWEEN_DELIMS.get(c))
          case (_, lu @ Some(s)) => inner(start, check+1, acc, lu)
          case (c @ IS_DELIMITER_REGEX(_*), None) => 
            inner(check+1, check+1, 
                  Delimiter(c) +: JsonKey(unescapeJsonKey(pathString.substring(start, check))) +: acc, 
                  LITERAL_BETWEEN_DELIMS.get(c))
          case (c, None) => inner(start, check+1, acc, None)
        }
      }
    }
    
//    // Splits the pathString, separating delimiters from keys.
//    // For example "one.two[three].four>.five" is split into
//    // Seq("one", ".", "two", "[", "three", "]" , ".", "four", ">", ".", "five")
//    pathString.split(WITH_DELIMITER_REGEX_STR).toSeq.filter { _ != "" }

    inner(0, 0, Nil);
  }
  
  /***
   * Takes a sequence of ExpressionElements and returns a sequence of tuples, which contain the 
   * expression type and the corresponding sequence of strings in REVERSE ORDER.
   * 
   * For example:
   * Seq(JsonKey("one"),
   *      Delimiter("."), 
   *      JsonKey("two"), 
   *      Delimiter("["), 
   *      JsonKey("three"), 
   *      Delimiter("]"), 
   *      Delimiter("."), 
   *      JsonKey("four"), 
   *      Delimiter(">"),
   *      Delimiter("."),
   *      JsonKey("five"))
   * 
   *   transforms into -
   *
   *  Seq(
   *    (AccessExpression, Seq(Delimiter("."), JsonKey("five")))
   *    (LinkExpression, Seq(Delimiter(">"))),
   *    (AccessExpression, Seq(Delimiter("."), JsonKey("four"))),
   *    (AccessExpression, Seq(Delimiter("["), JsonKey("three"), Delimiter("]"))),
   *    (AccessExpression, Seq(Delimiter("."), JsonKey("two"))),
   *    (StartAccessExpression, Seq(JsonKey("one"))),
   *  )
   */
  @throws(classOf[JPathException])
  private def transformToExpressions(exprSeq: Seq[ExpressionElement]): Seq[(ExpressionType, Seq[ExpressionElement])] = {
    
    /***
     * Tail recurses through the full expression sequence extracting expressions.
     * Returns a sequence of tuples, which contain the expression type and the corresponding sequence of strings.
     * This sequence is in reverse order, which allows for the AST to be build leaf up.
     * 
     * For example:
     *  Seq(JsonKey("one"),
     *      Delimiter("."), 
     *      JsonKey("two"), 
     *      Delimiter("["), 
     *      JsonKey("three"), 
     *      Delimiter("]"), 
     *      Delimiter("."), 
     *      JsonKey("four"), 
     *      Delimiter(">"),
     *      Delimiter("."),
     *      JsonKey("five"))
     *      
     *   transforms into -
     *  
     *  Seq(
     *    (AccessExpression, Seq(Delimiter("."), JsonKey("five")))
     *    (LinkExpression, Seq(Delimiter(">"))),
     *    (AccessExpression, Seq(Delimiter("."), JsonKey("four"))),
     *    (AccessExpression, Seq(Delimiter("["), JsonKey("three"), Delimiter("]"))),
     *    (AccessExpression, Seq(Delimiter("."), JsonKey("two"))),
     *    (StartAccessExpression, Seq(JsonKey("one"))),
     *  )
     */
    def inner(exprSeq: Seq[ExpressionElement], acc: Seq[(ExpressionType, Seq[ExpressionElement])]): Seq[(ExpressionType, Seq[ExpressionElement])] = exprSeq match {
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
     * Expression test takes in the expression sequence and boolean denoted whether the remainder is Nil.
     * (i.e. that this is the complete remaining expression sequence)
     * 
     * If no sub-sequence passes the expression test an exception is thrown.
     */
    @throws(classOf[JPathException])
    def extractNextExpression(exprSeq: Seq[ExpressionElement], expressionTest: (Seq[ExpressionElement], Boolean)=>Option[ExpressionType]): ((ExpressionType, Seq[ExpressionElement]), Seq[ExpressionElement]) = exprSeq match {
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
        throw new JPathException("String path of invalid format at " + exprSeq, exprSeq.mkString)
      }
    }
    
    /***
     * Takes the expression types that should be tested for and constructs a lambda
     * that returns the expression type if a sequence of strings matches it.
     * Lambda also takes a boolean to denote whether the sequence ends at the string/line end.
     * 
     * And JsonKeys or Literals are replaced by a placeholder and then concatenated to a single string with Delimiters verbatim.
     * This allows for easy matching and simple declaration of the expression patterns.
     */
    def testForExpressionType(exprTypes: ExpressionType*): (Seq[ExpressionElement], Boolean)=>Option[ExpressionType] = {
      (exprEleSeq: Seq[ExpressionElement], end: Boolean) => {
        val exprStr = exprEleSeq.map { exE => exE match {
          case Delimiter(d) => d
          case JsonKey(_) => exprTypes.head.keyStr
          case Literal(_) => exprTypes.head.literalStr
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