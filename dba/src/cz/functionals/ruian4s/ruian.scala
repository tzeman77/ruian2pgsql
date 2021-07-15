package cz.functionals.ruian4s

import com.wacai.config.annotation.conf

import scala.annotation.nowarn

@nowarn @conf trait ruian {

  final val db = new {
    val host = "localhost"
    val port = 5432
    val username = "ruian"
    val password = "ruian"
    val database = "ruian"
  }

}
