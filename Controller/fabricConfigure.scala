package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class fabricConfigure(dataWidth : Int, columnIndex : Int, coordWidth: Int, swConfigRegWidth: Int, cuConfigRegWidth: Int ) extends Module{
	val io 		= new Bundle{
		val inConfig		= UInt(INPUT, width = dataWidth)
		val inValid		= Bool(INPUT)		
		val outConfig		= UInt(OUTPUT, width = dataWidth+1)
		val outValid		= Bool(OUTPUT)		
		val outRdy		= Bool(INPUT)
		val rst 		= Bool(INPUT)
	}
	
	val inDataReg	= Reg(init = UInt(0, width = dataWidth)) 
	val configValid	= Reg(init = Bool(false))
	val ownIndex	= UInt(width = 3)
	val outDataReg	= Reg(init = UInt(0, width = dataWidth+1))
	
	
	ownIndex	:= UInt(columnIndex)
	inDataReg	:= io.inConfig
	
	when(inDataReg(dataWidth-1) === UInt(0) && io.inValid){
		
		when(inDataReg(dataWidth-2, dataWidth -3)=== UInt(1)){
			when(inDataReg(2, 0) === ownIndex){
				configValid	:= Bool(true)
			}
			.otherwise{
				configValid	:= Bool(false)
			}
		}
		.otherwise{
			configValid	:= Bool(false)
		}
		
	}

	
	when((inDataReg(dataWidth-1) === UInt(1)) && configValid && io.outRdy){
		when(inDataReg(dataWidth-2, dataWidth -3)=== UInt(1)){

				outDataReg(dataWidth)					:= UInt(1)
				outDataReg(dataWidth-1, dataWidth- coordWidth)		:= inDataReg(dataWidth-5, dataWidth -coordWidth -4)
				when(inDataReg(dataWidth - coordWidth -5).toBool){
					//CU UNIT
					outDataReg(dataWidth - coordWidth -1)		:= UInt(0)
					outDataReg(dataWidth - coordWidth -2, dataWidth - coordWidth -16)	:= UInt(0, width = 15)
					outDataReg(cuConfigRegWidth-1, 0)				:= inDataReg(cuConfigRegWidth-1,0)
				}
				.otherwise{
					//Switch UNIT	
					outDataReg(dataWidth - coordWidth -1)		:= UInt(1)
					outDataReg(dataWidth - coordWidth -2, dataWidth - coordWidth -5)	:= UInt(0, width = 4)
					outDataReg(swConfigRegWidth-1, 0)		:= inDataReg(swConfigRegWidth-1, 0)
				}
				
				io.outValid			:= Bool(true)
		}
		.otherwise{
			io.outValid			:= Bool(false)
		}
	}
	.otherwise{
		io.outValid		:= Bool(false)
	}
	
	when(io.rst){
		configValid	:= Bool(false)
	}
	
	io.outConfig		:= outDataReg
	
	

}

//object fabricConfigureMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new fabricConfigure(32, 1, 3, 24, 13)))

//    }
//}
