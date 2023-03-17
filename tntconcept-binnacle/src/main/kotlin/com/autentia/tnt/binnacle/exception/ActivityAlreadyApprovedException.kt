package com.autentia.tnt.binnacle.exception

class ActivityAlreadyApprovedException(message: String) : BinnacleException(message) {
    constructor() : this("Activity could not been approved.")
}
