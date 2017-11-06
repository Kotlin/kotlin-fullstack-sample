package org.jetbrains.demo.thinkter.dao

import io.mockk.Runs
import io.mockk.every
import io.mockk.junit.MockKJUnit4Runner
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.demo.thinkter.model.User
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.dialect.BaseSQLDialect
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

        mockSingleRowResponse(response, row)

        every {
            row.hint(Int::class).get<Int>(0)
        } returns 3

        val nReplies = database.countReplies(1)
        Assert.assertEquals(3, nReplies)

        verify {
            with(tx()) {
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
            with(tx()) {
                assert<InsertValuesStatement<Thoughts, Int>> {
                    "INSERT INTO Thoughts (user_id, \"date\", reply_to, text) VALUES (?, ?, NULL, ?)" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Int::class).execute()
            }
        }
    }

    @Test
    fun deleteThought() {
        every {
            with(tx()) {
                any<DeleteQueryStatement<Thoughts>>().hint(Int::class).execute()
            }
        } just Runs

        database.deleteThought(1)

        verify {
            with(tx()) {
                assert<DeleteQueryStatement<Thoughts>> {
                    "DELETE FROM Thoughts  WHERE Thoughts.id = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Response::class).execute()
            }
        }
    }

    @Test
    @Ignore("requires mockk 1.4-SNAPSHOT")
    fun getThought() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().hint(Response::class).execute()
            }
        } returns response

        mockSingleRowResponse(response, row)


        every { row.hint(String::class)[Thoughts.user] } returns "user"
        every { row.hint(String::class)[Thoughts.text] } returns "text"

//        val localDateTimeRegistration = registerInstanceFactory(LocalDateTime::class, { LocalDateTime.now() })
//
//        localDateTimeRegistration.use {
//            every {
//                row.hint(LocalDateTime::class)[Thoughts.date]
//            } returns LocalDateTime.now()
//        }

        every { row.hint(Int::class)[Thoughts.replyTo] } returns null

        database.getThought(1)

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT * FROM Thoughts WHERE Thoughts.id = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Response::class).execute()
            }
        }
    }

    @Test
    fun userThoughts() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().hint(Response::class).execute()
            }
        } returns response

        mockSingleRowResponse(response, row)

        every { row.hint(Int::class)[Thoughts.id] } returns 1

        database.userThoughts("id")

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT Thoughts.id FROM Thoughts WHERE Thoughts.user_id = ? ORDER BY Thoughts.\"date\" DESC NULLS LAST LIMIT ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Response::class).execute()
            }
        }
    }

    @Test
    fun user() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().hint(Response::class).execute()
            }
        } returns response

        mockSingleRowResponse(response, row)


        every { row.hint(String::class)[Users.email] } returns "email"
        every { row.hint(String::class)[Users.displayName] } returns "user"
        every { row.hint(String::class)[Users.passwordHash] } returns "hash"

        database.user("user", "hash")

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT * FROM Users WHERE Users.id = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Response::class).execute()
            }
        }
    }

    @Test
    fun userByEmail() {
        val response = mockk<Response>()
        val row = mockk<ResultRow>()
        every {
            with(tx()) {
                any<QueryStatement>().hint(Response::class).execute()
            }
        } returns response

        mockSingleRowResponse(response, row)


        every { row.hint(String::class)[Users.id] } returns "user"
        every { row.hint(String::class)[Users.email] } returns "email"
        every { row.hint(String::class)[Users.displayName] } returns "user"
        every { row.hint(String::class)[Users.passwordHash] } returns "hash"

        database.userByEmail("email")

        verify {
            with(tx()) {
                assert<QueryStatement> {
                    "SELECT * FROM Users WHERE Users.email = ?" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Response::class).execute()
            }
        }
    }

    @Test
    fun createUser() {
        every {
            with(tx()) {
                any<InsertValuesStatement<Users, Int>>().hint(Int::class).execute()
            }
        } returns 1

        database.createUser(User("id", "email", "name", "pwd"))

        verify {
            with(tx()) {
                assert<InsertValuesStatement<Thoughts, Int>> {
                    "INSERT INTO Users (id, display_name, email, password_hash) VALUES (?, ?, ?, ?)" ==
                            BaseSQLDialect("dialect").statementSQL(it).sql
                }.hint(Int::class).execute()
            }
        }
    }

    private fun mockSingleRowResponse(response: Response, row: ResultRow) {
        every {
            response.iterator().hasNext()
        } returnsMany listOf(true, false)

        every {
            response.iterator().hint(ResultRow::class).next()
        } returnsMany listOf(row, null)
    }
}
