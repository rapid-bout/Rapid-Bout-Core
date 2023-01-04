package com.github.rapid_bout.game

/** 対象プレイヤーを表すオブジェクト */
trait PlayerKey

object PlayerKey {

  /** ユーザーIDを利用して一意に指定する */
  case class PlayerKeyForUser(name: String) extends PlayerKey

  /** 対戦中の両ユーザーをまとめて指定する */
  object Both extends PlayerKey
}
