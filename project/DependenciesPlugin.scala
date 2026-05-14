import sbt.Keys.*
import sbt.*

object DependenciesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    implicit class DependencyOps(p: Project) {
      def withCats: Project =
        p
          .settings(libraryDependencies += "org.typelevel" %% "cats-core" % Versions.catsCore)

      def withYaml: Project =
        p
          .settings(libraryDependencies += "io.circe" %% "circe-yaml" % Versions.circeYaml)

      def withTesting: Project = {
        p.settings(
          libraryDependencies ++= Seq(
            "com.disneystreaming" %% "weaver-cats"       % Versions.weaver % Test,
            "com.disneystreaming" %% "weaver-scalacheck" % Versions.weaver % Test
          )
        )
      }
    }
  }
}
