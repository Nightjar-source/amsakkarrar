package com.karrar.amsakiah.core.astronomy

import kotlin.math.*

object AstronomicalEngine {

    const val DTR: Double = PI / 180.0
    const val RTD: Double = 180.0 / PI

    fun fixAngle(a: Double): Double {
        val b = a - 360.0 * floor(a / 360.0)
        return if (b < 0) b + 360.0 else b
    }

    fun fixHour(a: Double): Double {
        val b = a - 24.0 * floor(a / 24.0)
        return if (b < 0) b + 24.0 else b
    }

    fun dsin(d: Double): Double = sin(d * DTR)
    fun dcos(d: Double): Double = cos(d * DTR)
    fun darcsin(x: Double): Double = asin(x) * RTD
    fun darccos(x: Double): Double = acos(x) * RTD
    fun darctan2(y: Double, x: Double): Double = atan2(y, x) * RTD

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * DTR
        val dLon = (lon2 - lon1) * DTR
        val a = sin(dLat / 2).pow(2) + cos(lat1 * DTR) * cos(lat2 * DTR) * sin(dLon / 2).pow(2)
        return r * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

    fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    data class SolarPosition(val declination: Double, val eqt: Double)

    fun getSolarPosition(jd: Double): SolarPosition {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.02 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        val declination = darcsin(dsin(e) * dsin(l))
        val eqt = q / 15.0 - fixHour(ra)
        return SolarPosition(declination, eqt)
    }

    fun getHourAngle(angle: Double, declination: Double, latitude: Double): Double {
        val cosHA = (-dsin(angle) - dsin(declination) * dsin(latitude)) / (dcos(declination) * dcos(latitude))
        if (cosHA > 1.0 || cosHA < -1.0) return Double.NaN
        return darccos(cosHA) / 15.0
    }

    fun calculateQiblaAndMoon(lat: Double, lon: Double, jd: Double): QiblaAndMoon {
        val kaabaLat = 21.4225
        val kaabaLon = 39.8262
        val dLon = (kaabaLon - lon) * DTR
        val latRad = lat * DTR
        val kaabaLatRad = kaabaLat * DTR

        val y = sin(dLon)
        val x = cos(latRad) * tan(kaabaLatRad) - sin(latRad) * cos(dLon)
        var qibla = darctan2(y, x)
        qibla = (qibla + 360.0) % 360.0

        val distToKaaba = calculateDistance(lat, lon, kaabaLat, kaabaLon).roundToLong()

        // دورة القمر الفلكية (الشهر الاقتراني = 29.530588853 يوماً)
        val moonCycle = ((jd - 2451550.1) / 29.530588853) % 1.0
        val normalizedPhase = if (moonCycle < 0) moonCycle + 1.0 else moonCycle
        val illumination = ((1 - cos(normalizedPhase * 2 * PI)) / 2.0 * 100.0).roundToInt()
        val moonAgeDays = ((normalizedPhase * 29.530588853 * 10.0).roundToInt() / 10.0).toString()

        val (phaseName, phaseSymbol) = when {
            normalizedPhase <= 0.02 || normalizedPhase >= 0.98 -> Pair("محاق", "🌑")
            normalizedPhase < 0.23 -> Pair("هلال أول الشهر (متزايد)", "🌒")
            normalizedPhase <= 0.27 -> Pair("تربيع أول", "🌓")
            normalizedPhase < 0.48 -> Pair("أحدب متزايد", "🌔")
            normalizedPhase <= 0.52 -> Pair("بدر مكتمل", "🌕")
            normalizedPhase < 0.73 -> Pair("أحدب متناقص", "🌖")
            normalizedPhase <= 0.77 -> Pair("تربيع أخير", "🌗")
            else -> Pair("هلال آخر الشهر (متناقص)", "🌘")
        }

        return QiblaAndMoon(
            qiblaBearing = qibla,
            distanceKm = distToKaaba,
            phaseName = phaseName,
            phaseSymbol = phaseSymbol,
            illuminationPercent = illumination,
            normalizedPhase = normalizedPhase,
            moonAgeDays = moonAgeDays
        )
    }

