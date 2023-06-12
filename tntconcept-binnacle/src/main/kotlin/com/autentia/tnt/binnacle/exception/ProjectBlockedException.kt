package com.autentia.tnt.binnacle.exception

class ProjectBlockedException(message: String) : BinnacleException(message) {
    constructor() : this("The project is blocked")
}