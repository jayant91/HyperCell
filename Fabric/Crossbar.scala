package HyperCell

import Chisel._

class MuxSwitch[T <: Data] (gen : T, ports: Int, widthParam : Int) extends Module{
	val io	= new Bundle{
		val inMuxPort	= Vec.fill(ports){gen.clone.asInput}
		val sel 	= UInt(INPUT, width = log2Up(ports))
		val outMuxPort	= UInt(OUTPUT, width = widthParam)
		}
		
		io.outMuxPort := io.inMuxPort(io.sel)
}

		
class Crossbar[T <: Data] ( gen: T, ports : Int, widthParam : Int, configRegWidth: Int, cuConfigRegWidth: Int) extends Module{
	
	val io	= new Bundle{
		val inPort		= Vec.fill(ports){gen.clone.asInput}
		val outPort		= Vec.fill(ports){gen.clone.asOutput}
		val inSwitchConfig	= UInt(INPUT, width = configRegWidth)	
		val enqValidIn		= Vec.fill(ports){UInt(width=1).asInput}     	
		val enqValidOut		= Vec.fill(ports){UInt(width=1).asOutput}
		val inConfigData	= UInt(INPUT, widthParam)
		val enqConfigPort_1	= Bool(INPUT)
		}
	
	val muxAryInput		= (0 until ports).map(x=>  {  Module(new MuxSwitch[UInt](UInt(width=widthParam),(ports), widthParam))}).toList
	val muxAryEnq		= (0 until ports).map(x=>  {  Module(new MuxSwitch[UInt](UInt(width=1),(ports), 1))}).toList
	
	for(i<-0 until ports)
	{
	
		var k: Int = 0
		for(j<-0 until (ports))
		{
			if(j==i)
			{
			
				k += 1
			}
			else
			{
				muxAryInput(i).io.inMuxPort(j)	:= io.inPort(k)
				muxAryEnq(i).io.inMuxPort(j)	:= io.enqValidIn(k)
				k +=1
			}
		
		}
		
		muxAryInput(i).io.sel	:= Cat(io.inSwitchConfig(3*i+2), io.inSwitchConfig(3*i+1), io.inSwitchConfig(3*i))
		muxAryEnq(i).io.sel	:= Cat(io.inSwitchConfig(3*i+2), io.inSwitchConfig(3*i+1), io.inSwitchConfig(3*i))
		
		if(i==1){
			io.outPort(i)		:= Mux(io.enqConfigPort_1,io.inConfigData, muxAryInput(i).io.outMuxPort)
			io.enqValidOut(i)	:= Mux(io.enqConfigPort_1,io.enqConfigPort_1, muxAryEnq(i).io.outMuxPort)
				
		}
		else{
			io.outPort(i)		:= muxAryInput(i).io.outMuxPort
			io.enqValidOut(i)	:= muxAryEnq(i).io.outMuxPort
		}
		
		
	
	}
	
	
	
}

	
		
