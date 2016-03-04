package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.LoadSeqConfig._

class loadSeqDP extends Module{

	val io		= new Bundle{	
		val inConfig			= UInt(INPUT, width = dataWidth)
		val inValid			= Bool(INPUT)

		val spillEnd			= Bool(INPUT)
		val nextIterStart		= Bool(INPUT)
		val seqMemAddr			= UInt(INPUT, width = loadSeqMemAddrWidth)
		val seqMemAddrValid		= Bool(INPUT)
		
		val memBankEnq			= Vec.fill(memBanks){UInt(OUTPUT, (memWidth + memAddrWidth))}
		val memBankValid		= Vec.fill(memBanks){Bool(OUTPUT)}
		val memBankRdy			= Vec.fill(memBanks){Bool(INPUT)}
		
		val loadRqst			= UInt(OUTPUT, width = lrReqFifoWidth)
		val loadRqstValid		= Bool(OUTPUT)
		val loadRqstRdy			= Bool(INPUT)
		
		val loadResp			= UInt(INPUT, width = lrRespFifoWidth)
		val loadRespValid		= Bool(INPUT)
		val loadRespRdy			= Bool(OUTPUT)
		
		val seqProceed			= Bool(OUTPUT)
	}
	
	//TODO Initialize the following memories
	// Base Address Memory
	val baseAddrMem		= Module(new customReg(baseMemDepth, baseMemWidth))
	
	// Register LookUp Memory
	val regLookupMem	= Module(new customReg(regLookupMemDepth , regLookupMemWidth))
	
	// Sequence Memory
	val loadSeqMem		= Module(new customReg(loadSeqMemDepth, loadSeqMemWidth))
	
	// Loop offset
	val loopOffsetMem	= Module(new customReg(loopOffsetMemDepth, loopOffsetMemWidth))
	
	// Spill Offset
	val spillOffsetMem	= Module(new customReg(spillMemDepth , spillMemWidth))
	
	
	
	///Instantiate memory Controllers
	val loadSeqMemConfig 			= Module(new memConfig(2, loadSeqMemAddrWidth, loadSeqMemWidth))
	loadSeqMemConfig.io.inConfig		:= io.inConfig
	loadSeqMemConfig.io.inValid		:= io.inValid
	loadSeqMem.io.inData			:= loadSeqMemConfig.io.memData
	loadSeqMem.io.writeAddr			:= loadSeqMemConfig.io.memAddr
	loadSeqMem.io.writeEn			:= loadSeqMemConfig.io.memOutValid
	
	val regLkupMemConfig			= Module(new memConfig(3, regLkupIndexAddrWidth , regLookupMemWidth))
	regLkupMemConfig.io.inConfig		:= io.inConfig
	regLkupMemConfig.io.inValid		:= io.inValid
	regLookupMem.io.inData			:= regLkupMemConfig.io.memData
	regLookupMem.io.writeAddr		:= regLkupMemConfig.io.memAddr
	regLookupMem.io.writeEn			:= regLkupMemConfig.io.memOutValid
	
	val baseAddrMemConfig			= Module(new memConfig(4, addrLkupIndexWidth, baseMemWidth))
	baseAddrMemConfig.io.inConfig		:= io.inConfig
	baseAddrMemConfig.io.inValid		:= io.inValid
	baseAddrMem.io.inData			:= baseAddrMemConfig.io.memData
	baseAddrMem.io.writeAddr		:= baseAddrMemConfig.io.memAddr
	baseAddrMem.io.writeEn			:= baseAddrMemConfig.io.memOutValid
	
	val loopOffsetMemConfig			= Module(new memConfig(5, addrLkupIndexWidth, loopOffsetMemWidth))
	loopOffsetMemConfig.io.inConfig		:= io.inConfig
	loopOffsetMemConfig.io.inValid		:= io.inValid
	loopOffsetMem.io.inData			:= loopOffsetMemConfig.io.memData
	loopOffsetMem.io.writeAddr		:= loopOffsetMemConfig.io.memAddr
	loopOffsetMem.io.writeEn		:= loopOffsetMemConfig.io.memOutValid
	
	val spillOffsetMemConfig		= Module(new memConfig(6, addrLkupIndexWidth, spillMemWidth))
	spillOffsetMemConfig.io.inConfig	:= io.inConfig
	spillOffsetMemConfig.io.inValid		:= io.inValid
	spillOffsetMem.io.inData		:= spillOffsetMemConfig.io.memData
	spillOffsetMem.io.writeAddr		:= spillOffsetMemConfig.io.memAddr
	spillOffsetMem.io.writeEn		:= spillOffsetMemConfig.io.memOutValid
	
	
	
	
	
	//FIFO for receiving Load Response
	val lRespFifoDeq	= Bool()
	lRespFifoDeq		:= Bool(false)
	
