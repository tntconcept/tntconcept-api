package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import java.time.LocalDate

data class HolidayRequest(
    val id: Long,
    val description: String,
    val date: LocalDate,
) {

    companion object {
        fun from(holidayDto: HolidayDTO): HolidayRequest =
            HolidayRequest(
                holidayDto.id,
                holidayDto.description,
                holidayDto.date
            )
    }
}