import bintray.Keys._

sbtPlugin := true

organization := "com.databricks"

name := "sbt-databricks"

version := "0.1.6-SNAPSHOT"

scalaVersion := "2.10.7"

val httpCompsV = "4.5.5"

libraryDependencies ++= Seq(
    "org.apache.httpcomponents" % "httpclient" % httpCompsV,
    "org.apache.httpcomponents" % "httpmime" % httpCompsV,
    "org.apache.httpcomponents" % "httpclient-cache" % httpCompsV,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.4",
    "commons-fileupload" % "commons-fileupload" % "1.3.3"
)

version in ThisBuild := s"${version.value}"

organization in ThisBuild := s"${organization.value}"

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

pomExtra := (
  <url>https://github.com/databricks/sbt-databricks</url>
  <scm>
      <url>git@github.com:databricks/sbt-databricks.git</url>
      <connection>scm:git:git@github.com:databricks/sbt-databricks.git</connection>
  </scm>
  <developers>
      <developer>
          <id>brkyvz</id>
          <name>Burak Yavuz</name>
          <url>https://github.com/brkyvz</url>
      </developer>
      <developer>
        <id>marmbrus</id>
        <name>Michael Armbrust</name>
        <url>https://github.com/marmbrus</url>
      </developer>
  </developers>)
