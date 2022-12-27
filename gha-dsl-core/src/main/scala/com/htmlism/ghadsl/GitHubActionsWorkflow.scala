package com.htmlism.ghadsl

import cats.data.NonEmptyList
import cats.syntax.all._

import com.htmlism.ghadsl.GitHubActionsWorkflow.TriggerEvent._
import com.htmlism.ghadsl.GitHubActionsWorkflow._
import com.htmlism.ghadsl.LineEncoder._

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
        wf.name.map(n => List("name: " + n))

      val triggers =
        List("on:") ++ wf.triggerEvents.toList.flatMap(_.encode).pipe(intended)

      val jobs =
        List("jobs:") ++ wf
          .jobs
          .toList
          .map(_.encode.pipe(intended))
          .pipe(interConcat(List("")))

      (nameLines.toList ::: List(triggers, jobs))
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
          List("runs-on: " + j.runsOn.s, "steps:") ++
            j.steps
              .toList
              .map(_.encode.pipe(asArrayElement))
              .pipe(interConcat(List("")))

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
                .pipe(intended)

          List("uses: " + s) ::: withLines

        case Run(s, env) =>
          val envLines =
            if (env.isEmpty)
              Nil
            else
              List("env:") ++ env
                .map { case (k, v) =>
                  s"$k: $v"
                }
                .pipe(intended)

          List("run: " + s) ::: envLines
      }

      case class Uses(s: String, args: List[(String, String)]) extends Step {
        def parameters(xs: (String, String)*): Uses =
          copy(args = xs.toList)
      }

      object Uses {
        def apply(s: String): Uses =
          Uses(s, Nil)
      }

      case class Run(s: String, env: List[(String, String)]) extends Step {
        def withEnv(xs: (String, String)*): Run =
          copy(env = xs.toList)
      }

      object Run {
        def apply(s: String): Run =
          Run(s, Nil)
      }
    }
  }
}

trait LineEncoder[A] {
  def encode(x: A): List[String]
}

object LineEncoder {
  def intended(xs: List[String]): List[String] =
    xs.map { s =>
      if (s.isEmpty)
        s
      else
        "  " + s
    }

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