	val lrRespFifo		= Module(new fifo (lrRespFifoWidth,lrRespFifoDepth))
	val lrResp		= lrRespFifo.io.deqData
	val lrRespValid		= lrRespFifo.io.deqValid
	lrRespFifo.io.deqRdy	:= lRespFifoDeq
	lrRespFifo.io.enqData	:= io.loadResp
	lrRespFifo.io.enqValid	:= io.loadRespValid
	io.loadRespRdy		:= lrRespFifo.io.enqRdy
	
	//FIFO for sending Load Request
	val lrReqFifo		= Module(new fifo (lrReqFifoWidth,lrReqFifoDepth))
	io.loadRqst		:= lrReqFifo.io.deqData
	io.loadRqstValid	:= lrReqFifo.io.deqValid
	lrReqFifo.io.deqRdy	:= io.loadRqstRdy 
	
	//Instantiate FIFO for Input to memory Banks
	val outDPFifo		= Vec.fill(memBanks){Module(new fifo((memWidth + memAddrWidth),lRespOutFifoDepth)).io}
	
	
	for(i<-0 until memBanks){
		io.memBankEnq(i)		:= outDPFifo(i).deqData
		io.memBankValid(i)		:= outDPFifo(i).deqValid
		outDPFifo(i).deqRdy		:= io.memBankRdy(i)
	}
	
	
	//Working on Load Response
	val lookupIndex		= Reg(init = UInt(0, width = lookupIndexWidth))
	val regLookupIndex	= UInt(width = regLkupIndexAddrWidth)
	
	regLookupIndex		:= UInt(0)
	
	val regLookup		= UInt(width = regLookupMemWidth)
	val regLookupData	= UInt(width = dataWidth)
	
	regLookup		:= UInt(0)
	regLookupData		:= UInt(0)
	
	val lRespData		= Reg(init = UInt(0, width = dataWidth)	)	
	val lRespDest		= Reg(init = UInt(0, width = regLookupMemWidth))		
	val lRespLkupValid	= Reg(init = Bool(false))
	
	//Enquing to FIFO logic registers annd wires
	val enqReqWire		= Vec.fill(memBanks){Bool()}
	val enqDoneReg		= Reg(init = UInt(0, width = memBanks))
	val enqComplete		= Bool()					//When enqDoneReg contains all one
	enqComplete		:= Bool(false)
	
	when(lrRespValid && enqComplete){
		regLookupData	:= lrResp(dataWidth-1, 0)		
		regLookupIndex	:= lrResp(dataWidth + regLkupIndexAddrWidth -1 , dataWidth)
		
		regLookupMem.io.readAddr	:= regLookupIndex + lookupIndex
		regLookupMem.io.readEn		:= Bool(true)
		regLookup			:= regLookupMem.io.outData	
		
		when(regLookup(regLookupMemWidth-1) === UInt(1)){
			lookupIndex		:= UInt(0)
			lRespFifoDeq		:= Bool(true)
		}
		.otherwise{
			lookupIndex		:= lookupIndex + UInt(1)
			lRespFifoDeq		:= Bool(false)
		}
		
		lRespData 		:= regLookupData
		lRespDest		:= regLookup
		lRespLkupValid		:= Bool(true)
	}
	.elsewhen(!lrRespValid){
		regLookupMem.io.readAddr	:= UInt(0)
		regLookupMem.io.readEn		:= Bool(false)
		lRespLkupValid			:= Bool(false)
		lRespFifoDeq			:= Bool(false)
	}
	.otherwise{
		regLookupMem.io.readAddr	:= UInt(0)
		regLookupMem.io.readEn		:= Bool(false)
		lRespFifoDeq			:= Bool(false)
	}
	
	when(lRespLkupValid){
		for(i<-0 until memBanks){
			enqReqWire(i)	:= lRespDest(memAddrWidth*i + memAddrWidth -1)
		}
	}
	.otherwise{
		for(i<-0 until memBanks){
			enqReqWire(i)	:= Bool(false)
		}
	}
	
	for(i<-0 until memBanks){
		enqDoneReg(i)		:= ~enqReqWire(i) || outDPFifo(i).enqRdy 
	}
	
	for(i<-0 until memBanks){
		when(enqReqWire(i) && outDPFifo(i).enqRdy){
			outDPFifo(i).enqData	:=  Cat(lRespDest((i+1)*(memAddrWidth+1)-2,i*(memAddrWidth+1)), lRespData(dataWidth-1, 0))
			outDPFifo(i).enqValid	:= Bool(true)
			enqDoneReg(i)		:= UInt(1)
		}
		.otherwise{
			outDPFifo(i).enqData	:= UInt(0)
			outDPFifo(i).enqValid	:= Bool(false)			
		}
	}
	
