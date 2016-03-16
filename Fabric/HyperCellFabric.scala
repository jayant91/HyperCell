package HyperCell

import Chisel._
import java.io._
import scala.io.Source


class HyperCellFabric[T <: Data] (gen: T, rows: Int, columns: Int) extends Module{		// m X n Switch mesh, (m-1) x (n-1) CU mesh
	
	var m 			: Int 		= columns
	var n 			: Int 		= rows
	
	var wordWidth		: Int		= 32
	var cuDataWidth		: Int 		= wordWidth
	var cuFifoDepth		: Int		= 2
	var opCodeWidth		: Int		= 5
	var cuPorts		: Int		= 4
	var cuConfigWidth	: Int		= 24	
	
	var coordWidth		: Int		= 3
	var switchPorts		: Int		= 8
	var switchDataWidth	: Int 		= wordWidth+1
	var switchConfigWidth	: Int		= switchPorts * log2Up(switchPorts)
	var switchFifoDepth	: Int		= 2
	
	val io = new Bundle{
		val inData 	= Vec.fill(2*m + 2*n -4){gen.clone.asInput}
		val outData	= Vec.fill(2*m + 2*n -4){gen.clone.asOutput}
		val enqRdy	= Vec.fill(2*m + 2*n -4){UInt(width=1).asInput}	
		val deqValid	= Vec.fill(2*m + 2*n -4){UInt(width=1).asInput}			
		val deqRdy	= Vec.fill(2*m + 2*n -4){UInt(width=1).asOutput}	
		val enqValid	= Vec.fill(2*m + 2*n -4){UInt(width=1).asOutput}
	}
	
	
	val cuClass	= (0 until m-1).map(i => {(0 until n-1).map(j => {Module(new CU[T](gen, cuDataWidth, cuFifoDepth, opCodeWidth, cuPorts, cuConfigWidth))}).toList}).toList
	val switchClass	= (0 until m).map(i => {(0 until n).map(j => {Module(new Switch[T](gen, coordWidth, i, j, switchPorts, switchDataWidth, switchConfigWidth, switchFifoDepth, cuConfigWidth))}).toList}).toList

	
	
	
	
