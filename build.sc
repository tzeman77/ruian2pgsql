/*
 * REPL:
 *
 * ./mill --repl -w
 *
 * Generate Idea project:
 *
 * ./mill mill.scalalib.GenIdea/idea
 *
 */ 

import mill._
import mill.api.Loose
import mill.define.{Command, Sources}
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
import mill.scalalib.publish._
import upickle.default._

object V {
  val app = "0.3-SNAPSHOT"
  val scala213 = "2.13.6"
  val scalaJs = "1.5.1"
}

object D {
  val configAnnotation = ivy"com.wacai::config-annotation:0.4.2"
  val quill = ivy"io.getquill::quill-jasync-postgres:3.11.0"
  val upickle = ivy"com.lihaoyi::upickle::1.4.0"
}

val compilerOptions = Seq(
  "-Xlint:deprecation",      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",      // Specify character encoding used by source files.
  "-explaintypes",           // Explain type errors in more detail.
  "-feature",                // Emit warning and location for usages of features that should be imported explicitly.
  "-language:higherKinds",   // Allow higher-kinded types
  "-unchecked",              // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",             // Wrap field accessors to throw an exception on uninitialized access.
  "-target:jvm-1.8",
  "-Ymacro-annotations",
  "-Wdead-code",             // Warn when dead code is identified.
  "-Wextra-implicit",        // Warn when more than one implicit parameter section is defined.
  "-Wnumeric-widen",         // Warn when numerics are widened.
  "-Wunused:imports",        // Warn if an import selector is not referenced.
  "-Wunused:locals",         // Warn if a local definition is unused.
  "-Wunused:params",         // Warn if a value parameter is unused.
  "-Wunused:patvars",        // Warn if a variable bound in a pattern is unused.
  "-Wunused:privates",       // Warn if a private member is unused.
  "-Wvalue-discard"          // Warn when non-Unit expression results are unused.
)

trait Common extends ScalaModule with PublishModule {

  def pomSettings: T[PomSettings] = PomSettings(
    description = "RUIAN data model for Scala",
    organization = "cz.functionals",
    url = "https://fossil.functionals.cz/ruian2pgsql",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl(),
    developers = Seq(
      Developer("tzeman", "Tomas Zeman", "https://functionals.cz")
    )
  )

  override def scalaVersion: T[String] = V.scala213

  override def artifactName: T[String] =
    s"ruian4s-${super.artifactName()}".stripSuffix("-jvm").stripSuffix("-js")

  override def publishVersion: T[String] = V.app

  override def scalacOptions = T{compilerOptions}

  override def sources: Sources = T.sources {
    super.sources() :+ PathRef(millSourcePath / 'shared)
  }

}

object model extends Module {

  object jvm extends Common {
    override def ivyDeps: T[Loose.Agg[Dep]] = Agg(D.upickle)
  }

  object js extends Common with ScalaJSModule {
    override def ivyDeps: T[Loose.Agg[Dep]] = Agg(D.upickle)
    override def scalaJSVersion: T[String] = V.scalaJs
  }

}

object dba extends Common {
  override def moduleDeps = Seq(model.jvm)
  override def ivyDeps: T[Loose.Agg[Dep]] = Agg(D.configAnnotation, D.quill)
  override def scalacOptions = T {
    super.scalacOptions.map(_ :+
      s"-Xmacro-settings:conf.output.dir=${millSourcePath / 'resources}"
    )
  }
}

def compileAll(): Command[Unit] = T.command{
  model.jvm.compile()
  model.js.compile()
  dba.compile()
  ()
}

def publishLocal(): Command[Unit] = T.command{
  model.jvm.publishLocal()()
  model.js.publishLocal()()
  dba.publishLocal()()
}

def publishM2Local(p: os.Path): Command[Unit] = T.command{
  model.jvm.publishM2Local(p.toString)()
  model.js.publishM2Local(p.toString)()
  dba.publishM2Local(p.toString)()
  ()
}

// vim: et ts=2 sw=2 syn=scala
