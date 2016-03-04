scalaVersion:="2.10.5"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-language:reflectiveCalls")
addSbtPlugin("com.github.scct"%"sbt-scct"%"0.2")
libraryDependencies+="edu.berkeley.cs"%%"chisel"%"latest.release"
