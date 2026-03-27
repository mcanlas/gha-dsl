import sbt.Keys.*
import sbt.*

object DependenciesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    implicit class DependencyOps(p: Project) {
      val circeYamlVersion =
        "0.16.1"

      def withCats: Project =
        p
          .settings(libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0")

      def withYaml: Project =
        p
          .settings(libraryDependencies += "io.circe" %% "circe-yaml" % circeYamlVersion)

      def withTesting: Project = {
        val weaverVersion =
          "0.8.4"

        p.settings(
          libraryDependencies ++= Seq(
            "com.disneystreaming" %% "weaver-cats"       % weaverVersion % Test,
            "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test
          )
        )
      }
    }
  }
}
