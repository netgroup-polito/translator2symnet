package org.change.v2.abstractnet.click.sefl

import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}
import org.change.v2.analysis.expression.concrete._
import org.change.v2.analysis.processingmodels._
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.util.canonicalnames._
import org.change.v2.util.conversion.NumberFor
import org.change.v2.util.conversion.RepresentationConversion._
import org.change.v2.util.regexes._

class ApplicationClassifier(name: String,
                    inputPorts: List[Port],
                    outputPorts: List[Port],
                    configParams: List[ConfigParameter])
  extends GenericElement(name,
    "ApplicationClassifier",
    inputPorts,
    outputPorts,
    configParams) {

  val lastIndex = configParams.length - 1


  /**
   * The method takes an atomic tcpdump condition and creates it's associated constraint.
   * @param condition
   * @return
   */
  private def conditionToConstraint(condition: String): Instruction = condition match {
    case ApplicationClassifier.color(v) => ConstrainNamedSymbol(Paint.COLOR, :==:(ConstantValue(v.toInt)))

  
    case ApplicationClassifier.appProto(v) => ConstrainRaw(ApplicationProto, :==:(ConstantValue(v.toInt)))

   
    case ApplicationClassifier.body(port) => ConstrainRaw(Body, :==:(ConstantValue(port.toInt)))

    case ApplicationClassifier.email(email) => ConstrainRaw(EmailFrom, :==:(ConstantValue(email.toInt)))
   
  }

  val portToInstr = scala.collection.mutable.Map[Int, Instruction]()

  /**
   * The construction of instructions from config params works backwards since the i-th
   * if needs the i+1-th if as its the else branch.
   */
  private def buildClassifier(): Unit = for {
    (p,i) <- configParams.zipWithIndex.reverse
  } {
    portToInstr += ((i, paramsToInstructionBlock(p.value,i)))
  }

  override def instructions: Map[LocationId, Instruction] = {
    // Build it first
    if (portToInstr.isEmpty) buildClassifier() else ()
    // Return it later
    Map( inputPortName(0) -> portToInstr(0) )
  }

  def paramsToInstructionBlock(param: String, whichOne: Int): Instruction = param match {
    case ApplicationClassifier.any(_) => Forward(outputPortName(whichOne))

    /*case ApplicationClassifier.none() => if (whichOne < lastIndex)
//      If the none/false condition is found, then nothing is processed here, the next instruction
//      gets executed instead.
      portToInstr(whichOne + 1)
//      Otherwise, nothing is done here
      else
        Fail(ApplicationClassifier.failErrorMessage)*/

//      Conversion of tcpdump rules
    case _ => {
      val conditions = param.split(ApplicationClassifier.conditionSeparator).toList

      def conditionsToInstruction(conds: List[String]): Instruction = {
        val cond = conds.head
        If(conditionToConstraint(cond),
          // then branch (if condition is met)
          if (conds.length == 1)
            Forward(outputPortName(whichOne))
          else
            conditionsToInstruction(conds.tail),
          // else branch,
          if (whichOne < lastIndex)
            portToInstr(whichOne + 1)
          else
            Fail(ApplicationClassifier.failErrorMessage)
        )
      }

      conditionsToInstruction(conditions)
    }
  }

  override def outputPortName(which: Int): String = s"$getName-out-$which"
  override def inputPortName(which: Int): String = s"$getName-in-$which"
}

class ApplicationClassifierElementBuilder(name: String)
  extends ElementBuilder(name, "ApplicationClassifier") {

  addInputPort(Port())

  override def buildElement: GenericElement = {
    new ApplicationClassifier(name, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object ApplicationClassifier {
  // Supported condition formats.
  val conditionSeparator = """\s+(and|&&)\s+"""

  val color = ("paint color (" + number + ")").r

  val appProto = ("app proto (" + number + ")").r

  val body = ("body (" + number + ")").r

  val email =("email (" + number + ")").r
 
  val any = """\s*(\-)\s*""".r
 

  val failErrorMessage = "No other alternative output port remaining."

  private var unnamedCount = 0

  private val genericElementName = "ApplicationClassifier"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): ApplicationClassifierElementBuilder = {
    increment ; new ApplicationClassifierElementBuilder(name)
  }

  def getBuilder: ApplicationClassifierElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}
