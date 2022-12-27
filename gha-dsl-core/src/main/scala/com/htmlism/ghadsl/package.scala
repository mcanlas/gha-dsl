package com.htmlism

package object ghadsl {
  implicit class EncoderOps[A](x: A)(implicit enc: LineEncoder[A]) {
    def encode: List[String] =
      enc.encode(x)
  }

  def ctx(s: String): String =
    s"$${{ $s }}"
}
