package com.autentia.tnt.binnacle.core.utils

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration

fun Duration.toBigDecimalHours(): BigDecimal =
    BigDecimal.valueOf(this.inWholeMinutes)
        .divide(BigDecimal.valueOf(60L), 2, RoundingMode.HALF_EVEN)
