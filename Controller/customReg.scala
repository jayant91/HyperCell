package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class customReg(memDepth: Int, memWidth: Int) extends Module{
	
	val addrWidth 	= log2Up(memDepth)
	
	val io = new Bundle{
		val inData		= UInt(INPUT, width = memWidth)
		val outData		= UInt(OUTPUT, width = memWidth)
		
		val readEn		= Bool(INPUT)
		val writeEn		= Bool(INPUT)
		
		val readAddr		= UInt(INPUT, width = addrWidth)
		val writeAddr		= UInt(INPUT, width = addrWidth)
	}
	
	val ram		= Mem(UInt(width = memWidth), memDepth)
	
	when(io.writeEn){
		ram(io.writeAddr)	:= io.inData
	}
	
	io.outData	:= ram(io.readAddr)
	
}


//object customRegMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new customReg(64, 32)))

//    }
//}
