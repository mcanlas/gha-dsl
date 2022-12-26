package com.htmlism.ghadsl

import java.nio.file._

import scala.jdk.CollectionConverters._
import scala.util.chaining._

import cats.data.NonEmptyList

import com.htmlism.ghadsl.GitHubActionsWorkflow.TriggerEvent._
import com.htmlism.ghadsl.GitHubActionsWorkflow._
import com.htmlism.ghadsl.LineEncoder._

object WriteYaml extends App {
  val workflow =
    GitHubActionsWorkflow(
      NonEmptyList.of(PullRequest(), Push()),
      NonEmptyList.of(
        Job(
          "foo",
          NonEmptyList.of(
            Job.Step.Runs("echo hello")
          )
        ),
        Job(
          "bar",
          NonEmptyList.of(
            Job.Step.Uses("actions/checkout@v2")
          )
        )
      )
    )

  Files
    .write(Path.of(".github", "workflows", "ci.yml"), workflow.encode.asJava)
}

/**
  * A GHA job will not run without triggers specified
  * @param triggerEvents
  *   At least one trigger is required
  * @param jobs
  *   At least one job is required
  */
case class GitHubActionsWorkflow(
    triggerEvents: NonEmptyList[TriggerEvent],
    jobs: NonEmptyList[Job]
)

object GitHubActionsWorkflow {
  implicit val ghaEncoder: LineEncoder[GitHubActionsWorkflow] =
    (wf: GitHubActionsWorkflow) => {
      val triggers =
        List("on:") ++ wf.triggerEvents.toList.flatMap(_.encode).pipe(indents)

      val jobs =
        List("jobs:") ++ wf
          .jobs
          .toList
          .map(_.encode.pipe(indents))
          .pipe(interConcat(List("")))

      List(triggers, jobs)
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

  case class Job(name: String, steps: NonEmptyList[Job.Step])

  object Job {
    implicit val jobEncoder: LineEncoder[Job] =
      (j: Job) => {
        val stepsLines =
          List("steps:") ++ j.steps.toList.flatMap(_.encode.pipe(asArrayElement))

        List(j.name + ":") ++ stepsLines.pipe(indents)
      }

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
