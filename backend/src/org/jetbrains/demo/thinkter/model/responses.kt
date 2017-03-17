package org.jetbrains.demo.thinkter.model

interface RpcData

data class IndexResponse(val top: List<Thought>, val latest: List<Thought>) : RpcData
data class PostThoughtToken(val user: String, val date: Long, val code: String) : RpcData
data class PostThoughtResult(val thought: Thought) : RpcData
data class UserThoughtsResponse(val user: User, val thoughts: List<Thought>) : RpcData
data class ViewThoughtResponse(val thought: Thought, val date: Long, val code: String?) : RpcData
data class LoginResponse(val user: User? = null, val error: String? = null) : RpcData
data class PollResponse(val time: Long, val count: String) : RpcData