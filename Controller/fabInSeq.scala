package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.fabInConfig._

class fabInSeq extends Module{
	val io		= new Bundle{
		val inConfig		= UInt(INPUT, width = datawidth)
		val inValid		= Bool(INPUT)
		
		val fabInData		= Vec.fill(fabPortCount){UInt(OUTPUT, width = datawidth)}
		val fabInValid		= Vec.fill(fabPortCount){Bool(OUTPUT)}
		val fabInRdy		= Vec.fill(fabPortCount){Bool(INPUT)}
		
		val loadStore		= Vec.fill(memBankCount){UInt(INPUT, width = localStorageWidth)}
		val loadStoreValid	= Vec.fill(memBankCount){Bool(INPUT)}
		val loadStoreRdy	= Vec.fill(memBankCount){Bool(OUTPUT)}
		
		val fabStore		= Vec.fill(memBankCount){UInt(INPUT, width = localStorageWidth)}
		val fabStoreValid	= Vec.fill(memBankCount){Bool(INPUT)}
		val fabStoreRdy		= Vec.fill(memBankCount){Bool(OUTPUT)}
		
		val computeDone		= Bool(OUTPUT)
	}
	
	val ctrlClass	= Module(new fabInSeqCtrl())
	val dpClass	= Module(new fabInSeqDP())
	
	ctrlClass.io.inConfig		:= io.inConfig
	ctrlClass.io.inValid		:= io.inValid
	
	dpClass.io.inConfig		:= io.inConfig
	dpClass.io.inValid		:= io.inValid
	
	ctrlClass.io.seqProceed		:= dpClass.io.seqProceed
	
	dpClass.io.seqMemAddr		:= ctrlClass.io.seqMemAddr
	dpClass.io.seqMemAddrValid	:= ctrlClass.io.seqMemAddrValid
	
	io.fabInData			:= dpClass.io.fabInData
	io.fabInValid			:= dpClass.io.fabInValid
	dpClass.io.fabInRdy		:= io.fabInRdy
	
	dpClass.io.loadStore		:= io.loadStore
	dpClass.io.loadStoreValid	:= io.loadStoreValid
	io.loadStoreRdy			:= dpClass.io.loadStoreRdy
	
	dpClass.io.fabStore		:= io.fabStore
	dpClass.io.fabStoreValid	:= io.fabStoreValid
	io.fabStoreRdy			:= dpClass.io.fabStoreRdy
	
	io.computeDone			:= ctrlClass.io.computeDone
		
}

//object fabInSeqMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new fabInSeq()))

//    }
//}
