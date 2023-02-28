package com.autentia.tnt.binnacle.exception

class ProjectRoleNotFoundException(val id: Long, message: String) : ResourceNotFoundException(message) {
    constructor(id: Long) : this(id, "Project role (id: $id) not found")
}
