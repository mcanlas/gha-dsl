package com.htmlism.ghadsl

import java.nio.file._

import scala.jdk.CollectionConverters._

object WriteYaml extends App {
  val workflow =
    GitHubActionsWorkflow()

  Files
    .write(Path.of(".github", "workflows", "ci.yml"), Encoder.encode(workflow).asJava)
}

case class GitHubActionsWorkflow()

object Encoder {
  def encode(wf: GitHubActionsWorkflow): List[String] =
    Nil
}
