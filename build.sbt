import java.nio.file.Path
import eie.io._
import sbt.KeyRanks
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

val repo = "geometry"
name := repo

val username            = "aaronp"
val scalaThirteen       = "2.13.2"
val defaultScalaVersion = scalaThirteen
val scalaVersions       = Seq(scalaThirteen)

crossScalaVersions := scalaVersions
organization := s"com.github.${username}"
ThisBuild / scalaVersion := defaultScalaVersion
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

enablePlugins(GhpagesPlugin)
enablePlugins(GitVersioning)
enablePlugins(SiteScaladocPlugin)

// see http://scalameta.org/scalafmt/
scalafmtOnCompile in ThisBuild := true

// Define a `Configuration` for each project, as per http://www.scala-sbt.org/sbt-site/api-documentation.html
val Geometry = config("geometryJVM")

git.remoteRepo := s"git@github.com:$username/$repo.git"
ghpagesNoJekyll := true

val typesafeConfig: ModuleID = "com.typesafe"      % "config"  % "1.3.3"
val args4cModule: ModuleID   = "com.github.aaronp" %% "args4c" % "0.6.6"

val logging = List("com.typesafe.scala-logging" %% "scala-logging" % "3.9.2", "ch.qos.logback" % "logback-classic" % "1.2.3")

def testLogging = logging.map(_ % "test")

val monix = List("monix", "monix-execution", "monix-eval", "monix-reactive")

//.map(artifact => "io.circe" %% artifact % "0.11.0")

val circeVersion      = "0.11.0"
val circeDependencies = List("circe-core", "circe-generic", "circe-parser", "circe-optics", "circe-generic-extras")

val testDependencies = List(
  "junit"                  % "junit"      % "4.12"  % "test",
  "org.scalatest"          %% "scalatest" % "3.1.2" % "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0" % "test",
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
  //.enablePlugins(TestNGPlugin)
  .settings(name := "geometry")
  .settings(libraryDependencies ++= monix.map { art =>
    "io.monix" %%% art % "3.1.0"
  })
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
  .jsSettings(
    name := "geometry-js",
    scalaVersion := defaultScalaVersion)
  .jsSettings(libraryDependencies ++= List(
    "com.lihaoyi" %%% "scalatags" % "0.9.1",
    "org.scala-js" %%% "scalajs-dom" % "1.0.0"))

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
