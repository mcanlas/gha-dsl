package com.htmlism.ghadsl

import java.nio.file._

import scala.jdk.CollectionConverters._
import scala.util.chaining._

import cats.data.NonEmptyList
import cats.syntax.all._

import com.htmlism.ghadsl.GitHubActionsWorkflow.TriggerEvent._
import com.htmlism.ghadsl.GitHubActionsWorkflow._
import com.htmlism.ghadsl.LineEncoder._

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

/**
  * A GHA job will not run without triggers specified
  *
  * @param name
  *   Workflow name is optional; if it isn't provided, the file path will be used
  * @param triggerEvents
  *   At least one trigger is required
  * @param jobs
  *   At least one job is required
  */
case class GitHubActionsWorkflow(
    name: Option[String],
    triggerEvents: NonEmptyList[TriggerEvent],
    jobs: NonEmptyList[Job]
) {
  def withName(s: String): GitHubActionsWorkflow =
    copy(name = s.some)
}

object GitHubActionsWorkflow {
  implicit val ghaEncoder: LineEncoder[GitHubActionsWorkflow] =
    (wf: GitHubActionsWorkflow) => {
      val nameLines =
        wf.name.map("name: " + _).toList

      val triggers =
        List("on:") ++ wf.triggerEvents.toList.flatMap(_.encode).pipe(intended)

      val jobs =
        List("jobs:") ++ wf
          .jobs
          .toList
          .map(_.encode.pipe(intended))
          .pipe(interConcat(List("")))

      (
        if (wf.name.isEmpty)
          List(triggers, jobs)
        else
          List(nameLines, triggers, jobs)
      )
        .pipe(interConcat(List("")))
    }

  sealed trait TriggerEvent

  object TriggerEvent {
    implicit val triggerEventEncoder: LineEncoder[TriggerEvent] = {
      case PullRequest() =>
        List("pull_request:") ++ List("branches: ['**']").pipe(intended)

      case Push() =>
        List("push:") ++ List("branches: ['**']").pipe(intended)
    }

    case class PullRequest() extends TriggerEvent

    case class Push() extends TriggerEvent
  }

  case class Job(id: String, runsOn: Job.Runner, steps: NonEmptyList[Job.Step])

  object Job {
    implicit val jobEncoder: LineEncoder[Job] =
      (j: Job) => {
        val jobLinesss =
          List("runs-on: " + j.runsOn.s, "steps:") ++ j.steps.toList.flatMap(_.encode.pipe(asArrayElement))

        List(j.id + ":") ++ jobLinesss.pipe(intended)
      }

    final case class Runner(s: String) extends AnyVal

    sealed trait Step

    object Step {
      implicit val stepEncoder: LineEncoder[Step] = {
        case Uses(s, xs) =>
          val withLines =
            if (xs.isEmpty)
              Nil
            else
              List("with:") ++ xs
                .map { case (k, v) =>
                  s"$k: $v"
                }
                .toList
                .pipe(intended)

          List("uses: " + s) ::: withLines

        case Run(s) =>
          List("run: " + s)
      }

      case class Uses(s: String, args: Map[String, String]) extends Step {
        def parameters(xs: (String, String)*): Uses =
          copy(args = xs.toMap)
      }

      object Uses {
        def apply(s: String): Uses =
          Uses(s, Map())
      }

      case class Run(s: String) extends Step
    }
  }
}

trait LineEncoder[A] {
  def encode(x: A): List[String]
}

object LineEncoder {
  def intended(xs: List[String]): List[String] =
    xs.map("  " + _)

  def asArrayElement(xs: List[String]): List[String] =
    xs match {
      case head :: tail =>
        ("  - " + head) +: tail.map("    " + _)

      case Nil =>
        Nil
    }

  def interConcat[A](join: List[A])(xxs: List[List[A]]): List[A] =
    xxs match {
      case head :: tail =>
        head ::: tail.flatMap(a => join ::: a)

      case Nil =>
        Nil
    }
}

object GitHub {
  object Runners {
    val UbuntuLatest =
      Job.Runner("ubuntu-latest")
  }
}
