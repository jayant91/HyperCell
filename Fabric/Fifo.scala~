package HyperCell

import Chisel._



class Fifo (val ownVal: Int, widthParam:Int, depthParam:Int, ports : Int, configRegWidth: Int) extends Module{
	
	// widthParam FalseWidth 33
	
	
	var portBits: Int	= log2Up(ports)
	var ptrSize: Int	= log2Up(depthParam)
	
	val io		= new Bundle{
		val enqData	= UInt(INPUT, widthParam)
		val deqData	= UInt(OUTPUT, widthParam)
		val enqRdy	= UInt(OUTPUT, 1)
		val deqRdy	= UInt(INPUT, ports)
		val enqValid	= UInt(INPUT,1)
		val deqValid	= UInt(OUTPUT, 1)
		val configVal	= UInt(INPUT, configRegWidth)
		val deqOR	= Bool(INPUT)
	}
	

	val ownReg	= UInt(width = portBits)
	ownReg		:= UInt(ownVal)

		
	val deqReg	= Vec.fill(ports){UInt(width =1)}
	for(i <- 0 until ports)
	{
		
		when(ownReg === Cat(io.configVal(portBits*i + 2),io.configVal(portBits*i + 1), io.configVal(portBits*i)))
		{
			deqReg(i) := UInt(1)
		}	
		.otherwise
		{
			deqReg(i) := UInt(0)
		}
			
		
	}
	
	val deqCond = Cat((0 until ports).map(i=> {io.deqRdy(i) | (~deqReg(i))}))
	
	val deqCombined		= Mux(io.deqOR, io.deqRdy(1), andR(deqCond))
	
	
	
	val enqPtr	= Reg(init = UInt(0, ptrSize))
	val deqPtr	= Reg(init = UInt(0, ptrSize))
	val isFull	= Reg(init = Bool(false))
	val doEnq	= (io.enqRdy& io.enqValid).toBool
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
	
	io.enqRdy	:= !isFull
	io.deqValid	:= (!isEmpty & deqCombined)
	ram(deqPtr) <> io.deqData
	
}

//object FifoMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new Fifo(5, 33, 32, 8, 24)))

//    }
//}
