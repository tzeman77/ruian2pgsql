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

import io.getquill.{PostgresJAsyncContext, SnakeCase}

import scala.concurrent.{ExecutionContext, Future}

trait RuianDb {

  def configPrefix: String = "ruian.db"

  lazy val ctx = new PostgresJAsyncContext(SnakeCase, configPrefix)

  import ctx._

  implicit val encodeTypSo: MappedEncoding[TypSo, String] =
    MappedEncoding[TypSo, String](_.id)
  implicit val decodeTypSo: MappedEncoding[String, TypSo] =
    MappedEncoding[String, TypSo](TypSo(_))

  implicit val encodeJtsk: MappedEncoding[Jtsk, String] =
    MappedEncoding[Jtsk, String](v => s"POINT(${v.x}, ${v.y})")
  implicit val decodeJtsk: MappedEncoding[String, Jtsk] =
    MappedEncoding[String, Jtsk](s => {
      val a = s.stripPrefix("(").stripSuffix(")").split(',')
      Jtsk(BigDecimal(a(0)), BigDecimal(a(1)))
    })

  implicit class TsvQuery(col: String) {

    def @@(q: String) = quote {
      infix"${col}_tsv @@ to_tsquery($q)".as[Boolean]
    }

  }

  implicit class TsvQueryOpt(col: Option[String]) {

    def @@(q: String) = quote {
      infix"${col}_tsv @@ to_tsquery($q)".as[Boolean]
    }

  }

  def adresniMisto(kod: Kod[AdresniMisto])(
    implicit ec: ExecutionContext): Future[Option[AdresniMisto]] = {
    run(query[AdresniMisto].filter(_.kod == lift(kod))) map(_.headOption)
  }

}

object RuianDb extends RuianDb
