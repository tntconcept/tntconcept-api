package com.autentia.tnt.binnacle.exception

import java.time.LocalDate

class ProjectBlockedException(val blockedDate: LocalDate) :
    BinnacleException("The project is blocked")