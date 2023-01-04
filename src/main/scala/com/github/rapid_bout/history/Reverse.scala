package com.github.rapid_bout.history

import com.github.rapid_bout.game.PlayerKey

case class Reverse(player: PlayerKey, indexes: Seq[Int]) extends History
