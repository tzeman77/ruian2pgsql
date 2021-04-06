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

import Pickler._

case class AdresniMisto(
  kod: Kod[AdresniMisto],
  kodObce: Kod[Obec],
  kodMomc: Option[Kod[Momc]] = None,
  kodMop: Option[Kod[Mop]] = None,
  kodCastiObce: Option[Kod[CastObce]] = None,
  kodUlice: Option[Kod[Ulice]] = None,
  typSo: TypSo,
  cisloDomovni: Int,
  cisloOrientacni: Option[Int] = None,
  znakCislaOrientacniho: Option[String] = None,
  psc: Psc,
  jtsk: Option[Jtsk] = None
  //platiOd: Option[Date] = None
)

object AdresniMisto {
  implicit val rw: ReadWriter[AdresniMisto] = macroRW[AdresniMisto]
}
