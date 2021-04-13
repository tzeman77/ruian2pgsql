/*
 * Copyright 2021 Tomas Zeman <tomas@functionals.cz>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.functionals.ruian4s

import cz.functionals.ruian4s.GpsPoint._
import cz.functionals.ruian4s.Pickler._

case class GpsPoint(lat: Latitude, lon: Longitude)

object GpsPoint {

  implicit val rw: ReadWriter[GpsPoint] = macroRW[GpsPoint]

  def degrees(v: Double): String = {
    val deg = v.abs.floor.toInt
    val min = ((v - deg) * 60).floor.toInt
    val sec = ((((v - deg) * 60) - min) * 60 * 1000).floor / 1000
    "%d°%d'%5.3f".format(deg, min, sec)
  }

  case class Latitude(v: Double) extends AnyVal {
    def direction: Char = if (v < 0) 'S' else 'N'
    def toHuman: String = direction +: degrees(v)
  }

  object Latitude {
    implicit val rw: ReadWriter[Latitude] =
      readwriter[Double].bimap(_.v, Latitude(_))
  }

  case class Longitude(v: Double) extends AnyVal {
    def direction: Char = if (v < 0) 'W' else 'E'
    def toHuman: String = direction +: degrees(v)
  }

  object Longitude {
    implicit val rw: ReadWriter[Longitude] =
      readwriter[Double].bimap(_.v, Longitude(_))
  }

}