@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.jetbrains.demo.thinkter

import io.ktor.locations.Location


@Location("/")
class Index

@Location("/poll")
class Poll(val lastTime: String = "")

@Location("/post-new")
data class PostThought(val text: String = "", val date: Long = 0L, val code: String = "", val replyTo: Int? = null)

@Location("/thought/{id}/delete")
data class ThoughtDelete(val id: Int, val date: Long, val code: String)

@Location("/thought/{id}")
data class ViewThought(val id: Int)

@Location("/user/{user}")
@Deprecated("")
data class UserPage(val user: String)

@Location("/user/{user}/thoughts")
data class UserThoughts(val user: String)

@Location("/register")
data class Register(val userId: String = "", val displayName: String = "", val email: String = "", val password: String = "", val error: String = "")

@Location("/login")
data class Login(val userId: String = "", val password: String = "", val error: String = "")

@Location("/logout")
class Logout