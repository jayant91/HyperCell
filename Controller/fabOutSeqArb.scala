package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.fabOutConfig._

class fabOutSeqArb extends Module{
	val io 		= new Bundle{
		val fabOutLoc 		= Vec.fill(fabPortCount){UInt(INPUT, width = fabOutLocDataWidth)}
		val fabOutLocValid	= Vec.fill(fabPortCount){Bool(INPUT)}
		val fabOutLocRdy	= Vec.fill(fabPortCount){Bool(OUTPUT)}
		
		val locStoreData	= UInt(OUTPUT, width =  fabOutLocDataWidth)
		val locStoreValid	= Bool(OUTPUT)
		val locStoreRdy		= Bool(INPUT)
		
		val rst			= Bool(INPUT)
	}
	
	val arb 	= Module(new RRArbiter(fabPortCount))
	
	val inFifo	= Vec.fill(fabPortCount){Module(new fifoFabOut(fabOutLocDataWidth , 3)).io}	
	for(i<-0 until fabPortCount){
		inFifo(i).enqData	:= io.fabOutLoc(i)
		inFifo(i).enqValid	:= io.fabOutLocValid(i)
		io.fabOutLocRdy(i)	:= inFifo(i).enqRdy
	}
	
	val outFifo	= Module(new fifoFabOut(fabOutLocDataWidth, 4))				
	io.locStoreData		:= outFifo.io.deqData
	io.locStoreValid	:= outFifo.io.deqValid
	outFifo.io.deqRdy	:= io.locStoreRdy
	
	val muxReg	= Vec.fill(5){Reg(init = UInt(0, width = fabOutLocDataWidth))}
	val muxValidReg	= Vec.fill(5){Reg(init = Bool(false))}
	val grantReg	= Vec.fill(fabPortCount){Reg(init = Bool(false))}
	
	val grantWire	= Vec.fill(fabPortCount){Bool()}
	
	val anyGrant	= Bool()
	val muxValid	= Bool()
	
	val request 	= Vec.fill(fabPortCount){Bool()}
	
	anyGrant	:= grantReg.fold(Bool(false))(_||_)
	muxValid	:= muxValidReg.fold(Bool(false))(_||_)
	
	when((outFifo.io.emptySpace >= UInt(1)) ||  (!muxValid && !anyGrant)){
		for(i<-0 until fabPortCount){
			when(inFifo(i).deqValid && !((inFifo(i).emptySpace >= UInt(1)) && grantReg(i))){
				request(i)	:= Bool(true)
			}
			.otherwise{
				request(i)	:= Bool(false)
			}
		}
	}
	.otherwise{
		for(i<-0 until fabPortCount){
			request(i)		:= Bool(false)
		}
	}
	
	for(i<-0 until fabPortCount){
		arb.io.request(i)		:= request(i)
		grantWire(i)			:= arb.io.grant(i)
	}
	
	for(i<-0 until fabPortCount){
		grantReg(i)		:= grantWire(i)
	}
	
	
	for(i<-0 until fabPortCount){
		when(grantReg(i)){
			muxReg(i/4)		:= inFifo(i).deqData
			muxValidReg(i/4)	:= inFifo(i).deqValid
			inFifo(i).deqRdy	:= Bool(true)
		}
		.otherwise{
			inFifo(i).deqRdy	:= Bool(false)
		}
	}
	
	for(i<-0 until 5){
		when(muxValidReg(i) && outFifo.io.enqRdy){
			outFifo.io.enqData		:= muxReg(i)
			outFifo.io.enqValid		:= muxValidReg(i)
		}
		.otherwise{
			outFifo.io.enqData		:= muxReg(i)
			outFifo.io.enqValid		:= Bool(false)
		}
	}

	when(io.rst){
		for(i<-0 until 5){
			muxValidReg	:= Bool(false)
		}
		
		for(i<-0 until fabPortCount){
			inFifo(i).rst	:= Bool(true)
		}
		
		outFifo.io.rst		:= Bool(true)
	}
	.otherwise{
		for(i<-0 until fabPortCount){
			inFifo(i).rst	:= Bool(false)
		}
		outFifo.io.rst		:= Bool(false)
	}	
	
	


	
	
}