	for(i<-0 until m){
		for(j<-0 until n){
			if(i==0){
				if(j==0){
					//BOTTOM LEFT CORNER
					
					switchClass(i)(j).io.inPort(0)		:=	switchClass(i+1)(j).io.outPort(2)
					switchClass(i)(j).io.inPort(1)		:=	switchClass(i)(j+1).io.outPort(3)
					switchClass(i)(j).io.inPort(3)		:=	io.inData(i)	
					
					io.outData(i)				:= 	switchClass(i)(j).io.outPort(3)
					
					io.deqRdy(i)				:=	switchClass(i)(j).io.enqRdy(3)
					
					     io.enqValid(i)			            := 	    switchClass(i)(j).io.deqValid(3)
					
					switchClass(i)(j).io.enqValid		:=	Cat(UInt(0, width =3), 
											    cuClass(i)(j).io.deqValid,
											    io.deqValid(i),
											    UInt(0, width =1),
											    switchClass(i)(j+1).io.deqValid(3),
											    switchClass(i+1)(j).io.deqValid(2))
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(UInt(0x7, width=3),
					      						    cuClass(i)(j).io.enqRdy,
					      						    io.enqRdy(i),
					      						    UInt(1, width =1),
					      						    switchClass(i)(j+1).io.enqRdy(3),
					      						    switchClass(i+1)(j).io.enqRdy(2))
					      						    
					switchClass(i)(j).io.deqOR		:=	switchClass(i)(j+1).io.enqOR
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(UInt(0x7, width=3),
										    	    cuClass(i)(j).io.enqRdyOut)	
					
				}
				
				else if(j==(n-1)){
					//TOP LEFT CORNER
					
					switchClass(i)(j).io.inPort(0)		:=	switchClass(i+1)(j).io.outPort(2)
					switchClass(i)(j).io.inPort(1)		:=	io.inData(2*m +n -3)
					switchClass(i)(j).io.inPort(3)		:=	switchClass(i)(j-1).io.outPort(1)
					
					io.outData(2*m +n -3)			:= 	switchClass(i)(j).io.outPort(1)	
					
					io.deqRdy(2*m +n -3)			:=	switchClass(i)(j).io.enqRdy(1)
					
					io.enqValid(2*m +n -3)			:= 	switchClass(i)(j).io.deqValid(1)
					
					switchClass(i)(j).io.enqValid		:=	Cat(cuClass(i)(j-1).io.deqValid,
											    UInt(0, width =3), 
											    switchClass(i)(j-1).io.deqValid(1),
											    UInt(0, width =1),
											    io.deqValid(2*m +n -3),
											    switchClass(i+1)(j).io.deqValid(2))
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(cuClass(i)(j-1).io.enqRdy,
											    UInt(0x7, width=3),
											    switchClass(i)(j-1).io.enqRdy(1),
											    UInt(1, width =1),
											    io.enqRdy(2*m +n -3),
											    switchClass(i+1)(j).io.enqRdy(2))
											    
											    
					switchClass(i)(j).io.deqOR		:=	Bool(false)
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(cuClass(i)(j-1).io.enqRdyOut,
										    	    UInt(0x7, width=3))	
				}
				
				else{
					//LEFT EDGE
					
					switchClass(i)(j).io.inPort(0)		:=	switchClass(i+1)(j).io.outPort(2)
					switchClass(i)(j).io.inPort(1)		:=	switchClass(i)(j+1).io.outPort(3)
					switchClass(i)(j).io.inPort(2)		:=	io.inData(2*m + 2*n -4 -j)
					switchClass(i)(j).io.inPort(3)		:=	switchClass(i)(j-1).io.outPort(1)
					
					io.outData(2*m + 2*n -4 - j)		:= 	switchClass(i)(j).io.outPort(2)
					
					io.deqRdy(2*m + 2*n -4 - j)		:=	switchClass(i)(j).io.enqRdy(2)
					
					io.enqValid(2*m + 2*n -4 - j)		:= 	switchClass(i)(j).io.deqValid(2)
					
					switchClass(i)(j).io.enqValid		:=	Cat(cuClass(i)(j-1).io.deqValid,
											    UInt(0, width =2), 
											    cuClass(i)(j).io.deqValid,
											    switchClass(i)(j-1).io.deqValid(1),
											    io.deqValid(2*m + 2*n -4 -j),
											    switchClass(i)(j+1).io.deqValid(3),
											    switchClass(i+1)(j).io.deqValid(2))
													 
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(cuClass(i)(j-1).io.enqRdy,
					                                                    UInt(0x3, width=2),
					                                                    cuClass(i)(j).io.enqRdy,
					                                                    switchClass(i)(j-1).io.enqRdy(1),
					                                                    io.enqRdy(2*m + 2*n -4 -j),
					                                                    switchClass(i)(j+1).io.enqRdy(3),
					                                                    switchClass(i+1)(j).io.enqRdy(2))
					                                                    
					switchClass(i)(j).io.deqOR		:=	switchClass(i)(j+1).io.enqOR
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(cuClass(i)(j-1).io.enqRdyOut,
										    	    UInt(0x3, width=2),
										    	    cuClass(i)(j).io.enqRdyOut)	
					
				}
				
			}
			
			else if(i==(m-1)){
				if(j==0){
					//BOTTOM RIGHT CORNER
					
					switchClass(i)(j).io.inPort(1)		:=	switchClass(i)(j+1).io.outPort(3)
					switchClass(i)(j).io.inPort(2)		:=	switchClass(i-1)(j).io.outPort(0)
					switchClass(i)(j).io.inPort(3)		:=	io.inData(i)	
					
					io.outData(i)				:= 	switchClass(i)(j).io.outPort(3)
					
					io.deqRdy(i)				:=	switchClass(i)(j).io.enqRdy(3)
					
					     io.enqValid(i)			            := 	    switchClass(i)(j).io.deqValid(3)
					
					switchClass(i)(j).io.enqValid		:=	Cat(UInt(0, width =2), 
											    cuClass(i-1)(j).io.deqValid,
											    UInt(0, width =1),
											    io.deqValid(i),
											    switchClass(i-1)(j).io.deqValid(0),
											    switchClass(i)(j+1).io.deqValid(3),
											    UInt(0, width =1))
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(UInt(0x3, width=2),
					                                                    cuClass(i-1)(j).io.enqRdy,
					                                                    UInt(0x1, width=1),
					                                                    io.enqRdy(i),
					                                                    switchClass(i-1)(j).io.enqRdy(0),
					                                                    switchClass(i)(j+1).io.enqRdy(3),
					                                                    UInt(1, width =1))
					                                                    
					switchClass(i)(j).io.deqOR		:=	switchClass(i)(j+1).io.enqOR
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(UInt(0x3, width=2),
										    	    cuClass(i-1)(j).io.enqRdyOut,
										   	    UInt(0x1, width=1))	
				}
				
				else if(j==(n-1)){
					//TOP RIGHT CORNER
					
					switchClass(i)(j).io.inPort(1)		:=	io.inData(m + n -2)
					switchClass(i)(j).io.inPort(2)		:=	switchClass(i-1)(j).io.outPort(0)
					switchClass(i)(j).io.inPort(3)		:=	switchClass(i)(j-1).io.outPort(1)
					
					io.outData(m + n -2)			:= 	switchClass(i)(j).io.outPort(1)	
					
					io.deqRdy(m + n -2)			:=	switchClass(i)(j).io.enqRdy(1)
					
					     io.enqValid(m + n -2)			   := 	    switchClass(i)(j).io.deqValid(1)
					
					switchClass(i)(j).io.enqValid		:=	Cat(UInt(0, width =1),
											    cuClass(i-1)(j-1).io.deqValid,
											    UInt(0, width =2),
											    switchClass(i)(j-1).io.deqValid(1),
											    switchClass(i-1)(j).io.deqValid(0),
											    io.deqValid(m + n -2), 
											    UInt(0, width =1))
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(UInt(0x1, width=1),
					                                                    cuClass(i-1)(j-1).io.enqRdy,
					                                                    UInt(0x3, width=2),
					                                                    switchClass(i)(j-1).io.enqRdy(1),
					                                                    switchClass(i-1)(j).io.enqRdy(0),
					                                                    io.enqRdy(m + n -2),
					                                                    UInt(1, width =1))
					                                     
					switchClass(i)(j).io.deqOR		:=	Bool(false)
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(UInt(0x1, width=1),
										    	    cuClass(i-1)(j-1).io.enqRdyOut,
										    	    UInt(0x3, width=2))	
				}
				
				else{
					//RIGHT EDGE
					
					switchClass(i)(j).io.inPort(0)		:=	io.inData(m + j -1)
					switchClass(i)(j).io.inPort(1)		:=	switchClass(i)(j+1).io.outPort(3)
					switchClass(i)(j).io.inPort(2)		:=	switchClass(i-1)(j).io.outPort(0)
					switchClass(i)(j).io.inPort(3)		:=	switchClass(i)(j-1).io.outPort(1)
					
					io.outData(m + j -1)			:= 	switchClass(i)(j).io.outPort(0)
					
					io.deqRdy(m + j -1)			:=	switchClass(i)(j).io.enqRdy(0)
					io.enqValid(m + j -1)			:= 	switchClass(i)(j).io.deqValid(0)
					
					switchClass(i)(j).io.enqValid		:=	Cat(UInt(0, width =1),
											    cuClass(i-1)(j-1).io.deqValid,
											    cuClass(i-1)(j).io.deqValid,
											    UInt(0, width =1),
											    switchClass(i)(j-1).io.deqValid(1),
											    switchClass(i-1)(j).io.deqValid(0),
											    switchClass(i)(j+1).io.deqValid(3),
											    io.deqValid(m + j -1))
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(UInt(0x1, width=1),
											    cuClass(i-1)(j-1).io.enqRdy,
											    cuClass(i-1)(j).io.enqRdy,
											    UInt(0x1, width=1),
											    switchClass(i)(j-1).io.enqRdy(1),
											    switchClass(i-1)(j).io.enqRdy(0),
											    switchClass(i)(j+1).io.enqRdy(3),
											    io.enqRdy(m + j -1))
											    
											    
					switchClass(i)(j).io.deqOR		:=	switchClass(i)(j+1).io.enqOR
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(UInt(0x1, width=1),
										    	    cuClass(i-1)(j-1).io.enqRdyOut,
										       	    cuClass(i-1)(j).io.enqRdyOut,
										    	    UInt(0x1, width=1))	
				}
				
			}
			
			
			else if(j==0){
				if(i!=0 & i!=(m-1)){
					//BOTTOM EDGE
					
					switchClass(i)(j).io.inPort(0)		:=	switchClass(i+1)(j).io.outPort(2)
					switchClass(i)(j).io.inPort(1)		:=	switchClass(i)(j+1).io.outPort(3)
					switchClass(i)(j).io.inPort(2)		:=	switchClass(i-1)(j).io.outPort(0)
					switchClass(i)(j).io.inPort(3)		:=	io.inData(i)	
					
					io.outData(i)				:= 	switchClass(i)(j).io.outPort(3)	
					
					io.deqRdy(i)				:=	switchClass(i)(j).io.enqRdy(3)
					io.enqValid(i)				:=	switchClass(i)(j).io.deqValid(3)
					
					switchClass(i)(j).io.enqValid		:=	Cat(UInt(0, width =2), 
											    cuClass(i-1)(j).io.deqValid,
											    cuClass(i)(j).io.deqValid,
											    io.deqValid(i), 
											    switchClass(i-1)(j).io.deqValid(0),
											    switchClass(i)(j+1).io.deqValid(3),
											    switchClass(i+1)(j).io.deqValid(2))
													 
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(UInt(0x3, width=2),
					                       				    cuClass(i-1)(j).io.enqRdy,
					                       				    cuClass(i)(j).io.enqRdy,
					                       				    io.enqRdy(i),
					                       				    switchClass(i-1)(j).io.enqRdy(0),
					                       				    switchClass(i)(j+1).io.enqRdy(3),
					                       				    switchClass(i+1)(j).io.enqRdy(2))
					                       				    
					                       				    
					switchClass(i)(j).io.deqOR		:=	switchClass(i)(j+1).io.enqOR
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(UInt(0x3, width=2),
										    	    cuClass(i-1)(j).io.enqRdyOut,
										    	    cuClass(i)(j).io.enqRdyOut)	
				}
				
			}
			
			else if(j==(n-1)){
				if(i!=0 & i!=(m-1)){
					//TOP EDGE
					
					switchClass(i)(j).io.inPort(0)		:=	switchClass(i+1)(j).io.outPort(2)
					switchClass(i)(j).io.inPort(1)		:=	io.inData(2*m + n -3 - i)
					switchClass(i)(j).io.inPort(2)		:=	switchClass(i-1)(j).io.outPort(0)
					switchClass(i)(j).io.inPort(3)		:=	switchClass(i)(j-1).io.outPort(1)	
					
					io.outData(2*m + n -3 - i)		:= 	switchClass(i)(j).io.outPort(1)	
					
					io.deqRdy(2*m + n -3 - i)		:=	switchClass(i)(j).io.enqRdy(1)
					
					io.enqValid(2*m + n -3 - i)	        := 	switchClass(i)(j).io.deqValid(1)
					
					switchClass(i)(j).io.enqValid		:=	Cat(cuClass(i)(j-1).io.deqValid,
											    cuClass(i-1)(j-1).io.deqValid,
											    UInt(0, width =2), 
											    switchClass(i)(j-1).io.deqValid(1),
											    switchClass(i-1)(j).io.deqValid(0),
											    io.deqValid(2*m + n-3 -i),
											    switchClass(i+1)(j).io.deqValid(2))
													 
					switchClass(i)(j).io.deqRdy		:=	Cat(cuClass(i)(j-1).io.enqRdy,
											    cuClass(i-1)(j-1).io.enqRdy,
											    UInt(0x3, width=2), 
											    switchClass(i)(j-1).io.enqRdy(1),
											    switchClass(i-1)(j).io.enqRdy(0),
											    io.enqRdy(2*m + n-3 -i),
											    switchClass(i+1)(j).io.enqRdy(2))
											    
					switchClass(i)(j).io.deqOR		:=	Bool(false)
					
					switchClass(i)(j).io.deqRdyOut		:=	Cat(cuClass(i)(j-1).io.enqRdyOut,
										    	    cuClass(i-1)(j-1).io.enqRdyOut,
										    	    UInt(0x3, width=2))	
					
				}
				
			}
			
			else{
				switchClass(i)(j).io.inPort(0)		:=	switchClass(i+1)(j).io.outPort(2)
				switchClass(i)(j).io.inPort(1)		:=	switchClass(i)(j+1).io.outPort(3)
				switchClass(i)(j).io.inPort(2)		:=	switchClass(i-1)(j).io.outPort(0)
				switchClass(i)(j).io.inPort(3)		:=	switchClass(i)(j-1).io.outPort(1)	
				
				switchClass(i)(j).io.enqValid		:=	Cat(cuClass(i)(j-1).io.deqValid,
				                                                    cuClass(i-1)(j-1).io.deqValid,
				                                                    cuClass(i-1)(j).io.deqValid,
				                                                    cuClass(i)(j).io.deqValid,
				                                                    switchClass(i)(j-1).io.deqValid(1),
				                                                    switchClass(i-1)(j).io.deqValid(0),
				                                                    switchClass(i)(j+1).io.deqValid(3), 
				                                                    switchClass(i+1)(j).io.deqValid(2) )
				
				switchClass(i)(j).io.deqRdy		:=	Cat(cuClass(i)(j-1).io.enqRdy,
										    cuClass(i-1)(j-1).io.enqRdy,
										    cuClass(i-1)(j).io.enqRdy,
										    cuClass(i)(j).io.enqRdy,
										    switchClass(i)(j-1).io.enqRdy(1),
										    switchClass(i-1)(j).io.enqRdy(0),
										    switchClass(i)(j+1).io.enqRdy(3),
										    switchClass(i+1)(j).io.enqRdy(2))	

				switchClass(i)(j).io.deqOR		:=	switchClass(i)(j+1).io.enqOR
				
				switchClass(i)(j).io.deqRdyOut		:=	Cat(cuClass(i)(j-1).io.enqRdyOut,
										    cuClass(i-1)(j-1).io.enqRdyOut,
										    cuClass(i-1)(j).io.enqRdyOut,
										    cuClass(i)(j).io.enqRdyOut)			
	
				
									
			}
			
			
			
		}
	
	}
	
	
	
	
	
	
	for(i<-0 until (m-1)){
		for(j<-0 until (n-1)){
			cuClass(i)(j).io.in(0)			:=	switchClass(i+1)(j+1).io.outPort(6)
			cuClass(i)(j).io.in(1)			:=	switchClass(i)(j+1).io.outPort(7)
			cuClass(i)(j).io.in(2)			:=	switchClass(i)(j).io.outPort(4)
			cuClass(i)(j).io.in(3)			:=	switchClass(i+1)(j).io.outPort(5)
			
			switchClass(i+1)(j+1).io.inPort(6)	:=	cuClass(i)(j).io.out
			switchClass(i)(j+1).io.inPort(7)	:=	cuClass(i)(j).io.out	
			switchClass(i)(j).io.inPort(4)		:=	cuClass(i)(j).io.out
			switchClass(i+1)(j).io.inPort(5)	:=	cuClass(i)(j).io.out
			
			cuClass(i)(j).io.inConfig		:=	switchClass(i)(j).io.outCUConfig
			
			cuClass(i)(j).io.deqRdy			:=	Cat(switchClass(i+1)(j).io.enqRdy(5),     switchClass(i)(j).io.enqRdy(4), 
									    switchClass(i)(j+1).io.enqRdy(7),     switchClass(i+1)(j+1).io.enqRdy(6))
									    
			cuClass(i)(j).io.enqValid		:=	Cat(switchClass(i+1)(j).io.deqValid(5),   switchClass(i)(j).io.deqValid(4), 
									    switchClass(i)(j+1).io.deqValid(7),   switchClass(i+1)(j+1).io.deqValid(6))
									    
			
			
			
			
		
			
		}
	
	}
	
	
	
}


