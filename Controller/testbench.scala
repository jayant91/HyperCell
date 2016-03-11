package HyperCell

import Chisel._

class controllerTopTest(c: controlllerTop) extends Module{
	
}

object HyperCellFabricMain {
    def main(args: Array[String]) {
	chiselMain(Array[String]("--backend", "v"),
	() => Module(new HyperCellFabric[UInt](UInt(width=33), 6 ,6)))
    }
}
