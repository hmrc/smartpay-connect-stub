/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models

/**
 * Simple safe equals so we don't have to import cats into cor library
 */
object SafeEquals {

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit class EqualsOps[A](v: A) {
    def ===(other: A): Boolean = v == other
    def =!=(other: A): Boolean = v != other
  }
}
