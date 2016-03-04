import scala.io.Source
import java.io._

object read {
	def main(args: Array[String]) {
		val configFile = new PrintWriter(new File("config" ))
		for(line <- Source.fromFile("myConfig2.txt").getLines()){
			if(line.startsWith("{")){
				
	
				var z: Array[String] = line.split(",")
				
				
				print( (z(16).mkString(""))(0).asDigit)
				
				
				//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				
				var xCoord : Int = (z(0).mkString(""))(1).asDigit
				configFile.println(xCoord)
				
				
				
				////////////////////////////////////////////////////////////////////////////
				configFile.print("1")
				
				
				////////////////////////////////////////////////////////////////////////////
				var yCoord : Int = (z(1).mkString(""))(0).asDigit
				
				if(yCoord == 0){
					configFile.print("000")
				}
				else if(yCoord == 1){
					configFile.print("001")
				}
				else if(yCoord == 2){
					configFile.print("010")
				}
				else if(yCoord == 3){
					configFile.print("011")
				}
				else if(yCoord == 4){
					configFile.print("100")
				}
				else if(yCoord == 5){
					configFile.print("101")
				}
				
//				//SWITCH CONFIGURATION
				configFile.print("10000")
			
//				var config00 : Char	= (z(2).mkString(""))(1)
//				print(config00)
//				if(z(2).length>2){
//					var config01 : Char	= (z(2).mkString(""))(2)
//					print(config01)
//				}
//				
//				
//				var config10 : Char	= (z(3).mkString(""))(0)
//				print(config10)
//				if(z(3).length>1){
//					var config11 : Char	= (z(3).mkString(""))(1)
//					print(config11)
//				}
//				
//				
//				var config20 : Char	= (z(4).mkString(""))(0)
//				print(config20)
//				if(z(4).length>1){
//					var config21 : Char	= (z(4).mkString(""))(1)
//					print(config21)
//				}
//				
//				
//				var config30 : Char	= (z(5).mkString(""))(0)
//				print(config30)
//				if(z(5).length>1){
//					var config31 : Char	= (z(5).mkString(""))(1)
//					print(config31)
//				}
//				
//				
//				var config40 : Char	= (z(6).mkString(""))(0)
//				print(config40)
//				if(z(6).length>1){
//					var config41 : Char	= (z(6).mkString(""))(1)
//					print(config41)
//				}
//				
//			
//				var config50 : Char	= (z(7).mkString(""))(0)
//				print(config50)
//				if(z(7).length>1){
//					var config51 : Char	= (z(7).mkString(""))(1)
//					print(config51)
//				}
//				
//				
//				var config60 : Char	= (z(8).mkString(""))(0)
//				print(config60)
//				if(z(8).length>1){
//					var config61 : Char	= (z(8).mkString(""))(1)
//					print(config61)
//				}
//				
//				var config70 : Char	= (z(9).mkString(""))(0)
//				print(config60)
//				if(z(8).length>2){
//					var config71 : Char	= (z(9).mkString(""))(1)
//					print(config71)
//				}
//				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg70 : Char	= (z(9).mkString(""))(0)
				var configReg71 : Char	= 'X'
				if(z(9).length>2){
					configReg71 = (z(9).mkString(""))(1)
				}
			
				
				if(configReg70 == 'X'){
					configFile.print("111")
				}
				else if(configReg70 == 'E'){
					configFile.print("000")
				}
				else if(configReg70 == 'W'){
					configFile.print("010")
				}
				else if(configReg70 == 'N'){
					if(z(9).length>2){
						if(configReg71 == 'E'){
							configFile.print("100")
						}
						else if(configReg71 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg70 == 'S'){
					if(z(9).length>2){
						if(configReg71 == 'E'){
							configFile.print("111")
						}
						else if(configReg71 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				
				
				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg60 : Char	= (z(8).mkString(""))(0)
				var configReg61 : Char  = 'X'
				if(z(8).length>1){
					configReg61	= (z(8).mkString(""))(1)
				}
			
				
				if(configReg60 == 'X'){
					configFile.print("110")
				}
				else if(configReg60 == 'E'){
					configFile.print("000")
				}
				else if(configReg60 == 'W'){
					configFile.print("010")
				}
				else if(configReg60 == 'N'){
					if(z(8).length>1){
						if(configReg61 == 'E'){
							configFile.print("100")
						}
						else if(configReg61 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg60 == 'S'){
					if(z(8).length>1){
						if(configReg61 == 'E'){
							configFile.print("111")
						}
						else if(configReg61 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg50 : Char	= (z(7).mkString(""))(0)
				var configReg51 : Char  = 'X'
				if(z(7).length>1){
					configReg51	= (z(7).mkString(""))(1)
				}
			
				
				if(configReg50 == 'X'){
					configFile.print("101")
				}
				else if(configReg50 == 'E'){
					configFile.print("000")
				}
				else if(configReg50 == 'W'){
					configFile.print("010")
				}
				else if(configReg50 == 'N'){
					if(z(7).length>1){
						if(configReg51 == 'E'){
							configFile.print("100")
						}
						else if(configReg51 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg50 == 'S'){
					if(z(7).length>1){
						if(configReg51 == 'E'){
							configFile.print("111")
						}
						else if(configReg51 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg40 : Char	= (z(6).mkString(""))(0)
				var configReg41 : Char  = 'X'
				if(z(6).length>1){
					configReg41	= (z(6).mkString(""))(1)
				}
			
				
				if(configReg40 == 'X'){
					configFile.print("100")
				}
				else if(configReg40 == 'E'){
					configFile.print("000")
				}
				else if(configReg40 == 'W'){
					configFile.print("010")
				}
				else if(configReg40 == 'N'){
					if(z(6).length>1){
						if(configReg41 == 'E'){
							configFile.print("100")
						}
						else if(configReg41 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg40 == 'S'){
					
					if(z(6).length>1){
						if(configReg41 == 'E'){
							configFile.print("111")
						}
						else if(configReg41 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg30 : Char	= (z(5).mkString(""))(0)
				var configReg31 : Char  = 'X'
				if(z(5).length>1){
					configReg31	= (z(5).mkString(""))(1)
				}
			
				
				if(configReg30 == 'X'){
					configFile.print("011")
				}
				else if(configReg30 == 'E'){
					configFile.print("000")
				}
				else if(configReg30 == 'W'){
					configFile.print("010")
				}
				else if(configReg30 == 'N'){
					if(z(5).length>1){
						if(configReg31 == 'E'){
							configFile.print("100")
						}
						else if(configReg31 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg30 == 'S'){
					
					if(z(5).length>1){
						if(configReg31 == 'E'){
							configFile.print("111")
						}
						else if(configReg31 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
						
					}
						
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg20 : Char	= (z(4).mkString(""))(0)
				var configReg21 : Char  = 'X'
				if(z(4).length>1){
					configReg21	= (z(4).mkString(""))(1)
				}
			
				
				if(configReg20 == 'X'){
					configFile.print("010")
				}
				else if(configReg20 == 'E'){
					configFile.print("000")
				}
				else if(configReg20 == 'W'){
					configFile.print("010")
				}
				else if(configReg20 == 'N'){
					if(z(4).length>1){
						if(configReg21 == 'E'){
							configFile.print("100")
						}
						else if(configReg21 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg20 == 'S'){
					if(z(4).length>1){
						if(configReg21 == 'E'){
							configFile.print("111")
						}
						else if(configReg21 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg10 : Char	= (z(3).mkString(""))(0)
				var configReg11 : Char  = 'X'
				if(z(3).length>1){
					configReg11	= (z(3).mkString(""))(1)
				}
			
				
				if(configReg10 == 'X'){
					configFile.print("001")
				}
				else if(configReg10 == 'E'){
					configFile.print("000")
				}
				else if(configReg10 == 'W'){
					configFile.print("010")
				}
				else if(configReg10 == 'N'){
					if(z(3).length>1){
						if(configReg11 == 'E'){
							configFile.print("100")
						}
						else if(configReg11 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg10 == 'S'){
					if(z(3).length>1){
						if(configReg11 == 'E'){
							configFile.print("111")
						}
						else if(configReg11 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				var configReg00 : Char	= (z(2).mkString(""))(1)
				var configReg01 : Char  = 'X'
				if(z(2).length>2){
					configReg01	= (z(2).mkString(""))(2)
				}
			
				
				if(configReg00 == 'X'){
					configFile.print("000")
				}
				else if(configReg00 == 'E'){
					configFile.print("000")
				}
				else if(configReg00 == 'W'){
					configFile.print("010")
				}
				else if(configReg00 == 'N'){
					if(z(2).length>2){
						if(configReg01 == 'E'){
							configFile.print("100")
						}
						else if(configReg01 == 'W'){
							configFile.print("101")
						}
					}
					else{
						configFile.print("001")
					}
					
				}
				else if(configReg00 == 'S'){
					if(z(2).length>2){
						if(configReg01 == 'E'){
							configFile.print("111")
						}
						else if(configReg01 == 'W'){
							configFile.print("110")
						}
					}
					else{
						configFile.print("011")
					}
						
				}
				//////////////////////////////////////////////////////////////////////////////////////////
				
				configFile.println("")
			
				//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				
				
				////////////////////////////////////////////////////////////////////////////
				configFile.print("1")
				
				
				////////////////////////////////////////////////////////////////////////////
				
				if(yCoord == 0){
					configFile.print("000")
				}
				else if(yCoord == 1){
					configFile.print("001")
				}
				else if(yCoord == 2){
					configFile.print("010")
				}
				else if(yCoord == 3){
					configFile.print("011")
				}
				else if(yCoord == 4){
					configFile.print("100")
				}
				else if(yCoord == 5){
					configFile.print("101")
				}
				
				//SWITCH CONFIGURATION
				configFile.print("00000")
				configFile.print("00000000000")
				configFile.print(z(13) + z(14)+z(15) + (z(16).mkString(""))(0).asDigit)
				
						
				
				//////////////////////////////////////////////////////////////////////////////////
				var operand20 : Char = (z(12).mkString(""))(0)
				var operand21 : Char = 'X'
				if(z(12).length>1){
					operand21 = (z(12).mkString(""))(1)
				}
				
				if(operand20 == 'X'){
					configFile.print("00")
				}
				else if(operand20 == 'N'){
					if(z(12).length>1){
						if(operand21 == 'E'){
							configFile.print("00")
						}
						else if(operand21 == 'W'){
							configFile.print("01")
						}
					}
					
				}
				else if(operand20 == 'S'){
					if(z(12).length>1){
						if(operand21 == 'E'){
							configFile.print("11")
						}
						else if(operand21 == 'W'){
							configFile.print("10")
						}
					}
				}
				
				/////////////////////////////////////////////////////////////////////////////////////////////
				var operand10 : Char = (z(11).mkString(""))(0)
				var operand11 : Char = 'X'
				if(z(11).length>1){
					operand11	= (z(11).mkString(""))(1)
				}
				if(operand10 == 'X'){
					configFile.print("00")
				}
				else if(operand10 == 'N'){
					if(z(11).length>1){
						if(operand11 == 'E'){
							configFile.print("00")
						}
						else if(operand11 == 'W'){
							configFile.print("01")
						}
					}
					
				}
				else if(operand10 == 'S'){
					if(z(11).length>1){
						if(operand11 == 'E'){
							configFile.print("11")
						}
						else if(operand11 == 'W'){
							configFile.print("10")
						}
					}
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////	
				
				
				var operation : String = z(10)
				if(z(10)=="ADD"){
					configFile.print("00000")
				}
				else if(z(10) =="SUB"){
					configFile.print("00001")
				}
				else if(z(10) =="MUL"){
					configFile.print("00010")
				}
				else if(z(10) =="AND"){
					configFile.print("00011")
				}
				else if(z(10) =="OR"){
					configFile.print("00100")
				}
				else if(z(10) =="XOR"){
					configFile.print("00101")
				}
				else if(z(10) =="NOT"){
					configFile.print("00110")
				}
				else if(z(10) =="SLL"){
					configFile.print("00111")
				}
				else if(z(10) =="SRL"){
					configFile.print("01000")
				}
				else if(z(10) =="SRA"){
					configFile.print("01001")
				}
				else if(z(10) =="ROR"){
					configFile.print("01010")
				}
				
				else if(z(10) =="ROL"){
					configFile.print("01011")
				}
				
				else if(z(10) =="MIN"){
					configFile.print("01101")
				}
				
				else if(z(10) =="MAX"){
					configFile.print("01100")
				}
				else{
					configFile.print("01111")
				}
				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////			
				println("")
				configFile.println("")
     			}
		}
	
		configFile.close()
			
   	}
}

