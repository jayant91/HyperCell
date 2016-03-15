package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class mainConfigure extends Module{
	val io = new Bundle{
		val configData		= UInt(INPUT, width = dataWidth)
		val configDataValid	= Bool(INPUT)
		 
		val loadConfig		= UInt(OUTPUT, width = dataWidth)
		val loadConfigValid	= Bool(OUTPUT)
		
		val storeConfig		= UInt(OUTPUT, width = dataWidth)
		val storeConfigValid	= Bool(OUTPUT)
		
		val fabInConfig		= UInt(OUTPUT, width = dataWidth)
		val fabInConfigValid	= Bool(OUTPUT)
		
		val fabOutConfig	= UInt(OUTPUT, width = dataWidth)
		val fabOutConfigValid	= Bool(OUTPUT)
		
		val fabConfig		= UInt(OUTPUT, width = dataWidth)
		val fabConfigValid	= Bool(OUTPUT)
	}
	
	val configReg		= Reg(init = UInt(0, width = dataWidth))
	val configValidReg	= Reg(init = Bool(false))
	
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
	configReg		:= io.configData
	configValidReg		:= io.configDataValid
	
	io.storeConfig		:= configReg
	io.storeConfigValid	:= configValidReg
	
	io.fabInConfig		:= configReg
	io.fabInConfigValid	:= configValidReg
	
	io.fabOutConfig		:= configReg
	io.fabOutConfigValid	:= configValidReg
	
	io.fabConfig		:= configReg
	io.fabConfigValid	:= configValidReg	
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}
