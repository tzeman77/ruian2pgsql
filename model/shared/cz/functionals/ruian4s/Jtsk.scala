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
import cz.functionals.ruian4s.GpsPoint.{Latitude, Longitude}

/**
 * Souradnice v systemu S-JTSK.
 * Viz. https://cs.wikipedia.org/wiki/Systém_jednotné_trigonometrické_sítě_katastrální
 * @param x Souradnice X [m].
 * @param y Souradnice Y [m].
 */
case class Jtsk(x: BigDecimal, y: BigDecimal) {
  def toGps: GpsPoint = Jtsk.toGps(this)
}

object Jtsk {
  implicit val rw: ReadWriter[Jtsk] = macroRW[Jtsk]

  /**
   * Transformace S-JTSK -> GPS.
   * Viz. http://martin.hinner.info/geo/
   * @param jtsk S-JTSK souradnice.
   * @return GPS souradnice.
   */
  def toGps(jtsk: Jtsk, height: BigDecimal = 245): GpsPoint = {
    // negativni Krovak
    val (_X, _Y) = {
      val (x, y) =
        if (jtsk.x < 0 && jtsk.y < 0) (-jtsk.x, -jtsk.y)
        else (jtsk.x, jtsk.y)
      if (y > x) (y, x) else (x, y)
    }
    /*Vypocet zemepisnych souradnic z rovinnych souradnic*/
    var a=6377397.15508
    val e=0.081696831215303
    val n=0.97992470462083
    val konst_u_ro=12310230.12797036
    val sinUQ=0.863499969506341
    val cosUQ=0.504348889819882
    val sinVQ=0.420215144586493
    val cosVQ=0.907424504992097
    val alfa=1.000597498371542
    val k=1.003419163966575
    var ro=Math.sqrt((_X*_X+_Y*_Y).toDouble)
    val epsilon=2*Math.atan((_Y/(ro+_X)).toDouble)
    val D=epsilon/n
    val S=2*Math.atan(Math.exp(1/n*Math.log(konst_u_ro/ro)))-Math.PI/2
    val sinS=Math.sin(S)
    val cosS=Math.cos(S)
    val sinU=sinUQ*sinS-cosUQ*cosS*Math.cos(D)
    val cosU=Math.sqrt(1-sinU*sinU)
    val sinDV=Math.sin(D)*cosS/cosU
    val cosDV=Math.sqrt(1-sinDV*sinDV)
    val sinV=sinVQ*cosDV-cosVQ*sinDV
    val cosV=cosVQ*cosDV+sinVQ*sinDV
    val Ljtsk=2*Math.atan(sinV/(1+cosV))/alfa
    var t=Math.exp(2/alfa*Math.log((1+sinU)/cosU/k))
    var pom=(t-1)/(t+1)
    var sinB=pom
    do {
     sinB=pom
     pom=t*Math.exp(e*Math.log((1+e*sinB)/(1-e*sinB)))
     pom=(pom-1)/(pom+1)
    } while (Math.abs(pom-sinB)>1e-15)

    val Bjtsk=Math.atan(pom/Math.sqrt(1-pom*pom))
    /* Pravoúhlé souřadnice ve S-JTSK */
    //a=6377397.15508
    var f_1=299.152812853
    var e2=1-(1-1/f_1)*(1-1/f_1)
    ro=a/Math.sqrt(1-e2*Math.sin(Bjtsk)*Math.sin(Bjtsk))
    val x=(ro+height)*Math.cos(Bjtsk)*Math.cos(Ljtsk)
    val y=(ro+height)*Math.cos(Bjtsk)*Math.sin(Ljtsk)
    val z=((1-e2)*ro+height)*Math.sin(Bjtsk)

    /* Pravoúhlé souřadnice v WGS-84*/
    val dx = 570.69
    val dy = 85.69
    val dz = 462.84
    val wz = -5.2611/3600*Math.PI/180
    val wy = -1.58676/3600*Math.PI/180
    val wx = -4.99821/3600*Math.PI/180
    val m = 3.543e-6
    val xn=dx+(1+m)*(x+wz*y-wy*z)
    val yn=dy+(1+m)*(-wz*x+y+wx*z)
    val zn=dz+(1+m)*(wy*x-wx*y+z)

    /* Geodetické souřadnice v systému WGS-84*/
    a=6378137.0
    f_1=298.257223563
    val a_b=f_1/(f_1-1)
    val p=Math.sqrt((xn*xn+yn*yn).toDouble)
    e2=1-(1-1/f_1)*(1-1/f_1)
    val theta=Math.atan((zn*a_b/p).toDouble)
    val st=Math.sin(theta)
    val ct=Math.cos(theta)
    t=((zn+e2*a_b*a*st*st*st)/(p-e2*a*ct*ct*ct)).toDouble
    val B=Math.atan(t)
    val L=2*Math.atan((yn/(p+xn)).toDouble)
    //H=Math.sqrt(1+t*t)*(p-a/Math.sqrt(1+(1-e2)*t*t))

    /* Formát výstupních hodnot */
    //B=B/Math.PI*180
    //L=L/Math.PI*180
    GpsPoint(lat = Latitude(B/Math.PI*180), lon = Longitude(L/Math.PI*180))

  }
}
