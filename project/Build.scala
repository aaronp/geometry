object Build {

  val scalacSettings = List(
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
  )
}