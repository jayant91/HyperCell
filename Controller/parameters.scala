package HyperCellParams

import Chisel._
import HyperCellParams.GlobalConfig._

object MemConfig {
	
	val lsMemVar		= 2
	val lsRegLkupVar	= 3
	val lsBaseAddrVar	= 4
	val lsOffsetAddrVar	= 5
	val lsSpillOffsetVar	= 6
	
	val fabInMemVar		= 10
	
	val fabOutMemVar	= 18
	
	val srMemVar		= 26
	val srBaseAddrVar	= 27
	val srOffsetAddrVar	= 28
	val srSpillOffsetVar	= 29
	
	//Configuration Bit lengths for various fifferent type of memories
	val cMemWidth		= 10     	//Length of the Sequence that remains constant
	val vMemWidth		= 5		//No of bits that vary depending on the different type of memories
	val combMemWidth	= dataWidth - cMemWidth
	val memTypeWidth	= combMemWidth + 2
	
	//controllerConfigure
	
}

object fabInConfig{
	
	val localStorageDepth		= 64
	val localStorageWidth 		= log2Up(localStorageDepth) + datawidth

	val fabInpSeqMemWidth 		= (1 + 8*(log2Up(fabPortCount) + log2Up(localStorageDepth)))
	val srcToDestWidth		= fabInpSeqMemWidth -1
	val portIdWidth 		= log2Up(memBankCount)
	val regIndexSize		= portIdWidth + log2Up(localStorageDepth)
	val regIndexWidth		= log2Up(localStorageDepth)
	
	val inValidateBit		= fabInpSeqMemWidth -3
	val srcToDestValidBit		= fabInpSeqMemWidth -2


	val fabSeqWidth			= 10	//width of sequence which remains constant and desribes that it is meant for fabric Input Sequencer
	val dWidth			= 3	//Width of bits differentianting between interation counter head and steady state, prologue etc header Bit[21:19]
	val xBitNo			= 17	//Bit[17] differentiating b/w point 11 and 12
	val prologueSize		= 7	//No of bits defining prologue depths
	val epilogueSize		= 7
	val steadyStateSize		= 10
	val epilogueSpillSize		= 10
	val iterCntSize			= 19
	
	//storeSequencer
	val seqMemDepth			= 512
	val seqMemWidth			= fabInpSeqMemWidth
	val seqMemAddrWidth		= log2Up(seqMemDepth)
	val iterCountWidth		= 32	
	
}


object LoadSeqConfig{
	val maxFabricInputs		= 20
	
	val lWidth			= 10	//width of sequence which remains constant and desribes that it is meant for load Sequencer
	val sWidth			= 3	//Width of bits differentianting between interation counter head and steady state, prologue etc header Bit[21:19]
	val xBitNo			= 17	//Bit[17] differentiating b/w point 3 and 4
	val prologueSize		= 7	//No of bits defining prologue depths
	val epilogueSize		= 7
	val steadyStateSize		= 10
	val epilogueSpillSize		= 10
	val iterCntSize			= 19
	
	//loadSequencer
	val loadSeqMemDepth		= 512
	val loadSeqMemWidth		= 43
	val loadSeqMemAddrWidth		= log2Up(loadSeqMemDepth)
	val loadIterCountWidth		= 32
	
	val offsetAddrWidth		= 32
	
	val addrLkupMemWidth		= 32
	val addrLkupMemDepth		= maxFabricInputs
	val addrLkupIndexWidth		= log2Up(addrLkupMemDepth)
	
	val baseMemWidth		= addrLkupMemWidth
	val baseMemDepth		= addrLkupMemDepth
	
	val loopOffsetMemWidth		= addrLkupMemWidth
	val loopOffsetMemDepth		= addrLkupMemDepth
	
	val spillMemWidth		= addrLkupMemWidth
	val spillMemDepth		= addrLkupMemDepth 
	
	val lookupIndexWidth		= 2
	val regLookupMemWidth		= 57
	val regLookupMemDepth		= 60
	val regLkupIndexAddrWidth	= log2Up(regLookupMemDepth)
	val lRespOutFifoDepth		= 2
	
	//
	val lrRespFifoWidth		= 32 + regLkupIndexAddrWidth
	val lrRespFifoDepth		= 8
	val lrReqFifoWidth		= 32
	val lrReqFifoDepth		= 8
	
}

object StoreSeqConfig{

	val srWidth			= 10	//width of sequence which remains constant and desribes that it is meant for store Sequencer
	val sWidth			= 3	//Width of bits differentianting between interation counter head and steady state, prologue etc header Bit[21:19]
	val xBitNo			= 17	//Bit[17] differentiating b/w point 3 and 4
	val prologueSize		= 7	//No of bits defining prologue depths
	val epilogueSize		= 7
	val steadyStateSize		= 10
	val epilogueSpillSize		= 10
	val iterCntSize			= 19
	
	//storeSequencer
	val seqMemDepth			= 512
	val seqMemWidth			= 43
	val seqMemAddrWidth		= log2Up(seqMemDepth)
	val storeIterCountWidth		= 32
	
	val offsetAddrWidth		= 32
	
	val addrLkupMemWidth		= 32
	val addrLkupMemDepth		= 20
	val addrLkupIndexWidth		= log2Up(addrLkupMemDepth)
	
	val baseMemWidth		= addrLkupMemWidth
	val baseMemDepth		= addrLkupMemDepth
	
	val loopOffsetMemWidth		= addrLkupMemWidth
	val loopOffsetMemDepth		= addrLkupMemDepth
	
	val spillMemWidth		= addrLkupMemWidth
	val spillMemDepth		= addrLkupMemDepth
	
	val maxFabricInputs		= fabPortCount
	
	val srReqFifoWidth		= extMemAddrWidth + 32
	val srReqFifoDepth		= 8
	val portIDWidth			= log2Up(maxFabricInputs)
}

object fabOutConfig{
	
	val bankInfoWidth		= memBanks * (memAddrWidth+1)
	val nextSeqWidth		= bankInfoWidth + 2
	
	val seqMemDepth			= 512
	val seqMemAddrWidth		= log2Up(seqMemDepth)
	val seqMemWidth			= 58 
	val iterCountWidth		= 32
	
	val fabOutLocDataWidth		= bankInfoWidth
	
	val fabSeqWidth			= 10
	val dWidth			= 3
	val xBitNo			= 17
	val prologueSize		= 7	//No of bits defining prologue depths
	val epilogueSize		= 7
	val steadyStateSize		= 10
	val epilogueSpillSize		= 10
	val iterCntSize			= 19
}

object controllerTopConfig{
	val fabLanes 			= 6				//TODO
}
