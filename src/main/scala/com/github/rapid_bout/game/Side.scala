package com.github.rapid_bout.game

/** カードが表か裏か */
sealed trait Side {

  /** 反対の面 */
  val opposite: Side

  /** 裏側かどうか？ */
  val isReverse: Boolean
}

object Side {
  def fromBoolean(back: Boolean): Side = if (back) Back else Front

  /** 表 */
  case object Front extends Side {
    override val opposite: Side = Back
    override val isReverse: Boolean = false
  }

  /** 裏 */
  case object Back extends Side {
    override val opposite: Side = Front
    override val isReverse: Boolean = true
  }
}
