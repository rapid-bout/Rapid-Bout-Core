import sbt.Keys.scalacOptions

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / scalafixDependencies += "com.sandinh" %% "scala-rewrites" % "1.1.0-M1"
ThisBuild / scalafixDependencies += "net.pixiv" %% "scalafix-pixiv-rule" % "3.0.0"
ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Wunused:imports,locals,patvars",
  "-Ypatmat-exhaust-depth",
  "40"
)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / Test / coverageEnabled := true

target in Compile in doc := baseDirectory.value / "docs/scaladoc"

lazy val root = (project in file("."))
  .settings(
    name := "Rapid Bout Core",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.mockito" % "mockito-core" % "4.9.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.typelevel" %% "cats-core" % "2.8.0"
    )
  )
