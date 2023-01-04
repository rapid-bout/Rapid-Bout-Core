package com.github.rapid_bout.game

import scala.util.Random

import com.github.rapid_bout.util.Exceptions.{IllegalDeckNumberException, IllegalDuplicateCardException}

/**
  * 山札
  * @param decks 山札に含まれるカードリスト、順序固定
  */
case class Deck private (decks: Seq[MutableCard]) {

  /**
    * カードを1枚一番上から取り出す (pop)
    * @param number カードを取り出す枚数
    * @return 取り出したカードのリスト
    */
  @throws[IllegalArgumentException]
  def draw(number: Int): (Deck, Seq[MutableCard]) = {
    if (number < 0) throw new IllegalArgumentException
    (copy(decks = decks.drop(number)), decks.take(number))
  }

  /**
    * 末尾にカードを追加する
    * @param cards 追加するカードのリスト
    */
  def appendAll(cards: Seq[MutableCard]): Deck = copy(decks = decks ++ cards)

  /**
    * 末尾にカードを追加する
    * @param card 追加するカード
    */
  def append(card: MutableCard): Deck = copy(decks = decks :+ card)

  /** カードの順序をランダムに入れ替える */
  def shuffle()(implicit random: Random): Deck = copy(decks = random.shuffle(this.decks))

  /**
    * 指定されたインデックスのカードを全て取り出す
    * @param indexes 取り出したいカードの座標
    * @return 取り出したカードのリスト
    */
  def removeAll(indexes: Seq[Int]): (Deck, List[MutableCard]) = {
    val (decks, cards) = this.decks.zipWithIndex.partitionMap {
      case (card, i) if indexes.contains(i) => Right(card)
      case (card, _) => Left(card)
    }
    (copy(decks = decks), cards.toList)
  }
}

object Deck {
  private val firstDeckNumber = 20

  private val duplicateNumber = 2

  @throws[IllegalDeckNumberException]
  @throws[IllegalDuplicateCardException]
  def apply(deckList: Seq[MutableCard]): Deck = {
    if (deckList.size != firstDeckNumber)
      throw new IllegalDeckNumberException

    if (deckList.groupBy(_.card).exists(_._2.size > duplicateNumber))
      throw new IllegalDuplicateCardException

    new Deck(deckList)
  }
}
