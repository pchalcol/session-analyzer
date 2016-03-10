organization  := "com.chaltec.web"

name          := "session-analyzer-spray"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "sprest snapshots" at "http://markschaake.github.com/releases"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= {
  val akkaV = "2.2.4"
  val sprayV = "1.2.1"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.play"   %%  "play-json"     % "2.2.3",
    "org.reactivemongo"   %% "play2-reactivemongo" % "0.10.2",
    "org.reactivemongo"   %%  "reactivemongo" % "0.10.0",
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test"
  )
}

Revolver.settings

atmosSettings

atmosTestSettings

