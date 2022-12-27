package com.htmlism.ghadsl
package examples

import java.nio.file._

import scala.jdk.CollectionConverters._

import cats.data.NonEmptyList

import com.htmlism.ghadsl.GitHubActionsWorkflow.TriggerEvent._
import com.htmlism.ghadsl.GitHubActionsWorkflow._

object WriteYaml extends App {
  val minimalWorkflow =
    GitHubActionsWorkflow(
      None,
      NonEmptyList.of(Push()),
      NonEmptyList.of(
        Job(
          "mimimal-foo",
          GitHub.Runners.UbuntuLatest,
          NonEmptyList.of(
            Job.Step.Run("echo hello")
          )
        )
      )
    )

  val workflow =
    GitHubActionsWorkflow(
      None,
      NonEmptyList.of(PullRequest(), Push()),
      NonEmptyList.of(
        Job(
          "foo",
          GitHub.Runners.UbuntuLatest,
          NonEmptyList.of(
            Job.Step.Uses("actions/checkout@v2"),
            Job.Step.Run("echo hello")
          )
        ),
        Job(
          "bar",
          GitHub.Runners.UbuntuLatest,
          NonEmptyList.of(
            Job.Step.Uses("actions/checkout@v2"),
            Job
              .Step
              .Uses("actions/setup-java@v3")
              .parameters(
                "distribution" -> "temurin",
                "java-version" -> "17",
                "cache" -> "sbt"
              ),
            Job.Step.Run("sbt 'scalafixAll --check' scalafmtCheck +test")
          )
        )
      )
    )
      .withName("big workflow")

  val heading =
    List("# This file was automatically generated", "")

  Files
    .write(Path.of(".github", "workflows", "ci.yml"), (heading ::: workflow.encode).asJava)

  Files
    .write(Path.of(".github", "workflows", "minimal.yml"), (heading ::: minimalWorkflow.encode).asJava)
}