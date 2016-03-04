package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class controllerLocalStorage (fifoDepth: Int, memDepth: Int, memWidth: Int, bankCount : Int) extends Module{
	
	val addrWidth 	= log2Up(memDepth)
	val fifoWidth	= addrWidth + memWidth
	
	val io	= new Bundle{
		val inDataLoad		= Vec.fill(bankCount){UInt(INPUT, width = memWidth)}
		val inDataFabric	= Vec.fill(bankCount){UInt(INPUT, width = memWidth)}
		
		val outData		= Vec.fill(bankCount){UInt(OUTPUT, width = memWidth)}
		val isReadValid		= Vec.fill(bankCount){Bool(OUTPUT)}
		val readAddr		= Vec.fill(bankCount){UInt(INPUT, width = addrWidth)}
		val readEn		= Vec.fill(bankCount){Bool(INPUT)}
		val doInvalidate	= Vec.fill(bankCount){Bool(INPUT)}
		
		
		val readSuccess		= Vec.fill(bankCount){Bool(OUTPUT)}
		val writeSuccess	= Vec.fill(bankCount){Bool(OUTPUT)}
		
		val enqRdyLoad		= Vec.fill(bankCount){UInt(OUTPUT, width = 1)}
		val enqRdyFabric	= Vec.fill(bankCount){UInt(OUTPUT, width = 1)}
		
		val enqValidLoad	= Vec.fill(bankCount){UInt(INPUT, width = 1)}
		val enqValidFabric	= Vec.fill(bankCount){UInt(INPUT, width = 1)}
		
		val rst			= Bool(INPUT)
		
		
		
	}
	
	
	val fifoLoadClass	= Vec.fill(bankCount){Module(new fifo(fifoWidth, fifoDepth)).io}
	val fifoFabricClass	= Vec.fill(bankCount){Module(new fifo(fifoWidth, fifoDepth)).io}
	val memoryBankClass	= Vec.fill(bankCount){Module(new memoryBank()).io}
	
	
	for(i<-0 until bankCount){
		fifoLoadClass(i).enqData		:= io.inDataLoad(i)
		fifoFabricClass(i).enqData		:= io.inDataFabric(i)
		
		io.enqRdyLoad(i)			:= fifoLoadClass(i).enqRdy
		fifoLoadClass(i).enqValid		:= io.enqValidLoad(i)
		
		io.enqRdyFabric(i)			:= fifoFabricClass(i).enqRdy
		fifoFabricClass(i).enqValid		:= io.enqValidFabric(i)
		
		memoryBankClass(i).readAddr		:= io.readAddr(i)
		memoryBankClass(i).readEn		:= io.readEn(i)
		memoryBankClass(i).doInvalidate		:= io.doInvalidate(i)
		io.isReadValid(i)			:= memoryBankClass(i).isValid
		io.outData(i)				:= memoryBankClass(i).outData
		io.readSuccess(i)			:= memoryBankClass(i).readSuccess
		
		memoryBankClass(i).rst			:= io.rst
		fifoLoadClass(i).rst			:= io.rst
		fifoFabricClass(i).rst			:= io.rst
		
	}
	
	
	for(i<-0 until bankCount){
	
		memoryBankClass(i).writeLoadAddr		:= fifoLoadClass(i).deqData(fifoWidth-1, memWidth)
		memoryBankClass(i).writeFabricAddr		:= fifoFabricClass(i).deqData(fifoWidth-1, memWidth)
			
		when(fifoLoadClass(i).deqValid.toBool && memoryBankClass(i).isWriteLoadRdy){

			memoryBankClass(i).inData		:= fifoLoadClass(i).deqData(memWidth-1, 0)
			memoryBankClass(i).writeAddr		:= fifoLoadClass(i).deqData(fifoWidth-1, memWidth)
			memoryBankClass(i).writeEn		:= Bool(true)
			fifoLoadClass(i).deqRdy			:= memoryBankClass(i).isWriteLoadRdy
			io.writeSuccess(i)			:= memoryBankClass(i).writeSuccess
		}

		
		.elsewhen(fifoFabricClass(i).deqValid.toBool && memoryBankClass(i).isWriteFabricRdy){

			memoryBankClass(i).inData		:= fifoFabricClass(i).deqData(memWidth-1, 0)
			memoryBankClass(i).writeAddr		:= fifoFabricClass(i).deqData(fifoWidth-1, memWidth)
			memoryBankClass(i).writeEn		:= Bool(true)
			fifoFabricClass(i).deqRdy		:= memoryBankClass(i).isWriteFabricRdy
			io.writeSuccess(i)			:= memoryBankClass(i).writeSuccess
			
		}
		
		.otherwise{
			memoryBankClass(i).inData		:= UInt(0)
			memoryBankClass(i).writeAddr		:= UInt(0)
			memoryBankClass(i).writeEn		:= Bool(false)
			fifoLoadClass(i).deqRdy			:= Bool(false)
			fifoFabricClass(i).deqRdy		:= Bool(false)
			io.writeSuccess(i)			:= Bool(false)
			
		}
	}

}


//object controllerLocalStorageMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new controllerLocalStorage(2, 64, 32, 8)))

//    }
//}
