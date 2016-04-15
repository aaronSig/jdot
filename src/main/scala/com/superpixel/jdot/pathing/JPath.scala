package com.superpixel.jdot.pathing

import scala.util.matching.Regex
import java.util.regex.Pattern
import scala.language.implicitConversions
import scala.collection.immutable.Stack
import scala.annotation.tailrec

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

class JPathException(message: String, pathString: String, index: Int = -1, cause: Throwable = null) 
  extends RuntimeException(s"$message @: ${if (index == -1) "Unknown" else index.toString} IN: '$pathString'", cause)


object JPath {
  def apply(jPathElements: JPathElement*): JPath = new JPath(jPathElements)
  
  implicit def string2JPath(s: String): JPath = JPath.fromString(s)
  implicit def seq2JPath(seq: Seq[JPathElement]): JPath = new JPath(seq)
  implicit def path2Seq(path: JPath): Seq[JPathElement] = path.seq
  
    /***
   * Takes path string, validates and converts in to JPathElement tree.
   * 
   * apply method can be called from an object as follows
   *  JParser.fromString(pathString)
   */
  @throws(classOf[JPathException])
  def fromString(pathString: String): JPath = {
    
        jsonPathConstructor(pathString,
          transformToExpressions(
            splitToDelimiterSequence(pathString)))
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
  
  
  sealed trait ExpressionOutput
  object ExpressionOutput {
	  def compatible(in: Option[ExpressionOutput], expecting: Option[ExpressionOutput]): Boolean = (in, expecting) match {
	  case (None, _) => true
    case (_, None) => true
	  case (Some(Path), Some(_)) => true
	  case (Some(Value), Some(Value)) => true
	  case _ => false
	  }
  }
  /***
   * Can always be passed to an expression expecting a Value.
   * Cannot be given to an expression expecting a Path.
   */
  object Value extends ExpressionOutput;
  /***
   * Can always be given to an expression expecting a Path.
   * If passed to an expression expecting a Value, a value will be extracted from that node.
   * If the value at the node is an json object or array, then behaviour is undefined and perhaps implemented on an individual basis.
   */
  object Path extends ExpressionOutput;
  
  
  /***
   * ExpressionTypes are determined from a sequence of ExpressionElements via Regex.
   * 
   * Any JsonKeys are replaced by the keyStr, any Literals are replaced by literalStr before regex matching.
   * This allows for simple declaration of regex patterns.
   */
  sealed trait ExpressionType {
    final val keyStr = "key";
    final val literalStr = "lit";
    final val nestedPathStr = "nest";
    
    def accepts: ExpressionOutput;
    def outputs: ExpressionOutput;
    def patterns: Seq[Regex];
  }
  
  case object StartAccessExpression extends ExpressionType {
    override val accepts = Path
    override val outputs = Path
    override val patterns = Seq(
      keyStr.r,
      "".r,
      ("""\[""" + keyStr + """\]""").r
    )
  }
  case object AccessExpression extends ExpressionType {
    override val accepts = Path
    override val outputs = Path
    override val patterns = Seq(
      ("""\.""" + keyStr).r,
      ("""\[""" + keyStr + """\]""").r
    )
  }
  case object LinkExpression extends ExpressionType {
    override val accepts = Value
    override val outputs = Path
    override val patterns = Seq(
      """\>""".r
    )
  }
  case object LiteralExpression extends ExpressionType {
    override val accepts = Path
    override val outputs = Value
    override val patterns = Seq(
      ("""\((""" + literalStr + """(""" + TransmuteExpression.patterns.head.toString() + """)?)?\)""").r    
    )
  }
  case object DefaultValueExpression extends ExpressionType {
    override val accepts = Path
    override val outputs = Path
    override val patterns = Seq(
      ("""\((""" + literalStr + """(""" + TransmuteExpression.patterns.head.toString() + """)?)?\)""").r    
    )
  }
  case object StringFormatExpression extends ExpressionType {
    override val accepts = Path
    override val outputs = Value
    override val patterns = Seq(
      ("""\|("""+ literalStr +""")?(\{"""+ nestedPathStr +"""\}("""+ literalStr +""")?)*""").r
    )
  }
  case object ConditionalExpression extends ExpressionType {
    override val accepts = Path
    override val outputs = Value
    override val patterns = Seq(
      ("""\~""" + nestedPathStr + """(=""" + nestedPathStr + """)?\?""" + nestedPathStr + """\:""" + nestedPathStr).r
    )
  }
  case object TransmuteExpression extends ExpressionType {
    override val accepts = Value
    override val outputs = Value
    override val patterns = Seq(
      ("""\^""" + """([a-z\%]+)""" + """(\<""" + literalStr + """)?""").r
    )
  }
  
  /***
   * Marks expressions that can-only/cannot be used at the start of the path string
   */
  val allowedStartingExpressions = Seq(StartAccessExpression, LiteralExpression, StringFormatExpression, ConditionalExpression, TransmuteExpression)
  val allowedNonStartingExpressions = Seq(AccessExpression, LinkExpression, DefaultValueExpression, StringFormatExpression, ConditionalExpression, TransmuteExpression)
  
  
  
  val ESCAPE = """\"""
  
  /***
   * Helper trait for interpreting pathStrings
   */
  sealed trait ExpressionMode {
    def brackets: Option[Tuple2[String, String]] = None;
    def internalBrackets: Option[Tuple2[String, String]] = None;
    def starters: Option[Set[String]] = None;
    def betweenMap: Option[Map[String, String]] = None;
    def delimiters: Option[Set[String]] = None;
    def starterDelimeters: Option[Map[String, Set[String]]] = None;
    def canMoveToModes: Seq[ExpressionMode]
  }
  /**
   * Base mode for parsing pathStings, denotes tree head
   */
  case object BaseMode extends ExpressionMode {
    def canMoveToModes: Seq[ExpressionMode] = Seq(PathMode);
  }
  /***
   * Default mode which interprets JsonKeys and Delimiters
   * This is the starting mode of the a string, and is also active in NestedMode
   */
  case object PathMode extends ExpressionMode {
    override val delimiters = Some(Set(""".""", """[""", """]""", """>""", """(""", """)""", """|""", """{""", """}""", """~""", """^"""))
    override val canMoveToModes: Seq[ExpressionMode] = Seq(LiteralMode, NestMode, SpecialMode)
  }
  /***
   * NestMode defines a nested path with in an expression
   * Parses initially in PathMode with overrides from NestMode fields
   */
  case object NestMode extends ExpressionMode {
    override val brackets = Some(("""{""", """}"""))
    override val internalBrackets = Some(("""(""", """)"""))
    override val starters = Some(Set("""~"""))
    override val starterDelimeters = 
      Some(Map("""~""" -> Set("""=""", """?""", """:""")))
    override val canMoveToModes: Seq[ExpressionMode] = Seq()
  }
  /***
   * LiteralMode takes every character literally (except the escape character).
   * If literal mode is started by a bracket, then the closing bracket must be escaped.
   */
  case object LiteralMode extends ExpressionMode {
    override val brackets = Some(("""(""", """)"""))
    override val starters = Some(Set("""|""", """<"""))
    override val delimiters = Some(Set("""^"""))
    override val starterDelimeters = 
      Some(Map("""|""" -> Set("""{""", """}""")))
    override val canMoveToModes: Seq[ExpressionMode] = Seq(NestMode, SpecialMode)
  }
  /***
   * SpecialMode takes characters literally, but these characters must be matched explicitly by ExpressionType regex.
   * SpecialMode only occurs between certain delimiters.
   * If the end delimiter is needed literally, it must be escaped.
   */
  case object SpecialMode extends ExpressionMode {
    override val betweenMap = Some(Map("""^""" -> """<"""))
    override val canMoveToModes: Seq[ExpressionMode] = Seq(LiteralMode)
  }
  
  
  sealed trait ModeBreakout {
    val chars: Set[String]
  }
  case class Bracketed(char: String) extends ModeBreakout {
    val chars = Set(char)
  }
  case class Context(inherited: Set[String], thisLevel: Set[String]) extends ModeBreakout {
    val chars = inherited
  }
  
  
  /***
   * Helper trait for splitting and identifying pathString elements
   */
  sealed trait ExpressionElement
  case class JsonKey(str: String) extends ExpressionElement
  case class Delimiter(str: String) extends ExpressionElement
  case class Literal(str: String) extends ExpressionElement
  case class Special(str: String) extends ExpressionElement //acts like literal, but not replaced
  case class NestedPath(seq: Seq[ExpressionElement]) extends ExpressionElement
 
  
  def unescapeJsonKey(key: String): String = {
    val isDelimOption = PathMode.delimiters.map { stS => """[""" + stS.map(s => Pattern.quote(s)).mkString + """]""" }
    isDelimOption match {
      case None => return key
      case Some(isDelimStr) => {
    	  val escapedDelimRegex = ("""(""" + Regex.quote(ESCAPE) + """)(""" + isDelimStr + """)""").r
    		escapedDelimRegex.replaceAllIn(key, """$2""")        
      }
    }
  }
  private def unescape(key: String): String = {
    val escapedDelimRegex = ("""(""" + Regex.quote(ESCAPE) + """)(.)""").r
    escapedDelimRegex.replaceAllIn(key, """$2""")        
  }
  
  def escapeJsonKey(key: String): String = {
    val isDelimOption = PathMode.delimiters.map { stS =>  stS.mkString("[\\", "\\", "]")  }
//    println(isDelimOption.get)
    isDelimOption match {
      case None => return key
      case Some(isDelimStr) => {
        val unescapedDelimRegex = ("""((?<=(?<!\""" + ESCAPE + """)(?:\""" + ESCAPE + """{2}){0,10})""" + isDelimStr + """)""").r
//        println(unescapedDelimRegex.toString())
        unescapedDelimRegex.replaceAllIn(key, """\""" + ESCAPE + """$1""")
      }
    }
  }
  
  
  /***
   * Returns a sequence of strings, which splits the string into ExpressionElements
   * 
   * For example:
   * "one.two[3].four>.five^f<2.2"   
   * 
   *   transforms into -
   *
   *  Seq(JsonKey("one"),
   *      Delimiter("."), 
   *      JsonKey("two"), 
   *      Delimiter("["), 
   *      JsonKey("3"), 
   *      Delimiter("]"), 
   *      Delimiter("."), 
   *      JsonKey("four"), 
   *      Delimiter(">"),
   *      Delimiter("."),
   *      JsonKey("five"),
   *      Delimiter("^"),
   *      Special("f"),
   *      Delimiter("<"),
   *      Literal("2.2"))
   *  
   *  And:
   *    "one.two|web.start?id={three(1.234)}"
   *
   *  transforms into -
   *  
   *  Seq(JsonKey("one"),
   *      Delimiter("."),
   *      JsonKey("two"), 
   *      Delimiter("|"), 
   *      Literal("web.start?id="),
   *      Delimiter("{"), 
   *      NestedPath(Seq(JsonKey("three"), 
   *                     Delimiter("("), 
   *                     Literal("1.234"), 
   *                     Delimiter(")"))), 
   *      Delimiter("}"))
   * 
   */
  private def splitToDelimiterSequence(pathString: String): Seq[ExpressionElement] = {
    val length = pathString.length();
    
    type ModeBox = (ExpressionMode, Option[Delimiter], ModeBreakout, Int, Seq[ExpressionElement])
    type BracketBox = (Int, String, String)
    
    def stringIndex(s: String, i: Int): Option[String] = {
      val sLen = s.length
      if (i >= sLen || i < -sLen) None 
      else if (i < 0) Some(s(sLen + i).toString())
      else Some(s(i).toString)
    }
    
    def pathStringIndex(index: Int): Option[String] = stringIndex(pathString, index)
    
    def optionSetExists(set: Option[Set[String]], string: String): Boolean = set.exists { st => st.contains(string) }
    
    def isDelimiter(mode: ExpressionMode, modeStarter: Option[String], d: String): Boolean = {
      return optionSetExists(mode.delimiters, d) || 
             optionSetExists(mode.starterDelimeters.flatMap(mp => modeStarter.flatMap(s => mp.get(s))), d)
    }
    
    def isStarter(mode: ExpressionMode, d: String): Boolean = {
      return mode.brackets.exists(bTup => bTup._1 == d) ||
             optionSetExists(mode.starters, d) || 
             optionSetExists(mode.betweenMap.map(_.keySet), d)
    }
    
    def subpath(startInc: Int, endExc: Int): Option[String] = 
    	if (startInc >= endExc) None
      else Some(pathString.substring(startInc, endExc))
    
    def getJsonKey(startInc: Int, endExc: Int): Option[JsonKey] = subpath(startInc, endExc).map{s=>JsonKey(unescapeJsonKey(s))}
    
    def getLiteral(startInc: Int, endExc: Int): Option[Literal] = {
      @tailrec
      def removeOuterBrackets(string: String, check:Int, starterIndex: Option[Int]): String = {
        (starterIndex, stringIndex(string, check), LiteralMode.brackets) match {
          case (_, None, _) => string
          case (_, Some(e), _) if e == ESCAPE => 
            removeOuterBrackets(string, check+2, starterIndex)
          case (None, Some(b), Some((left, _))) if b == left =>
            removeOuterBrackets(string, check+1, Some(check))
          case (Some(sI), Some(b), Some((_, right))) if b == right =>
            string.take(sI) + string.slice(sI+1, check) + string.drop(check+1)
          case _ =>
            removeOuterBrackets(string, check+1, starterIndex)
        }
      }
      subpath(startInc, endExc)
        .map{s=> Literal(unescape(removeOuterBrackets(s, 0, None)))}
    }
    
    def getSpecial(startInc: Int, endExc: Int): Option[Special] = subpath(startInc, endExc).map{s=>Special(unescape(s))}
    
    def getNestedPath(startInc: Int, endExc: Int): Option[NestedPath] = (pathStringIndex(startInc), pathStringIndex(endExc-1)) match {
      case (Some(l), Some(r)) if NestMode.brackets.exists{ case (left, right) => l == left && r == right } => 
    	  subpath(startInc+1, endExc-1).map { sp => NestedPath(splitToDelimiterSequence(sp)) }
      
      case _ => 
        subpath(startInc, endExc).map { sp => NestedPath(splitToDelimiterSequence(sp)) }
    }
    
    /**
     * Get mode stack -> empty exit acc reverse
     * 
     */
    @tailrec
    def inner2(start: Int, check: Int, bracketStack: Seq[BracketBox], modeStack: Seq[ModeBox]): Seq[ExpressionElement] = {
      
      def pushMode(nextCheck: Int, starter: Option[Delimiter], mode: ExpressionMode, bracketStack: Seq[BracketBox],
                        modeStack: Seq[ModeBox]): (Int, Seq[BracketBox], Seq[ModeBox])  = modeStack match {
        case (curMode, curModeStarter, curBreakout, curStartDepth, curModeAcc) +: modeTl => 
          val depth = bracketStack.headOption.map(tup => tup._1).getOrElse(0)
          (starter, pathStringIndex(nextCheck), mode.brackets) match {
                  
                  //If delimiter is bracket, start bracket mode
                  case (Some(bD), _, Some((left, right))) if bD.str == left =>
                    (nextCheck, 
                        (depth+1, left, right) +: bracketStack, 
                        (mode, Some(bD), Bracketed(right), depth+1, Nil) +:
                            (curMode, curModeStarter, curBreakout, curStartDepth, bD +: curModeAcc) +: modeTl) 
            
                  //Clarifying bracket checker - immediately after a starter delim starts a new mode, 
                  //brackets can be added to clarify the expression e.g. one|({two^n} March 2016)^d
                  //                                                        ^^                  ^
                  case (Some(d), Some(b), Some((left, right))) if b == left => 
                    (nextCheck+1, 
                        (depth+1, left, right) +: bracketStack,
                        (mode, Some(d), Bracketed(right), depth+1, Nil) +: 
                            (curMode, curModeStarter, curBreakout, curStartDepth, d +: curModeAcc) +: modeTl) //Bracket removed               
                        
                  //If no bracket then start new mode with out bracket mode enabled (which is then ended by context only)
                  case (dOpt, _, _) => {
                    val breaks: Set[String] = dOpt.flatMap(delimiter => 
                      mode.betweenMap.flatMap( mp => mp.get(delimiter.str).map( s => Set(s))))
                      .getOrElse(Set())
                      
                    (nextCheck, 
                        bracketStack,
                        (mode, dOpt, Context(breaks ++ curBreakout.chars, breaks), depth, Nil) +:
                           (curMode, curModeStarter, curBreakout, curStartDepth, dOpt ++: curModeAcc) +: modeTl)
                  }
          }
      }
      
      def pushModeFromDelimiter(delimiter: Delimiter, nextCheck: Int, bracketStack: Seq[BracketBox], 
          modeStack: Seq[ModeBox]): (Int, Seq[BracketBox], Seq[ModeBox]) = modeStack match {
        case (curMode, curModeStarter, curBreakout, curStartDepth, curModeAcc) +: modeTl => {
          curMode.canMoveToModes.find { mode => isStarter(mode, delimiter.str) } match {
            case Some(next) =>  pushMode(nextCheck, Some(delimiter), next, bracketStack, modeStack)
            case None => (nextCheck, bracketStack, (curMode, curModeStarter, curBreakout, curStartDepth, delimiter +: curModeAcc) +: modeTl)
          }
        }
      }
      
      @tailrec
      def popMode(delimiter: Option[Delimiter], nextCheck: Int, bracketStack: Seq[BracketBox], 
          modeStack: Seq[ModeBox]): (Int, Seq[BracketBox], Seq[ModeBox]) = modeStack match {
        case (_, curStarter, curBreakout, curStartDepth, curModeAcc) +: (prevMode, prevStarter, prevBreakout, prevStartDepth, prevModeAcc) +: modeTl => (bracketStack, delimiter) match {
          
          case (Nil, None) => curBreakout match {
            case _:Context if curStartDepth == 0 =>
              (nextCheck, bracketStack, (prevMode, prevStarter, prevBreakout, prevStartDepth, curModeAcc ++: prevModeAcc) +: modeTl)
            case _:Bracketed =>
              throw new JPathException("INTERNAL_ERROR: No bracket stack and end of path found, but mode is set to bracketed", pathString, nextCheck-1)
            case _ =>
              throw new JPathException("INTERNAL_ERROR: No bracket stack and end of path found, modes start depth not 0", pathString, nextCheck-1)
          }
          
          case ((_, _, right) +: tl, None) => {
            throw new JPathException("Expected closing bracket: " + right + ", but found end of path", pathString, nextCheck-1)
          }
            
          case (Nil, Some(d)) => curBreakout match {
            case Context(_,thisLevel) if curStartDepth == 0 && thisLevel.contains(d.str) =>
              pushModeFromDelimiter(d, nextCheck, bracketStack, (prevMode, prevStarter, prevBreakout, prevStartDepth, curModeAcc ++: prevModeAcc) +: modeTl)
            case Context(inherited, _) if curStartDepth == 0 && inherited.contains(d.str) =>
              popMode(Some(d), nextCheck, bracketStack, (prevMode, prevStarter, prevBreakout, prevStartDepth, curModeAcc ++: prevModeAcc) +: modeTl)
            case _:Bracketed =>
              throw new JPathException("INTERNAL_ERROR: Mode should end on bracket, but bracket stack is empty", pathString, nextCheck-1)
            case _ =>
              throw new JPathException("INTERNAL_ERROR: Mode stack pop requested, but delimiter doesn't match any brackout clause.", pathString, nextCheck-1)
          }
          
          case ((bDepth, bLeft, bRight) +: brackTl, Some(d)) => 
            if (bDepth != curStartDepth) {
              throw new JPathException("INTENRAL_ERROR: stack pop requested, but bracket depth does not match mode start depth", pathString, nextCheck-1)
            } else curBreakout match {
              
              case Bracketed(expRight) if expRight == bRight && d.str == bRight =>
                //Only add bracket into the exprSeq if it is not a clarifying bracket
                if (curStarter.exists(sD => sD.str == bLeft)) {
                	(nextCheck, brackTl, (prevMode, prevStarter, prevBreakout, prevStartDepth, d +: curModeAcc ++: prevModeAcc) +: modeTl)                  
                } else {
                  (nextCheck, brackTl, (prevMode, prevStarter, prevBreakout, prevStartDepth, curModeAcc ++: prevModeAcc) +: modeTl)
                }
              case Bracketed(expRight) =>
                throw new JPathException("INTERNAL_ERROR: pop requested but brackets do not match", pathString, nextCheck-1)    
              case Context(_,thisLevel) if thisLevel.contains(d.str) && d.str == bRight =>
                throw new JPathException("INTERNAL_ERROR: delimiter matches expected receiving bracket and mode pop requested at proper depth, but breackout is context and bracket found in this level chars.", pathString, nextCheck-1)
              case Context(_,thisLevel) if thisLevel.contains(d.str) =>
                pushModeFromDelimiter(d, nextCheck, bracketStack, (prevMode, prevStarter, prevBreakout, prevStartDepth, curModeAcc ++: prevModeAcc) +: modeTl)
              case Context(inherited, _) if inherited.contains(d.str) =>
                popMode(Some(d), nextCheck, bracketStack, (prevMode, prevStarter, prevBreakout, prevStartDepth, curModeAcc ++: prevModeAcc) +: modeTl)
              case _ =>
                pushModeFromDelimiter(d, nextCheck, bracketStack, modeStack)
          }
          
        }
      }
      
      (modeStack, bracketStack.headOption.getOrElse((0, "", ""))) match {
        case ((mode, modeStarter, breakout, startDepth, modeAcc) +: modeStackTl, (depth, _, receivingBrack)) => 
          pathStringIndex(check) match {
          case Some(ESCAPE) => inner2(start, check+2, bracketStack, modeStack)
          case cO => mode match {
              case BaseMode => (cO, breakout) match {
                case (None, _: Context) if depth == startDepth => 
                    modeAcc.reverse
                case (Some(brack), Bracketed(expBrack)) if brack == expBrack && depth == startDepth && receivingBrack == brack => 
                    modeAcc.reverse
                case (Some(d), Context(chars, _)) if depth == startDepth && chars.contains(d) =>
                    modeAcc.reverse
                case _ if depth == startDepth => 
                    pushMode(check, None, PathMode, bracketStack, modeStack) match {
                      case(newCheck, newBS, newMS) => inner2(newCheck, newCheck, newBS, newMS);
                    }
                case _ =>
                    throw new JPathException("Expected closing bracket, but found end of path", pathString, check)
              }
              
              case _ => {
                val elementLamb: (Int, Int) => Option[ExpressionElement] = mode match {
                  case PathMode => getJsonKey
                  case LiteralMode => getLiteral
                  case SpecialMode => getSpecial
                  case NestMode => getNestedPath
                  case _ => (a: Int, b: Int) => None
                }
                (cO, breakout) match {
                  case (None, _: Context) => 
                      popMode(None, check, bracketStack, (mode, modeStarter, breakout, startDepth, elementLamb(start, check) ++: modeAcc) +: modeStackTl) match {
                        case(newCheck, newBS, newMS) => inner2(newCheck, newCheck, newBS, newMS);
                      }
                  case (Some(brack), Bracketed(expBrack)) if brack == expBrack && depth == startDepth && receivingBrack == brack => 
                      popMode(Some(Delimiter(brack)), check+1, bracketStack, (mode, modeStarter, breakout, startDepth, elementLamb(start, check) ++: modeAcc) +: modeStackTl) match {
                        case(newCheck, newBS, newMS) => inner2(newCheck, newCheck, newBS, newMS);
                      }
                  case (Some(d), Context(chars, _)) if depth == startDepth && chars.contains(d) =>
                      popMode(Some(Delimiter(d)), check+1, bracketStack, (mode, modeStarter, breakout, startDepth, elementLamb(start, check) ++: modeAcc) +: modeStackTl) match {
                        case(newCheck, newBS, newMS) => inner2(newCheck, newCheck, newBS, newMS);
                      }
                  case (Some(brack), _) if mode.brackets.exists{ case (left, right) => brack == left } => 
                      inner2(start, check+1, (depth+1, mode.brackets.get._1,  mode.brackets.get._2) +: bracketStack, modeStack)
                  case (Some(brack), _) if mode.internalBrackets.exists{ case (left, right) => brack == left } => 
                      inner2(start, check+1, (depth+1, mode.internalBrackets.get._1,  mode.internalBrackets.get._2) +: bracketStack, modeStack)
                      
                  case (Some(brack), _) if mode.brackets.exists{ case (left, right) => brack == right } && brack == receivingBrack => 
                      inner2(start, check+1, bracketStack.tail, modeStack)
                  case (Some(brack), _) if mode.internalBrackets.exists{ case (left, right) => brack == right } && brack == receivingBrack => 
                      inner2(start, check+1, bracketStack.tail, modeStack)
                      
                  case (Some(brack), _) if mode.brackets.exists{ case (left, right) => brack == right } && brack != receivingBrack => 
                      throw new JPathException(s"Expecting '$receivingBrack' bracket, but found '$brack'", pathString, check)
                  case (Some(brack), _) if mode.internalBrackets.exists{ case (left, right) => brack == right } && brack != receivingBrack => 
                      throw new JPathException(s"Expecting '$receivingBrack' bracket, but found '$brack'", pathString, check)
                      
                  case (Some(d), _) if depth == startDepth && isDelimiter(mode, modeStarter.map(_.str), d)  =>
                      pushModeFromDelimiter(Delimiter(d), check+1, bracketStack, (mode, modeStarter, breakout, startDepth, elementLamb(start, check) ++: modeAcc) +: modeStackTl) match {
                        case(newCheck, newBS, newMS) => inner2(newCheck, newCheck, newBS, newMS);
                      }
                  case _ => 
                      inner2(start, check+1, bracketStack, modeStack)
                }
              }
          }
        }
      }
    }
    

    inner2(0, 0, Nil, Seq((BaseMode, None, Context(Set(), Set()), 0, Nil)));
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
      case Nil => acc.reverse
      case seq => {
        val prevOutput: Option[ExpressionOutput] = acc.headOption.map{ case (et, _) => et.accepts}
        val retTup = extractNextExpression(seq)
//        println("Extracted: " + retTup)
        val exprType: ExpressionType = retTup._1._1;
        if (ExpressionOutput.compatible(Some(exprType.outputs), prevOutput)) {
          inner(retTup._2, retTup._1 +: acc)
        } else {
          throw new JPathException("A " + exprType + " cannot be placed after a " + acc.head._1 + " as its output cannot be accepted by a " + exprType , exprSeq.mkString)
        }
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
    def extractNextExpression(exprSeq: Seq[ExpressionElement]): ((ExpressionType, Seq[ExpressionElement]), Seq[ExpressionElement]) = exprSeq match {
      case Nil => testForExpressionType(allowedStartingExpressions)(Nil) match {
        case Some(exprType) => return ((exprType, Nil), Nil)
        case None => throw new JPathException("Empty expression sequence does not match to any allowed expression type.", exprSeq.mkString)
      }
      case _ => {
        for (i <- 0 to exprSeq.size) {
          exprSeq.splitAt(i) match {
            case (Nil, expr) => testForExpressionType(allowedStartingExpressions)(expr) match {
              case Some(exprType) => return ((exprType, expr), Nil)
              case None => 
            }
            case (remainder, expr) => testForExpressionType(allowedNonStartingExpressions)(expr) match {
              case Some(exprType) => return ((exprType, expr), remainder)
              case None => 
            }
          }
        }
        throw new JPathException("String path of invalid format.", exprSeq.mkString("[", " ", "]"))
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
    def testForExpressionType(exprTypes: Seq[ExpressionType]): (Seq[ExpressionElement])=>Option[ExpressionType] = {
      (exprEleSeq: Seq[ExpressionElement]) => {
        val exprStr = exprEleSeq.map { exE => exE match {
          case Delimiter(d) => d
          case Special(s) => s
          case JsonKey(_) => exprTypes.head.keyStr
          case Literal(_) => exprTypes.head.literalStr
          case NestedPath(_) => exprTypes.head.nestedPathStr
        }}.mkString
//        println(exprStr)
        exprTypes.find { x => x.patterns.exists { (p: Regex) => exprStr match {
          case p(_*) => true
          case _ => false
        }}}
      }
    }
    
    //Pulls out the starting expression, usually just a key name ("one"), throws a JPathException
    //if the expression doesn't start properly.
    // In our example This tuple looks like:
    // ((StartAccessExpression, Seq("one")), Seq(".", "two", "[", "three", "]" , ".", "four", ">", ".", "five"))
//    val startingTuple = extractNextExpression(exprSeq, testForExpressionType(allowedStartingExpressions))
    
    //Pulls out the remaining expressions tail recursively and returns them
    return inner(exprSeq, Nil);
  }
  
  
  
  
  
  val IS_NUMERIC_REGEX = """([0-9]+)""".r
  def jsonPathConstructor(pathString: String, ls: Seq[(ExpressionType, Seq[ExpressionElement])]) : Seq[JPathElement] = {
    /***
     * Takes expression type tuples in reverse order and adds them tail recursively into a JPathElement tree.
     * Expression type is mapped to a JPathElement case class, extracting the relevant information 
     * from the expression sequence included.
     * 
     */
    def innerJPConstr(ls: Seq[(ExpressionType, Seq[ExpressionElement])], acc: Seq[JPathElement] = Nil): Seq[JPathElement] = {
      ls match { 
        case Nil => acc
        case (StartAccessExpression, Nil) +: Nil => acc
        case (StartAccessExpression, exprSeq) +: Nil => 
          innerJPConstr(Nil, parseAccessExpression(exprSeq) +: acc)
          
        case (LiteralExpression, exprSeq) +: Nil => parseValueExpression(exprSeq) match {
          case (value, transmute) => innerJPConstr(Nil, JPathValue(value, transmute) +: acc)
        }
        
        case (DefaultValueExpression, exprSeq) +: tl => parseValueExpression(exprSeq) match {
          case (value, transmute) => innerJPConstr(tl, JDefaultValue(value, transmute) +: acc)
        }
        
        case (StringFormatExpression, exprSeq) +: tl =>
          innerJPConstr(tl, parseStringFormatExpression(exprSeq) +: acc)
          
        case (ConditionalExpression, exprSeq) +: tl => 
          innerJPConstr(tl, parseCondtionalExpression(exprSeq) +: acc) 
                   
        case (TransmuteExpression, exprSeq) +: tl =>
          innerJPConstr(tl, parseTransmuteExpression(exprSeq) +: acc)
          
        case (AccessExpression, exprSeq) +: tl => 
          innerJPConstr(tl, parseAccessExpression(exprSeq) +: acc)
        
        case (LinkExpression, exprSeq) +: tl => 
          innerJPConstr(tl, JPathLink +: acc)
          
        case _ => throw new JPathException("JPath semantic error.", pathString)
      }
    }
    
    def parseValueExpression(exprSeq: Seq[ExpressionElement]): (String, Option[JTransmute]) = exprSeq match {
      case Seq(Delimiter("("), Literal(value), Delimiter(")")) => (value, None)
      case Seq(Delimiter("("), Delimiter(")")) => ("", None)
      case Delimiter("(") +: Literal(value) +: tl => tl.reverse match {
        case Delimiter(")") +: revTransmuteExpr =>
          (value, Some(parseTransmuteExpression(revTransmuteExpr.reverse)))
        case _ => throw new JPathException(s"Cannot understand Default/Pure Value expression due to missing end bracket: $exprSeq", pathString)
      }
      case _ => throw new JPathException(s"Cannot understand Default/Pure Value expression: $exprSeq", pathString)
    }
    
    /***
     * Helper method to parse AccessExpression sequences.
     */
    def parseAccessExpression(exprSeq: Seq[ExpressionElement]): JPathElement = exprSeq match {
      case Seq(Delimiter("["), JsonKey(IS_NUMERIC_REGEX(idx)), Delimiter("]")) => 
        JArrayPath(idx.toInt)
      case _ =>  
        exprSeq filter {!_.isInstanceOf[Delimiter]} match {
          case JsonKey(key) +: Nil => JObjectPath(key)
          case _ => throw new JPathException(s"Cannot find Json Key expression element in access expression: $exprSeq", pathString)
        }
    }
    
    /***
     * Helper method to parse StringFormatExpression sequences.
     */
    def parseStringFormatExpression(exprSeq: Seq[ExpressionElement]) : JStringFormat = {
      def innerStrFormat(expr: Seq[ExpressionElement], accFormatString: Seq[StringFormat], accValuePaths: Seq[JPath]): JStringFormat = expr match {
        case Nil => JStringFormat(accFormatString.reverse, accValuePaths.reverse)
        
        case Delimiter("{") +: NestedPath(innerExprSeq) +: Delimiter("}") +: tl =>
          innerStrFormat(tl, ReplaceHolder +: accFormatString, new JPath(innerJPConstr(transformToExpressions(innerExprSeq))) +: accValuePaths)
          
        case Literal(lit) +: tl => innerStrFormat(tl, FormatLiteral(lit) +: accFormatString, accValuePaths)
        
        case seq => throw new JPathException("String format path parsing error in: " + exprSeq.mkString + " - " + seq.mkString, pathString)
      }
      exprSeq.head match {
        case Delimiter("|") => innerStrFormat(exprSeq.tail, Nil, Nil)
        case _ => throw new JPathException("String format expression does not start with '|': " + exprSeq.mkString, pathString)
      }
    }
    
    /***
     * Helper method to parse ConditionalExpression sequences.
     */
    def parseCondtionalExpression(exprSeq: Seq[ExpressionElement]): JConditional = exprSeq match {
      case Delimiter("~") +: NestedPath(conditionSeq) +:
           Delimiter("=") +: NestedPath(testSeq) +:
           Delimiter("?") +: NestedPath(trueSeq) +:
           Delimiter(":") +: NestedPath(falseSeq) +:
               tl  =>
        JConditional(innerJPConstr(transformToExpressions(conditionSeq)),
                     Some(innerJPConstr(transformToExpressions(testSeq))),
                     innerJPConstr(transformToExpressions(trueSeq)),
                     innerJPConstr(transformToExpressions(falseSeq)));
      case Delimiter("~") +: NestedPath(conditionSeq) +:
           Delimiter("?") +: NestedPath(trueSeq) +:
           Delimiter(":") +: NestedPath(falseSeq) +:
               tl  =>
        JConditional(innerJPConstr(transformToExpressions(conditionSeq)), None,
                     innerJPConstr(transformToExpressions(trueSeq)),
                     innerJPConstr(transformToExpressions(falseSeq)));
      case _ =>
        throw new JPathException("Conditional Expressions is malformed and cannot be understood: " + exprSeq.mkString, pathString)
    }
    
    /***
     * Helper method to parse TransmuteExpression sequences.
     */
    def parseTransmuteExpression(exprSeq: Seq[ExpressionElement]): JTransmute = exprSeq match {
      case Delimiter("^") +: Special(spec) +: Delimiter("<") +: Literal(argLit)
        +: tl  =>
          JTransmute(spec, Some(argLit))
      case Delimiter("^") +: Special(spec)
        +: tl  =>
          JTransmute(spec, None)
      case _ =>
        throw new JPathException("Transmute Expressions is malformed and cannot be understood: " + exprSeq.mkString, pathString)
    }
    
    def seqMatchOptionalSemiColon(seq: Seq[ExpressionElement]): Boolean = seq match {
      case Nil => true
      case Seq(Delimiter(";")) => true
      case _ => false
    }
    
    innerJPConstr(ls);
  }  
  
}