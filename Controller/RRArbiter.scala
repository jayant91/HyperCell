package HyperCell

import Chisel._
import HyperCellParams.GlobalConfig._

class RRArbiter(inputCount : Int) extends Module{
	val io 		= new Bundle{
		val request 		= Vec.fill(inputCount){Bool(INPUT)}
		val grant		= Vec.fill(inputCount){Bool(OUTPUT)}
	}
	
	val pointerReg		= Vec.fill(inputCount){Reg(init = Bool(false))}
	
	val requestMasked	= Vec.fill(inputCount){Bool()}
	val anyReqMasked	= Bool()
	val requestVec		= Vec.fill(inputCount){Bool()}
	val requestAny		= Bool()
	val noReqMasked		= Bool()
	val grantMasked		= Vec.fill(inputCount){Bool()}
	val grantUnMasked	= Vec.fill(inputCount){Bool()}
	val grant		= Vec.fill(inputCount){Bool()}
	
	val maskHihgerReq	= Vec.fill(inputCount){Bool()}
	val unMaskHigherReq	= Vec.fill(inputCount){Bool()}
	
	
	requestVec		:= io.request
	requestAny		:= requestVec.fold(Bool(false))(_||_)

	for(i<-0 until inputCount){
		requestMasked(i)		:= requestVec(i) && pointerReg(i)
	}
	
	anyReqMasked		:= requestMasked.fold(Bool(false))(_||_)
	noReqMasked		:= ~anyReqMasked
	
	for(i<-1 until inputCount){
		maskHihgerReq(i)	:= maskHihgerReq(i-1) || requestMasked(i-1)
		unMaskHigherReq(i)	:= unMaskHigherReq(i-1) || requestVec(i-1)
	}
	
	for(i<-0 until inputCount){
		grantMasked(i)		:= requestMasked(i) && (~maskHihgerReq(i))
		grantUnMasked(i)	:= requestVec(i) && (~unMaskHigherReq(i))
	}
	
	for(i<-0 until inputCount){
		grant(i)		:= (noReqMasked && grantUnMasked(i)) || grantMasked(i)
	}
	
	when(anyReqMasked){
		pointerReg		:= maskHihgerReq
	}
	.elsewhen(requestAny){
		pointerReg		:= unMaskHigherReq
	}

	for(i<-0 until inputCount){
		io.grant(i)		:= grant(i)
	}	
	
}

//class RRArbiterTest(c: RRArbiter) extends Tester(c){	
//      		poke(c.io.request(0), 0x0)
//      		poke(c.io.request(1), 0x1)
//      		poke(c.io.request(2), 0x1)
//      		poke(c.io.request(3), 0x0)
//      		poke(c.io.request(4), 0x1)
//      		poke(c.io.request(5), 0x0)
//      		poke(c.io.request(6), 0x0)
//      		poke(c.io.request(7), 0x1)
//      		poke(c.io.request(8), 0x0)
//      		poke(c.io.request(9), 0x0)  
//      		for(i<-0 until 10){
//      			peek(c.io.grant(i))
//      		} 
//      		for(i<-0 until 10){
//      			peek(c.requestMasked(i))
//      		}
//      		
//      		peek(c.requestAny)
//      		peek(c.anyReqMasked)
//      		peek(c.noReqMasked)
//      		
//      		for(i<-0 until 10){
//      			peek(c.maskHihgerReq(i))
//      		}
//      		for(i<-0 until 10){
//      			peek(c.unMaskHigherReq(i))
//      		}
//      		for(i<-0 until 10){
//      			peek(c.grantMasked(i))
//      		}
//      		for(i<-0 until 10){
//      			peek(c.grantUnMasked(i))
//      		}
//      		
//      		
//      		step(1)
//      		expect(c.io.grant(1), 0x1)
//		poke(c.io.request(0), 0x1)
//      		poke(c.io.request(1), 0x0)
//      		poke(c.io.request(2), 0x0)
//      		poke(c.io.request(3), 0x0)
//      		poke(c.io.request(4), 0x0)
//      		poke(c.io.request(5), 0x1)
//      		poke(c.io.request(6), 0x0)
//      		poke(c.io.request(7), 0x1)
//      		poke(c.io.request(8), 0x0)
//      		poke(c.io.request(9), 0x0)
//      		for(i<-0 until 10){
//      			peek(c.io.grant(i))
//      		} 
//      		
//      		
//      		step(1)
//      		expect(c.io.grant(5), 0x1)
//		poke(c.io.request(0), 0x0)
//      		poke(c.io.request(1), 0x0)
//      		poke(c.io.request(2), 0x1)
//      		poke(c.io.request(3), 0x1)
//      		poke(c.io.request(4), 0x0)
//      		poke(c.io.request(5), 0x0)
//      		poke(c.io.request(6), 0x0)
//      		poke(c.io.request(7), 0x0)
//      		poke(c.io.request(8), 0x0)
//      		poke(c.io.request(9), 0x0)
//      		for(i<-0 until 10){
//      			peek(c.io.grant(i))
//      		} 
//      		
//      		step(1)
//      		expect(c.io.grant(2), 0x1)
//      		for(i<-0 until 10){
//      			peek(c.requestVec(i))
//      			peek(c.io.grant(i))
//      		} 
//}		
//		

//object RRArbiterMain {
//    def main(args: Array[String]) {
//	chiselMainTest(Array[String]("--backend", "c",  "--compile", "--debug",  "--test",  "--genHarness"),
//	() => Module(new RRArbiter(10))){c => new RRArbiterTest(c)}
//    }
//}

