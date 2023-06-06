package com.autentia.tnt.binnacle.exception

class ActivityForBlockedProjectException(message: String) : BinnacleException(message) {
    constructor() : this("Project is blocked for that date")
}