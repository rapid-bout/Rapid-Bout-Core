package com.github.rapid_bout.game

import scala.util.Random

import com.github.rapid_bout.game.effect.Effect
import com.github.rapid_bout.game.effect.Effect.Effect
import com.github.rapid_bout.util.Exceptions.StackIsEmptyException

/**
  * 片方のプレイヤー場
  * @param key プレイヤーを一意に指定するキー
  * @param stack プレイ待機中のカード
  * @param hand 手札
  * @param field 場
  * @param deck 山札
  */
case class Player private (
    key: PlayerKey,
    stack: Option[(MutableCard, Side)],
    hand: Hand,
    field: Field,
    deck: Deck
) {

  /** @see [[Field.count]] */
  def count(): Int = field.count()

  /** @see [[Hand.counterable]] */
  def counterable(game: Game, index: Int, willPlay: MutableCard): Boolean = hand.counterable(game, index, willPlay)

  /**
    * スタックに積まれているカードを盤面に置く
    * @return 場に出されたカードの設置時効果
    */
  @throws[StackIsEmptyException]
  def play(): (Player, Effect) = {
    stack match {
      case Some((stack, side @ Side.Front)) => (copy(stack = None, field = field.append(stack, side)), stack.effect)
      case Some((stack, side @ Side.Back)) => (copy(stack = None, field = field.append(stack, side)), Effect.DoNothing)
      case None => throw new StackIsEmptyException
    }
  }

  /**
    * 手札のカードをスタック上に積む
    * @param index 積むカードの座標
    * @param reverse 裏面で積むかどうか
    */
  def stack(index: Int, reverse: Boolean): Player = {
    val (hand, card) = this.hand.remove(index)
    copy(hand = hand, stack = Some((card, Side.fromBoolean(reverse))))
  }

  /** @see [[Hand.playable]] */
  def playable(game: Game, index: Int): Boolean = hand.playable(game, index)

  /**
    * 場のカードを山札に戻す
    * @param indexes 戻したいカードの座標
    */
  def returnOfField(indexes: Seq[Int]): Player = {
    val (field, cards) = this.field.removeAll(indexes)
    val deck = this.deck.appendAll(cards)
    copy(field = field, deck = deck)
  }

  /** @see [[Field.reverse]] */
  def reverse(indexes: Seq[Int], isReverse: Option[Boolean]): Player = {
    val field = this.field.reverse(indexes, isReverse)
    copy(field = field)
  }

  /** スタック上のカードを山札に戻す */
  @throws[StackIsEmptyException]
  def returnOfStack(): Player = this.stack match {
    case Some(stack) => copy(deck = this.deck.append(stack._1), stack = None)
    case None => throw new StackIsEmptyException
  }

  /** @see [[Field.reverseAll]] */
  def reverseAll(): Player = {
    copy(field = this.field.reverseAll())
  }

  /** @see [[Field.reverseAllWithFront]] */
  def reverseAllWithFront(): Player = {
    copy(field = this.field.reverseAllWithFront())
  }

  /** @see [[Deck.shuffle]] */
  def shuffle()(implicit random: Random): Player = {
    copy(deck = this.deck.shuffle())
  }

  /**
    * 山札のカードを引いて手札に加える
    * @param number 引く枚数
    */
  def draw(number: Int): Player = {
    val (deck, cards) = this.deck.draw(number)
    val hand = this.hand.appendAll(cards)
    copy(deck = deck, hand = hand)
  }

  /**
    * 場のカードを手札に戻す
    * @param indexes 戻したいカードの座標
    */
  def bounce(indexes: Seq[Int]): Player = {
    val (field, cards) = this.field.removeAll(indexes)
    val hand = this.hand.appendAll(cards)
    copy(field = field, hand = hand)
  }

  /**
    * 手札のカードを山札に戻す
    * @param indexes 戻したいカードの座標
    */
  def returnOfHand(indexes: Seq[Int]): Player = {
    val (hand, cards) = this.hand.removeAll(indexes)
    val deck = this.deck.appendAll(cards)
    copy(deck = deck, hand = hand)
  }

  /**
    * 手札のカードを場に置く.
    * 設置時の効果処理を行う場合は [[Player.stack]], [[Player.play]] を利用する
    * @param indexes 場に置きたいカードの座標
    * @param reverse 裏面かどうか
    */
  def putOfHand(indexes: Seq[Int], reverse: Boolean): Player = {
    val (hand, cards) = this.hand.removeAll(indexes)
    val field = this.field.appendAll(cards, reverse)
    copy(field = field, hand = hand)
  }

  /**
    * 山札のカードを場に置く。
    * @param indexes 場に置きたいカードの座標
    * @param reverse 裏面かどうか
    */
  def putOfDeck(indexes: Seq[Int], reverse: Boolean): Player = {
    val (deck, cards) = this.deck.removeAll(indexes)
    val field = this.field.appendAll(cards, reverse)
    copy(field = field, deck = deck)
  }

}

object Player {
  def apply(key: PlayerKey, deckList: Seq[MutableCard])(implicit random: Random): Player = new Player(
    key = key,
    stack = None,
    hand = Hand(),
    field = Field(),
    deck = Deck(deckList)
  ).shuffle()
}
