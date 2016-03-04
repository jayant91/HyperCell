package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.fabOutConfig._

class fabOutSeqTop extends Module{
	val io		= new Bundle{
		val inConfig		= UInt(INPUT, width = datawidth)
		val inValid		= Bool(INPUT)
		
		val fabOut		= Vec.fill(fabPortCount){UInt(INPUT, width = datawidth)}
		val fabOutValid		= Vec.fill(fabPortCount){Bool(INPUT)}
		val fabOutRdy		= Vec.fill(fabPortCount){Bool(OUTPUT)}
		
		val fabOutStore		= Vec.fill(fabPortCount){UInt(OUTPUT, width = dataWidth)}
		val fabOutStoreValid	= Vec.fill(fabPortCount){Bool(OUTPUT)}
		val fabOutStoreRdy	= Vec.fill(fabPortCount){Bool(INPUT)}
		
		val fabOutLoc		= Vec.fill(memBankCount){UInt(OUTPUT, width = bankInfoWidth + dataWidth)}
		val fabOutLocValid	= Vec.fill(memBankCount){Bool(OUTPUT)}
		val fabOutLocRdy	= Vec.fill(memBankCount){Bool(INPUT)}
	}
	
	
	val fabOutSeqIntClass		= Module(new fabOutSeq)
	val fabOutSeqArbClass		= Vec.fill(memBankCount){Module(new fabOutSeqArb).io}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	fabOutSeqIntClass.io.inConfig			:= io.inConfig
	fabOutSeqIntClass.io.inValid			:= io.inValid
	
	for(i<-0 until fabPortCount){
		
		fabOutSeqIntClass.io.fabOut(i)			:= io.fabOut(i)
		fabOutSeqIntClass.io.fabOutValid(i)		:= io.fabOutValid(i)
		io.fabOutRdy(i)					:= fabOutSeqIntClass.io.fabOutRdy(i)
		
		io.fabOutStore(i)				:= fabOutSeqIntClass.io.fabOutStore(i)
		io.fabOutStoreValid(i)				:= fabOutSeqIntClass.io.fabOutStoreValid(i)
		fabOutSeqIntClass.io.fabOutStoreRdy(i)		:= io.fabOutStoreRdy(i)	

	}
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	for(j<-0 until memBankCount){
		for(i<-0 until fabPortCount){
	
			fabOutSeqArbClass(j).fabOutLoc(i)		:= fabOutSeqIntClass.io.fabOutLoc(i)
			fabOutSeqArbClass(j).fabOutLocValid(i)		:= fabOutSeqIntClass.io.fabOutLocValid(i)
			fabOutSeqIntClass.io.fabOutLocRdy(i)		:= fabOutSeqArbClass(j).fabOutLocRdy(i)
		}
		
		io.fabOutLoc(j)					:= fabOutSeqArbClass(j).locStoreData
		io.fabOutLocValid(j)				:= fabOutSeqArbClass(j).locStoreValid
		fabOutSeqArbClass(j).locStoreRdy		:= io.fabOutLocRdy(j)
		
		
		fabOutSeqArbClass(j).rst			:= fabOutSeqIntClass.io.rst
	}	
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	
}
