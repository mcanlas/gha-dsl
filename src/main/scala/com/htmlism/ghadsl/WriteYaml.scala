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
            Job.Step.Runs("echo hello")
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
            Job.Step.Runs("echo hello")
          )
        ),
        Job(
          "bar",
          GitHub.Runners.UbuntuLatest,
          NonEmptyList.of(
            Job.Step.Uses("actions/checkout@v2"),
            Job.Step.Runs("echo hello")
          )
        )
      )
    )
      .withName("big workflow")

  Files
    .write(Path.of(".github", "workflows", "ci.yml"), workflow.encode.asJava)

  Files
    .write(Path.of(".github", "workflows", "minimal.yml"), minimalWorkflow.encode.asJava)
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
        List("on:") ++ wf.triggerEvents.toList.flatMap(_.encode).pipe(indents)

      val jobs =
        List("jobs:") ++ wf
          .jobs
          .toList
          .map(_.encode.pipe(indents))
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
        List("pull_request:") ++ List("branches: ['**']").pipe(indents)

      case Push() =>
        List("push:") ++ List("branches: ['**']").pipe(indents)
    }

    case class PullRequest() extends TriggerEvent

    case class Push() extends TriggerEvent
  }

  case class Job(name: String, runsOn: Job.Runner, steps: NonEmptyList[Job.Step])

  object Job {
    implicit val jobEncoder: LineEncoder[Job] =
      (j: Job) => {
        val jobLinesss =
          List("runs-on: " + j.runsOn.s, "steps:") ++ j.steps.toList.flatMap(_.encode.pipe(asArrayElement))

        List(j.name + ":") ++ jobLinesss.pipe(indents)
      }

    final case class Runner(s: String) extends AnyVal

    sealed trait Step

    object Step {
      implicit val stepEncoder: LineEncoder[Step] = {
        case Uses(s) =>
          List("uses: " + s)

        case Runs(s) =>
          List("run: " + s)
      }

      case class Uses(s: String) extends Step

      case class Runs(s: String) extends Step
    }
  }
}

trait LineEncoder[A] {
  def encode(x: A): List[String]
}

object LineEncoder {
  def indents(xs: List[String]): List[String] =
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
