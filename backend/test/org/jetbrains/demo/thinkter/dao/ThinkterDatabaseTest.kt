package org.jetbrains.demo.thinkter.dao

import io.mockk.Runs
import io.mockk.every
import io.mockk.junit.MockKJUnit4Runner
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.demo.thinkter.model.Thought
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.dialect.BaseSQLDialect
import org.jetbrains.squash.results.Response
import org.jetbrains.squash.results.ResultRow
import org.jetbrains.squash.results.get
import org.jetbrains.squash.statements.InsertQueryStatement
import org.jetbrains.squash.statements.InsertValuesStatement
import org.jetbrains.squash.statements.QueryStatement
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(MockKJUnit4Runner::class)
class ThinkterDatabaseTest {
    val connection = mockk<DatabaseConnection>("db")
    fun tx() = connection.createTransaction()

    init {
        every { tx().databaseSchema().create(any()) } just Runs
        every { tx().close() } just Runs
    }

    val database = ThinkterDatabase(connection)

    @Test
    fun countReplies() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().hint(Response::class).execute()
            }
        } returns response

        every {
            response.iterator().hasNext()
        } returnsMany listOf(true, false)

        every {
            response.iterator().hint(ResultRow::class).next()
        } returnsMany listOf(row, null)

        every {
            row.hint(Int::class).get<Int>(0)
        } returns 3

        val nReplies = database.countReplies(1)
        assertEquals(3, nReplies)

        verify {
            with(connection.createTransaction()) {
                assert<QueryStatement> {
                    "SELECT COUNT(Thoughts.id), Thoughts.reply_to = ? FROM Thoughts" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Response::class).execute()
            }
        }
    }

    @Test
    fun createThought() {
        every {
            with(tx()) {
                any<InsertValuesStatement<Thoughts, Int>>().hint(Int::class).execute()
            }
        } returns 1

        database.createThought("userId", "text")

        verify {
            with(connection.createTransaction()) {
                assert<InsertValuesStatement<Thoughts, Int>> {
                    println(BaseSQLDialect("dialect").statementSQL(it).sql)
                    "INSERT INTO Thoughts (user_id, \"date\", reply_to, text) VALUES (?, ?, NULL, ?)" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Int::class).execute()
            }
        }
    }
}
