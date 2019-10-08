name := """dlp-test"""
organization := "com.disneylandparis"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice,
  ehcache,
  javaWs,

  // JQuery
  "org.webjars" % "jquery" % "2.2.2",
  //"org.webjars" % "jquery" % "3.2.1",

  // Plugin Datatables jQuery pour la gestion des tableaux
  "org.webjars" % "datatables" % "1.10.15",
  "org.webjars" % "datatables-plugins" % "1.10.15",

  // Twitter Bootstrap
  "org.webjars" % "bootstrap" % "3.3.6" exclude("org.webjars", "jquery"),

  // JQuery UI
  "org.webjars" % "jquery-ui" % "1.11.4" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery-ui-themes" % "1.11.4",
)
