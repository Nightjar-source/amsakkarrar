package com.karrar.amsakiah.core.astronomy

import kotlin.math.*

object AstronomicalEngine {

    const val DEGREES_TO_RADIANS: Double = PI / 180.0
    const val RADIANS_TO_DEGREES: Double = 180.0 / PI

    fun normalizeAngleInDegrees(angleDegrees: Double): Double {
        val normalized = angleDegrees - 360.0 * floor(angleDegrees / 360.0)
        return if (normalized < 0) normalized + 360.0 else normalized
    }

    fun normalizeHourOfDay(hour: Double): Double {
        val normalized = hour - 24.0 * floor(hour / 24.0)
        return if (normalized < 0) normalized + 24.0 else normalized
    }

    fun sineOfDegrees(angleDegrees: Double): Double = sin(angleDegrees * DEGREES_TO_RADIANS)
    fun cosineOfDegrees(angleDegrees: Double): Double = cos(angleDegrees * DEGREES_TO_RADIANS)
    fun arcSineOfDegrees(value: Double): Double = asin(value) * RADIANS_TO_DEGREES
    fun arcCosineOfDegrees(value: Double): Double = acos(value) * RADIANS_TO_DEGREES
    fun arctangentOfDegrees(sineValue: Double, cosineValue: Double): Double = atan2(sineValue, cosineValue) * RADIANS_TO_DEGREES

