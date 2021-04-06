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
 * Cast obce.
 * @param kod Kod casti obce.
 * @param nazev Nazev casti obce.
 * @param kodObce Kod obce.
 */
case class CastObce(kod: Kod[CastObce], nazev: String, kodObce: Kod[Obec])

object CastObce {
  implicit val rw: ReadWriter[CastObce] = macroRW[CastObce]
}