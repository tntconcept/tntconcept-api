package com.autentia.tnt.binnacle.exception

class UserPermissionException(message: String) : BinnacleException(message) {
    constructor() : this("You don't have permission to access the resource")
}
