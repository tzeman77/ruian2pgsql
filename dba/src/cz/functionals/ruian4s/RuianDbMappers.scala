package cz.functionals.ruian4s

import cz.functionals.ruian4s.GpsPoint.{Latitude, Longitude}
import io.getquill.context.jasync.JAsyncContext

trait RuianDbMappers {

  val ctx: JAsyncContext[_, _, _]

  import ctx._

  implicit val encodeTypSo: MappedEncoding[TypSo, String] =
    MappedEncoding[TypSo, String](_.id)
  implicit val decodeTypSo: MappedEncoding[String, TypSo] =
    MappedEncoding[String, TypSo](TypSo(_))

  implicit val encodeJtsk: MappedEncoding[Jtsk, String] =
    MappedEncoding[Jtsk, String](v => s"(${v.x}, ${v.y})")

  implicit val decodeJtsk: MappedEncoding[String, Jtsk] =
    MappedEncoding[String, Jtsk](s => {
      val a = s.stripPrefix("(").stripSuffix(")").split(',')
      Jtsk(BigDecimal(a(0)), BigDecimal(a(1)))
    })

  implicit val encodeGps: MappedEncoding[GpsPoint, String] =
    MappedEncoding[GpsPoint, String](v => s"(${v.lon.v}, ${v.lat.v})")

  implicit val decodeGps: MappedEncoding[String, GpsPoint] =
    MappedEncoding[String, GpsPoint](s => {
      val a = s.stripPrefix("(").stripSuffix(")").split(',')
      GpsPoint(lon = Longitude(a(0).toDouble), lat = Latitude(a(1).toDouble))
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

}
