package org.jetbrains.demo.thinkter

import org.jetbrains.ktor.locations.*


@location("/")
class Index()

@location("/post-new")
data class PostThought(val text: String = "", val date: Long = 0L, val code: String = "", val replyTo: Int? = null)

@location("/thought/{id}/delete")
data class ThoughtDelete(val id: Int, val date: Long, val code: String)

@location("/thought/{id}")
data class ViewThought(val id: Int)

@location("/user/{user}")
@Deprecated("")
data class UserPage(val user: String)

@location("/user/{user}/thoughts")
data class UserThoughts(val user: String)

@location("/register")
data class Register(val userId: String = "", val displayName: String = "", val email: String = "", val password: String = "", val error: String = "")

@location("/login")
data class Login(val userId: String = "", val password: String = "", val error: String = "")

@location("/logout")
class Logout()