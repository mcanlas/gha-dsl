package com.htmlism.ghadsl

import java.nio.file._

import scala.jdk.CollectionConverters._

object WriteYaml extends App {
  Files
    .write(Path.of(".github", "workflows", "ci.yml"), Nil.asJava)
}
