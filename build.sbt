lazy val root =
  Project("gha-dsl", file("."))
    .aggregate(core, examples)

lazy val core =
  module("core")
    .withCats
    .withTesting

lazy val examples =
  module("examples")
    .dependsOn(core)
