package com.autentia.tnt.binnacle.exception

class ProjectClosedException(message: String) : BinnacleException(message) {
    constructor() : this("The project is closed")
}