//class HyperCellFabricTest(c: HyperCellFabric[UInt]) extends Tester(c){	
//	val fileName	: String	= "config"
//	
//	var node 	: Int		= 0
//	var nline	: String	= null
//	
//	//val range = scala.math.pow(2, 32).toInt
//	val range : Int = 1000
//	 
//	// FileReader reads text files in the default encoding.
//	val fileReader =  new FileReader(fileName)
//	 	
//	// Always wrap FileReader in BufferedReader.
//	val bufferedReader = new BufferedReader(fileReader)
//        for(i<-0 until 36)
//        {    
//                nline	= bufferedReader.readLine()
//		node	= Integer.parseInt(nline, 10)
//		nline	= bufferedReader.readLine()
//				
//		poke(c.io.inData(node),  java.lang.Long.parseLong(nline, 2))
//		poke(c.io.deqValid(node), 0x1)
//		poke(c.io.enqRdy(node), 0x1)
//		
//		step(1)
//		
//		nline	= bufferedReader.readLine()
//		poke(c.io.inData(node),  java.lang.Long.parseLong(nline, 2))
//		poke(c.io.deqValid(node), 0x1)
//		poke(c.io.enqRdy(node), 0x1)	
//		
//		step(1)
//        
//        }
//        
//        step(5)
//        
//        for(i<-0 until 6){
//        	for(j<-0 until 6){
//        		peek(c.switchClass(i)(j).sConfigReg)
//        		peek(c.switchClass(i)(j).cuConfigReg)
//        	}
//        }
//        
//        for(i<-0 until 5)
//        {
//       		step(1)
//       		var i0  : Int = rnd.nextInt(range)
//       	 	var i1  : Int = rnd.nextInt(range)
//		var i2  : Int = rnd.nextInt(range)
//		var i3  : Int = rnd.nextInt(range)
//		var i5  : Int = rnd.nextInt(range)
//		var i6  : Int = rnd.nextInt(range)
//		var i7  : Int = rnd.nextInt(range)
//		var i8  : Int = rnd.nextInt(range)
//		var i9  : Int = rnd.nextInt(range)
//		var i10 : Int = rnd.nextInt(range)
//		var i11 : Int = rnd.nextInt(range)
//		var i12 : Int = rnd.nextInt(range)
//		var i13 : Int = rnd.nextInt(range)
//		var i14 : Int = rnd.nextInt(range)
//		var i15 : Int = rnd.nextInt(range)
//		var i16 : Int = rnd.nextInt(range)
//		var i17 : Int = rnd.nextInt(range)
//		var i18 : Int = rnd.nextInt(range)
//		var i19 : Int = rnd.nextInt(range)