	enqComplete		:= andR(enqDoneReg).toBool()
	
	
	
	
	
	// LOAD REQUEST
	val seqInfoReg			= Reg(init = UInt(0, width = loadSeqMemWidth))	
	val seqInfoRegValid		= Reg(init = Bool(false))		
	val spillEnd			= Reg(init = Bool(false))
	val nextIterStart		= Reg(init = Bool(false))
	
	val spillEndVal			= Bool()
	val nextIterStartVal		= Bool()
	val seqMemAddrVal		= UInt(width = loadSeqMemAddrWidth)
	val seqMemAddrValidVal		= Bool()
	
	spillEndVal			:= Bool(false)
	nextIterStartVal		:= Bool(false)
	seqMemAddrVal			:= UInt(0)
	seqMemAddrValidVal		:= Bool(false)
	
	val addrLkupIndex		= UInt(width = addrLkupIndexWidth)	
	val nextLkupIndex		= UInt(width = regLkupIndexAddrWidth)		
	val offsetAddr			= UInt(width = offsetAddrWidth)
	
	addrLkupIndex			:= UInt(0)	
	nextLkupIndex			:= UInt(0)	
	offsetAddr			:= UInt(0)
	
	
	
	val nextRqst			= Reg(init = UInt(0,width = (loadSeqMemAddrWidth + regLkupIndexAddrWidth)))
	val nextRqstValid		= Reg(init = Bool(false))
	
	val baseAddrLkup		= UInt(width = baseMemWidth)
	val loopOffsetLkup		= UInt(width = loopOffsetMemWidth)
	val spillLkup			= UInt(width = spillMemWidth)
	
	baseAddrLkup			:= UInt(0)
	loopOffsetLkup			:= UInt(0)
	spillLkup			:= UInt(0)
	
	val epilogueAfterSpill		= Reg(init = Bool(false))
	val offsetUpdate		= Vec.fill(maxFabricInputs){Reg(init = Bool(false))}
	val noCopyBaseAddr		= Vec.fill(maxFabricInputs){Reg(init = Bool(false))}
	val savedOffsets		= Vec.fill(maxFabricInputs){Reg(init = UInt(0, width = loadSeqMemAddrWidth))}	
	
	val epilogueAfterSpillVal	= Bool()
	val offsetUpdateVal		= Vec.fill(maxFabricInputs){Bool()}
	val savedOffsetsVal		= UInt(width = loadSeqMemAddrWidth)	
	val noCopyBaseAddrVal		= Bool()
	
	for(i<-0 until maxFabricInputs){
		offsetUpdateVal(i)	:= Bool(false)
	}
	savedOffsetsVal		:= UInt(0)
	noCopyBaseAddrVal	:= Bool(false)
	epilogueAfterSpillVal	:= Bool(false)


	
	when(lrReqFifo.io.enqRdy && io.seqMemAddrValid){
		loadSeqMem.io.readAddr		:= io.seqMemAddr
		loadSeqMem.io.readEn		:= io.seqMemAddrValid
		seqInfoReg			:= loadSeqMem.io.outData
		seqInfoRegValid			:= Bool(true)
		nextIterStart			:= io.nextIterStart
		spillEnd			:= io.spillEnd
		
		io.seqProceed			:= Bool(true)
		
	}
	.otherwise{
		loadSeqMem.io.readAddr		:= UInt(0)
		loadSeqMem.io.readEn		:= Bool(false)
		io.seqProceed			:= Bool(false)
	}
	
