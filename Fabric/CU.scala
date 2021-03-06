package HyperCell

import Chisel._

class Mux[T <: Data] (gen : T, widthParam : Int, cuPorts: Int) extends Module{

	//widthParam TrueWidth 32
	
	
	val io	= new Bundle{
		val inMuxPort	= Vec.fill(cuPorts){gen.clone.asInput}
		val sel 	= UInt(INPUT, width = log2Up(cuPorts))
		val outMuxPort	= UInt(OUTPUT, width = widthParam)
		}
		
		io.outMuxPort := io.inMuxPort(io.sel)
}


class CU[T <: Data] (gen: T, widthParam:Int, depthParam: Int, opCodeWidth:Int, cuPorts:Int, cuConfigWidth:Int) extends Module(){

//	widthParam TrueWidth 32
//	
//	COMPUTE UNIT CONFIG REG
//	
//	 _______________________________________________________________________________________________________________________________
//	|Config/ | yCoordinate |  FU/	  | 	  	|   			|  	      |		    |	Opcode   |		|
//	|Data    |	       |  Switch  |   <-----> 	|    Deq Combination	|  Operand 2  |	 Operand 1  |  	 Type	 |    Opcode	|
//	|________|_____________|__________|_____________|_______________________|_____________|_____________|____________|______________|		
//	|1 bit	 |    3 bit    |   1 bit  |  		|        4 bit		|    2 bit    |    2 bit    |     1 bit	 |     4 bit 	|
//	|________|_____________|__________|_____________|_______________________|_____________|_____________|____________|______________|
//	|					Default Values for FU Configuration Packet						|
//	|_______________________________________________________________________________________________________________________________|
//	|   1	 |             |     0    |             |                       |             |             | FPU-1 ALU-0|              |
//	|________|_____________|__________|_____________|_______________________|_____________|_____________|____________|______________|
//	
//	Deq Combination
//	 ________________________________________________________________
//	|     Bit 1	|     Bit 2	|      Bit 3	|     Bit 4	|
//	|_______________|_______________|_______________|_______________|
//	|  South East	|   South West	|   North West	|  North East	|
//	|_______________|_______________|_______________|_______________|
//
//	Operand Nomenclature
//	
//	North East 		00
//	North West		01
//	South West		10
//	South East		11
//
//



	val io	= new Bundle{
		val in		= Vec.fill(cuPorts){gen.clone.asInput}
		val out		= gen.clone.asOutput
		val inConfig	= UInt(INPUT, width = cuConfigWidth)
		val enqRdy	= UInt(OUTPUT, 1)
		val deqRdy	= UInt(INPUT, cuPorts)
		val enqValid	= UInt(INPUT, cuPorts)
		val deqValid	= UInt(OUTPUT, 1)
		val enqRdyOut	= UInt(OUTPUT, 1)
	}
	
	
//	val FPUinOp1		= UInt(0)			//TO BE REPLACED WITH FPU io
//	val FPUinOp2		= UInt(0)			//TO BE REPLACED WITH FPU io
//	val FPUinOp1Valid	= Bool(false)			//TO BE REPLACED WITH FPU io
//	val FPUinOp2Valid	= Bool(false)			//TO BE REPLACED WITH FPU io
//	val FPUopCode		= UInt(0)			//TO BE REPLACED WITH FPU io
//	val FPUoutValid		= UInt(0)			//TO BE REPLACED WITH FPU io
//	val FPUout		= Bool(false)			//TO BE REPLACED WITH FPU io
	
	
	val ALUClass			= Module(new ALU[T](gen, widthParam, opCodeWidth))
	val FifoCUClass			= Module(new FifoCU(widthParam, depthParam, cuPorts))
	
	
	val MuxIn1			= Module(new Mux[T](gen, widthParam, cuPorts))
	val MuxIn2			= Module(new Mux[T](gen, widthParam, cuPorts))
	val MuxInValid1			= Module(new Mux[UInt](UInt(width = 1), 1, cuPorts))
	val MuxInValid2			= Module(new Mux[UInt](UInt(width = 1), 1, cuPorts))
	
	
	
	
	
