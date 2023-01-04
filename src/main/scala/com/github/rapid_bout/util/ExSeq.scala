package com.github.rapid_bout.util

object ExSeq {
  implicit class SwapSeq[T](val seq: Seq[T]) {
    def swap(a: Int, b: Int): Seq[T] =
      seq.updated(a, seq(b)).updated(b, seq(a))
  }
}
