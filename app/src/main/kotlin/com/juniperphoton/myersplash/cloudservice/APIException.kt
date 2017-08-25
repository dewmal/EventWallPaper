package com.juniperphoton.myersplash.cloudservice

data class APIException(val code: Int,
                        val ur: String?,
                        val msg: String? = null
) : Exception()