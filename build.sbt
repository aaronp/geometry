import java.nio.file.Path
import eie.io._
import sbt.KeyRanks
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

val repo = "geometry"
name := repo

val username            = "aaronp"
val scalaTwelve         = "2.12.10"
val scalaThirteen       = "2.13.0"
val defaultScalaVersion = scalaTwelve
val scalaVersions       = Seq(scalaTwelve) //, scalaThirteen)

crossScalaVersions := scalaVersions
organization := s"com.github.${username}"
scalaVersion := defaultScalaVersion
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// see https://github.com/sbt/sbt-ghpages
// this exposes the 'ghpagesPushSite' task
enablePlugins(GhpagesPlugin)
enablePlugins(GitVersioning)
//enablePlugins(PamfletPlugin)
enablePlugins(SiteScaladocPlugin)

// see http://scalameta.org/scalafmt/
scalafmtOnCompile in ThisBuild := true
scalafmtVersion in ThisBuild := "1.4.0"

// Define a `Configuration` for each project, as per http://www.scala-sbt.org/sbt-site/api-documentation.html
val Geometry = config("geometryJVM")

git.remoteRepo := s"git@github.com:$username/$repo.git"
ghpagesNoJekyll := true

val typesafeConfig: ModuleID = "com.typesafe"      % "config"  % "1.3.3"
val args4cModule: ModuleID   = "com.github.aaronp" %% "args4c" % "0.6.6"

val logging = List("com.typesafe.scala-logging" %% "scala-logging" % "3.9.2", "ch.qos.logback" % "logback-classic" % "1.2.3")

def testLogging = logging.map(_ % "test")

val monix = List("monix", "monix-execution", "monix-eval", "monix-reactive", "monix-tail")

val monixDependencies = monix.map { art =>
  "io.monix" %% art % "3.0.0-RC2"
}

//.map(artifact => "io.circe" %% artifact % "0.11.0")

val circeVersion      = "0.11.0"
val circeDependencies = List("circe-core", "circe-generic", "circe-parser", "circe-optics", "circe-generic-extras")

val testDependencies = List(
  "junit"                  % "junit"      % "4.12"  % "test",
  "org.scalatest"          %% "scalatest" % "3.0.7" % "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.1.1" % "test",
  "org.pegdown"            % "pegdown"    % "1.6.0" % "test"
)

lazy val scaladocSiteProjects = List(
  (geometryJVM, Geometry)
)

lazy val scaladocSiteSettings = scaladocSiteProjects.flatMap {
  case (project: Project, conf) =>
    SiteScaladocPlugin.scaladocSettings(
      conf,
      mappings in (Compile, packageDoc) in project,
      s"api/${project.id}"
    )
  case _ => Nil // ignore cross-projects
}

lazy val settings = scalafmtSettings

def additionalScalcSettings = List(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-unchecked",
  //  "-explaintypes", // Explain type errors in more detail.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match", // Pattern match may not be typesafe.
  "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",     // Warn when nullary methods return Unit.
  //  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

val baseScalacSettings = List(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:reflectiveCalls", // Allow reflective calls
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",
  "-language:reflectiveCalls", // Allow reflective calls
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  //"-Xlog-implicits",
  "-Xfuture" // Turn on future language features.
)

val scalacSettings = baseScalacSettings

val commonSettings: Seq[Def.Setting[_]] = Seq(
  //version := parentProject.settings.ver.value,
  organization := s"com.github.${username}",
  scalaVersion := defaultScalaVersion,
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  autoAPIMappings := true,
  exportJars := false,
  crossScalaVersions := scalaVersions,
  libraryDependencies ++= testDependencies,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= scalacSettings,
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := s"${repo}.build",
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case str if str.contains("application.conf") => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
  // see http://www.scalatest.org/user_guide/using_scalatest_with_sbt
  //(testOptions in Test) += (Tests.Argument(TestFrameworks.ScalaTest, "-h", s"target/scalatest-reports-${name.value}", "-oN"))
)

test in assembly := {}

// don't publish the root artifact
publishArtifact := false

publishMavenStyle := true

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(SiteScaladocPlugin)
  .enablePlugins(ParadoxPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(
    geometryJVM,
    geometryJS
  )
  .settings(scaladocSiteSettings)
  .settings(
    paradoxProperties += ("project.url" -> "https://aaronp.github.io/geometry/docs/current/"),
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    siteSourceDirectory := target.value / "paradox" / "site" / "main",
    siteSubdirName in ScalaUnidoc := "api/latest",
    publish := {},
    publishLocal := {}
  )

lazy val geometry = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .withoutSuffixFor(JVMPlatform)
  .enablePlugins(TestNGPlugin)
  .settings(name := "geometry")
  .in(file("geometry"))
  .jvmSettings(commonSettings: _*)
  .jvmSettings(
    name := "geometry-jvm",
    coverageMinimum := 85,
    coverageFailOnMinimum := true,
    libraryDependencies ++= testLogging ++ testDependencies,
    // put scaladocs under 'api/latest'
    siteSubdirName in SiteScaladoc := "api/latest"
  )
  .jsSettings(name := "geometry-js")
  .jsSettings(libraryDependencies ++= List("com.lihaoyi" %%% "scalatags" % "0.7.0", "org.scala-js" %%% "scalajs-dom" % "0.9.7"))

lazy val geometryJVM = geometry.jvm
lazy val geometryJS  = geometry.js

// see https://leonard.io/blog/2017/01/an-in-depth-guide-to-deploying-to-maven-central/
pomIncludeRepository := (_ => false)

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
  <url>https://github.com/${username}/${repo}
  </url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>${username}</id>
        <name>Aaron Pritzlaff</name>
        <url>https://github.com/${username}/${repo}
        </url>
      </developer>
    </developers>
}
