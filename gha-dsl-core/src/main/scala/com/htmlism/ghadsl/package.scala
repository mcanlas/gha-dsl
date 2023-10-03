package com.htmlism

import io.circe.*
import io.circe.syntax.*

package object ghadsl {
  implicit class LineEncoderOps[A](x: A)(implicit enc: LineEncoder[A]) {
    def encode: List[String] =
      enc.encode(x)
  }

  /**
    * Replicates chaining syntax introduced in 2.13, unavailable in 2.12
    */
  implicit class PipeOps[A](val x: A) extends AnyVal {
    def pipe[B](f: A => B): B =
      f(x)
  }

  def ctx(s: String): String =
    s"$${{ $s }}"

  implicit def toJson[A: Encoder](x: A): Json =
    x.asJson
}
