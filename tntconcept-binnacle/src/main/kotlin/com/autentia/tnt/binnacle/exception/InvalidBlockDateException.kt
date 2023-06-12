package com.autentia.tnt.binnacle.exception

class InvalidBlockDateException(message: String) : BinnacleException(message) {
   constructor() : this("Invalid block date")
}
