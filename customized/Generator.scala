package org.change.v2.abstractnet.click.sefl

import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}

import org.change.v2.analysis.expression.concrete.{SymbolicValue, ConstantValue}
import org.change.v2.analysis.expression.concrete.nonprimitive.{Address, :@, Symbol}
import org.change.v2.analysis.memory.TagExp
import org.change.v2.analysis.processingmodels.{LocationId, Instruction}
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.util.regexes._
import org.change.v2.util.canonicalnames._
import org.change.v2.util.conversion.RepresentationConversion._

/**
 * Element corresponding to: "[name] :: Generator(web ip-src ip-dst body(numero di porta) 800 || mail ip-src ip-dst email_from 150)"
 */
class Generator(name: String,
                 inputPorts: List[Port],
                 outputPorts: List[Port],
                 configParams: List[ConfigParameter])
  extends GenericElement(name,
    "Generator",
    inputPorts,
    outputPorts,
    configParams) {

 val lastIndex = configParams.length - 1

	def installRulesForWeb(whichRule: Int,
                            sa: String, da: String,
                            port: Int, proto: Int
                            ): Instruction = {
	
   		 InstructionBlock(
      			AssignRaw(IPSrc, sa match{
				case ipv4Regex() => ConstantValue(ipToNumber(sa))
				
			}),
      			AssignRaw(IPDst, da match{
				case ipv4Regex() => ConstantValue(ipToNumber(da))
				
			}),
    			 AssignRaw(Body, port match{
				case numberRegex() => ConstantValue(port.toInt)

				
			}),
   			AssignRaw(ApplicationProto, proto match{
				case numberRegex() => ConstantValue(proto.toInt)
				
			}),
		     
		      Forward(outputPortName(whichRule))
   		 )
	}
	def installRulesForMail(whichRule: Int,
                            sa: String, da: String,
                            email: Int, proto: Int
                            ): Instruction = {
    		InstructionBlock(
      			AssignRaw(IPSrc, sa match{
				case ipv4Regex() => ConstantValue(ipToNumber(sa))
				
			}),
     			AssignRaw(IPDst, da match{
				case ipv4Regex() => ConstantValue(ipToNumber(da))
				
			}),
			
     			AssignRaw(ApplicationProto, proto match{
				case numberRegex() => ConstantValue(proto.toInt)
				
			}),
			AssignRaw(EmailFrom, email match{
				case numberRegex() => ConstantValue(email.toInt)
				
			}),
			
      
     			 Forward(outputPortName(whichRule))
    		)
	}

	def installRulesForIpsrc(whichRule: Int,
                            ipsrc: String
                            ): Instruction = {
    		InstructionBlock(
      			AssignRaw(IPSrc, ipsrc match{
				case ipv4Regex() => ConstantValue(ipToNumber(ipsrc))
				
			}),			
      
     			 Forward(outputPortName(whichRule))
    		)
	}

	def installRulesForIpdst(whichRule: Int,
                            ipdst: String
                            ): Instruction = {
    		InstructionBlock(
      			AssignRaw(IPDst, ipdst match{
				case ipv4Regex() => ConstantValue(ipToNumber(ipdst))
				
			}),			
      
     			 Forward(outputPortName(whichRule))
    		)
	}

def installRulesForMail(whichRule: Int,
                            proto: Int
                            ): Instruction = {
    		InstructionBlock(
      			
     			AssignRaw(ApplicationProto, proto match{
				case numberRegex() => ConstantValue(proto.toInt)
				
			}),
			
      
     			 Forward(outputPortName(whichRule))
    		)
	}

	def installRulesForForward(whichRule: Int,
                            addr: String
                            ): Instruction = {
    		InstructionBlock(  						
      
     			 Forward(outputPortName(whichRule))
    		)
	}
	def installRulesForDefault(): Instruction = {
   		 InstructionBlock(
     			 Forward(outputPortName(0))
    		)
  	}


def buildFullInstructions(param: String, which: Int) : InstructionBlock = param match {

     case Generator.webPattern(sa, da, port, proto) => {
		
		InstructionBlock(
      			AssignRaw(IPSrc, sa match{
				case ipv4Regex() => ConstantValue(ipToNumber(sa))
				
			}),
     			AssignRaw(IPDst, da match{
				case ipv4Regex() => ConstantValue(ipToNumber(da))
				
			}),
     			AssignRaw(Body, port match{
				case numberRegex() => ConstantValue(port.toInt)

				
			}),
			AssignRaw(ApplicationProto, proto match{
				case numberRegex() => ConstantValue(proto.toInt)
				
			}),
      
     			Forward(outputPortName(which))
    		)
    
     }
    case Generator.mailPattern(sa, da, email, proto) => {
	
	InstructionBlock(
      			AssignRaw(IPSrc, sa match{
				case ipv4Regex() => ConstantValue(ipToNumber(sa))
				
			}),
     			AssignRaw(IPDst, da match{
				case ipv4Regex() => ConstantValue(ipToNumber(da))
				
			}),			
     			AssignRaw(ApplicationProto, proto match{
				case numberRegex() => ConstantValue(proto.toInt)
				
			}),
			AssignRaw(EmailFrom, email match{
				case numberRegex() => ConstantValue(email.toInt)
				
			}),
			
      
     			Forward(outputPortName(which))
    		)
     
     
    }

   case Generator.ipsrcPattern(ipsrc) => {
		
		InstructionBlock(
      			AssignRaw(IPSrc, ipsrc match{
				case ipv4Regex() => ConstantValue(ipToNumber(ipsrc))
				
			}),	
      
     			Forward(outputPortName(which))
    		)
    
     }
   case Generator.ipdstPattern(ipdst) => {
		
		InstructionBlock(
      			
     			AssignRaw(IPDst, ipdst match{
				case ipv4Regex() => ConstantValue(ipToNumber(ipdst))
				
			}),     			
      
     			Forward(outputPortName(which))
    		)
    
     }

case Generator.protoPattern(proto) => {
	
	InstructionBlock(
      						
     			AssignRaw(ApplicationProto, proto match{
				case numberRegex() => ConstantValue(proto.toInt)
				
			}),
		
      
     			Forward(outputPortName(which))
    		)
     
     
    }
 


    case Generator.forwardPattern(addr) => {
		
		InstructionBlock(  			
     			   			
      
     			Forward(outputPortName(which))
    		)
    
     }

   case "" => InstructionBlock(
		Forward(outputPortName(0))
		)    		

   case _ => InstructionBlock( Fail("Configuration not valid."))
 }

  private val iCache: scala.collection.mutable.Map[String, Instruction] = scala.collection.mutable.Map() 
  private var iBlock: scala.collection.mutable.ListBuffer[InstructionBlock] = scala.collection.mutable.ListBuffer()
  private var blocco: scala.collection.mutable.ListBuffer[InstructionBlock] = scala.collection.mutable.ListBuffer()

  private def buildGenerator(): Unit = for (
    (cp, i) <- configParams.zipWithIndex
  ) {
	//insert instructionBlock
  	iBlock += buildFullInstructions(cp.value, i)

  }



 override def instructions : Map[LocationId, Instruction] = {
    if (blocco.isEmpty) {
        buildGenerator()
 var blockList= iBlock.toList


//println("\n\nBLOCK: " + blockList)

	//Serve la Fork per creare flussi diversi. InstructionBlock( la fork divide i diversi pacchetti in diversi flussi)) la INstructionBlock serve per avere la mappa di istruzioni
     iCache += (inputPortName(0) -> InstructionBlock(Fork(blockList)))
     iCacheMap = iCache.toMap
    }

    iCacheMap
  }
    
  


 private var iCacheMap: Map[LocationId, Instruction] = _



override def outputPortName(which: Int): String = s"$getName-out-$which"
  override def inputPortName(which: Int): String = s"$getName-in-$which"


}


class GeneratorElementBuilder(name: String)
  extends ElementBuilder(name, "Generator") {

	addInputPort(Port())
       addOutputPort(Port())

  override def buildElement: GenericElement = {
    new Generator(name, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object Generator {

  val addr = ipv4
  val port = number 
  val proto=number
  val email=number
  val webPattern = ("web (" + addr+ ") (" + addr+ ") (" + port+ ") (" + proto+ ")").r
  val mailPattern = ("mail (" + addr+ ") (" + addr+ ") (" + email+ ") (" + proto+ ")").r
  val ipsrcPattern=("ip_src (" + addr+ ")").r
  val ipdstPattern=("ip_dst (" + addr+ ")").r
  val forwardPattern=("fw (" + addr+ ")").r
  val protoPattern=("proto (" + proto+ ")").r


  private var unnamedCount = 0

  private val genericElementName = "generator"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): GeneratorElementBuilder = {
    increment ; new GeneratorElementBuilder(name)
  }

  def getBuilder: GeneratorElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}

