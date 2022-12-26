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
    GitHubActionsWorkflow(NonEmptyList.of(PullRequest(), Push()))

  Files
    .write(Path.of(".github", "workflows", "ci.yml"), workflow.encode.asJava)
}

/**
  * A GHA job will not run without triggers specified
  */
case class GitHubActionsWorkflow(triggerEvents: NonEmptyList[TriggerEvent])

object GitHubActionsWorkflow {
  implicit val ghaEncoder: LineEncoder[GitHubActionsWorkflow] =
    (wf: GitHubActionsWorkflow) => List("on:") ++ wf.triggerEvents.toList.flatMap(_.encode).pipe(indents)

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
}

trait LineEncoder[A] {
  def encode(x: A): List[String]
}

object LineEncoder {
  def indents(xs: List[String]): List[String] =
    xs.map("  " + _)
}
