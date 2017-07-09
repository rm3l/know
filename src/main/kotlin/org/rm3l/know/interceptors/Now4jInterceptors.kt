/*
 * Copyright (c) 2017 Armel Soro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.rm3l.know.interceptors

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.rm3l.know.NowClient

import java.io.IOException

class Now4jInterceptors private constructor() {

    init {
        throw UnsupportedOperationException("Not instantiable")
    }

    class HeadersInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestWithNewHeaders = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-By", NowClient::class.java.getCanonicalName())
                    .build()
            return chain.proceed(requestWithNewHeaders)
        }
    }

    class AuthenticationInterceptor(private val token: String) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestWithNewHeaders = chain.request().newBuilder()
                    .addHeader("Authorization",
                            String.format("Bearer %s", this.token))
                    .build()
            return chain.proceed(requestWithNewHeaders)
        }
    }

    class TeamInterceptor(private val team: String?) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val newRequest : Request
            if (team?.isNotBlank()?:false) {
                val request = chain.request()
                val newUrl = request.url().newBuilder()
                        .addQueryParameter("team", team)
                        .build()
                newRequest = request.newBuilder().url(newUrl).build()
            } else {
                newRequest = chain.request()
            }
            return chain.proceed(newRequest)
        }
    }

    companion object {
        val HEADERS_INTERCEPTOR = HeadersInterceptor()
    }

}
