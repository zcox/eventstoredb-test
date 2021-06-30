scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.eventstore" % "db-client-java" % "1.0.0",
   "io.circe" %% "circe-generic" % "0.14.1",
   "io.circe" %% "circe-parser" % "0.14.1",
)
