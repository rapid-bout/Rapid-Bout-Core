package com.github.rapid_bout.game

/** 場の位置を指定するトレイト */
trait Zone

object Zone {
  case object Hand extends Zone
  case object Field extends Zone
  case object Deck extends Zone
  case object Stack extends Zone
}
