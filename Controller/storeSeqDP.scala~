package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.StoreSeqConfig._

class storeSeqDP extends Module{

	val io = new Bundle{
			
		val inConfig			= UInt(INPUT, width = dataWidth)
		val inValid			= Bool(INPUT)
		
		val spillEnd			= Bool(INPUT)
		val nextIterStart		= Bool(INPUT)
		val seqMemAddr			= UInt(INPUT, width = seqMemAddrWidth)
		val seqMemAddrValid		= Bool(INPUT)
		
		
		val storeMemData		= UInt(OUTPUT, (dataWidth + extMemAddrWidth))
		val storeMemValid		= Bool(OUTPUT)
		val storeMemRdy			= Bool(INPUT)
		
		val seqProceed			= Bool(OUTPUT)
		
		val fabOutToStore		= Vec.fill(maxFabricInputs){UInt(INPUT, width = dataWidth)}
		val fabOutToStoreValid		= Vec.fill(maxFabricInputs){Bool(INPUT)}
		val fabOutToStoreRdy		= Vec.fill(maxFabricInputs){Bool(OUTPUT)}
		
		val computeDone			= Bool(OUTPUT)
		
		val computeDoneCtrl		= Bool(INPUT)
	}
	
	
	// Base Address Memory
	val baseAddrMem			= Module(new customReg(baseMemDepth, baseMemWidth))
	
	// Sequence Memory
	val storeSeqMem			= Module(new customReg(seqMemDepth, seqMemWidth))
	
	// Loop offset
	val loopOffsetMem		= Module(new customReg(loopOffsetMemDepth, loopOffsetMemWidth))
	
	// Spill Offset
	val spillOffsetMem		= Module(new customReg(spillMemDepth , spillMemWidth))
	

	//Instantiate Memory Configureres
	val storeSeqMemConfig 			= Module(new memConfig(26, seqMemAddrWidth, seqMemWidth))
	storeSeqMemConfig.io.inConfig		:= io.inConfig
	storeSeqMemConfig.io.inValid		:= io.inValid
	storeSeqMem.io.inData			:= storeSeqMemConfig.io.memData
	storeSeqMem.io.writeAddr		:= storeSeqMemConfig.io.memAddr
	storeSeqMem.io.writeEn			:= storeSeqMemConfig.io.memOutValid
	
	val baseAddrMemConfig			= Module(new memConfig(27, addrLkupIndexWidth, baseMemWidth))
	baseAddrMemConfig.io.inConfig		:= io.inConfig
	baseAddrMemConfig.io.inValid		:= io.inValid
	baseAddrMem.io.inData			:= baseAddrMemConfig.io.memData
	baseAddrMem.io.writeAddr		:= baseAddrMemConfig.io.memAddr
	baseAddrMem.io.writeEn			:= baseAddrMemConfig.io.memOutValid
	
	val loopOffsetMemConfig			= Module(new memConfig(28, addrLkupIndexWidth, loopOffsetMemWidth))
	loopOffsetMemConfig.io.inConfig		:= io.inConfig
	loopOffsetMemConfig.io.inValid		:= io.inValid
	loopOffsetMem.io.inData			:= loopOffsetMemConfig.io.memData
	loopOffsetMem.io.writeAddr		:= loopOffsetMemConfig.io.memAddr
	loopOffsetMem.io.writeEn		:= loopOffsetMemConfig.io.memOutValid
	
	val spillOffsetMemConfig		= Module(new memConfig(29, addrLkupIndexWidth, spillMemWidth))
	spillOffsetMemConfig.io.inConfig	:= io.inConfig
	spillOffsetMemConfig.io.inValid		:= io.inValid
	spillOffsetMem.io.inData		:= spillOffsetMemConfig.io.memData
	spillOffsetMem.io.writeAddr		:= spillOffsetMemConfig.io.memAddr
	spillOffsetMem.io.writeEn		:= spillOffsetMemConfig.io.memOutValid


	//FIFO for sending store Request
	val storeReqFifo		= Module(new fifo (srReqFifoWidth, srReqFifoDepth))
		

