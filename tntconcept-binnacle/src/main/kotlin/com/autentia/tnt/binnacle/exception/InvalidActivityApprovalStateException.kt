package com.autentia.tnt.binnacle.exception

class InvalidActivityApprovalStateException(message: String) : BinnacleException(message) {
    constructor() : this("Activity could not been approved")
}
