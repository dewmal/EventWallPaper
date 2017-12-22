package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class CustomInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        var url = request.url().toString()
        if (url.last() == '&') {
            url = url.substring(0, url.length - 2)
        }
        url = "$url&client_id=${BuildConfig.UNSPLASH_APP_KEY}"
        builder.url(url)

        val resp = chain.proceed(builder.build())
        if (!resp.isSuccessful) {
            throw APIException(resp.code(), request.url().toString())
        }

        return resp
    }
}