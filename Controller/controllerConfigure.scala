
package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._
import HyperCellParams.LoadSeqConfig._


class controllerConfigure(configType: Int) extends Module{

	//configType
	//	0  ->  Load Sequencer
	//	1  ->  Fabric Input Sequencer
	//	2  ->  Fabric Output Sequencer
	//	3  ->  Store Sequencer
	////////////////////////////////////
	val io 		= new Bundle{
		val inConfig		= UInt(INPUT, width = dataWidth)
		val inValid		= Bool(INPUT)			
			
		val outConfig		= UInt(OUTPUT, width = dataWidth)
		val outValid		= Bool(OUTPUT)	
		
		val computeCtrl		= Bool(OUTPUT)	
		val computeCtrlValid	= Bool(OUTPUT)	
	}
	
	val inDataReg	= Reg(init = UInt(0, width = dataWidth))
	
	when(io.inValid){
		inDataReg	:=	io.inConfig
	}
	
	io.outConfig		:= inDataReg
	
	when(UInt(configType) === UInt(0)){
		when((inDataReg(dataWidth-1) === UInt(0))){
			when((inDataReg(0) === UInt(1)) && (inDataReg(dataWidth-1, dataWidth-2) === UInt(0))){
				io.computeCtrl		:= Bool(true)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
			.elsewhen(inDataReg(dataWidth-1, dataWidth - lWidth) === UInt(256)){
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(false)
				io.outValid		:= Bool(true)
				
			}
			.otherwise{
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
		}
		.otherwise{
			io.computeCtrl		:= Bool(false)
			io.computeCtrlValid	:= Bool(false)
			io.outValid		:= Bool(false)
		}
	}
	.elsewhen(UInt(configType) === UInt(1)){
		when((inDataReg(dataWidth-1) === UInt(0))){
			when((inDataReg(0) === UInt(1)) && (inDataReg(dataWidth-1, dataWidth-2) === UInt(0))){
				io.computeCtrl		:= Bool(true)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
			.elsewhen(inDataReg(dataWidth-1, dataWidth - lWidth) === UInt(257)){
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(false)
				io.outValid		:= Bool(true)
				
			}
			.otherwise{
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
		}
		.otherwise{
			io.computeCtrl		:= Bool(false)
			io.computeCtrlValid	:= Bool(false)
			io.outValid		:= Bool(false)
		}
	}
	.elsewhen(UInt(configType) === UInt(2)){
		when((inDataReg(dataWidth-1) === UInt(0))){
			when((inDataReg(0) === UInt(1)) && (inDataReg(dataWidth-1, dataWidth-2) === UInt(0))){
				io.computeCtrl		:= Bool(true)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
			.elsewhen(inDataReg(dataWidth-1, dataWidth - lWidth) === UInt(258)){
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(false)
				io.outValid		:= Bool(true)
				
			}
			.otherwise{
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
		}
		.otherwise{
			io.computeCtrl		:= Bool(false)
			io.computeCtrlValid	:= Bool(false)
			io.outValid		:= Bool(false)
		}
	}
	.elsewhen(UInt(configType) === UInt(3)){
		when((inDataReg(dataWidth-1) === UInt(0))){
			when((inDataReg(0) === UInt(1)) && (inDataReg(dataWidth-1, dataWidth-2) === UInt(0))){
				io.computeCtrl		:= Bool(true)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
			.elsewhen(inDataReg(dataWidth-1, dataWidth - lWidth) === UInt(259)){
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(false)
				io.outValid		:= Bool(true)
				
			}
			.otherwise{
				io.computeCtrl		:= Bool(false)
				io.computeCtrlValid	:= Bool(true)
				io.outValid		:= Bool(false)
			}
		}
		.otherwise{
			io.computeCtrl		:= Bool(false)
			io.computeCtrlValid	:= Bool(false)
			io.outValid		:= Bool(false)
		}
	}
	.otherwise{
		io.computeCtrl		:= Bool(false)
		io.computeCtrlValid	:= Bool(false)
		io.outValid		:= Bool(false)
	}
	
}
