package com.autentia.tnt.binnacle.exception

class ProjectNotFoundException (val id: Long, message: String)  : ResourceNotFoundException(message)  {
    constructor(id: Long) : this(id, "Project (id: $id) not found")
}