    fun greatCircleDistanceKm(latitude1: Double, longitude1: Double, latitude2: Double, longitude2: Double): Double {
        val earthRadiusKm = 6371.0
        val deltaLatitude = (latitude2 - latitude1) * DEGREES_TO_RADIANS
        val deltaLongitude = (longitude2 - longitude1) * DEGREES_TO_RADIANS
        val a = sin(deltaLatitude / 2).pow(2) +
            cos(latitude1 * DEGREES_TO_RADIANS) * cos(latitude2 * DEGREES_TO_RADIANS) * sin(deltaLongitude / 2).pow(2)
        return earthRadiusKm * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

    fun calculateJulianDay(year: Int, month: Int, day: Int): Double {
        var effectiveYear = year
        var effectiveMonth = month
        if (effectiveMonth <= 2) {
            effectiveYear -= 1
            effectiveMonth += 12
        }
        val century = floor(effectiveYear / 100.0)
        val gregorianCorrection = 2 - century + floor(century / 4.0)
        return floor(365.25 * (effectiveYear + 4716)) + floor(30.6001 * (effectiveMonth + 1)) + day + gregorianCorrection - 1524.5
    }

    data class SolarPosition(val declination: Double, val equationOfTime: Double)

    fun calculateSolarPosition(julianDay: Double): SolarPosition {
        val daysSinceJ2000 = julianDay - 2451545.0
        val meanAnomaly = normalizeAngleInDegrees(357.529 + 0.98560028 * daysSinceJ2000)
        val meanLongitude = normalizeAngleInDegrees(280.459 + 0.98564736 * daysSinceJ2000)
        val trueLongitude = normalizeAngleInDegrees(
            meanLongitude + 1.915 * sineOfDegrees(meanAnomaly) + 0.02 * sineOfDegrees(2 * meanAnomaly)
        )
        val obliquityOfEcliptic = 23.439 - 0.00000036 * daysSinceJ2000
        val rightAscension = arctangentOfDegrees(
            cosineOfDegrees(obliquityOfEcliptic) * sineOfDegrees(trueLongitude),
            cosineOfDegrees(trueLongitude)
        ) / 15.0
        val declination = arcSineOfDegrees(sineOfDegrees(obliquityOfEcliptic) * sineOfDegrees(trueLongitude))
        val equationOfTime = meanLongitude / 15.0 - normalizeHourOfDay(rightAscension)
        return SolarPosition(declination, equationOfTime)
    }

    fun calculateHourAngle(angle: Double, declination: Double, latitude: Double): Double {
        val cosHA = (-sineOfDegrees(angle) - sineOfDegrees(declination) * sineOfDegrees(latitude)) / (cosineOfDegrees(declination) * cosineOfDegrees(latitude))
        if (cosHA > 1.0 || cosHA < -1.0) return Double.NaN
        return arcCosineOfDegrees(cosHA) / 15.0
    }

    fun calculateQiblaAndMoonPhase(userLatitude: Double, userLongitude: Double, julianDay: Double): QiblaAndMoon {
        val kaabaLatitude = 21.4225
        val kaabaLongitude = 39.8262
        val deltaLongitude = (kaabaLongitude - userLongitude) * DEGREES_TO_RADIANS
        val userLatitudeRadians = userLatitude * DEGREES_TO_RADIANS
        val kaabaLatitudeRadians = kaabaLatitude * DEGREES_TO_RADIANS

        val numerator = sin(deltaLongitude)
        val denominator = cos(userLatitudeRadians) * tan(kaabaLatitudeRadians) -
            sin(userLatitudeRadians) * cos(deltaLongitude)
        var qiblaBearing = arctangentOfDegrees(numerator, denominator)
        qiblaBearing = (qiblaBearing + 360.0) % 360.0

        val distanceToKaaba = greatCircleDistanceKm(userLatitude, userLongitude, kaabaLatitude, kaabaLongitude).roundToLong()

        // دورة القمر الفلكية (الشهر الاقتراني = 29.530588853 يوماً)
        val moonCycle = ((julianDay - 2451550.1) / 29.530588853) % 1.0
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
            qiblaBearing = qiblaBearing,
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
        val julianDay = calculateJulianDay(year, month, day)
        val (declination, equationOfTime) = calculateSolarPosition(julianDay)
        val noonUtc = normalizeHourOfDay(12.0 - equationOfTime)

        val fajrAngle = method.fajr
        val maghribAngle = method.maghribVal
        val ishaAngle = method.ishaVal
        val useMaghribAngle = method.maghribType == 0
        var ishaMinutesOffset = if (method.ishaType == 1) method.ishaVal else 0.0
        val isShiaMode = method.isShia

        val hijri = calculateHijriDate(year, month, day, isShiaMode)
        if (method.code == "ummalqura" && hijri.monthNum == 9) {
            ishaMinutesOffset = 120.0
        }

        val sunsetAngle = 0.833
        var hourAngleSunrise = calculateHourAngle(sunsetAngle, declination, lat)

        if (hourAngleSunrise.isNaN()) {
            hourAngleSunrise = calculateHourAngle(sunsetAngle, declination, if (lat > 0) 47.5 else -47.5)
        }

        val sunriseUtc = noonUtc - hourAngleSunrise
        val sunsetUtc = noonUtc + hourAngleSunrise
        val nightDuration = (sunriseUtc + 24.0) - sunsetUtc

        val isHighLatRegion = abs(lat) >= 47.5
        val rawHaFajr = calculateHourAngle(fajrAngle, declination, lat)
        val isFajrAnomaly = isHighLatRegion && rawHaFajr.isNaN()
        val rawHaIsha = if (ishaMinutesOffset > 0) Double.NaN else calculateHourAngle(ishaAngle, declination, lat)
        val isIshaAnomaly = isHighLatRegion && ishaMinutesOffset == 0.0 && rawHaIsha.isNaN()
        val isSummerAnomaly = isHighLatRegion && (isFajrAnomaly || isIshaAnomaly)

        var fajrUtc = noonUtc - rawHaFajr
        var maghribUtc = if (useMaghribAngle) noonUtc + calculateHourAngle(maghribAngle, declination, lat) else sunsetUtc
        var ishaUtc = if (ishaMinutesOffset > 0) maghribUtc + (ishaMinutesOffset / 60.0) else noonUtc + calculateHourAngle(ishaAngle, declination, lat)
        var imsakUtc: Double

        if (isFajrAnomaly && highLatRule != HighLatitudeRule.NONE) {
            if (isShiaMode) {
                if (highLatRule == HighLatitudeRule.SAYEGH) {
                    val astronomicalMidnight = sunsetUtc + (nightDuration / 2.0)
                    imsakUtc = astronomicalMidnight - (15.0 / 60.0)
                    fajrUtc = astronomicalMidnight
                } else {
                    val calculationLatitude = if (highLatRule == HighLatitudeRule.AQRAB_45) (if (lat > 0) 45.0 else -45.0) else (if (lat > 0) 48.0 else -48.0)
                    val hourAngleSunriseProj = calculateHourAngle(sunsetAngle, declination, calculationLatitude)
                    val sunriseProj = noonUtc - hourAngleSunriseProj
                    var haFajrProj = calculateHourAngle(fajrAngle, declination, calculationLatitude)
                    if (haFajrProj.isNaN()) haFajrProj = calculateHourAngle(15.0, declination, calculationLatitude)
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
                        val hourAngleSunriseProj = calculateHourAngle(sunsetAngle, declination, calculationLatitude)
                        val sunriseProj = noonUtc - hourAngleSunriseProj
                        val haFajrProj = calculateHourAngle(fajrAngle, declination, calculationLatitude)
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
                    val hourAngleSunriseProj = calculateHourAngle(sunsetAngle, declination, calculationLatitude)
                    val sunsetProj = noonUtc + hourAngleSunriseProj
                    val ishaProj = noonUtc + calculateHourAngle(ishaAngle, declination, calculationLatitude)
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
                        val hourAngleSunriseProj = calculateHourAngle(sunsetAngle, declination, calculationLatitude)
                        val sunsetProj = noonUtc + hourAngleSunriseProj
                        val ishaProj = noonUtc + calculateHourAngle(ishaAngle, declination, calculationLatitude)
                        ishaUtc = sunsetUtc + (ishaProj - sunsetProj)
                    }
                }
            }
        }

        if (isShiaMode) {
            if (isSummerAnomaly && highLatRule == HighLatitudeRule.SAYEGH) {
                maghribUtc = sunsetUtc + (15.0 / 60.0)
            } else {
                val rawMaghribHa = calculateHourAngle(maghribAngle, declination, lat)
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
        val stringAngleRad = abs(lat - declination) * DEGREES_TO_RADIANS
        val asrAngleDeg = atan(1.0 / (shadowRatio + tan(stringAngleRad))) * RADIANS_TO_DEGREES
        var asrUtc = noonUtc + calculateHourAngle(-asrAngleDeg, declination, lat)
        if (asrUtc.isNaN()) asrUtc = noonUtc + (sunsetUtc - noonUtc) * 0.5

        // منتصف الليل الشرعي
        val midnightStart = sunsetUtc
        var midnightEnd = if (isShiaMode) fajrUtc else sunriseUtc
        if (midnightEnd < midnightStart) midnightEnd += 24.0
        val midnightDuration = midnightEnd - midnightStart
        val midnightUtc = normalizeHourOfDay(midnightStart + (midnightDuration / 2.0))

        var isfarUtc: Double? = if (isShiaMode && isSummerAnomaly && highLatRule == HighLatitudeRule.SAYEGH) {
            // الإسفار وفق "تقويم الصائغ": صلاة الفجر الفعلية وفق حد الإشراق،
            // ... تُملأ فقط عند خطوط العرض العليا في الصيف الفلكي.
            sunriseUtc - 1.25
        } else {
            null
        }
        fun toLocal(utcHour: Double): Double = normalizeHourOfDay(utcHour + tzOffset - (lon / 15.0))

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
            equationOfTimeMinutes = ((equationOfTime * 60.0 * 10.0).roundToInt() / 10.0).toString(),
            dayLengthHours = (((sunsetUtc - sunriseUtc) * 10.0).roundToInt() / 10.0).toString(),
            nightLengthHours = ((nightDuration * 10.0).roundToInt() / 10.0).toString()
        )
    }

    fun calculateHijriDate(year: Int, month: Int, day: Int, isShiaMode: Boolean = true, adjustmentDays: Int = 0): HijriDate {
        val julianDay = calculateJulianDay(year, month, day)
        val shiaOffset = if (isShiaMode) -1 else 0
        val julianDayHijri = julianDay - 1948439.5 + 0.5 + shiaOffset + adjustmentDays

        val cycles = floor(julianDayHijri / 10631.0).toInt()
        val rem = floor(julianDayHijri % 10631.0).toInt()
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
            julianDay = julianDay
        )
    }

    fun formatHourOfDay(rawHourFloat: Double, use24Hour: Boolean = false): Triple<String, String, String> {
        if (rawHourFloat.isNaN()) return if (use24Hour) Triple("00:00", "", "00:00") else Triple("00:00", "صباحاً", "00:00")
        val local = ((rawHourFloat % 24.0) + 24.0) % 24.0
        var localHour = floor(local).toInt()
        var localMinute = floor((local - localHour) * 60.0 + 0.5).toInt()
        if (localMinute >= 60) {
            localHour = (localHour + 1) % 24
            localMinute = 0
        }
        val mStr = localMinute.toString().padStart(2, '0')
        val h24Str = localHour.toString().padStart(2, '0')
        return if (use24Hour) {
            Triple("$h24Str:$mStr", "", "$h24Str:$mStr")
        } else {
            val ap = if (localHour >= 12) "مساءً" else "صباحاً"
            val h12 = if (localHour % 12 == 0) 12 else localHour % 12
            Triple("$h12:$mStr", ap, "$h24Str:$mStr")
        }
    }
}
