package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class fabricConfigureTop(dataWidth : Int, columns : Int, coordWidth: Int, swConfigRegWidth: Int, cuConfigRegWidth: Int) extends Module{

	var n : Int 	= columns
	
	
	val io		= new Bundle{
		val inConfig		= UInt(INPUT, width = dataWidth)
		val inValid		= Bool(INPUT)
		val outConfig		= Vec.fill(n){UInt(OUTPUT, width = dataWidth+1)}
		val outValid		= Vec.fill(n){Bool(OUTPUT)}
		val outRdy		= Vec.fill(n){Bool(INPUT)}
		val rst			= Bool(INPUT)
	}
	
	val fabricConfigureClass	= (0 until n).map(i => {Module(new fabricConfigure(dataWidth, i, coordWidth, swConfigRegWidth, cuConfigRegWidth))})
	for(i<-0 until n){
		fabricConfigureClass(i).io.inConfig		:= io.inConfig
		fabricConfigureClass(i).io.inValid		:= io.inValid
		io.outConfig(i)					:= fabricConfigureClass(i).io.outConfig
		io.outValid(i)					:= fabricConfigureClass(i).io.outValid
		fabricConfigureClass(i).io.outRdy		:= io.outRdy(i)
		fabricConfigureClass(i).io.rst			:= io.rst
		
	}
	
	
}

//object fabricConfigureTopMain {
//    def main(args: Array[String]) {
//    
//    	chiselMain(Array[String]("--backend", "v"),
//	() => Module(new fabricConfigureTop(32, 6, 3, 24, 13)))

//    }
//}
