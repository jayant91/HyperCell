package HyperCell

import Chisel._
import java.io._
import scala.io.Source

class controllerTopTest(c: controllerTop) extends Tester(c){
	val fileName	: String	="Config_1.txt"
	
	var config	: Long 		= 0
	var nline 	: String	= null
	
	val fileReader			= new FileReader(fileName)
	
	val bufferedReader 		= new BufferedReader(fileReader)
	
	for(i<-0 until 50){
		nline	= bufferedReader.readLine()
		config	= java.lang.Long.parseLong(nline, 16)  
		
		poke(c.io.inConfig, config)
		poke(c.io.inValid, 0x1)
		poke(c.io.loadRqstRdy, 0x1)
		poke(c.io.loadResp, 0x4535)
		poke(c.io.loadRespValid, 0x0)
		poke(c.io.storeMemRdy, 0x1)
		
		for(i<-0 until 20){
			poke(c.io.fabInRdy(i), 0x1)
			poke(c.io.fabOutValid(i), 0x1)
			poke(c.io.fabOut(i), 0xa432)
		}
		
		
		step(1)
	}
	
	bufferedReader.close()
}

object controllerTopMain {
    def main(args: Array[String]) {
	chiselMainTest(Array[String]("--backend", "c", "--genHarness",  "--debug", "--compile", "--test"),
	() => Module(new controllerTop())){c => new controllerTopTest(c)}
    }
}
