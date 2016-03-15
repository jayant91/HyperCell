package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.LoadSeqConfig._

class loadSeqCtrl extends Module{
	val io 	= new Bundle{
		val inConfig			= UInt(INPUT, width = dataWidth)
		val inValid			= Bool(INPUT)

		val spillEnd			= Bool(OUTPUT)
		val nextIterStart		= Bool(OUTPUT)
		val seqMemAddr			= UInt(OUTPUT, width = loadSeqMemAddrWidth)
		val seqMemAddrValid		= Bool(OUTPUT)
		val computeEnable		= Bool(OUTPUT)
		val seqProceed			= Bool(INPUT)
	}
	
	val loadCtrlConfigure 		= Module(new controllerConfigure(0))
	val computeCtrl			= Bool()
	val computeCtrlValid		= Bool()
	computeCtrl			:= loadCtrlConfigure.io.computeCtrl
	computeCtrlValid		:= loadCtrlConfigure.io.computeCtrlValid
	loadCtrlConfigure.io.inConfig	:= io.inConfig
	loadCtrlConfigure.io.inValid	:= io.inValid
	
	//REGISTERS
	val prologueDepth		= Reg(init = UInt(0, width = loadSeqMemAddrWidth))
	val epilogueDepth		= Reg(init = UInt(0, width = loadSeqMemAddrWidth))
	val steadyStateDepth		= Reg(init = UInt(0, width = loadSeqMemAddrWidth))
	val epilogueSpill		= Reg(init = UInt(0, width = loadSeqMemAddrWidth))
	val iterCount			= Reg(init = UInt(0, width = loadIterCountWidth))
	val seqMemAddr			= Reg(init = UInt(0, width = loadSeqMemDepth))
	val currentIter			= Reg(init = UInt(0, width = loadIterCountWidth))
	
	val spillEnd			= Reg(init = Bool(false))
	val computeEnable		= Reg(init = Bool(false))
	val nextIterStart		= Reg(init = Bool(false))
	
	val startComputeValid		= Bool()		
	val resetComputeValid		= Bool()		
	val nextRequest			= Bool()
	val computeDone			= Bool()
	
	startComputeValid		:= Bool(false)		
	resetComputeValid		:= Bool(false)		
	nextRequest			:= Bool(false)
	computeDone			:= Bool(false)
	
	val ssEnd			= UInt(width = loadSeqMemDepth+2)			
	val lastAddr			= UInt(width = loadSeqMemDepth+3)			
	val spillEndAddr		= UInt(width = loadSeqMemDepth+2)			
	
	
	io.nextIterStart		:= nextIterStart
	io.spillEnd			:= spillEnd
	nextRequest			:= io.seqProceed
	
	when(computeCtrlValid && computeCtrl){
		startComputeValid	:= Bool(true)	
	}
	.elsewhen(computeCtrlValid && (!computeCtrl)){
		resetComputeValid	:= Bool(true)	
	}
	.otherwise{
		startComputeValid	:= Bool(false) 
		resetComputeValid	:= Bool(false)
	}
	
	
	when(computeEnable && nextRequest){
		when((epilogueDepth != UInt(0)) && (steadyStateDepth === UInt(0))){
			computeDone	:=	(seqMemAddr === (lastAddr - UInt(1)))
		}
		
		.elsewhen(epilogueSpill === UInt(0)){
			computeDone	:= 	((seqMemAddr === (lastAddr - UInt(1))) && ((currentIter + UInt(1)) === iterCount))
		}
		
		.otherwise{
			computeDone	:= 	((seqMemAddr === (spillEndAddr - UInt(1))) && ((currentIter) === iterCount))
		}
	}
	
	when(startComputeValid || resetComputeValid || computeDone){
		//No need to start if none of prologue, epilogue and ss are 0.
		when(startComputeValid && (lastAddr != UInt(0))){
			computeEnable 	:= Bool(true)
		}

		.elsewhen (resetComputeValid){
			computeEnable	:= Bool(false)
		}
			
		.otherwise{
			computeEnable 	:= Bool(false)
		}
	}
	
	io.computeEnable		:= computeEnable
	
	//next request is set only on compute_enable		
	when(startComputeValid || nextRequest){
		when(startComputeValid){
			seqMemAddr	:= UInt(0)
			currentIter	:= UInt(0)
			nextIterStart	:= Bool(false)
			spillEnd	:= Bool(false)
		}
		
		.otherwise{
			//end of iteration, loop back
			when(seqMemAddr === (ssEnd -UInt(1))){
				spillEnd	:= Bool(false)
				//if not last iteration or if there is spill, loop back to end of prologue
				when((currentIter < (iterCount - UInt(1))) || (epilogueSpill != UInt(0))){
					nextIterStart		:= Bool(true)
					seqMemAddr 		:= prologueDepth
					currentIter		:= currentIter + UInt(1);
				}
				
				//last iteration and no spill, go to epilogue 
				.otherwise{
					nextIterStart		:= Bool(false)
					seqMemAddr		:= seqMemAddr + UInt(1)
				}
			}
			
			//if epilogue spill, and last spill address, go to epilogue  
			.elsewhen((currentIter === iterCount) && (seqMemAddr === (spillEnd - UInt(1)))){
				nextIterStart		:= Bool(false)
				spillEnd		:= Bool(true)
				seqMemAddr		:= ssEnd
			}
			
			
			.otherwise{
				seqMemAddr		:= seqMemAddr + UInt(1);
				nextIterStart		:= Bool(false)
				spillEnd		:= Bool(false)
			}
		}
		
	}
	
	io.seqMemAddr		:= seqMemAddr
	
	

	when(io.inValid){
		when(io.inConfig(dataWidth-1, dataWidth -lWidth) === UInt(256)){
			when(io.inConfig(dataWidth -lWidth -1, dataWidth -lWidth -sWidth) === UInt(0)){
				when(io.inConfig(xBitNo) === UInt(0)){
					prologueDepth		:= io.inConfig(prologueSize-1, 0)
					steadyStateDepth	:= io.inConfig(steadyStateSize + prologueSize -1 , prologueSize)
				}
				.elsewhen(io.inConfig(xBitNo) === UInt(1)){
					epilogueDepth		:= io.inConfig(epilogueSize-1, 0)
					epilogueSpill		:= io.inConfig(epilogueSpillSize + epilogueSize-1, epilogueSize)
				}
				
			}
			.elsewhen(io.inConfig(dataWidth -lWidth -1, dataWidth -lWidth -sWidth) === UInt(1)){
				iterCount		:= io.inConfig(iterCntSize-1, 0)
			}
		}

	}
	
	ssEnd				:= prologueDepth + steadyStateDepth 
	lastAddr			:= ssEnd + epilogueDepth
	spillEndAddr			:= prologueDepth + epilogueSpill
	
}

//object loadSeqCtrlMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new loadSeqCtrl()))

//    }
//}
