package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.controllerTopConfig._
import HyperCellParams.LoadSeqConfig._


class controllerTop extends Module{
	val io 	= new Bundle {
		val inConfig			= UInt(INPUT, width = dataWidth)
		val inValid			= Bool(INPUT)
				
		val loadRqst			= UInt(OUTPUT, width = lrReqFifoWidth)
		val loadRqstValid		= Bool(OUTPUT)
		val loadRqstRdy			= Bool(INPUT)
		
		val loadResp			= UInt(INPUT, width = lrRespFifoWidth)
		val loadRespValid		= Bool(INPUT)
		val loadRespRdy			= Bool(OUTPUT)
		
		val storeMemData		= UInt(OUTPUT, (dataWidth + extMemAddrWidth))
		val storeMemValid		= Bool(OUTPUT)
		val storeMemRdy			= Bool(INPUT)
		
		val fabInData			= Vec.fill(fabPortCount){UInt(OUTPUT, width = datawidth)}
		val fabInValid			= Vec.fill(fabPortCount){Bool(OUTPUT)}
		val fabInRdy			= Vec.fill(fabPortCount){Bool(INPUT)}
		
		val fabOut			= Vec.fill(fabPortCount){UInt(INPUT, width = datawidth)}
		val fabOutValid			= Vec.fill(fabPortCount){Bool(INPUT)}
		val fabOutRdy			= Vec.fill(fabPortCount){Bool(OUTPUT)}
		
//		val computeDone			= Bool()
//		val storeDone			= Bool()
//		val loadDone			= Bool()
		
	}
	
	val fabInSeqClass		= Module(new fabInSeq)
	val fabOutSeqClass		= Module(new fabOutSeqTop)
	val loadSeqClass		= Module(new loadSeq)
	val storeSeqClass		= Module(new storeSeq)
	
	val mainConfigClass		= Module(new mainConfigure)
	val fabConfigClass		= Module(new fabricConfigureTop(dataWidth, 6, 3, 24, 13))
	
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	mainConfigClass.io.configData			:= io.inConfig
	mainConfigClass.io.configDataValid		:= io.inValid
	
	loadSeqClass.io.inConfig			:= mainConfigClass.io.loadConfig
	loadSeqClass.io.inValid				:= mainConfigClass.io.loadConfigValid
	
	storeSeqClass.io.inConfig			:= mainConfigClass.io.storeConfig
	storeSeqClass.io.inValid			:= mainConfigClass.io.storeConfigValid
	
	fabInSeqClass.io.inConfig			:= mainConfigClass.io.fabInConfig
	fabInSeqClass.io.inValid			:= mainConfigClass.io.fabInConfigValid
	
	fabOutSeqClass.io.inConfig			:= mainConfigClass.io.fabOutConfig
	fabOutSeqClass.io.inValid			:= mainConfigClass.io.fabOutConfigValid
	
	fabConfigClass.io.inConfig			:= io.inConfig
//	fabConfigClass.io.inValid			:= io.inValid
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	io.loadRqst					:= loadSeqClass.io.loadRqst
	io.loadRqstValid				:= loadSeqClass.io.loadRqstValid
	loadSeqClass.io.loadRqstRdy			:= io.loadRqstRdy
	
	loadSeqClass.io.loadResp			:= io.loadResp
	loadSeqClass.io.loadRespValid			:= io.loadRespValid
	io.loadRespRdy					:= loadSeqClass.io.loadRespRdy
	
	for(i<-0 until memBankCount){
		fabInSeqClass.io.loadStore(i)			:= loadSeqClass.io.memBankEnq(i)
		fabInSeqClass.io.loadStoreValid(i)		:= loadSeqClass.io.memBankValid(i)
		loadSeqClass.io.memBankRdy(i)			:= fabInSeqClass.io.loadStoreRdy(i)
	}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	for(i<-0 until fabPortCount){
		storeSeqClass.io.fabOutToStore(i)		:= fabOutSeqClass.io.fabOutStore(i)
		storeSeqClass.io.fabOutToStoreValid(i)		:= fabOutSeqClass.io.fabOutStoreValid(i)
	//	fabOutSeqClass.io.fabOutStoreRdy(i)		:= 			//TODO
	}	
	
	io.storeMemData					:= storeSeqClass.io.storeMemData
	io.storeMemValid				:= storeSeqClass.io.storeMemValid
	storeSeqClass.io.storeMemRdy			:= io.storeMemRdy
	
	//						:= storeSeqClass.io.computeDone		//TODO
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	for(i<-0 until fabPortCount){
		io.fabInData(i)					:= fabInSeqClass.io.fabInData(i)
		io.fabInValid(i)				:= fabInSeqClass.io.fabInValid(i)
		fabInSeqClass.io.fabInRdy(i)			:= io.fabInRdy(i)
	}
	
	for(i<-0 until memBankCount){
		fabInSeqClass.io.fabStore(i)			:= fabOutSeqClass.io.fabOutLoc(i)
		fabInSeqClass.io.fabStoreValid(i)		:= fabOutSeqClass.io.fabOutLocValid(i)
		fabOutSeqClass.io.fabOutLocRdy(i)			:= fabInSeqClass.io.fabStoreRdy(i)
	}
	
	//						:= fabInSeqClass.io.computeDone		//TODO
							
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	
	for(i <-0 until fabPortCount){
		fabOutSeqClass.io.fabOut(i)			:= io.fabOut(i)
		fabOutSeqClass.io.fabOutValid(i)		:= io.fabOutValid(i)
		io.fabOutRdy(i)					:= fabOutSeqClass.io.fabOutRdy(i)
	}
	
}

//object controllerTopMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new controllerTop))

//    }
//}
