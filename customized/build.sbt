organization  := "org.change"

version       := "0.2"

scalaVersion  := "2.11.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

fork := true

libraryDependencies ++= {
  Seq(
    "org.antlr" % "antlr4" % "4.3",
    "commons-io" % "commons-io" % "2.4",
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
    "io.spray" %%  "spray-json" % "1.3.2"
  )
}

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

lazy val sample = taskKey[Unit]("Interpreting")

fullRunTask(sample, Compile, "org.change.v2.runners.experiments.SEFLRunner")

lazy val rewtest = taskKey[Unit]("Interpreting")

fullRunTask(rewtest, Compile, "test.scala.clickfiletoexecutor.IPRewriterTests")

lazy val click = taskKey[Unit]("Symbolically running Template.click")

fullRunTask(click, Compile, "org.change.v2.runners.experiments.TemplateRunner")

lazy val rewrite = taskKey[Unit]("Symbolically running IpRewriter.click")

fullRunTask(rewrite, Compile, "org.change.v2.runners.experiments.IPRewriterRunner")

lazy val click_exampl = taskKey[Unit]("Symbolically running TemplateExampl.click with example generation")

fullRunTask(click_exampl, Compile, "org.change.v2.runners.experiments.TemplateRunnerWithExamples")

lazy val network_click = taskKey[Unit]("Symbolically running network.click ")

fullRunTask(network_click, Compile, "org.change.v2.runners.experiments.NetworkRunner")

lazy val network = taskKey[Unit]("Symbolically running network.click ")

fullRunTask(network, Compile, "org.change.v2.runners.experiments.NetworkRunner")

lazy val network_prova = taskKey[Unit]("Symbolically running NetworkProva.click ")

fullRunTask(network_prova, Compile, "org.change.v2.runners.experiments.NetworkRunnerProva")

lazy val mc = taskKey[Unit]("Running multiple VMs")

fullRunTask(mc, Compile, "org.change.v2.runners.experiments.MultipleVms")

lazy val sefl = taskKey[Unit]("SEFL execution")

fullRunTask(sefl, Compile, "org.change.v2.runners.sefl.SEFLExecutor")

seq(Revolver.settings: _*)
