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
 * Souradnice v systemu S-JTSK.
 * Viz. https://cs.wikipedia.org/wiki/Systém_jednotné_trigonometrické_sítě_katastrální
 * @param x Souradnice X [m].
 * @param y Souradnice Y [m].
 */
case class Jtsk(x: BigDecimal, y: BigDecimal)

object Jtsk {
  implicit val rw: ReadWriter[Jtsk] = macroRW[Jtsk]
}
