package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class memoryBank extends Module{
	
	val addrWidth 	= log2Up(memDepth)
	
	val io		=  new Bundle{
		val inData		= UInt(INPUT, width = memWidth)
		val outData		= UInt(OUTPUT, width = memWidth)
		
		val readEn		= Bool(INPUT)
		val writeEn		= Bool(INPUT)
		
		val writeSuccess	= Bool(OUTPUT)
		val readSuccess		= Bool(OUTPUT)
		
		val doInvalidate	= Bool(INPUT)
		
		val isValid		= Bool(OUTPUT)
		val isWriteLoadRdy	= Bool(OUTPUT)
		val isWriteFabricRdy	= Bool(OUTPUT)
		
		val readAddr		= UInt(INPUT, width = addrWidth)
		val writeAddr		= UInt(INPUT, width = addrWidth)
		
		val writeLoadAddr	= UInt(INPUT, width = addrWidth)
		val writeFabricAddr	= UInt(INPUT, width = addrWidth)
		
		val rst			= Bool(INPUT)
			

	}
	
	val memoryClass 	= Module(new customReg(memDepth, memWidth))
	
	val validBit		= Reg(init = UInt("b0", memDepth))
	
	memoryClass.io.inData		:= io.inData
	io.outData			:= memoryClass.io.outData
	
	memoryClass.io.readEn		:= io.readEn
	memoryClass.io.writeEn		:= (io.writeEn & ~validBit(io.writeAddr))
	
	memoryClass.io.readAddr		:= io.readAddr
	memoryClass.io.writeAddr	:= io.writeAddr
	
	when(io.rst){
		validBit	:= UInt(0, width = memDepth)
	}
	
	when((~validBit(io.writeLoadAddr)) | ((io.readAddr === io.writeLoadAddr) & io.readEn & io.doInvalidate)){
		io.isWriteLoadRdy		:= Bool(true)	
	}
	.otherwise{
		io.isWriteLoadRdy		:= Bool(false)
	}
	
	when((~validBit(io.writeFabricAddr)) | ((io.readAddr === io.writeFabricAddr) & io.readEn & io.doInvalidate)){
		io.isWriteFabricRdy		:= Bool(true)	
	}
	.otherwise{
		io.isWriteFabricRdy		:= Bool(false)
	}
	
	
	when(io.readEn & validBit(io.readAddr)){
		io.isValid		:= Bool(true)
		io.readSuccess		:= Bool(true)
		
		when(io.doInvalidate){
			validBit(io.readAddr)	:= Bool(false)
		}
	}
	.otherwise{
		io.isValid		:= Bool(false)
		io.readSuccess		:= Bool(false)
	}
	
	
	
	
	when((io.writeEn & ~validBit(io.writeAddr)) |  ((io.readAddr === io.writeAddr) & io.readEn & io.doInvalidate)){
		io.writeSuccess		:= Bool(true)
		validBit(io.writeAddr)	:= Bool(true)
	}
	.otherwise{
		io.writeSuccess		:= Bool(false)
	}
	
}

//object memoryBankMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new memoryBank()))

//    }
//}