    fun calculatePrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        lat: Double,
        lon: Double,
        tzOffset: Double,
        method: CalculationMethod,
        highLatRule: HighLatitudeRule = HighLatitudeRule.AQRAB_45,
        asrMethod: String = "shafii"
    ): PrayerTimes {
        val jd = getJulianDay(year, month, day)
        val (declination, eqt) = getSolarPosition(jd)
        val noonUtc = fixHour(12.0 - eqt)

        val fajrAngle = method.fajr
        val maghribAngle = method.maghribVal
        val ishaAngle = method.ishaVal
        val useMaghribAngle = method.maghribType == 0
        var ishaMinutesOffset = if (method.ishaType == 1) method.ishaVal else 0.0
        val isShiaMode = method.isShia

        val hijri = getHijriDateSafe(year, month, day, isShiaMode)
        if (method.code == "ummalqura" && hijri.monthNum == 9) {
            ishaMinutesOffset = 120.0
        }

        val sunsetAngle = 0.833
        var haSunrise = getHourAngle(sunsetAngle, declination, lat)

        if (haSunrise.isNaN()) {
            haSunrise = getHourAngle(sunsetAngle, declination, if (lat > 0) 47.5 else -47.5)
        }

        val sunriseUtc = noonUtc - haSunrise
        val sunsetUtc = noonUtc + haSunrise
        val nightDuration = (sunriseUtc + 24.0) - sunsetUtc

        val isHighLatRegion = abs(lat) >= 47.5
        val rawHaFajr = getHourAngle(fajrAngle, declination, lat)
        val isFajrAnomaly = isHighLatRegion && rawHaFajr.isNaN()
        val rawHaIsha = if (ishaMinutesOffset > 0) Double.NaN else getHourAngle(ishaAngle, declination, lat)
        val isIshaAnomaly = isHighLatRegion && ishaMinutesOffset == 0.0 && rawHaIsha.isNaN()
        val isSummerAnomaly = isHighLatRegion && (isFajrAnomaly || isIshaAnomaly)

        var fajrUtc = noonUtc - rawHaFajr
        var maghribUtc = if (useMaghribAngle) noonUtc + getHourAngle(maghribAngle, declination, lat) else sunsetUtc
        var ishaUtc = if (ishaMinutesOffset > 0) maghribUtc + (ishaMinutesOffset / 60.0) else noonUtc + getHourAngle(ishaAngle, declination, lat)
        var imsakUtc: Double

        if (isFajrAnomaly && highLatRule != HighLatitudeRule.NONE) {
            if (isShiaMode) {
                if (highLatRule == HighLatitudeRule.SAYEGH) {
                    val astronomicalMidnight = sunsetUtc + (nightDuration / 2.0)
                    imsakUtc = astronomicalMidnight - (15.0 / 60.0)
                    fajrUtc = astronomicalMidnight
                } else {
                    val calculationLatitude = if (highLatRule == HighLatitudeRule.AQRAB_45) (if (lat > 0) 45.0 else -45.0) else (if (lat > 0) 48.0 else -48.0)
                    val haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude)
                    val sunriseProj = noonUtc - haSunriseProj
                    var haFajrProj = getHourAngle(fajrAngle, declination, calculationLatitude)
                    if (haFajrProj.isNaN()) haFajrProj = getHourAngle(15.0, declination, calculationLatitude)
                    val fajrProj = noonUtc - haFajrProj
                    fajrUtc = sunriseUtc - (sunriseProj - fajrProj)
                    imsakUtc = fajrUtc - (10.0 / 60.0)
                }
            } else {
                when (highLatRule) {
                    HighLatitudeRule.SAB_UL_LAYL -> {
                        fajrUtc = sunriseUtc - (nightDuration / 7.0)
                        imsakUtc = fajrUtc - (10.0 / 60.0)
                    }
                    HighLatitudeRule.FIXED_90 -> {
                        fajrUtc = sunriseUtc - 1.5
                        imsakUtc = fajrUtc - (10.0 / 60.0)
                    }
                    HighLatitudeRule.ANGLE_BASED -> {
                        fajrUtc = sunriseUtc - (nightDuration * (fajrAngle / 60.0))
                        imsakUtc = fajrUtc - (10.0 / 60.0)
                    }
                    HighLatitudeRule.NIGHT_MIDDLE -> {
                        fajrUtc = sunriseUtc - (nightDuration * 0.5)
                        imsakUtc = fajrUtc - (10.0 / 60.0)
                    }
                    else -> {
                        val calculationLatitude = if (lat > 0) 45.0 else -45.0
                        val haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude)
                        val sunriseProj = noonUtc - haSunriseProj
                        val haFajrProj = getHourAngle(fajrAngle, declination, calculationLatitude)
                        val fajrProj = noonUtc - haFajrProj
                        fajrUtc = sunriseUtc - (sunriseProj - fajrProj)
                        imsakUtc = fajrUtc - (10.0 / 60.0)
                    }
                }
            }
        } else {
            imsakUtc = fajrUtc - (10.0 / 60.0)
        }

        if (isIshaAnomaly && ishaMinutesOffset == 0.0 && highLatRule != HighLatitudeRule.NONE) {
            if (isShiaMode) {
                if (highLatRule == HighLatitudeRule.SAYEGH) {
                    ishaUtc = sunsetUtc + 1.0
                } else {
                    val calculationLatitude = if (highLatRule == HighLatitudeRule.AQRAB_45) (if (lat > 0) 45.0 else -45.0) else (if (lat > 0) 48.0 else -48.0)
                    val haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude)
                    val sunsetProj = noonUtc + haSunriseProj
                    val ishaProj = noonUtc + getHourAngle(ishaAngle, declination, calculationLatitude)
                    ishaUtc = sunsetUtc + (ishaProj - sunsetProj)
                }
            } else {
                when (highLatRule) {
                    HighLatitudeRule.SAB_UL_LAYL -> ishaUtc = sunsetUtc + (nightDuration / 7.0)
                    HighLatitudeRule.FIXED_90 -> ishaUtc = sunsetUtc + 1.5
                    HighLatitudeRule.ANGLE_BASED -> ishaUtc = sunsetUtc + (nightDuration * (ishaAngle / 60.0))
                    HighLatitudeRule.NIGHT_MIDDLE -> ishaUtc = sunsetUtc + (nightDuration * 0.5)
                    else -> {
                        val calculationLatitude = if (lat > 0) 45.0 else -45.0
                        val haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude)
                        val sunsetProj = noonUtc + haSunriseProj
                        val ishaProj = noonUtc + getHourAngle(ishaAngle, declination, calculationLatitude)
                        ishaUtc = sunsetUtc + (ishaProj - sunsetProj)
                    }
                }
            }
        }

        if (isShiaMode) {
            if (isSummerAnomaly && highLatRule == HighLatitudeRule.SAYEGH) {
                maghribUtc = sunsetUtc + (15.0 / 60.0)
            } else {
                val rawMaghribHa = getHourAngle(maghribAngle, declination, lat)
                maghribUtc = if (rawMaghribHa.isNaN()) sunsetUtc + (15.0 / 60.0) else noonUtc + rawMaghribHa
            }
            if (maghribUtc < sunsetUtc + 0.15) {
                maghribUtc = sunsetUtc + (15.0 / 60.0)
            }
        } else {
            if (maghribUtc < sunsetUtc) maghribUtc = sunsetUtc
        }

        // حساب وقت العصر
        val shadowRatio = if (asrMethod == "hanafi") 2.0 else 1.0
        val stringAngleRad = abs(lat - declination) * DTR
        val asrAngleDeg = atan(1.0 / (shadowRatio + tan(stringAngleRad))) * RTD
        var asrUtc = noonUtc + getHourAngle(-asrAngleDeg, declination, lat)
        if (asrUtc.isNaN()) asrUtc = noonUtc + (sunsetUtc - noonUtc) * 0.5

        // منتصف الليل الشرعي
        val midnightStart = sunsetUtc
        var midnightEnd = if (isShiaMode) fajrUtc else sunriseUtc
        if (midnightEnd < midnightStart) midnightEnd += 24.0
        val midnightDuration = midnightEnd - midnightStart
        val midnightUtc = fixHour(midnightStart + (midnightDuration / 2.0))

        var isfarUtc: Double? = if (isShiaMode && isSummerAnomaly && highLatRule == HighLatitudeRule.SAYEGH) {
            // الإسفار وفق "تقويم الصائغ": صلاة الفجر الفعلية وفق حد الإشراق،
            // ... تُملأ فقط عند خطوط العرض العليا في الصيف الفلكي.
            sunriseUtc - 1.25
        } else {
            null
        }
        fun toLocal(utcHour: Double): Double = fixHour(utcHour + tzOffset - (lon / 15.0))

        return PrayerTimes(
            imsak = toLocal(imsakUtc),
            fajr = toLocal(fajrUtc),
            sunrise = toLocal(sunriseUtc),
            dhuhr = toLocal(noonUtc),
            asr = toLocal(asrUtc),
            sunset = toLocal(sunsetUtc),
            maghrib = toLocal(maghribUtc),
            isha = toLocal(ishaUtc),
            midnight = toLocal(midnightUtc),
            // الإسفار: فقط عند خطوط العرض العليا صيفًا (المرجع: الفجر الفلكي
            // في ظل "تقويم الصائغ" — يبدأ بعد الظهر فلكيًا + صلاة في وقت مبدئي).
            isfar = isfarUtc?.let { toLocal(it) },
            isHighLatAnomaly = isSummerAnomaly,
            declination = ((declination * 100.0).roundToInt() / 100.0).toString(),
            eqtMinutes = ((eqt * 60.0 * 10.0).roundToInt() / 10.0).toString(),
            dayLengthHours = (((sunsetUtc - sunriseUtc) * 10.0).roundToInt() / 10.0).toString(),
            nightLengthHours = ((nightDuration * 10.0).roundToInt() / 10.0).toString()
        )
    }

    fun getHijriDateSafe(year: Int, month: Int, day: Int, isShiaMode: Boolean = true, adjustmentDays: Int = 0): HijriDate {
        val jd = getJulianDay(year, month, day)
        val shiaOffset = if (isShiaMode) -1 else 0
        val jdHijri = jd - 1948439.5 + 0.5 + shiaOffset + adjustmentDays

        val cycles = floor(jdHijri / 10631.0).toInt()
        val rem = floor(jdHijri % 10631.0).toInt()
        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)

        var yearInCycle = 1
        var daysAccumulated = 0
        for (yr in 1..30) {
            val yearLen = if (leapYears.contains(yr)) 355 else 354
            if (daysAccumulated + yearLen > rem) {
                yearInCycle = yr
                break
            }
            daysAccumulated += yearLen
        }

        val daysThisYear = rem - daysAccumulated + 1
        val hYear = cycles * 30 + yearInCycle

        var hMonth = 1
        var hDay = daysThisYear
        val monthLengths = intArrayOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30)
        val isLeapYear = leapYears.contains(yearInCycle)

        for (mn in 1..12) {
            val mLen = if (mn == 12) (if (isLeapYear) 30 else 29) else monthLengths[mn - 1]
            if (hDay <= mLen) {
                hMonth = mn
                break
            }
            hDay -= mLen
        }

        val monthNames = arrayOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )

        return HijriDate(
            day = max(1, hDay),
            monthName = monthNames.getOrElse(hMonth - 1) { "محرم" },
            year = max(1, hYear),
            monthNum = hMonth,
            jd = jd
        )
    }

    fun formatArcHour(rawHourFloat: Double, use24Hour: Boolean = false): Triple<String, String, String> {
        if (rawHourFloat.isNaN()) return if (use24Hour) Triple("00:00", "", "00:00") else Triple("00:00", "صباحاً", "00:00")
        val local = ((rawHourFloat % 24.0) + 24.0) % 24.0
        var h = floor(local).toInt()
        var m = floor((local - h) * 60.0 + 0.5).toInt()
        if (m >= 60) {
            h = (h + 1) % 24
            m = 0
        }
        val mStr = m.toString().padStart(2, '0')
        val h24Str = h.toString().padStart(2, '0')
        return if (use24Hour) {
            Triple("$h24Str:$mStr", "", "$h24Str:$mStr")
        } else {
            val ap = if (h >= 12) "مساءً" else "صباحاً"
            val h12 = if (h % 12 == 0) 12 else h % 12
            Triple("$h12:$mStr", ap, "$h24Str:$mStr")
        }
    }
}
