package com.github.rapid_bout.game.card
import com.github.rapid_bout.game.effect.CounterTerm.Forbidden
import com.github.rapid_bout.game.effect.Effect.{DoNothing, Effect}
import com.github.rapid_bout.game.effect.Term.AnyTime
import com.github.rapid_bout.game.effect.{CounterTerm, Term}

trait Card {

  /** カードが表向きで存在する場合の打点 */
  val point: Int

  /** カードを使うために満たすべき条件 */
  val term: Term = AnyTime

  /** WIP: カードを対抗時に使うために満たすべき条件 (デフォルトでは対抗不可) */
  val counterTerm: CounterTerm = Forbidden

  /** カードを使う際に支払うコスト */
  val preprocess: Effect = DoNothing

  /** カードを使った際に実行される効果 */
  val effect: Effect = DoNothing
}
