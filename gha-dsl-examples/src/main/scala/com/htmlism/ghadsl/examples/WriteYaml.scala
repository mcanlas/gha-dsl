package com.htmlism.ghadsl
package examples

import java.nio.file._

import cats.data.NonEmptyList

import com.htmlism.ghadsl.GitHubActionsWorkflow.TriggerEvent.WorkflowDispatch.Input
import com.htmlism.ghadsl.GitHubActionsWorkflow.TriggerEvent._
import com.htmlism.ghadsl.GitHubActionsWorkflow._

object WriteYaml extends App {
  val minimalWorkflow =
    GitHubActionsWorkflow(
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

  val manuallyTriggered =
    GitHubActionsWorkflow(
      NonEmptyList.of(
        WorkflowDispatch(
          List(
            Input(
              "sha",
              false
            ),
            Input(
              "env",
              false
            )
          )
        )
      ),
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
      NonEmptyList.of(Push()),
      NonEmptyList.of(
        Job(
          "bar",
          GitHub.Runners.UbuntuLatest,
          NonEmptyList.of(
            Job.Step.Uses("actions/checkout@v3"),
            Job
              .Step
              .Uses("actions/setup-java@v3")
              .parameters(
                "distribution" -> "temurin",
                "java-version" -> "17",
                "cache" -> "sbt"
              ),
            Job
              .Step
              .Run("sbt 'scalafixAll --check' scalafmtCheck +test +publish")
              .withEnv("GITHUB_TOKEN" -> ctx("secrets.WRITE_PACKAGES_TOKEN"))
          )
        )
      )
    )
      .withName("big workflow")

  val heading =
    List("# This file was automatically generated", "")

  Files
    .write(Path.of(".github", "workflows", "ci.yml"), list(heading ::: workflow.encode))

  Files
    .write(Path.of(".github", "workflows", "minimal.yml"), list(heading ::: minimalWorkflow.encode))

  Files
    .write(Path.of(".github", "workflows", "manually-triggered.yml"), list(heading ::: manuallyTriggered.encode))
}
