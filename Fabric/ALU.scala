package HyperCell

import Chisel._

class ALU[T <: Data] (gen: T, widthParam:Int, opCodeWidth:Int) extends Module(){

	//widthParam TrueWidth 32
	
	
	val io	= new Bundle{
		val inOp1	= UInt(INPUT, width = widthParam)
		val inOp2	= UInt(INPUT, width = widthParam)
		val out		= UInt(OUTPUT, width = widthParam)
		val opCode	= UInt(INPUT, width = opCodeWidth)
		val inOp1Valid	= Bool(INPUT)
		val inOp2Valid	= Bool(INPUT)
		val outValid	= Bool(OUTPUT)	
	}
	
		//	OPCODE TABLE
		//	0  ----	ADD			8  ---- SRL
		//	1  ---- SUB			9  ---- SRA
		//	2  ---- MUL			10 ---- ROR
		//	3  ---- AND			11 ---- ROL
		//	4  ---- OR			12 ---- UMAX
		//	5  ---- XOR			13 ---- UMIN
		//	6  ---- NOT			14 ---- 
		//	7  ---- SLL			15 ---- 
		
	
	val widthVal 		= UInt(widthParam)
	val tempInS		= SInt(width = widthParam)
	val temp1		= UInt(width = widthParam)
	val temp2		= UInt(width = widthParam)
	
	
	tempInS			:= SInt(0)
	temp1			:= UInt(0)
	temp2			:= UInt(0)
	
	
	when(io.opCode === UInt(0)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 + io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(1)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 - io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(2)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 * io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(3)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 & io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(4)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 | io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(5)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 ^ io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(6)){
		when(io.inOp1Valid){
			io.out			:= ~(io.inOp1)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(7)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 << io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(8)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= (io.inOp1 >> io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(9)){
		when(io.inOp1Valid & io.inOp2Valid){
			tempInS			:= io.inOp1
			io.out			:= tempInS >> io.inOp2
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(10)){
			
		when(io.inOp1Valid & io.inOp2Valid){
			temp1			:= io.inOp1 >> (io.inOp2%widthVal)
			temp2			:= io.inOp1 << (widthVal - (io.inOp2%widthVal))
			io.out			:= temp1 + temp2
							//Cat(io.inOp1(io.inOp2-UInt(1),UInt(0)), io.inOp1(widthVal, io.inOp2))
			io.outValid		:= Bool(true)
		}
		.otherwise{
			
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(11)){
		when(io.inOp1Valid & io.inOp2Valid){
			temp1			:= io.inOp1 << (io.inOp2%widthVal)
			temp2			:= io.inOp1 >> (widthVal - (io.inOp2%widthVal))
			io.out			:= temp1 + temp2
							//Cat(io.inOp1(widthVal-io.inOp2 , UInt(0)), io.inOp1(widthVal, widthVal -io.inOp2 + UInt(1)))
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(12)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= Mux((io.inOp1 >= io.inOp2), io.inOp1, io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.elsewhen(io.opCode === UInt(13)){
		when(io.inOp1Valid & io.inOp2Valid){
			io.out			:= Mux((io.inOp1 <= io.inOp2), io.inOp1, io.inOp2)
			io.outValid		:= Bool(true)
		}
		.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
		}
	}
	
	.otherwise{
			io.out			:= UInt(0)
			io.outValid		:= Bool(false)
	}
}


//class ALUTest(c: ALU[UInt]) extends Tester(c){

//	val range = scala.math.pow(2, 32).toInt
//	

//	for(t<-0 until 1000){
//		val in1 	= rnd.nextInt(range)
//		val in2 	= rnd.nextInt(range)
//		val in2S	= rnd.nextInt(32)
//		val in1Valid	= true		rnd.nextBoolean()
//		val in2Valid	= true		rnd.nextBoolean()
//		val opCode	= rnd.nextInt(16)
//		var out		= 0
//		var outValid	= 0
//		
//		poke(c.io.inOp1, in1)
//		if(opCode == 7 | opCode ==8 | opCode == 9 | opCode == 10 | opCode == 11){
//			poke(c.io.inOp2, in2S)
//		}
//		else{
//			poke(c.io.inOp2, in2)
//		}
//		
//		poke(c.io.inOp1Valid, in1Valid)
//		poke(c.io.inOp2Valid, in2Valid)
//		poke(c.io.opCode, opCode)
//		
//		
//		if(opCode == 0){
//			out	= in1 + in2
//			outValid= 1
//		}
//		else if(opCode ==1){
//			out 	= in1 - in2
//			outValid= 1
//		}
//		else if(opCode ==2){
//			out 	= in1 * in2
//			outValid= 1
//		}
//		else if(opCode ==3){
//			out 	= in1 & in2
//			outValid= 1
//		}
//		else if(opCode ==4){
//			out 	= in1 | in2
//			outValid= 1
//		}
//		else if(opCode ==5){
//			out 	= in1 ^ in2
//			outValid= 1
//		}
//		else if(opCode ==6){
//			out 	= ~in1
//			outValid= 1
//		}
//		else if(opCode ==7){
//			out 	= in1 << in2S
//			outValid= 1
//		}
//		else if(opCode ==8){
//			out 	= in1 >>> in2S
//			outValid= 1
//		}
//		else if(opCode ==9){
//			out 	= in1 >> in2S
//			outValid=1
//		}
//		else if(opCode ==10){
//			var temp1 = in1 >>> in2S
//			var temp2 = in1 << (32-in2S)
//			out 	  = temp1 + temp2
//			outValid  = 1
//		}
//		else if(opCode ==11){
//			var temp1 = in1 << in2S
//			var temp2 = in1 >>> (32-in2S)
//			out 	  = temp1 + temp2
//			outValid  = 1		
//		}
//		else if(opCode ==12){
//			if(in1 >= in2){
//				out = in1
//			}
//			else{
//				out = in2
//			}
//			outValid= 1
//			
//		}
//		else if(opCode ==13){
//			if(in1 <= in2){
//				out = in1
//			}
//			else{
//				out = in2
//			}
//			outValid= 1
//		}
//		else{
//			out 	= 0
//			outValid= 0
//		}
//		
//		if(in1Valid){
//			if(opCode != 6){
//				if(in2Valid){
//					expect(c.io.out, out)
//					expect(c.io.outValid, outValid)
//				}
//				else{
//					expect(c.io.out, 0)
//					expect(c.io.outValid, 0)				
//				}
//				
//			}
//			else{
//				expect(c.io.out, out)
//				expect(c.io.outValid, outValid)				
//			}
//			
//				
//		}
//		else{
//			expect(c.io.out, 0)
//			expect(c.io.outValid, 0)
//		}
//		
//		
//		step(1)
//	}
//}


//object ALUMain {
//    def main(args: Array[String]) {
//	chiselMainTest(Array[String]("--backend", "c", "--debug", "--compile", "--test",  "--genHarness"),
//	() => Module(new ALU[UInt](UInt(width=32), 32, 4))){c => new ALUTest(c)}
//    }
//}
