package HyperCell

import Chisel._

class Switch[T <: Data] (gen: T, widthCoord: Int, xCoord: Int, yCoord: Int, ports : Int, widthParam : Int, configRegWidth: Int, depthParam:Int, cuConfigRegWidth: Int ) extends Module{


//	 _______________________________________________________________________________________________________________________________
//	|Config/ | yCoordinate |  FU/	  | 	  	|   										|
//	|Data    |	       |  Switch  |   <-----> 	|   			Switch Configuration					|
//	|________|_____________|__________|_____________|_______________________________________________________________________________|		
//	|1 bit	 |    3 bit    |   1 bit  |  		|       									|
//	|________|_____________|__________|_____________|_______________________________________________________________________________|
//	|					Default Values for Switch Configuration Packet						|
//	|_______________________________________________________________________________________________________________________________|
//	|   1	 |             |     1    |             |              									|
//	|________|_____________|__________|_____________|_______________________________________________________________________________|
//
	
	val io = new Bundle{
		val inPort		= Vec.fill(ports){gen.clone.asInput}
		val outPort		= Vec.fill(ports){gen.clone.asOutput}
		val deqValid		= UInt(OUTPUT, ports)			//DONE
		val enqRdy		= UInt(OUTPUT, ports)			//DONE
		val deqRdy		= UInt(INPUT, ports)			//DONE
		val enqValid		= UInt(INPUT, ports)			//DONE
		
		val outConfigData	= UInt(OUTPUT, widthParam)
		val outCUConfig		= UInt(OUTPUT, cuConfigRegWidth)
		
		val deqOR		= Bool(INPUT)
		val enqOR		= Bool(OUTPUT)
		
		val deqRdyOut		= UInt(INPUT, 4)
		
	}
	
	//INSTANTIATION
	val fifoClass	  = (0 until ports).map(x=>  { Module(new Fifo(x, widthParam, depthParam, ports, configRegWidth))}).toList
	val crossbarClass = Module(new Crossbar[T](gen, ports, widthParam, configRegWidth, cuConfigRegWidth))
	
	
	
	val inCheckData		= UInt(width = widthParam)		//Data coming on the South port, to be checked
	val inType		= UInt(width = 1)			//Type of input Data 1= Config Data, 0= Normal Data
	val enqValidTweaked	= UInt(width = ports)			//Tweaked enqValid Signals, for stoping Config Data to be enqeued into the wrong port
	val inConfigData	= UInt(width = widthParam)		//configuration Data
	val inYCoord		= UInt(width = widthCoord)		//Value to store yCoord to make decision
	val configType		= UInt(width = 1)			// 1-> sConfigReg 0-> cuConfigReg
	val sConfigReg		= Reg(init = UInt("b0", configRegWidth))
	val cuConfigReg 	= Reg(init = UInt("b0", cuConfigRegWidth)) 
	val enqConfigPort_1	= UInt(width = 1)
	
	

	
	inCheckData		:= io.inPort(3)
	inType			:= inCheckData(widthParam -1)
	io.outConfigData	:= io.inPort(3)
	io.outCUConfig		:= cuConfigReg
	
	
	when(inType.toBool){
		
		val enqValidTemp1	= Cat((0 until 4).map(x=>{io.enqValid(ports-x-1)}))
		val enqValidTemp2 	= Bits(0)
		val enqValidTemp3 	= Cat((5 until ports).map(x=>{io.enqValid(ports-x-1)}))
		enqValidTweaked		:= Cat(enqValidTemp1, enqValidTemp2, enqValidTemp3)

		inConfigData	:= io.inPort(3)
		inYCoord	:= Cat((0 until widthCoord).map(x=> {inConfigData(widthParam-x-2)}))
		
		when(UInt(inYCoord) === UInt(yCoord)){
			
			configType		:=  inConfigData(widthParam-widthCoord-2)
			enqConfigPort_1		:=  Bool(false)
			io.enqOR		:=  Bool(true)
			
			when(configType === UInt(1)){
				sConfigReg			:= Cat((0 until configRegWidth).map(x=> {inConfigData(configRegWidth-x-1)})) 
			}
			.otherwise{
				cuConfigReg			:= Cat((0 until cuConfigRegWidth).map(x=> {inConfigData(cuConfigRegWidth-x-1)}))
			}
		}
		
		.otherwise{
			configType	:= UInt(0)
			enqConfigPort_1 := io.enqValid(3)
			io.enqOR	:= io.enqRdy(1).toBool
		}
	}

	.otherwise{
		enqValidTweaked			:= io.enqValid
		inConfigData			:= UInt(0)
		inYCoord			:= UInt(0)
		configType			:= UInt(0)
		enqConfigPort_1			:= Bool(false)
		io.enqOR			:= Bool(false)
		
	}
	
	
	
	//CONNECTIONS
	//crossbar and switch
	
	crossbarClass.io.enqValidIn		:= enqValidTweaked
	crossbarClass.io.inPort 		:= io.inPort	
	crossbarClass.io.inSwitchConfig 	:= sConfigReg
	crossbarClass.io.enqConfigPort_1	:= enqConfigPort_1
	crossbarClass.io.inConfigData		:= io.outConfigData
	
	// switch and fifo
	io.deqValid	:= Cat((0 until ports).map(x=>{fifoClass(ports-1-x).io.deqValid}))  // JOIN TO enqValid of other switch
	io.enqRdy	:= Cat((0 until ports).map(x=>{fifoClass(ports-1-x).io.enqRdy}))   // JOIN TO deqRdy of other switch
	
	for(i<-0 until ports){
		
		//switch and fifo
		io.outPort(i)			:= fifoClass(i).io.deqData
		fifoClass(i).io.configVal	:= sConfigReg
				
				
		if(i==4){
			fifoClass(i).io.deqRdy	:= Cat(io.deqRdy(7,5), io.deqRdyOut(0), io.deqRdy(3,0))
		}
		
		else if(i==5){
			fifoClass(i).io.deqRdy	:= Cat(io.deqRdy(7,6), io.deqRdyOut(1), io.deqRdy(4,0))
		}
		
		else if(i==6){
			fifoClass(i).io.deqRdy	:= Cat(io.deqRdy(7), io.deqRdyOut(2), io.deqRdy(5,0))
		}
		
		else if(i==7){
			fifoClass(i).io.deqRdy	:= Cat(io.deqRdyOut(3), io.deqRdy(6,0))
		}
		
		else{
			fifoClass(i).io.deqRdy	:= io.deqRdy
		}					
		
		
		if(i==1)
		{
			fifoClass(i).io.deqOR	:= io.deqOR
		}
		else
		{
			fifoClass(i).io.deqOR	:= Bool(false)
		}
		
		
		//crossbar and fifo
		fifoClass(i).io.enqData		:= crossbarClass.io.outPort(i)					
		fifoClass(i).io.enqValid	:= crossbarClass.io.enqValidOut(i)					
		
		
		
	}
}
