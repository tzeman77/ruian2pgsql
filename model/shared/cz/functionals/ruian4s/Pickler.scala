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

import upickle.AttributeTagged

object Pickler extends AttributeTagged {

  override implicit def OptionWriter[T](implicit
    evidence$1: Pickler.Writer[T]): Pickler.Writer[Option[T]] =
    implicitly[Writer[T]].comap[Option[T]] {
      case None => null.asInstanceOf[T]
      case Some(v) => v
    }

  override implicit def OptionReader[T](implicit
    evidence$1: Pickler.Reader[T]): Pickler.Reader[Option[T]] =
    implicitly[Reader[T]].mapNulls{
      case null => None
      case v => Some(v)
    }

}
