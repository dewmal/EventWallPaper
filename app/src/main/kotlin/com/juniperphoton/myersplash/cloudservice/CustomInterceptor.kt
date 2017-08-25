package com.juniperphoton.myersplash.cloudservice

import okhttp3.Interceptor
import okhttp3.Response

class CustomInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val resp = chain.proceed(request)
        if (!resp.isSuccessful) {
            throw APIException(resp.code(), request.url().toString())
        }

        return resp
    }
}