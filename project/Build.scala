import sbt._
import sbt.{Build => SbtBuild}
import sbt.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._


object Build extends SbtBuild {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.monifu",
    version := "0.2-SNAPSHOT",
    scalaVersion := "2.10.3",

    scalacOptions ++= Seq(
      "-unchecked", "-deprecation", "-feature", "-Xlint", "-target:jvm-1.6", "-Yinline-warnings"
    ),

    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      Resolver.sonatypeRepo("releases")
    ),

    // -- Settings meant for deployment on oss.sonatype.org

    publishMavenStyle := true,

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false }, // removes optional dependencies
    
    pomExtra :=
      <url>http://www.monifu.org/</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>https://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:alexandru/monifu.git</url>
        <connection>scm:git:git@github.com:alexandru/monifu.git</connection>
      </scm>
      <developers>
        <developer>
          <id>alex_ndc</id>
          <name>Alexandru Nedelcu</name>
          <url>https://www.bionicspirit.com/</url>
        </developer>
      </developers>
  )

  // -- Actual Projects

  lazy val monifu: Project = Project(
    id = "monifu",
    base = file("."),
    settings = buildSettings ++ Seq(
      unmanagedSourceDirectories in Compile <+= sourceDirectory(_ / "shared" / "scala"),
      scalacOptions += "-optimise",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      )
    )
  ).aggregate(monifuJS)

  lazy val monifuJS: Project = Project(
    id = "monifu-js",
    base = file("js"),
    settings = scalaJSSettings ++ buildSettings ++ Seq(
      unmanagedSourceDirectories in Compile <+= sourceDirectory(_ / "shared" / "scala"),
      libraryDependencies ++= Seq(
        "com.lihaoyi" % "utest_2.10" % "0.1.2-JS" % "test"
      ),
      (loadedTestFrameworks in Test) := {
        import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
        (loadedTestFrameworks in Test).value.updated(
          sbt.TestFramework(classOf[utest.runner.JsFramework].getName),
          new utest.runner.JsFramework(environment = (scalaJSEnvironment in Test).value)
        )
      }
    )
  )
}