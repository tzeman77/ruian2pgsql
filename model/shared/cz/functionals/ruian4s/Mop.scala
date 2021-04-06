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

/**
 * Mestsky obvod Prahy.
 * @param kod Kod obvodu.
 * @param nazev Nazev obvodu.
 * @param kodObce Kod obce (Prahy).
 */
case class Mop(kod: Kod[Mop], nazev: String, kodObce: Kod[Obec])

object Mop {
  implicit val rw: ReadWriter[Mop] = macroRW[Mop]
}