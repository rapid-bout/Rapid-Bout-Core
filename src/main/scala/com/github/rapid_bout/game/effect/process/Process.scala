package com.github.rapid_bout.game.effect.process

import cats.data.Writer
import com.github.rapid_bout.game.Game
import com.github.rapid_bout.history.History

/** 効果処理の最小単位 */
sealed trait Process

object Process {

  /** クライアントからの入力を受けるもの */
  trait Select extends Process

  /** 自動でゲーム版に対して行う処理 */
  trait Action extends Process {

    /** 処理の固有名 */
    val name: String

    /**
      * ゲーム盤に対して効果処理を実行する
      * @param game 現在のゲーム盤面
      * @param args この効果を使うために事前に行われた [[Select]] の結果
      * @return 処理履歴と適用後のゲーム盤面
      */
    def apply(game: Game, args: List[Any]): Writer[Seq[History], Game]
  }

}
