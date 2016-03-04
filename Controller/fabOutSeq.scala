package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.fabOutConfig._

class fabOutSeq extends Module{
	val io 		= new Bundle{
		val inConfig		= UInt(INPUT, width = datawidth)
		val inValid		= Bool(INPUT)
		
		val fabOut		= Vec.fill(fabPortCount){UInt(INPUT, width = datawidth)}
		val fabOutValid		= Vec.fill(fabPortCount){Bool(INPUT)}
		val fabOutRdy		= Vec.fill(fabPortCount){Bool(OUTPUT)}
		
		val fabOutStore		= Vec.fill(fabPortCount){UInt(OUTPUT, width = dataWidth)}
		val fabOutStoreValid	= Vec.fill(fabPortCount){Bool(OUTPUT)}
		val fabOutStoreRdy	= Vec.fill(fabPortCount){Bool(INPUT)}
		
		val fabOutLoc		= Vec.fill(fabPortCount){UInt(OUTPUT, width = bankInfoWidth + dataWidth)}
		val fabOutLocValid	= Vec.fill(fabPortCount){Bool(OUTPUT)}
		val fabOutLocRdy	= Vec.fill(fabPortCount){Bool(INPUT)}

		val rst			= Bool(OUTPUT)		
	}
	
	
	
	val fabOutCtrl 			= Module(new fabOutSeqCtrl)
	val fabOutDP			= Vec.fill(fabPortCount){Module(new fabOutSeqDP).io}
	
	io.rst				:= fabOutDP(0).rst
	
	fabOutCtrl.io.inConfig		:= io.inConfig
	fabOutCtrl.io.inValid		:= io.inValid
	
	for(i<-0 until fabPortCount){
		fabOutDP(i).inConfig		:= io.inConfig
		fabOutDP(i).inValid		:= io.inValid
			
		fabOutDP(i).fabOut		:= io.fabOut(i)
		fabOutDP(i).fabOutValid		:= io.fabOutValid(i)
		io.fabOutRdy(i)			:= fabOutDP(i).fabOutRdy
		
		io.fabOutStore(i)		:= fabOutDP(i).fabOutStore
		io.fabOutStoreValid(i)		:= fabOutDP(i).fabOutStoreValid
		fabOutDP(i).fabOutStoreRdy	:= io.fabOutStoreRdy(i)
		
		io.fabOutLoc(i)			:= fabOutDP(i).fabOutLoc
		io.fabOutLocValid(i)		:= fabOutDP(i).fabOutLocValid
		fabOutDP(i).fabOutLocRdy	:= io.fabOutLocRdy(i)
		
		fabOutDP(i).seqMemAddr		:= fabOutCtrl.io.seqMemAddr(i)
		fabOutDP(i).seqMemAddrValid	:= fabOutCtrl.io.seqMemAddrValid(i)
	
		fabOutCtrl.io.seqProceed(i)	:= fabOutDP(i).seqProceed
	
		fabOutCtrl.io.outLocValid(i)	:= fabOutDP(i).outLocValid
	}
	
	
	
	
	
	
	
	
	
	
}
