import sbt._

object Dependencies {

  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-http"   % "10.1.7",
    "com.typesafe.akka" %% "akka-stream" % "2.5.21",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
    "net.virtual-void" %%  "json-lenses" % "0.6.2",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test
  )

  lazy val scalaSTM = Seq("org.scala-stm" %% "scala-stm" % "0.8")

  lazy val logging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.1.0" % Test
}
