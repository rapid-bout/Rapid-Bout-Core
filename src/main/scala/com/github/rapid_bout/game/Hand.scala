package com.github.rapid_bout.game

import scala.util.Random

case class Hand private (hands: Seq[MutableCard]) {

  /**
    * 指定された座標のカードが今スタックに乗っているカードに対して対抗可能か
    * @param index 対抗に利用したいカード
    * @param willPlay スタックに積まれている (プレイされようとしている) カード
    * @return 対抗可能か
    */
  def counterable(game: Game, index: Int, willPlay: MutableCard): Boolean =
    hands(index).counterTerm.verify(game, game.nonActivePlayer, willPlay)

  /**
    * 手札のカードを1枚取り出す
    * @param index 取り出すカードの座標
    * @return 取り出したカード
    */
  def remove(index: Int): (Hand, MutableCard) =
    (copy(hands = this.hands.zipWithIndex.filterNot { case (_, i) => i == index }.map(_._1)), this.hands(index))

  /**
    * 指定された座標にあるカードがプレイ可能か
    * @param index プレイしたいカードの座標
    * @return プレイ可能か
    */
  def playable(game: Game, index: Int): Boolean = hands(index).term.verify(game, game.activePlayer)

  /**
    * 末尾にカードを追加する
    * @param cards 追加するカードのリスト
    */
  def appendAll(cards: Seq[MutableCard]): Hand = copy(hands = hands ++ cards)

  /**
    * 末尾にカードを追加する
    * @param card 追加するカード
    */
  def append(card: MutableCard): Hand = copy(hands = hands :+ card)

  /**
    * ランダムな手札のカード座標を選択する
    * @param number 選択する枚数
    * @param duplicate 重複選択可能か
    * @param random 乱数オブジェクト
    * @return 選択された座標のリスト
    */
  def getRandomIndex(number: Int, duplicate: Boolean)(implicit random: Random): List[Int] =
    if (duplicate) {
      (0 until number).map(_ => random.between(0, number - 1)).toList
    } else {
      random.shuffle(hands.indices.toList).take(number)
    }

  /** 手札の枚数 */
  def count: Int = hands.length

  /**
    * 手札のカードを複数枚取り出す
    * @param indexes 取り出したいカードの座標
    * @return 取り出したカードのリスト
    */
  def removeAll(indexes: Seq[Int]): (Hand, List[MutableCard]) = {
    val (hands, cards) = this.hands.zipWithIndex.partitionMap {
      case (card, i) if indexes.contains(i) => Right(card)
      case (card, _) => Left(card)
    }
    (copy(hands = hands), cards.toList)
  }
}

object Hand {
  def apply(): Hand = new Hand(Seq.empty)
}
