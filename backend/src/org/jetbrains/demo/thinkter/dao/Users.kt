package org.jetbrains.demo.thinkter.dao

import org.jetbrains.squash.definition.*

object Users : TableDefinition() {
    val id = varchar("id", 20).primaryKey()
    val email = varchar("email", 128).uniqueIndex()
    val displayName = varchar("display_name", 256)
    val passwordHash = varchar("password_hash", 64)
}
