package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.StoreSeqConfig._

class storeSeq extends Module{
	val io 	= new Bundle{
		val inConfig			= UInt(INPUT, width = dataWidth)
		val inValid			= Bool(INPUT)
		
		val fabOutToStore		= Vec.fill(maxFabricInputs){UInt(INPUT, width = dataWidth)}
		val fabOutToStoreValid		= Vec.fill(maxFabricInputs){Bool(INPUT)}
		val fabOutToStoreRdy		= Vec.fill(maxFabricInputs){Bool(OUTPUT)}
		
		val storeMemData		= UInt(OUTPUT, (dataWidth + extMemAddrWidth))
		val storeMemValid		= Bool(OUTPUT)
		val storeMemRdy			= Bool(INPUT)
		
		val computeDone			= Bool(OUTPUT)				
		
	}
	
	val storeCtrlClass				= Module(new storeSeqCtrl())
	val storeDPClass				= Module(new storeSeqDP())
	
	storeCtrlClass.io.inConfig			:= io.inConfig
	storeCtrlClass.io.inValid			:= io.inValid
	
	storeDPClass.io.inConfig			:= io.inConfig
	storeDPClass.io.inValid				:= io.inValid

	storeDPClass.io.fabOutToStore			:= io.fabOutToStore
	storeDPClass.io.fabOutToStoreValid		:= io.fabOutToStoreValid
	io.fabOutToStoreRdy				:= storeDPClass.io.fabOutToStoreRdy
	
	storeDPClass.io.storeMemRdy			:= io.storeMemRdy
	io.storeMemData					:= storeDPClass.io.storeMemData
	io.storeMemValid				:= storeDPClass.io.storeMemValid
	
	
	storeCtrlClass.io.seqProceed			:= storeDPClass.io.seqProceed
	
	storeDPClass.io.spillEnd			:= storeCtrlClass.io.spillEnd
	storeDPClass.io.nextIterStart			:= storeCtrlClass.io.nextIterStart
	storeDPClass.io.seqMemAddr			:= storeCtrlClass.io.seqMemAddr
	storeDPClass.io.seqMemAddrValid			:= storeCtrlClass.io.seqMemAddrValid
	storeDPClass.io.computeDoneCtrl			:= storeCtrlClass.io.computeDone
	
	io.computeDone					:= storeDPClass.io.computeDone

}

//object storeSeqMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new storeSeq()))

//    }
//}
