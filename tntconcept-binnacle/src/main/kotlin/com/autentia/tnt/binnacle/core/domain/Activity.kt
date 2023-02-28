package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime
import kotlin.time.Duration


data class Activity(val duration: Duration, val date: LocalDateTime, val projectRole: ProjectRoleId)
