package HyperCellParams

import Chisel._

object GlobalConfig {
	val memBanks			= 8
 	val memDepth			= 64
	val memWidth			= 32
	val memAddrWidth		= log2Up(memDepth)

	val dataWidth			= 32
	val datawidth			= dataWidth
	
	val memBankCount		= 8
	val fabPortCount		= 20
	
	val extMemAddrWidth		= 32
}
