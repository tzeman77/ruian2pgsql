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

import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

/**
 * Implementuje DSL nad RUIAN databazi.
 * Napr. RuianQ.byCode[AdresniMisto].get(Kod[AdresniMisto](27))
 */
trait RuianDsl extends RuianDb {

  import ctx._

  //----- DSL spec -----//

  /** Nacte entitu podle kodu. */
  trait byCode[T] {
    def get(kod: Kod[T])(implicit ec: ExecutionContext): Future[Option[T]]
  }

  /** Fulltextove vyhleda entity dle nazvu. */
  trait byTsv[T] {
    def get(tsv: String, max: Int = 20)(
      implicit ec: ExecutionContext): Future[Seq[T]]
  }

  def okres(kod: Kod[Okres]) = new {

    final def obce(implicit @nowarn ec: ExecutionContext): Future[Seq[Obec]] =
      run(query[Obec] filter(_.kodOkresu == lift(kod)) sortBy(_.nazev))

  }

  def obec(kod: Kod[Obec]) = new {

    def ulice(tsv: String, max: Int = 20)(
      implicit @nowarn ec: ExecutionContext): Future[Seq[Ulice]] =
      run(query[Ulice]
        filter(_.kodObce == lift(kod))
        filter(_.nazev @@ lift(tsv))
        take lift(max)
      )

    def momc(implicit @nowarn ec: ExecutionContext): Future[Seq[Momc]] =
      run(query[Momc] filter(_.kodObce == lift(kod)) sortBy(_.nazev))

    def adresniMista(max: Int = 200)(
      implicit @nowarn ec: ExecutionContext): Future[Seq[AdresniMisto]] =
      run(query[AdresniMisto]
        filter(_.kodObce == lift(kod))
        take lift(max)
      )

    def castObce(tsv: String, max: Int = 20)(
      implicit @nowarn ec: ExecutionContext): Future[Seq[CastObce]] =
      run(query[CastObce]
        filter(_.kodObce == lift(kod))
        filter(_.nazev @@ lift(tsv))
        take lift(max)
      )

  }

  def ulice(kod: Kod[Ulice]) = new {

    def adresniMista(max: Int = 200)(
      implicit @nowarn ec: ExecutionContext): Future[Seq[AdresniMisto]] =
      run(query[AdresniMisto]
        filter(_.kodUlice contains lift(kod))
        take lift(max)
      )

  }

  def castObce(kod: Kod[CastObce]) = new {

    def adresniMista(max: Int = 200)(
      implicit @nowarn ec: ExecutionContext): Future[Seq[AdresniMisto]] =
      run(query[AdresniMisto]
        filter(_.kodCastiObce contains lift(kod))
        take lift(max)
      )

  }

  //----- DSL impl -----//

  object byCode {
    def apply[T](implicit instance: byCode[T]): byCode[T] = instance

    implicit val adresniMistoImplicit: byCode[AdresniMisto] =
      new byCode[AdresniMisto] {
        override def get(kod: Kod[AdresniMisto])(
          implicit ec: ExecutionContext): Future[Option[AdresniMisto]] =
          run(
            query[AdresniMisto] filter(_.kod == lift(kod)) take 1
          ) map(_.headOption)
    }

    implicit val krajImplicit: byCode[Kraj] = new byCode[Kraj] {
      override def get(kod: Kod[Kraj])(
        implicit ec: ExecutionContext): Future[Option[Kraj]] =
        run(query[Kraj] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

    implicit val okresImplicit: byCode[Okres] = new byCode[Okres] {
      override def get(kod: Kod[Okres])(
        implicit ec: ExecutionContext): Future[Option[Okres]] =
        run(query[Okres] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

    implicit val obecImplicit: byCode[Obec] = new byCode[Obec] {
      override def get(kod: Kod[Obec])(
        implicit ec: ExecutionContext): Future[Option[Obec]] =
        run(query[Obec] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

    implicit val castObceImplicit: byCode[CastObce] = new byCode[CastObce] {
      override def get(kod: Kod[CastObce])(
        implicit ec: ExecutionContext): Future[Option[CastObce]] =
        run(query[CastObce] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

    implicit val uliceImplicit: byCode[Ulice] = new byCode[Ulice] {
      override def get(kod: Kod[Ulice])(
        implicit ec: ExecutionContext): Future[Option[Ulice]] =
        run(query[Ulice] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

    implicit val momcImplicit: byCode[Momc] = new byCode[Momc] {
      override def get(kod: Kod[Momc])(
        implicit ec: ExecutionContext): Future[Option[Momc]] =
        run(query[Momc] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

    implicit val mopImplicit: byCode[Mop] = new byCode[Mop] {
      override def get(kod: Kod[Mop])(
        implicit ec: ExecutionContext): Future[Option[Mop]] =
        run(query[Mop] filter(_.kod == lift(kod)) take 1) map(_.headOption)
    }

  }

  object byTsv {
    def apply[T](implicit instance: byTsv[T]): byTsv[T] = instance

    implicit val krajImplicit: byTsv[Kraj] = new byTsv[Kraj] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[Kraj]] =
        run(query[Kraj] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

    implicit val okresImplicit: byTsv[Okres] = new byTsv[Okres] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[Okres]] =
        run(query[Okres] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

    implicit val obecImplicit: byTsv[Obec] = new byTsv[Obec] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[Obec]] =
        run(query[Obec] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

    implicit val castObceImplicit: byTsv[CastObce] = new byTsv[CastObce] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[CastObce]] =
        run(query[CastObce] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

    implicit val uliceImplicit: byTsv[Ulice] = new byTsv[Ulice] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[Ulice]] =
        run(query[Ulice] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

    implicit val momcImplicit: byTsv[Momc] = new byTsv[Momc] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[Momc]] =
        run(query[Momc] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

    implicit val mopImplicit: byTsv[Mop] = new byTsv[Mop] {
      override def get(tsv: String, max: Int)(
        implicit ec: ExecutionContext): Future[Seq[Mop]] =
        run(query[Mop] filter(_.nazev @@ lift(tsv)) take lift(max))
    }

  }

}

object RuianDsl extends RuianDsl