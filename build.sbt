name := "scrapper"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= {
  val akkaV       = "2.4.16"
  Seq(
    "com.typesafe.akka" % "akka-actor_2.12"                           % akkaV,
    "org.jsoup"         % "jsoup"                                 % "1.8+",
    "commons-validator" % "commons-validator"                     % "1.5+"
  )
}