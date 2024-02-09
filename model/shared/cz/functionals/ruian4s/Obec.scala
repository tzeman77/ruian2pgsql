/*
 * Copyright 2021-2024 Tomas Zeman <tomas@functionals.cz>
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
 * Obec.
 * @param kod Kod obce.
 * @param nazev Nazev obce.
 * @param kodOkresu Kod okresu.
 * @param kodKraje Kod kraje.
 */
case class Obec(kod: Kod[Obec],
  nazev: String,
  kodOkresu: Option[Kod[Okres]] = None,
  kodKraje: Kod[Kraj])

object Obec {
  implicit val rw: ReadWriter[Obec] = macroRW[Obec]
}
