package HyperCell

import Chisel._



class FifoCU (widthParam:Int, depthParam:Int, ports : Int) extends Module{
	
	//widthParam	FalseWidth	33
	
	var portBits: Int	= log2Up(ports)
	var ptrSize: Int	= log2Up(depthParam)
	
	val io		= new Bundle{
		val enqData	= UInt(INPUT, widthParam)
		val deqData	= UInt(OUTPUT, widthParam)
		val enqRdy	= UInt(OUTPUT, 1)
		val deqRdy	= UInt(INPUT, ports)
		val enqValid	= UInt(INPUT,1)
		val deqValid	= UInt(OUTPUT, 1)
		val deqReg	= UInt(INPUT, ports)
		val enqRdyOut	= UInt(OUTPUT, 1)
	}
	
	
	val deqCond = Cat((0 until ports).map(i=> {io.deqRdy(i) | (~io.deqReg(i))}))
	
	val deqCombined =  andR(deqCond)
	
	
	val enqPtr	= Reg(init = UInt(0, ptrSize))
	val deqPtr	= Reg(init = UInt(0, ptrSize))
	val isFull	= Reg(init = Bool(false))
	val doEnq	= (io.enqRdyOut).toBool
	val doDeq	= (io.deqValid).toBool
	val isEmpty	= !isFull && (enqPtr === deqPtr)
	val deqPtrInc	= (deqPtr + UInt(1))%(UInt(depthParam))
	val enqPtrInc	= (enqPtr + UInt(1))%(UInt(depthParam))
	val isFullNext	= Mux(doEnq && ~doDeq && (enqPtrInc === deqPtr), Bool(true), Mux(doDeq && isFull, Bool(false), isFull))
	
	
	enqPtr	:= Mux(doEnq, enqPtrInc, enqPtr)
	deqPtr	:= Mux(doDeq, deqPtrInc, deqPtr)
	
	isFull	:= isFullNext
	
	val ram = Mem(UInt(width=widthParam), depthParam)
	when(doEnq){
		ram(enqPtr)	:= io.enqData
	}
	
	io.enqRdyOut	:= (!isFull & io.enqValid) 
	io.enqRdy	:= (!isFull)
	io.deqValid	:= (!isEmpty & deqCombined)
	ram(deqPtr) <> io.deqData
	
}

//object FifoCUMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new FifoCU(33, 32, 4)))

//    }
//}