	val addrLkupIndex		= UInt(width = addrLkupIndexWidth)			
	val offsetAddr			= UInt(width = offsetAddrWidth)
	val baseAddrLkup		= UInt(width = baseMemWidth)
	val loopOffsetLkup		= UInt(width = loopOffsetMemWidth)
	val spillLkup			= UInt(width = spillMemWidth)
	
	addrLkupIndex			:= UInt(0)	
	baseAddrLkup			:= UInt(0)	
	offsetAddr			:= UInt(0)
	loopOffsetLkup			:= UInt(0)
	spillLkup			:= UInt(0)
	
	val nextSeq			= Reg(init = UInt(0, width = seqMemWidth	))	
	val nextSeqValid		= Reg(init = Bool(false))
	
	val spillEnd			= Reg(init = Bool(false))
	val nextIterStart		= Reg(init = Bool(false))
	val compDoneCnt			= Reg(init = UInt(0, width =3))
	
	val spillEndVal			= Bool()
	val nextIterStartVal		= Bool()
	val seqMemAddrVal		= UInt(width = seqMemAddrWidth)
	val seqMemAddrValidVal		= Bool()
	val getNextReq			= Bool()
	
	spillEndVal			:= Bool(false)
	nextIterStartVal		:= Bool(false)
	seqMemAddrVal			:= UInt(0)
	seqMemAddrValidVal		:= Bool(false)
	getNextReq			:= Bool(false)
	
	val epilogueAfterSpill		= Reg(init = Bool(false))
	val offsetUpdate		= Vec.fill(maxFabricInputs){Reg(init = Bool(false))}
	val savedOffsets		= Vec.fill(maxFabricInputs){Reg(init = UInt(0, width = seqMemAddrWidth))}	
	val noCopyBaseAddr		= Vec.fill(maxFabricInputs){Reg(init = Bool(false))}
	val portSel			= Vec.fill(maxFabricInputs){Reg(init = Bool(false))}
	val storeMemAddr		= Reg(init = UInt(0, width = extMemAddrWidth))	
	
	val epilogueAfterSpillVal	= Bool()
	val offsetUpdateVal		= Vec.fill(maxFabricInputs){Bool()}
	val savedOffsetsVal		= UInt(width = seqMemAddrWidth)	
	val noCopyBaseAddrVal		= Bool()
	
	epilogueAfterSpillVal		:= Bool(false)
	savedOffsetsVal			:= UInt(0)	
	noCopyBaseAddrVal		:= Bool(false)
	for(i<-0 until maxFabricInputs){
		offsetUpdateVal(i)		:= Bool(false)
	}
	
	val firstReqDone		= Reg(init = Bool(false))
	val storeMemAddrVal		= UInt(width = extMemAddrWidth)			
	val portID			= UInt(width = portIDWidth)	
	
	val storeReqData		= UInt(width = dataWidth)
	val storeReqAddr		= UInt(width = extMemAddrWidth)
	
	val computeDone			= Bool()
	
	storeMemAddrVal			:= UInt(0)			
	portID				:= UInt(0)	
	
	storeReqData			:= UInt(0)
	storeReqAddr			:= UInt(0)
	
	computeDone			:= Bool(false)
	