	io.deqValid			:= FifoCUClass.io.deqValid
	FifoCUClass.io.deqRdy		:= io.deqRdy
	FifoCUClass.io.deqReg		:= io.inConfig(12,9)		//TO DO PARAMETERIZATION
	
	
	MuxIn1.io.inMuxPort		:= io.in
	MuxIn2.io.inMuxPort		:= io.in
	MuxIn1.io.sel			:= io.inConfig(6,5)		//TO DO PARAMETERIZATION
	MuxIn2.io.sel			:= io.inConfig(8,7)		//TO DO PARAMETERIZATION
	
	MuxInValid1.io.inMuxPort	:= io.enqValid
	MuxInValid2.io.inMuxPort	:= io.enqValid
	MuxInValid1.io.sel		:= io.inConfig(6,5)		//TO DO PARAMETERIZATION
	MuxInValid2.io.sel		:= io.inConfig(8,7)		//TO DO PARAMETERIZATION
	
	
	io.enqRdy			:= FifoCUClass.io.enqRdy
	io.enqRdyOut			:= FifoCUClass.io.enqRdyOut
	
	when(io.inConfig(4)){
		//OpCode belongs to FPU
//		FPUopCode		:= io.inConfig(3,0)			//TO BE REPLACED WITH FPU io
//		FPUinOp1		:= MuxIn1.io.outMuxPort			//TO BE REPLACED WITH FPU io
//		FPUinOp2		:= MuxIn2.io.outMuxPort			//TO BE REPLACED WITH FPU io
//		FPUinOp1Valid		:= MuxInValid1.io.outMuxPort		//TO BE REPLACED WITH FPU io
//		FPUinOp2Valid		:= MuxInValid2.io.outMuxPort		//TO BE REPLACED WITH FPU io	
//		FifoCUClass.io.enqValid	:= FPUoutValid				//TO BE REPLACED WITH FPU io
//		FifoCUClass.io.enqData	:= Cat(UInt(0, width =1), FPUout)	//TO BE REPLACED WITH FPU io
		
		ALUClass.io.opCode	:= UInt(15)
		ALUClass.io.inOp1	:= UInt(0)
		ALUClass.io.inOp2	:= UInt(0)
		ALUClass.io.inOp1Valid	:= Bool(false)
		ALUClass.io.inOp2Valid	:= Bool(false)
		FifoCUClass.io.enqValid	:= UInt(0)
		FifoCUClass.io.enqData	:= UInt(0)
	}
	.otherwise{
		//OpCode belongs to ALU
//		FPUopCode		:= UInt(15)				//TO BE REPLACED WITH FPU io
//		FPUinOp1		:= UInt(0)				//TO BE REPLACED WITH FPU io
//		FPUinOp2		:= UInt(0)				//TO BE REPLACED WITH FPU io
//		FPUinOp1Valid		:= Bool(false)				//TO BE REPLACED WITH FPU io
//		FPUinOp2Valid		:= Bool(false)				//TO BE REPLACED WITH FPU io
		
		
		ALUClass.io.opCode	:= io.inConfig(3,0)
		ALUClass.io.inOp1	:= MuxIn1.io.outMuxPort
		ALUClass.io.inOp2	:= MuxIn2.io.outMuxPort
		ALUClass.io.inOp1Valid	:= MuxInValid1.io.outMuxPort
		ALUClass.io.inOp2Valid	:= MuxInValid2.io.outMuxPort
		FifoCUClass.io.enqValid	:= ALUClass.io.outValid
		FifoCUClass.io.enqData	:= Cat(UInt(0, width =1), ALUClass.io.out)
	}
	
	io.out			:= FifoCUClass.io.deqData
	io.deqValid		:= FifoCUClass.io.deqValid
	
}

//class CUTest(c: CU[UInt]) extends Tester(c){
//	val range = scala.math.pow(2, 32).toInt
//	
//	for(i<-0 until 10)
//	{
//		poke(c.io.in(0), rnd.nextInt(range))
//	}
//}


//object CUMain {
//    def main(args: Array[String]) {
//	chiselMainTest(Array[String]("--backend", "c", "--debug", "--compile", "--test",  "--genHarness"),
//	() => Module(new CU[UInt](UInt(width=32), 32, 2, 5, 8, 24))){c => new CUTest(c)}
//    }
//}
