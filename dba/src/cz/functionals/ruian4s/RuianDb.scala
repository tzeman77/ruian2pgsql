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

import io.getquill.{PostgresJAsyncContext, Query, Quoted, SnakeCase}

import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

trait RuianDb extends RuianDbMappers {

  def configPrefix: String = "ruian.db"

  lazy val ctx = new PostgresJAsyncContext(SnakeCase, configPrefix)

  import ctx._

  implicit class TsvQuery(col: String) {

    @nowarn def @@(q: String) = quote {
      infix"${col}_tsv @@ to_tsquery($q)".as[Boolean]
    }

  }

  implicit class TsvQueryOpt(col: Option[String]) {

    @nowarn def @@(q: String) = quote {
      infix"${col}_tsv @@ to_tsquery($q)".as[Boolean]
    }

  }

  object detail {

    val templateQ: Quoted[Query[(((((((AdresniMisto, Obec), Kraj), Option[Okres]), Option[Momc]), Option[Mop]), Option[CastObce]), Option[Ulice])]] = quote {
      query[AdresniMisto]
        .join(query[Obec]).on(_.kodObce == _.kod)
        .join(query[Kraj]).on(_._2.kodKraje == _.kod)
        .leftJoin(query[Okres]).on(_._1._2.kodOkresu contains _.kod)
        .leftJoin(query[Momc]).on(_._1._1._1.kodMomc contains _.kod)
        .leftJoin(query[Mop]).on(_._1._1._1._1.kodMop contains _.kod)
        .leftJoin(query[CastObce])
        .on(_._1._1._1._1._1.kodCastiObce contains _.kod)
        .leftJoin(query[Ulice])
        .on(_._1._1._1._1._1._1.kodUlice contains _.kod)
    }

    def mapper(d: (((((((AdresniMisto, Obec), Kraj), Option[Okres]), Option[Momc]), Option[Mop]), Option[CastObce]), Option[Ulice])): AdresniMisto.Detail = d match {
      case (((((((adm, ob), kr), okr), momc), mop), cp), str) =>
        AdresniMisto.Detail(
          adm = adm,
          obec = ob,
          momc = momc,
          mop = mop,
          castObce = cp,
          ulice = str,
          okres = okr,
          kraj = kr
        )
    }

  }

  @nowarn("msg=never used")
  def adresniMisto(kod: Kod[AdresniMisto])(
    implicit ec: ExecutionContext): Future[Option[AdresniMisto]] = {
    run(query[AdresniMisto].filter(_.kod == lift(kod))) map(_.headOption)
  }

  @nowarn("msg=never used")
  def adresniMistoDetail(kod: Kod[AdresniMisto])(
    implicit ec: ExecutionContext): Future[Option[AdresniMisto.Detail]] = {
    run(
      detail.templateQ
        .filter { case (((((((adm, _), _), _), _), _), _), _) =>
          adm.kod == lift(kod)
        }
        .take(1)
    ) map(_.headOption map detail.mapper)
  }

}

object RuianDb extends RuianDb
