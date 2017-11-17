package org.jetbrains.demo.thinkter.dao

import io.mockk.*
import io.mockk.junit.MockKJUnit4Runner
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.dialect.BaseSQLDialect
import org.jetbrains.squash.expressions.alias
import org.jetbrains.squash.expressions.invoke
import org.jetbrains.squash.results.Response
import org.jetbrains.squash.results.ResultRow
import org.jetbrains.squash.results.get
import org.jetbrains.squash.statements.DeleteQueryStatement
import org.jetbrains.squash.statements.InsertValuesStatement
import org.jetbrains.squash.statements.QueryStatement
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

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
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)

        every {
            row.get<Int>(0)
        } returns 3

        val nReplies = database.countReplies(1)
        Assert.assertEquals(3, nReplies)

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT COUNT(Thoughts.id), Thoughts.reply_to = ? FROM Thoughts" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun createThought() {
        every {
            with(tx()) {
                any<InsertValuesStatement<Thoughts, Int>>().execute()
            }
        } returns 1

        database.createThought("userId", "text")

        verify {
            with(tx()) {
                assert<InsertValuesStatement<Thoughts, Int>> {
                    "INSERT INTO Thoughts (user_id, \"date\", reply_to, text) VALUES (?, ?, NULL, ?)" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun deleteThought() {
        every {
            with(tx()) {
                any<DeleteQueryStatement<Thoughts>>().execute()
            }
        } just Runs

        database.deleteThought(1)

        verify {
            with(tx()) {
                assert<DeleteQueryStatement<Thoughts>> {
                    "DELETE FROM Thoughts  WHERE Thoughts.id = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun getThought() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)


        every { row[Thoughts.user] } returns "user"
        every { row[Thoughts.text] } returns "text"

        every {
            row[Thoughts.date]
        } returns LocalDateTime.now()

        every { row[Thoughts.replyTo] } returns null

        database.getThought(1)

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT * FROM Thoughts WHERE Thoughts.id = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun userThoughts() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)

        every { row[Thoughts.id] } returns 1

        database.userThoughts("id")

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT Thoughts.id FROM Thoughts WHERE Thoughts.user_id = ? ORDER BY Thoughts.\"date\" DESC NULLS LAST LIMIT ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun user() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)


        every { row[Users.email] } returns "email"
        every { row[Users.displayName] } returns "user"
        every { row[Users.passwordHash] } returns "hash"

        database.user("user", "hash")

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT * FROM Users WHERE Users.id = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun userByEmail() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)


        every { row[Users.id] } returns "user"
        every { row[Users.email] } returns "email"
        every { row[Users.displayName] } returns "user"
        every { row[Users.passwordHash] } returns "hash"

        database.userByEmail("email")

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT * FROM Users WHERE Users.email = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun createUser() {
        every {
            with(tx()) {
                any<InsertValuesStatement<Users, Unit>>().execute()
            }
        } just Runs

        database.createUser(User("id", "email", "name", "pwd"))

        verify {
            with(tx()) {
                assert<InsertValuesStatement<Thoughts, Unit>> {
                    "INSERT INTO Users (id, display_name, email, password_hash) VALUES (?, ?, ?, ?)" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun top() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)

        val k2 = Thoughts.alias("k2")
        every { row[Thoughts.id(k2)] } returns 1

        database.top()

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT Thoughts.id, COUNT(k2.id) FROM Thoughts LEFT OUTER JOIN Thoughts AS k2 ON Thoughts.id = k2.reply_to GROUP BY Thoughts.id ORDER BY COUNT(k2.id) DESC NULLS LAST LIMIT ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }

    @Test
    fun latest() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().execute()
            }
        } returns response

        mockSingleRowResponse(response, row)

        every { row[Thoughts.id] } returns 1

        database.latest(1)

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT Thoughts.id FROM Thoughts WHERE Thoughts.\"date\" > ? ORDER BY Thoughts.\"date\" DESC NULLS LAST LIMIT ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.execute()
            }
        }
    }


    private fun mockSingleRowResponse(response: Response, row: ResultRow) {
        every {
            response.iterator().hasNext()
        } returnsMany listOf(true, false)

        every {
            response.iterator().next()
        } returnsMany listOf(row, null)
    }
}
