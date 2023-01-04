package com.github.rapid_bout.history

import com.github.rapid_bout.game.{PlayerKey, Zone}

case class Move(player: PlayerKey, indexes: Seq[Int], zone: (Zone, Zone), reverse: Option[Boolean] = None)
    extends History
