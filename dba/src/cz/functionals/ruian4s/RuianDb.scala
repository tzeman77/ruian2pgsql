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

  def adresniMistoDetail(kod: Kod[AdresniMisto])(
    implicit ec: ExecutionContext): Future[Option[AdresniMisto.Detail]] = {
    run(
      query[AdresniMisto]
        .filter(_.kod == lift(kod))
        .join(query[Obec]).on(_.kodObce == _.kod)
        .join(query[Okres]).on(_._2.kodOkresu == _.kod)
        .join(query[Kraj]).on(_._2.kodKraje == _.kod)
        .leftJoin(query[Momc]).on(_._1._1._1.kodMomc contains _.kod)
        .leftJoin(query[Mop]).on(_._1._1._1._1.kodMop contains _.kod)
        .leftJoin(query[CastObce])
          .on(_._1._1._1._1._1.kodCastiObce contains _.kod)
        .leftJoin(query[Ulice])
          .on(_._1._1._1._1._1._1.kodUlice contains _.kod)
        .take(1)
    ) map(_.headOption map(v => AdresniMisto.Detail(
      adm = v._1._1._1._1._1._1._1,
      obec = v._1._1._1._1._1._1._2,
      momc = v._1._1._1._2,
      mop = v._1._1._2,
      castObce = v._1._2,
      ulice = v._2,
      okres = v._1._1._1._1._1._2,
      kraj = v._1._1._1._1._2
    )))
  }

}

object RuianDb extends RuianDb