//		poke(c.io.inData(0),  i0)
//		poke(c.io.deqValid(0), 0x1)
//		poke(c.io.enqRdy(0), 0x1)
//		poke(c.io.inData(1),  i1)
//		poke(c.io.deqValid(1), 0x1)
//		poke(c.io.enqRdy(1), 0x1)
//		poke(c.io.inData(2),  i2)
//		poke(c.io.deqValid(2), 0x1)
//		poke(c.io.enqRdy(2), 0x1)
//		poke(c.io.inData(3),  i3)
//		poke(c.io.deqValid(3), 0x1)
//		poke(c.io.enqRdy(3), 0x1)
//		poke(c.io.inData(5),  i5)
//		poke(c.io.deqValid(5), 0x1)
//		poke(c.io.enqRdy(5), 0x1)
//		poke(c.io.inData(6),  i6)
//		poke(c.io.deqValid(6), 0x1)
//		poke(c.io.enqRdy(6), 0x1)
//		poke(c.io.inData(7),  i7)
//		poke(c.io.deqValid(7), 0x1)
//		poke(c.io.enqRdy(7), 0x1)
//		poke(c.io.inData(8),  i8)
//		poke(c.io.deqValid(8), 0x1)
//		poke(c.io.enqRdy(8), 0x1)
//		poke(c.io.inData(9),  i9)
//		poke(c.io.deqValid(9), 0x1)
//		poke(c.io.enqRdy(9), 0x1)
//		poke(c.io.inData(10),  i10)
//		poke(c.io.deqValid(10), 0x1)
//		poke(c.io.enqRdy(10), 0x1)
//		poke(c.io.inData(11),  i11)
//		poke(c.io.deqValid(11), 0x1)
//		poke(c.io.enqRdy(11), 0x1)
//		poke(c.io.inData(12),  i12)
//		poke(c.io.deqValid(12), 0x1)
//		poke(c.io.enqRdy(12), 0x1)
//		poke(c.io.inData(13),  i13)
//		poke(c.io.deqValid(13), 0x1)
//		poke(c.io.enqRdy(13), 0x1)
//		poke(c.io.inData(14),  i14)
//		poke(c.io.deqValid(14), 0x1)
//		poke(c.io.enqRdy(14), 0x1)
//		poke(c.io.inData(15),  i15)
//		poke(c.io.deqValid(15), 0x1)
//		poke(c.io.enqRdy(15), 0x1)
//		poke(c.io.inData(16),  i16)
//		poke(c.io.deqValid(16), 0x1)
//		poke(c.io.enqRdy(16), 0x1)
//		poke(c.io.inData(17),  i17)
//		poke(c.io.deqValid(17), 0x1)
//		poke(c.io.enqRdy(17), 0x1)
//		poke(c.io.inData(18),  i18)
//		poke(c.io.deqValid(18), 0x1)
//		poke(c.io.enqRdy(18), 0x1)
//		poke(c.io.inData(19),  i19)
//		poke(c.io.deqValid(19), 0x1)
//		poke(c.io.enqRdy(19), 0x1)

//	
//	
//	
//	}	
//	
//      
//        // Always close files.
//        bufferedReader.close();         

//}		
		

//object HyperCellFabricMain {
//    def main(args: Array[String]) {
//	chiselMainTest(Array[String]("--backend", "v",  "--debug",  "--test",  "--genHarness"),
//	() => Module(new HyperCellFabric[UInt](UInt(width=33), 6 ,6))){c => new HyperCellFabricTest(c)}
//    }
//}



//object HyperCellFabricMain {
//    def main(args: Array[String]) {
//	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new HyperCellFabric[UInt](UInt(width=33), 6 ,6)))
//    }
//}
