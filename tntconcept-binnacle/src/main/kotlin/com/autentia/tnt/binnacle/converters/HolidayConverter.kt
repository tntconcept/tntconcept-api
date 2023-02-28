package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import jakarta.inject.Singleton

@Singleton
class HolidayConverter() {

    fun toHolidayDTO(holiday: Holiday) =
        HolidayDTO(
            holiday.id,
            holiday.description,
            holiday.date.toLocalDate()
        )

}
