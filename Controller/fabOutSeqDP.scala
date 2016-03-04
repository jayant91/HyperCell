package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.fabOutConfig._

class fabOutSeqDP extends Module{
	val io 		= new Bundle{
		val inConfig		= UInt(INPUT, width = datawidth)
		val inValid		= Bool(INPUT)
		
		val seqMemAddr		= UInt(INPUT, width = seqMemAddrWidth)		
		val seqMemAddrValid	= Bool(INPUT)
		
		val fabOut		= UInt(INPUT, width = datawidth)
		val fabOutValid		= Bool(INPUT)
		val fabOutRdy		= Bool(OUTPUT)
		
		val fabOutStore		= UInt(OUTPUT, width = dataWidth)
		val fabOutStoreValid	= Bool(OUTPUT)
		val fabOutStoreRdy	= Bool(INPUT)
		
		val fabOutLoc		= UInt(OUTPUT, width = bankInfoWidth + dataWidth)
		val fabOutLocValid	= Bool(OUTPUT)
		val fabOutLocRdy	= Bool(INPUT)
		
		val seqProceed		= Bool(OUTPUT)
		
		val rst			= Bool(OUTPUT)
		
		val outLocValid		= Bool(OUTPUT)	
	}
	
	
	
	//Memories and FIFO's
	val fabOutSeqMem	= Module(new customReg(seqMemDepth, seqMemWidth))
	
	val fabOutSeqMemConfig	= Module(new memConfig(18, seqMemAddrWidth, seqMemWidth))	
	fabOutSeqMemConfig.io.inConfig		:= io.inConfig
	fabOutSeqMemConfig.io.inValid		:= io.inValid
	
	val outStoreFifo	= Module(new fifo(dataWidth, 2))		
	io.fabOutStore				:= outStoreFifo.io.deqData
	io.fabOutStoreValid			:= outStoreFifo.io.deqValid
	outStoreFifo.io.deqRdy			:= io.fabOutStoreRdy
	
	val outLocalFifo	= Module(new fifo(dataWidth, 2))
	
	
	
	//REGISTERS
	val fabOutLocStrg		= Reg(init = UInt(0, width = bankInfoWidth + dataWidth))
	val fabOutLocStrgValid		= Reg(init = Bool(false))		//TODO Setting it to false based on the input from the fabOutTop Module
	val outDestStore		= Reg(init = Bool(false))
	val outDestLocal		= Reg(init = Bool(false))
	
	
	val nextSeq			= UInt(width = nextSeqWidth)
	val getNextSeq			= Bool()
	val seqDone			= Bool()
	val localStoreData		= UInt(width = dataWidth)
	val localStoreDataValid		= Bool()
	val bankInfo			= UInt(width = bankInfoWidth)
	
	
	nextSeq				:= UInt(0)
	getNextSeq			:= Bool(false)
	seqDone				:= Bool(false)
	localStoreData			:= UInt(0)
	localStoreDataValid		:= Bool(false)
	bankInfo			:= UInt(0)
	
	localStoreData		:= outLocalFifo.io.deqData
	localStoreDataValid	:= outLocalFifo.io.deqValid
	
	io.rst		:= fabOutSeqMemConfig.io.rst
	io.outLocValid	:= outDestLocal
	
	when(fabOutSeqMemConfig.io.rst){
		outStoreFifo.io.rst	:= Bool(true)
		outLocalFifo.io.rst	:= Bool(true)
		fabOutLocStrgValid	:= Bool(false)
		outDestLocal		:= Bool(false)
		outDestStore		:= Bool(false)
	}
	.otherwise{
		outStoreFifo.io.rst	:= Bool(false)
		outLocalFifo.io.rst	:= Bool(false) 
	}
	
	when(seqDone){
		io.seqProceed		:= Bool(true)
	}
	.otherwise{
		io.seqProceed		:= Bool(false)
	}
	
	fabOutSeqMem.io.readAddr	:= io.seqMemAddr
	fabOutSeqMem.io.readEn		:= io.seqMemAddrValid
		
	when((~fabOutLocStrgValid) || getNextSeq){
		
		nextSeq				:= fabOutSeqMem.io.outData
		
		
		//IF this port does not write to local store, then no processing
		//required. This can be avoided, but then even unused ports will require sequence
		//memory programming.

					
		when(~outDestLocal){
			seqDone		:= Bool(true)
		}
		.elsewhen(nextSeq(nextSeqWidth-1)){		
			seqDone		:= Bool(false)
		}
		.otherwise{
			seqDone		:= Bool(true)
		}
	}
	
	when(outLocalFifo.io.deqValid){
		when(nextSeq(nextSeqWidth-2)){	
			outLocalFifo.io.deqRdy		:= Bool(true)
		}
		.otherwise{
			outLocalFifo.io.deqRdy		:= Bool(false)
		}
		bankInfo			:= nextSeq(nextSeqWidth-3, 0)	
		fabOutLocStrg			:= Cat(bankInfo, localStoreData)
		fabOutLocStrgValid		:= Bool(true)
		seqDone				:= Bool(true)

	}
	.otherwise{
		outLocalFifo.io.deqRdy		:= Bool(false)
		bankInfo			:= UInt(0)
		seqDone				:= Bool(false)
	}
	
	outStoreFifo.io.enqData		:= io.fabOut
	outLocalFifo.io.enqData		:= io.fabOut
	
	
	when(outDestLocal || outDestStore){
		when(outDestStore && outStoreFifo.io.enqRdy){
			outStoreFifo.io.enqValid	:= io.fabOutValid
		}
		.otherwise{				
			outStoreFifo.io.enqValid	:= Bool(false)	
		}
		
		when(outDestLocal && outLocalFifo.io.enqRdy){
			outLocalFifo.io.enqValid	:= io.fabOutValid
		}
		.otherwise{
			outLocalFifo.io.enqValid	:= Bool(false)
		}
	}
	.otherwise{
		outStoreFifo.io.enqValid	:= Bool(false)	
		outLocalFifo.io.enqValid	:= Bool(false)
	}
	
	io.fabOutLoc		:= fabOutLocStrg
	
	when(fabOutLocStrgValid && io.fabOutLocRdy){
		io.fabOutLocValid	:= fabOutLocStrgValid
		fabOutLocStrgValid	:= Bool(false)
		getNextSeq		:= Bool(true)
	}
	.otherwise{
		io.fabOutLocValid	:= Bool(false)
		getNextSeq		:= Bool(false)
	}
}
