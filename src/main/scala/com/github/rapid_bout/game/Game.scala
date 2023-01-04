package com.github.rapid_bout.game

import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

import cats.data.Writer
import com.github.rapid_bout.game.card.Card
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.game.effect.process.Process
import com.github.rapid_bout.game.effect.process.Process.{Action, Select}
import com.github.rapid_bout.game.effect.process.select.UserSelect
import com.github.rapid_bout.history
import com.github.rapid_bout.history.{AddPlayCount, Finish, History, Move, Reverse, ReverseAll}
import com.github.rapid_bout.util.Exceptions.StackIsEmptyException

import Game.maxLife
import PlayerKey.Both

/**
  * ゲーム盤.
  * ゲーム上全ての状態管理を行う
  *
  * @param players プレイヤーのIDと盤面の組
  * @param playCount このターンにあと何回カードをプレイできるか
  * @param activePlayer ターンプレイヤー
  * @param turn 現在のターン数
  * @param args 効果解決などのために積まれている選択済みの値
  * @param effectStack 解決待ちの効果一覧
  * @param finish ゲームが終了しているか？
  */
case class Game(
    players: ((PlayerKey, Player), (PlayerKey, Player)),
    playCount: Int,
    activePlayer: PlayerKey,
    turn: Int,
    args: List[Any],
    effectStack: List[Process],
    finish: Boolean = false
) {

  /** 非ターンプレイヤー */
  def nonActivePlayer: PlayerKey = getOpponent(activePlayer).key

  /**
    * プレイヤーに対する処理を行うためのラッパー
    * @param target どのプレイヤーに対して処理を行うか？
    * @param f プレイヤーに対する処理
    * @return 処理結果を含む [[Game]] オブジェクトとその変更履歴
    */
  private[game] def adopt(target: PlayerKey)(f: Player => Writer[Seq[History], Player]): Writer[Seq[History], Game] = {
    val writer = f(get(target))
    writer.map(player => put(target, player))
  }

  /**
    * プレイヤーに対する処理を行うためのラッパー
    * @param target どのプレイヤーに対して処理を行うか？
    * @param f プレイヤーに対する処理
    * @tparam T プレイヤーに対する処理の副次的に発生する値の型
    * @return 処理結果を含む [[Game]] オブジェクトとその変更履歴、副結果
    */
  private[game] def adoptT[T](target: PlayerKey)(f: Player => Writer[Seq[History], (Player, T)])
      : Writer[Seq[History], (Game, T)] = {
    val writer = f(get(target))
    writer.map {
      case (game, result) => (put(target, game), result)
    }
  }

  /**
    * プレイヤーの盤面を取得する
    * @param initiator 取得したい盤面を持つユーザーID
    * @return プレイヤー盤面
    */
  def get(initiator: PlayerKey): Player =
    if (players._1._1 == initiator) players._1._2
    else if (players._2._1 == initiator) players._2._2
    else sys.error(s"該当のプレイヤーが存在しません: $initiator")

  /**
    * プレイヤー盤面を上書きする
    * @param initiator その盤面を持つユーザーID
    * @param player 盤面
    */
  private[game] def put(initiator: PlayerKey, player: Player): Game =
    if (players._1._1 == initiator) this.copy(players = ((initiator, player), this.players._2))
    else if (players._2._1 == initiator) this.copy(players = (this.players._1, (initiator, player)))
    else sys.error(s"該当のプレイヤーが存在しません: $initiator")

  /**
    * 対面プレイヤーの盤面を取得する
    * @param initiator 正面のプレイヤーID
    */
  private[game] def getOpponent(initiator: PlayerKey): Player =
    if (players._1._1 == initiator) players._2._2
    else if (players._2._1 == initiator) players._1._2
    else sys.error(s"存在しないプレイヤーが指定されました: $initiator")

  /**
    * @see [[Player.count]]
    * @param initiator どのプレイヤーか
    */
  private[game] def count(initiator: PlayerKey): Int = {
    get(initiator).count()
  }

  /**
    * @see [[Field.get]]
    * @param initiator どのプレイヤーか
    */
  private[game] def selectField(initiator: PlayerKey, target: MutableCard): Int = get(initiator).field.get(target)

  /**
    * ゲームが終了しているか判定し、判定後のゲーム状態を返す
    * @param initiator 判定対象ユーザーID
    */
  private[game] def judgeFinish(initiator: PlayerKey): Writer[Seq[History], Game] = {
    if (this.get(initiator).count() >= maxLife) {
      Writer.apply(List(Finish(initiator)), this.copy(finish = true))
    } else {
      Writer.value(this)
    }
  }

  /**
    * 山札からカードを場に置く.
    * ゲームが終了している場合、終了処理を行う
    * @see [[Player.putOfDeck]]
    * @param initiator どのプレイヤーか
    */
  private[game] def putOfDeck(initiator: PlayerKey, indexes: Seq[Int], reverse: Boolean): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(Move(initiator, indexes, (Zone.Deck, Zone.Field), Some(reverse))),
        player.putOfDeck(indexes = indexes, reverse = reverse)
      )
    }.flatMap(_.judgeFinish(initiator))

  /**
    * 手札からカードを場に置く.
    * ゲームが終了している場合、終了処理を行う
    * @see [[Player.putOfHand]]
    * @param initiator どのプレイヤーか
    */
  private[game] def putOfHand(initiator: PlayerKey, indexes: Seq[Int], reverse: Boolean): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(Move(initiator, indexes, (Zone.Hand, Zone.Field), Some(reverse))),
        player.putOfHand(indexes = indexes, reverse = reverse)
      )
    }.flatMap { game =>
      if (game.get(initiator).count() >= maxLife) {
        Writer.apply(List(Finish(initiator)), game.copy(finish = true))
      } else {
        Writer.value(game)
      }
    }

  /**
    * 手札のカードを全て場に置く.
    * ゲームが終了している場合、終了処理を行う
    * @see [[Player.putOfHand]]
    * @param initiator どのプレイヤーか
    */
  private[game] def putOfHandAll(initiator: PlayerKey, reverse: Boolean): Writer[Seq[History], Game] = {
    val indexes = 0 until get(initiator).hand.count
    putOfHand(initiator, indexes, reverse)
  }

  /**
    * @see [[Player.returnOfHand]]
    * @param initiator どのプレイヤーか
    */
  private[game] def returnOfHand(initiator: PlayerKey, indexes: Seq[Int]): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(history.Move(initiator, indexes, (Zone.Hand, Zone.Deck))),
        player.returnOfHand(indexes = indexes)
      )
    }

  /**
    * @see [[Player.bounce]]
    * @param initiator どのプレイヤーか
    */
  private[game] def bounce(initiator: PlayerKey, indexes: Seq[Int]): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(history.Move(initiator, indexes, (Zone.Field, Zone.Hand))),
        player.bounce(indexes = indexes)
      )
    }

  /**
    * @see [[Player.draw]]
    * @param initiator どのプレイヤーか
    */
  private[game] def draw(initiator: PlayerKey, number: Int): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(history.Move(initiator, 0 until number, (Zone.Deck, Zone.Hand))),
        player.draw(number = number)
      )
    }

  /** このターン中にプレイできる回数の上限を追加する */
  private[game] def addPlayCount(): Writer[Seq[History], Game] = Writer(
    List(AddPlayCount(1)),
    copy(playCount = playCount + 1)
  )

  /**
    * @see [[Player.reverseAllWithFront]]
    * @param initiator どのプレイヤーか
    */
  private[game] def reverseAllWithFront(initiator: PlayerKey): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(ReverseAll(isFront = true)),
        player.reverseAllWithFront()
      )
    }

  /**
    * @see [[Player.reverseAll]]
    * @param initiator どのプレイヤーか
    */
  private[game] def reverseAll(initiator: PlayerKey): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(ReverseAll(isFront = false)),
        player.reverseAll()
      )
    }

  /**
    * @see [[Player.returnOfStack]]
    * @param initiator どのプレイヤーか
    */
  private[game] def returnOfStack(initiator: PlayerKey): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(history.Move(activePlayer, List(0), (Zone.Stack, Zone.Field))),
        player.returnOfStack()
      )
    }

  /**
    * @see [[Player.returnOfField]]
    * @param initiator どのプレイヤーか
    */
  private[game] def returnOfField(initiator: PlayerKey, indexes: Seq[Int]): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(history.Move(initiator, indexes, (Zone.Field, Zone.Deck))),
        player.returnOfField(indexes)
      )
    }

  /**
    * @see [[Player.reverse]]
    * @param initiator どのプレイヤーか
    */
  private[game] def reverse(
      initiator: PlayerKey,
      indexes: Seq[Int],
      isReverse: Option[Boolean]
  ): Writer[Seq[History], Game] =
    adopt(initiator) { player =>
      Writer(
        List(Reverse(initiator, indexes)),
        player.reverse(indexes, isReverse)
      )
    }

  /**
    * @see [[Player.stack]]
    * @param initiator どのプレイヤーか
    */
  private def stack(
      initiator: PlayerKey,
      index: Int,
      reverse: Boolean
  ): Writer[Seq[History], Game] = {
    adopt(initiator) { player =>
      Writer(
        List(history.Move(activePlayer, List(index), (Zone.Hand, Zone.Stack), reverse = Some(reverse))),
        player.stack(index, reverse)
      )
    }
  }

  /**
    * カードを (プレイするために) 手札からスタックに積む [[Game.stack]].
    * 可能な限りは効果解決を進めるが、ユーザー選択が必要になったタイミングで処理を停止し、 [[Game.select]] を待つ
    * @param index 置きたいカードの手札の座標
    * @param reverse 裏向きで置くか？
    * @return 必要になったユーザー選択
    */
  @throws[StackIsEmptyException]
  @throws[IllegalArgumentException]
  def preprocess(index: Int, reverse: Boolean): Writer[Seq[History], (Game, Option[Select])] = {
    require(!finish)
    require(get(activePlayer).playable(this, index))
    require(this.playCount > 0)
    stack(activePlayer, index, reverse) map { game =>
      // stack 取得
      val stack = game.get(game.activePlayer) // player 取得
        .stack.getOrElse(throw new StackIsEmptyException)._1
      val process =
        stack.preprocess.apply(this, game.activePlayer, stack) // 効果処理取得
      game.copy(effectStack = this.effectStack ++ (if (!reverse) process else Nil), playCount = game.playCount - 1) // 効果処理は後から積まれる
    } flatMap { game => game.next() }
  }

  /**
    * 現在スタックに積まれているカードに対して対抗でカードを使う.
    * 可能な限りは効果解決を進めるが、ユーザー選択が必要になったタイミングで処理を停止し、 [[Game.select]] を待つ
    * @param index 置きたいカードの手札の座標
    * @return 必要になったユーザー選択
    */
  @throws[StackIsEmptyException]
  @throws[IllegalArgumentException]
  def counter(index: Int): Writer[Seq[History], (Game, Option[Select])] = {
    require(!finish)
    val stack = get(activePlayer).stack.getOrElse(throw new StackIsEmptyException)._1
    require(get(nonActivePlayer).counterable(this, index, stack))
    this.stack(nonActivePlayer, index, reverse = false) flatMap { game =>
      // 対抗時に効果前処理は行わない
      game.play()
    }
  }

  /**
    * スタックに積まれているカードを場に置き、設置時効果を解決する.
    * 可能な限りは効果解決を進めるが、ユーザー選択が必要になったタイミングで処理を停止し、 [[Game.select]] を待つ
    * @return 必要になったユーザー選択
    */
  def play(): Writer[Seq[History], (Game, Option[Select])] = {
    require(!finish)
    require(this.effectStack.isEmpty)
    (get(nonActivePlayer).stack match {
      case Some(stack) =>
        val parent = stack._1
        play(nonActivePlayer) map {
          case (game, effect) =>
            val process = effect.apply(game, nonActivePlayer, parent)
            // 対抗時は先に処理を積む
            game.copy(effectStack = process ++ this.effectStack)
        }
      case None =>
        play(activePlayer) map {
          case (game, effect) =>
            val parent = get(activePlayer).stack.getOrElse(throw new StackIsEmptyException)._1
            val process = effect.apply(game, activePlayer, parent)
            game.copy(effectStack = this.effectStack ++ process)
        }
    }) flatMap { game => game.next() }
  }

  /**
    * スタック上のカードを場に置き、ゲームの終了判定を行う
    * @param initiator どのプレイヤーか
    */
  private[game] def play(initiator: PlayerKey): Writer[Seq[History], (Game, Effect)] =
    adoptT[Effect](initiator) { player =>
      val (_, side) = player.stack.getOrElse(throw new StackIsEmptyException)
      Writer(
        List(history.Move(initiator, List(0), (Zone.Stack, Zone.Field), reverse = Some(side.isReverse))),
        player.play()
      )
    } flatMap { case (game, effect) => game.judgeFinish(initiator).map(game => (game, effect)) }

  /**
    * 効果解決などのためのユーザー選択を受け付ける.
    * 可能な限りは効果解決を進めるが、ユーザー選択が必要になったタイミングで処理を停止し、 [[Game.select]] を待つ
    * @param indexes 要求されたゾーン上の座標
    * @return 必要になったユーザー選択
    */
  def select(indexes: Seq[Int]): Writer[Seq[History], (Game, Option[Select])] = {
    require(!finish)
    effectStack.headOption match {
      case Some(select: UserSelect) if select.target != Both =>
      case _ => throw new IllegalArgumentException("スタックに積まれている処理が特定ユーザーのカード選択ではない")
    }
    this.copy(effectStack = effectStack.tail, args = List(indexes))
  } pipe { game => game.next() }

  /**
    * 効果解決などのためのユーザー選択を受け付ける (対象プレイヤーも選択する).
    * 可能な限りは効果解決を進めるが、ユーザー選択が必要になったタイミングで処理を停止し、 [[Game.select]] を待つ
    * @param initiator どのユーザーか
    * @param indexes 要求されたゾーン上の座標
    * @return 必要になったユーザー選択
    */
  @throws[IllegalArgumentException]
  def select(initiator: PlayerKey, indexes: Seq[Int]): Writer[Seq[History], (Game, Option[Select])] = {
    require(!finish)
    effectStack.headOption match {
      case Some(select: UserSelect) if select.target == Both =>
      case _ => throw new IllegalArgumentException("スタックに積まれている処理が任意ユーザーのカード選択ではない")
    }
    this.copy(effectStack = effectStack.tail, args = List(initiator, indexes))
  } pipe { game => game.next() }

  /**
    * 効果解決を進める.
    * 可能な限りは効果解決を進めるが、ユーザー選択が必要になったタイミングで処理を停止し、 [[Game.select]] を待つ
    * @return 必要になったユーザー選択
    */
  private[game] def next(): Writer[Seq[History], (Game, Option[Select])] = this.effectStack match {
    case _ if finish => Writer(Nil, (this, None))
    case (select: Select) :: _ => Writer(Nil, (this, Some(select)))
    case (action: Action) :: _ => action.apply(this, this.args)
        .flatMap(game => game.copy(effectStack = this.effectStack.tail, args = Nil).next())
    case Nil => Writer(Nil, (this, None))
  }

  /** ターン終了することを受け付ける */
  @throws[IllegalArgumentException]
  def turnEnd(): Writer[Seq[History], Game] = {
    require(!finish)
    require(this.effectStack.isEmpty)
    require(this.get(this.activePlayer).stack.isEmpty)
    require(this.get(this.nonActivePlayer).stack.isEmpty)
    val next = getOpponent(activePlayer)
    draw(next.key, 1).map(game =>
      game.copy(
        playCount = 2,
        turn = turn + 1,
        activePlayer = next.key
      )
    )
  }
}

object Game {
  private val maxLife = 20

  def apply(player1: (PlayerKey, Seq[Card]), player2: (PlayerKey, Seq[Card]))(implicit random: Random): Game = {
    val players = (
      (player1._1, Player(player1._1, player1._2.map(MutableCard.apply))),
      (player2._1, Player(player2._1, player2._2.map(MutableCard.apply)))
    )
    val activePlayer = random.between(0, 1) match {
      case 0 => players._1._1
      case 1 => players._2._1
      case i => sys.error(s"指定範囲外の値が返されました: $i")
    }
    new Game(players, 1, activePlayer, 1, Nil, Nil)
      // ゲーム開始時に各プレイヤーは3枚引く
      .draw(player1._1, number = 3)
      .flatMap(game => game.draw(player2._1, number = 3))
      // 先行ドロー
      .flatMap(game => game.draw(activePlayer, number = 1))
      .run._2
  }
}
