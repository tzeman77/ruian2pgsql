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
 * Identifikator/kod entity (phantom-type-safe), tj. RUIAN kod <-> DB ID.
 * @param v Kod entity.
 * @tparam T Typ entity.
 */
case class Kod[T](v: Int) extends AnyVal

object Kod {

  implicit def rw[T]: ReadWriter[Kod[T]] = readwriter[Int].bimap(_.v, Kod[T])

}
