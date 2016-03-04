package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._



class fifo (widthParam:Int, depthParam:Int) extends Module{

	val addrWidth 	= log2Up(depthParam)
	
	val io		= new Bundle{
		val enqData	= UInt(INPUT, widthParam)
		val deqData	= UInt(OUTPUT, widthParam)
		val enqRdy	= Bool(OUTPUT)
		val deqRdy	= Bool(INPUT)
		val enqValid	= Bool(INPUT)
		val deqValid	= Bool(OUTPUT)
		
		val rst		= Bool(INPUT)
	}
	

	val enqPtr	= Reg(init = UInt(0, addrWidth))
	val deqPtr	= Reg(init = UInt(0, addrWidth))
	val isFull	= Reg(init = Bool(false))
	val doEnq	= (io.enqRdy & io.enqValid).toBool
	val doDeq	= (io.deqValid & io.deqRdy).toBool
	val isEmpty	= !isFull && (enqPtr === deqPtr)
	val deqPtrInc	= (deqPtr + UInt(1))%(UInt(depthParam))
	val enqPtrInc	= (enqPtr + UInt(1))%(UInt(depthParam))
	val isFullNext	= Mux(doEnq && ~doDeq && (enqPtrInc === deqPtr), Bool(true), Mux(doDeq && isFull, Bool(false), isFull))
	
	
	enqPtr	:= Mux(doEnq, enqPtrInc, enqPtr)
	deqPtr	:= Mux(doDeq, deqPtrInc, deqPtr)
	
	isFull	:= isFullNext
	
	val fifoMem = Mem(UInt(width=widthParam), depthParam)
	
	when(io.rst){
		enqPtr		:= UInt(0, width = addrWidth)
		deqPtr		:= UInt(0, width = addrWidth)
		isFull		:= Bool(false)
	}
	
	when(doEnq){
		fifoMem(enqPtr)	:= io.enqData
	}
	
	io.enqRdy	:= !isFull 
	io.deqValid	:= !isEmpty
	fifoMem(deqPtr) <> io.deqData
	
}

//object fifoMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new fifo(32, 2)))

//    }
//}
