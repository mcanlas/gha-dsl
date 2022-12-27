package com.htmlism.ghadsl

import java.util

import scala.collection.JavaConverters._

package object examples {
  def list[A](xs: List[A]): util.List[A] =
    xs.asJava
}