	when(seqInfoRegValid){
		seqMemAddrVal			:= io.seqMemAddr
		nextIterStartVal		:= nextIterStart
		spillEndVal			:= spillEnd
		seqMemAddrValidVal		:= io.seqMemAddrValid
	
		addrLkupIndex			:= seqInfoReg(offsetAddrWidth + addrLkupIndexWidth -1 , offsetAddrWidth)
		offsetAddr			:= seqInfoReg(offsetAddrWidth-1, 0)						
		nextLkupIndex			:= seqInfoReg(offsetAddrWidth + addrLkupIndexWidth + regLkupIndexAddrWidth -1 , offsetAddrWidth + addrLkupIndexWidth)
		seqInfoRegValid			:= Bool(false)
		
		baseAddrMem.io.readAddr		:= addrLkupIndex
		baseAddrMem.io.readEn		:= Bool(true)
		baseAddrLkup			:= baseAddrMem.io.outData
		
		loopOffsetMem.io.readAddr	:= addrLkupIndex
		loopOffsetMem.io.readEn		:= Bool(true)
		loopOffsetLkup			:= loopOffsetMem.io.outData
		
		spillOffsetMem.io.readAddr	:= addrLkupIndex
		spillOffsetMem.io.readEn	:= Bool(true)
		spillLkup			:= spillOffsetMem.io.outData			
		
		when(spillEndVal){
			epilogueAfterSpillVal		:= Bool(true)
		}
		.otherwise{
			epilogueAfterSpillVal		:= epilogueAfterSpill
		}
		
		
		when((spillEndVal) || (nextIterStartVal)){
			for(i<-0 until maxFabricInputs){
				offsetUpdateVal(i)		:= Bool(true)
			}		
		}
		.otherwise{
			for(i<-0 until maxFabricInputs){
				offsetUpdateVal(i)		:= offsetUpdate(i)
			}
		}
		
		
		noCopyBaseAddrVal	:= noCopyBaseAddr(addrLkupIndex)
		
		
		when(noCopyBaseAddrVal){
			when(offsetUpdateVal(addrLkupIndex)){
				when(epilogueAfterSpill){
					savedOffsetsVal		:=	savedOffsets(addrLkupIndex) - spillLkup
				}
				.otherwise{
					savedOffsetsVal		:=	savedOffsets(addrLkupIndex) + loopOffsetLkup
				}
			}
			.otherwise{
				savedOffsetsVal		:=	savedOffsets(addrLkupIndex)				
			}
						
		}
		.otherwise{
			when(offsetUpdateVal(addrLkupIndex)){
				when(epilogueAfterSpill){
					savedOffsetsVal		:=	baseAddrLkup - spillLkup
				}
				.otherwise{
					savedOffsetsVal		:=	baseAddrLkup + loopOffsetLkup
				}
			}
			.otherwise{
				savedOffsetsVal		:=	baseAddrLkup				
			}
		}
		
		
		
		val combinedAddr	= savedOffsetsVal + offsetAddr
		//nextRqst	:= Cat(nextLkupIndex(regLkupIndexAddrWidth-1,  0), combinedAddr(loadSeqMemAddrWidth-1 , 0))
		
		nextRqst	:= Cat(nextLkupIndex, combinedAddr)
		nextRqstValid 	:= Bool(true)
		
		when(nextRqstValid && lrReqFifo.io.enqRdy){
			lrReqFifo.io.enqData		:= nextRqst
			lrReqFifo.io.enqValid		:= Bool(true)
			nextRqstValid			:= Bool(false)
		}
		.otherwise{
			lrReqFifo.io.enqData		:= UInt(0)
			lrReqFifo.io.enqValid		:= Bool(false)
		}

		offsetUpdateVal(addrLkupIndex)		:= Bool(false)
		
		
		when(!noCopyBaseAddrVal){
			noCopyBaseAddr(addrLkupIndex)		:= Bool(true)
		}
		
		for(i <-0 until maxFabricInputs){
			offsetUpdate(i)		:= offsetUpdateVal(i)
		}
		
		savedOffsets(addrLkupIndex)	:= savedOffsetsVal
		epilogueAfterSpill		:= epilogueAfterSpillVal	
		
		
	}
	.otherwise{
		baseAddrMem.io.readAddr		:= UInt(0)
		baseAddrMem.io.readEn		:= Bool(false)
		
		loopOffsetMem.io.readAddr	:= UInt(0)
		loopOffsetMem.io.readEn		:= Bool(true)
		
		spillOffsetMem.io.readAddr	:= UInt(0)
		spillOffsetMem.io.readEn	:= Bool(true)	
		
		spillEndVal			:= Bool(false)
		nextIterStartVal		:= Bool(false)
		seqMemAddrVal			:= UInt(0)
		seqMemAddrValidVal		:= Bool(false)
		
		baseAddrLkup			:= UInt(0)
		loopOffsetLkup			:= UInt(0)
		spillLkup			:= UInt(0)
		
		addrLkupIndex			:= UInt(0)	
		nextLkupIndex			:= UInt(0)	
		offsetAddr			:= UInt(0)
		
		lrReqFifo.io.enqData		:= UInt(0)
		lrReqFifo.io.enqValid		:= Bool(false)
	}
	
	when(loadSeqMemConfig.io.rst){
			lrRespFifo.io.rst	:= Bool(true)
			lrReqFifo.io.rst	:= Bool(true)
			for(i<-0 until memBanks){
				outDPFifo(i).rst	:= Bool(true)
			}
			
			for(i<-0 until maxFabricInputs){
				offsetUpdate(i)		:= Bool(false)
				savedOffsets(i)		:= UInt(0)
				noCopyBaseAddr(i)	:= Bool(false)
			}
			spillEnd		:= Bool(false)
			seqInfoRegValid		:= Bool(false)
			
	}
	.otherwise{
		lrRespFifo.io.rst	:= Bool(false)
		lrReqFifo.io.rst	:= Bool(false)
		for(i<-0 until memBanks){
				outDPFifo(i).rst	:= Bool(false)
			}
	}
	
	
}



//object loadSeqDPMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new loadSeqDP()))

//    }
//}








