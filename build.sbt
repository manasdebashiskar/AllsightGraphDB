lazy val root = (project in file(".")).
  settings(commonSettings:_*)
lazy val commonSettings = Seq(
  name:="AllsightGraphDB",
  scalaVersion:= "2.11.11",
  version:= "1.0.0",
  organization := "com.manas",
  fork in Test := true,
//  mainClass in (Compile, packageBin):=Some("AllsightGraphDB"),
  javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.4",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
    //"com.typesafe.akka" %% "akka-typed" % "2.5.4",
    //"com.typesafe.akka" %% "akka-typed-testkit" % "2.5.4" % "test",
    "junit" % "junit" % "4.11" % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.1",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.4",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
     "com.google.guava" % "guava" % "19.0"
    )
  )

addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17" )


