lazy val root =
  Project("gha-dsl", file("."))
    .aggregate(core, examples)
    .disablePublshing

lazy val core =
  module("core")
    .withCats
    .withYaml
    .withTesting

lazy val examples =
  module("examples")
    .dependsOn(core)
    .disablePublshing