	when(io.seqMemAddrValid){
		storeSeqMem.io.readAddr		:= io.seqMemAddr
		storeSeqMem.io.readEn		:= io.seqMemAddrValid
		nextSeq				:= storeSeqMem.io.outData
		nextSeqValid			:= Bool(true)
		nextIterStart			:= io.nextIterStart
		spillEnd			:= io.spillEnd
	}
	.otherwise{
		storeSeqMem.io.readAddr		:= UInt(0)
		storeSeqMem.io.readEn		:= Bool(false)
	}
	
	
	when(storeSeqMemConfig.io.rst){	
			storeReqFifo.io.rst	:= Bool(true)
			firstReqDone		:= Bool(false)
			for(i<-0 until maxFabricInputs){
				offsetUpdate(i)		:= Bool(false)
				savedOffsets(i)		:= UInt(0)
				noCopyBaseAddr(i)	:= Bool(false)
			}
			spillEnd		:= Bool(false)
			nextSeqValid		:= Bool(false)
			compDoneCnt		:= UInt(0)
			
	}
	.otherwise{
		storeReqFifo.io.rst		:= Bool(false)
	}
	
	
	when((getNextReq || !firstReqDone) && nextSeqValid){
	
		seqMemAddrVal		:= io.seqMemAddr
		nextIterStartVal	:= nextIterStart
		spillEndVal		:= spillEnd
		seqMemAddrValidVal	:= nextSeqValid
		
		firstReqDone		:= Bool(true)
		
		nextSeqValid		:= Bool(false)

		
		addrLkupIndex		:= nextSeq(addrLkupIndexWidth -1 , 0)
		offsetAddr		:= nextSeq(offsetAddrWidth + addrLkupIndexWidth -1, addrLkupIndexWidth)					
		portID			:= nextSeq(offsetAddrWidth + addrLkupIndexWidth + portIDWidth -1, offsetAddrWidth + addrLkupIndexWidth)
		
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
				when(spillEndVal){
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
				when(spillEndVal){
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
		
		storeMemAddr		:= savedOffsetsVal(addrLkupIndex) + offsetAddr
		portSel(portID)		:= Bool(true) 
	
				
		offsetUpdateVal(addrLkupIndex)		:= Bool(false)

		
		when(!noCopyBaseAddrVal){
			noCopyBaseAddr(addrLkupIndex)		:= Bool(true)
		}
		
		
		
		
		for(i <-0 until maxFabricInputs){
			offsetUpdate(i)		:= offsetUpdateVal(i)
		}
		savedOffsets(addrLkupIndex)	:= savedOffsetsVal
		epilogueAfterSpill		:= epilogueAfterSpillVal
		getNextReq			:= Bool(false)
	}
	.otherwise{
		baseAddrMem.io.readAddr		:= UInt(0)
		baseAddrMem.io.readEn		:= Bool(false)
		
		loopOffsetMem.io.readAddr	:= UInt(0)
		loopOffsetMem.io.readEn		:= Bool(false)
		
		spillOffsetMem.io.readAddr	:= UInt(0)
		spillOffsetMem.io.readEn	:= Bool(false)
	}	
		
	for(i<-0 until maxFabricInputs){
		when(portSel(i) && io.fabOutToStoreValid(i) && storeReqFifo.io.enqRdy){
			getNextReq			:= Bool(true)
			portSel(i)			:= Bool(false)
			storeReqData			:= io.fabOutToStore(i)
			storeReqAddr			:= storeMemAddr
			storeReqFifo.io.enqData		:= Cat(storeReqAddr(extMemAddrWidth-1, 0), storeReqData(dataWidth-1, 0))
			storeReqFifo.io.enqValid	:= Bool(true)	
			io.seqProceed			:= Bool(true)
			io.fabOutToStoreRdy(i)		:= Bool(true)	
				
		}
		.otherwise{
			getNextReq			:= Bool(false)
			storeReqData			:= UInt(0)
			storeReqAddr			:= UInt(0)
			storeReqFifo.io.enqData		:= UInt(0)
			storeReqFifo.io.enqValid	:= Bool(false)	
			io.seqProceed			:= Bool(false)	
			io.fabOutToStoreRdy(i)		:= Bool(false)	
		}
	}
	
	when(io.storeMemRdy){
		io.storeMemData			:= storeReqFifo.io.deqData
		io.storeMemValid		:= storeReqFifo.io.deqValid
		storeReqFifo.io.deqRdy		:= io.storeMemRdy
	}
	.otherwise{
		io.storeMemData			:= UInt(0)
		io.storeMemValid		:= Bool(false)
		storeReqFifo.io.deqRdy		:= io.storeMemRdy
	}
	
		
	when((compDoneCnt === UInt(2)) && io.storeMemRdy){
		computeDone		:= Bool(true)
	}
	.otherwise{
		computeDone		:= Bool(false)
	}
	
	io.computeDone		:= computeDone
	
	
	when(io.computeDoneCtrl && getNextReq){
		compDoneCnt		:= compDoneCnt + UInt(1)
	}

}












