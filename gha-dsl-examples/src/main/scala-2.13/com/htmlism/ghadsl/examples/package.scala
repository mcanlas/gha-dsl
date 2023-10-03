package com.htmlism.ghadsl

import java.util

import scala.jdk.CollectionConverters.*

package object examples {
  def list[A](xs: List[A]): util.List[A] =
    xs.asJava
}
