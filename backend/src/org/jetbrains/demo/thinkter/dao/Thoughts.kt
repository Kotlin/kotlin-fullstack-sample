package org.jetbrains.demo.thinkter.dao

import org.jetbrains.squash.definition.*

object Thoughts : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val user = varchar("user_id", 20).index()
    val date = datetime("date")
    val replyTo = integer("reply_to").nullable().index()
    val directReplyTo = integer("direct_reply_to").nullable().index()
    val text = varchar("text", 1024)
}
