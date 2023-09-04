lazy val root =
  Project("gha-dsl", file("."))
    .aggregate(core, examples)
    .disablePublishing

lazy val core =
  module("core")
    .settings(description := "A Scala DSL for generating GitHub Actions workflows")
    .withCats
    .withYaml
    .withTesting

lazy val examples =
  module("examples")
    .dependsOn(core)
    .disablePublishing
