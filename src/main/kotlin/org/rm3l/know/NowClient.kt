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
package org.rm3l.know

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.rm3l.know.api.NowService
import org.rm3l.know.contract.ClientCallback
import org.rm3l.know.contract.Now
import org.rm3l.know.exceptions.UnsuccessfulResponseException
import org.rm3l.know.interceptors.Now4jInterceptors
import org.rm3l.know.resources.domains.*
import org.rm3l.know.resources.aliases.Alias
import org.rm3l.know.resources.aliases.Aliases
import org.rm3l.know.resources.aliases.DeleteAliasResponse
import org.rm3l.know.resources.certs.Certificate
import org.rm3l.know.resources.certs.CertificateCreationOrUpdateRequest
import org.rm3l.know.resources.certs.CertificateCreationOrUpdateResponse
import org.rm3l.know.resources.certs.Certificates
import org.rm3l.know.resources.deployments.Deployment
import org.rm3l.know.resources.deployments.DeploymentFileStructure
import org.rm3l.know.resources.deployments.GetDeploymentsResponse
import org.rm3l.know.resources.secrets.CreateOrUpdateSecretRequest
import org.rm3l.know.resources.secrets.GetSecretsResponse
import org.rm3l.know.resources.secrets.Secret
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.io.*
import java.time.ZonedDateTime

open class NowClient : Now {

    private val token: String
    private val team: String?
    private var nowService: NowService? = null

    private constructor() {
        //Read from ~/.now.json or NOW_TOKEN variable
        var tokenFound: String?
        var teamFound: String?
        val nowJsonFile = File(HOME_DIR, NOW_JSON)
        if (nowJsonFile.exists()) {
            val gson = Gson()
            val fromJson: Map<*, *>
            try {
                fromJson = gson.fromJson<Map<*, *>>(FileReader(nowJsonFile), Map::class.java)
            } catch (e: FileNotFoundException) {
                throw IllegalStateException(e)
            }

            tokenFound = fromJson.get(TOKEN)?.toString()
            teamFound = fromJson.get(TEAM)?.toString()
        } else {
            tokenFound = System.getProperty(NOW_TOKEN)
            if (tokenFound?.isBlank()?:true) {
                tokenFound = System.getenv(NOW_TOKEN)
            }
            teamFound = System.getProperty(NOW_TEAM)
            if (teamFound?.isBlank()?:true) {
                teamFound = System.getenv(NOW_TEAM)
            }
        }
        if (tokenFound?.isBlank()?:false) {
            tokenFound = null
        }
        if (teamFound?.isBlank()?:false) {
            teamFound = null
        }

        this.token = tokenFound?:throw IllegalStateException("Token not found")
        this.team = teamFound
        this.buildNowService()
    }

    private constructor(token: String, team: String? = null) {
        if (token.isBlank()) {
            throw IllegalArgumentException("Token cannot be blank")
        }
        this.token = token
        this.team = team
        this.buildNowService()
    }

    private constructor(httpClient: OkHttpClient) {
        this.token = ""
        this.team = null
        this.buildNowService(httpClient)
    }

    private fun buildNowService() {
        val httpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(Now4jInterceptors.HEADERS_INTERCEPTOR)
                .addInterceptor(Now4jInterceptors.AuthenticationInterceptor(
                        this.token))

        this.team?.let {
            httpClientBuilder.addInterceptor(Now4jInterceptors.TeamInterceptor(it))
        }

        this.buildNowService(httpClientBuilder.build())
    }

    private fun buildNowService(httpClient: OkHttpClient) {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
        this.nowService = retrofit.create<NowService>(NowService::class.java)
    }

    @Throws(IOException::class)
    override fun getDeployments(): List<Deployment> {
            val response = this.nowService!!.getDeployments().execute()
            if (!response.isSuccessful) {
                throw UnsuccessfulResponseException(response.code(), response.message())
            }
            return response.body()?.deployments?:emptyList<Deployment>()
    }

    @Throws(IOException::class)
    override fun getDeployments(callback: ClientCallback<List<Deployment>>) {
        this.nowService!!.getDeployments()
                .enqueue(object : Callback<GetDeploymentsResponse> {
                    override fun onResponse(call: Call<GetDeploymentsResponse>, response: Response<GetDeploymentsResponse>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        val body = response.body()
                        callback.onSuccess(if (body != null) body.deployments else emptyList<Deployment>())
                    }

                    override fun onFailure(call: Call<GetDeploymentsResponse>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getDeployment(deploymentId: String): Deployment {
        val response = this.nowService!!.getDeployment(deploymentId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun getDeployment(deploymentId: String, callback: ClientCallback<Deployment>) {
        this.nowService!!.getDeployment(deploymentId)
                .enqueue(object : Callback<Deployment> {
                    override fun onResponse(call: Call<Deployment>, response: Response<Deployment>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body())
                    }

                    override fun onFailure(call: Call<Deployment>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun createDeployment(body: Map<String, Any>): Deployment {
        val response = this.nowService!!.createDeployment(body).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun createDeployment(body: Map<String, Any>, callback: ClientCallback<Deployment>) {
        this.nowService!!.createDeployment(body)
                .enqueue(object : Callback<Deployment> {
                    override fun onResponse(call: Call<Deployment>, response: Response<Deployment>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body())
                    }

                    override fun onFailure(call: Call<Deployment>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun deleteDeployment(deploymentId: String) {
        val response = this.nowService!!.deleteDeployment(deploymentId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
    }

    @Throws(IOException::class)
    override fun deleteDeployment(deploymentId: String, callback: ClientCallback<Void>) {
        this.nowService!!.deleteDeployment(deploymentId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(null)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getFiles(deploymentId: String): List<DeploymentFileStructure> {
        val response = this.nowService!!.getFiles(deploymentId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun getFiles(deploymentId: String, callback: ClientCallback<List<DeploymentFileStructure>>) {
        this.nowService!!.getFiles(deploymentId)
                .enqueue(object : Callback<List<DeploymentFileStructure>> {
                    override fun onResponse(call: Call<List<DeploymentFileStructure>>, response: Response<List<DeploymentFileStructure>>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body())
                    }

                    override fun onFailure(call: Call<List<DeploymentFileStructure>>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getFileAsString(deploymentId: String, fileId: String): String {
        val response = this.nowService!!.getFile(deploymentId, fileId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        val responseBody = response.body()
        return responseBody?.string()!!
    }

    @Throws(IOException::class)
    override fun getFileAsString(deploymentId: String,
                                 fileId: String,
                                 callback: ClientCallback<String>) {
        this.nowService!!.getFile(deploymentId, fileId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        val responseBody = response.body()
                        try {
                            callback.onSuccess(responseBody?.string())
                        } catch (e: IOException) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getFileAsInputStream(deploymentId: String,
                                      fileId: String): InputStream {
        val response = this.nowService!!.getFile(deploymentId, fileId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        val responseBody = response.body()
        return responseBody?.byteStream()!!
    }

    @Throws(IOException::class)
    override fun getFileAsInputStream(deploymentId: String,
                                      fileId: String,
                                      callback: ClientCallback<InputStream>) {
        this.nowService!!.getFile(deploymentId, fileId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        val responseBody = response.body()
                        callback.onSuccess(responseBody?.byteStream())
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    override val domains: List<Domain>
        @Throws(IOException::class)
        get() {
            val response = this.nowService!!.getDomains().execute()
            if (!response.isSuccessful) {
                throw UnsuccessfulResponseException(response.code(), response.message())
            }
            return response.body()?.domains?:emptyList<Domain>()
        }

    @Throws(IOException::class)
    override fun getDomains(callback: ClientCallback<List<Domain>>) {
        this.nowService!!.getDomains()
                .enqueue(object : Callback<Domains> {
                    override fun onResponse(call: Call<Domains>, response: Response<Domains>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        val body = response.body()
                        callback.onSuccess(body?.domains?:emptyList<Domain>())
                    }

                    override fun onFailure(call: Call<Domains>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun addDomain(name: String, isExternalDNS: Boolean): Domain {
        val newDomain = Domain()
        newDomain.name = name
        newDomain.external = isExternalDNS
        val response = this.nowService!!.createDomain(newDomain).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun addDomain(name: String, isExternalDNS: Boolean, callback: ClientCallback<Domain>) {
        val newDomain = Domain()
        newDomain.name = name
        newDomain.external = isExternalDNS
        this.nowService!!.createDomain(newDomain)
                .enqueue(object : Callback<Domain> {
                    override fun onResponse(call: Call<Domain>, response: Response<Domain>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body())
                    }

                    override fun onFailure(call: Call<Domain>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun deleteDomain(name: String): String {
        val response = this.nowService!!.deleteDomain(name).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        val body = response.body()
        return body?.uid!!
    }

    @Throws(IOException::class)
    override fun deleteDomain(name: String, callback: ClientCallback<String>) {
        this.nowService!!.deleteDomain(name)
                .enqueue(object : Callback<Domain> {
                    override fun onResponse(call: Call<Domain>, response: Response<Domain>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        val body = response.body()
                        callback.onSuccess(body?.uid)
                    }

                    override fun onFailure(call: Call<Domain>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getDomainRecords(name: String): List<DomainRecord> {
        val response = this.nowService!!.getDomainRecords(name).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        val body = response.body()
        return body?.records?:emptyList<DomainRecord>()
    }

    @Throws(IOException::class)
    override fun getDomainRecords(name: String, callback: ClientCallback<List<DomainRecord>>) {
        this.nowService!!.getDomainRecords(name)
                .enqueue(object : Callback<DomainRecords> {
                    override fun onResponse(call: Call<DomainRecords>, response: Response<DomainRecords>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        val body = response.body()
                        callback.onSuccess(body?.records?:emptyList<DomainRecord>())
                    }

                    override fun onFailure(call: Call<DomainRecords>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun addDomainRecord(name: String, record: DomainRecord): DomainRecord {
        val domainRecordCreationRequest = DomainRecordCreationRequest()
        domainRecordCreationRequest.data = record
        val response = this.nowService!!
                .createDomainRecord(name, domainRecordCreationRequest).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun addDomainRecord(name: String, record: DomainRecord, callback: ClientCallback<DomainRecord>) {
        val domainRecordCreationRequest = DomainRecordCreationRequest()
        domainRecordCreationRequest.data = record
        this.nowService!!.createDomainRecord(name, domainRecordCreationRequest)
                .enqueue(object : Callback<DomainRecord> {
                    override fun onResponse(call: Call<DomainRecord>, response: Response<DomainRecord>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body())
                    }

                    override fun onFailure(call: Call<DomainRecord>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun deleteDomainRecord(domainName: String, recordId: String) {
        val response = this.nowService!!.deleteDomainRecord(domainName, recordId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
    }

    @Throws(IOException::class)
    override fun deleteDomainRecord(domainName: String, recordId: String, callback: ClientCallback<Void>) {
        this.nowService!!.deleteDomainRecord(domainName, recordId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(null)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getCertificates(commonName: String): List<Certificate> {
        val response = this.nowService!!.getCertificates(commonName).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()?.certs ?: emptyList<Certificate>()
    }

    @Throws(IOException::class)
    override fun getCertificates(commonName: String, callback: ClientCallback<List<Certificate>>) {
        this.nowService!!.getCertificates(commonName)
                .enqueue(object : Callback<Certificates> {
                    override fun onResponse(call: Call<Certificates>, response: Response<Certificates>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body()?.certs ?: emptyList<Certificate>())
                    }

                    override fun onFailure(call: Call<Certificates>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun createCertificate(domains: List<String>): String {
        val request = CertificateCreationOrUpdateRequest()
        request.domains = domains
        val response = this.nowService!!.issueCertificate(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()?.uid!!
    }

    @Throws(IOException::class)
    override fun createCertificate(domains: List<String>, callback: ClientCallback<String>) {
        val request = CertificateCreationOrUpdateRequest()
        request.domains = domains
        this.nowService!!.issueCertificate(request)
                .enqueue(object : Callback<CertificateCreationOrUpdateResponse> {
                    override fun onResponse(call: Call<CertificateCreationOrUpdateResponse>, response: Response<CertificateCreationOrUpdateResponse>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body()?.uid)
                    }

                    override fun onFailure(call: Call<CertificateCreationOrUpdateResponse>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun renewCertificate(domains: List<String>): String {
        val request = CertificateCreationOrUpdateRequest()
        request.domains = domains
        request.renew = true
        val response = this.nowService!!.createOrReplaceCertificate(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()?.uid!!
    }

    @Throws(IOException::class)
    override fun renewCertificate(domains: List<String>, callback: ClientCallback<String>) {
        val request = CertificateCreationOrUpdateRequest()
        request.domains = domains
        request.renew = true
        this.nowService!!.createOrReplaceCertificate(request)
                .enqueue(object : Callback<CertificateCreationOrUpdateResponse> {
                    override fun onResponse(call: Call<CertificateCreationOrUpdateResponse>, response: Response<CertificateCreationOrUpdateResponse>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body()?.uid)
                    }

                    override fun onFailure(call: Call<CertificateCreationOrUpdateResponse>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun replaceCertificate(domains: List<String>, ca: String, cert: String, key: String): ZonedDateTime {
        val request = CertificateCreationOrUpdateRequest()
        request.domains = domains
        request.ca = ca
        request.cert = cert
        request.key = key
        val response = this.nowService!!.createOrReplaceCertificate(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()?.created_at!!
    }

    @Throws(IOException::class)
    override fun replaceCertificate(domains: List<String>, ca: String, cert: String, key: String, callback: ClientCallback<ZonedDateTime>) {
        val request = CertificateCreationOrUpdateRequest()
        request.domains = domains
        request.ca = ca
        request.cert = cert
        request.key = key
        this.nowService!!.createOrReplaceCertificate(request)
                .enqueue(object : Callback<CertificateCreationOrUpdateResponse> {
                    override fun onResponse(call: Call<CertificateCreationOrUpdateResponse>, response: Response<CertificateCreationOrUpdateResponse>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body()?.created_at)
                    }

                    override fun onFailure(call: Call<CertificateCreationOrUpdateResponse>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun deleteCertificate(commonName: String) {
        val response = this.nowService!!.deleteCertificate(commonName).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
    }

    @Throws(IOException::class)
    override fun deleteCertificate(commonName: String, callback: ClientCallback<Void>) {
        this.nowService!!.deleteCertificate(commonName)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(null)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    override val aliases: List<Alias>
        @Throws(IOException::class)
        get() {
            val response = this.nowService!!.getAliases().execute()
            if (!response.isSuccessful) {
                throw UnsuccessfulResponseException(response.code(), response.message())
            }
            return response.body()?.aliases ?: emptyList<Alias>()
        }

    @Throws(IOException::class)
    override fun getAliases(callback: ClientCallback<List<Alias>>) {
        this.nowService!!.getAliases()
                .enqueue(object : Callback<Aliases> {
                    override fun onResponse(call: Call<Aliases>, response: Response<Aliases>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(response.body()?.aliases ?: emptyList<Alias>())
                        }
                    }

                    override fun onFailure(call: Call<Aliases>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun deleteAlias(aliasId: String): String {
        val response = this.nowService!!.deleteAlias(aliasId).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()?.status!!
    }

    @Throws(IOException::class)
    override fun deleteAlias(aliasId: String, callback: ClientCallback<String>) {
        this.nowService!!.deleteAlias(aliasId)
                .enqueue(object : Callback<DeleteAliasResponse> {
                    override fun onResponse(call: Call<DeleteAliasResponse>, response: Response<DeleteAliasResponse>) {
                        if (!response.isSuccessful || response.body() == null) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(response.body()?.status)
                        }
                    }

                    override fun onFailure(call: Call<DeleteAliasResponse>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun getDeploymentAliases(deploymentId: String): List<Alias> {
        val response = this.nowService!!.getDeploymentAliases(deploymentId).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()?.aliases ?: emptyList<Alias>()
    }

    @Throws(IOException::class)
    override fun getDeploymentAliases(deploymentId: String, callback: ClientCallback<List<Alias>>) {
        this.nowService!!.getDeploymentAliases(deploymentId)
                .enqueue(object : Callback<Aliases> {
                    override fun onResponse(call: Call<Aliases>, response: Response<Aliases>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body()?.aliases ?: emptyList<Alias>())
                    }

                    override fun onFailure(call: Call<Aliases>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun createDeploymentAlias(deploymentId: String, alias: String): Alias {
        val aliasToCreate = Alias()
        aliasToCreate.alias = alias
        val response = this.nowService!!.createDeploymentAliases(deploymentId, aliasToCreate).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun createDeploymentAlias(deploymentId: String, alias: String, callback: ClientCallback<Alias>) {
        val aliasToCreate = Alias()
        aliasToCreate.alias = alias
        this.nowService!!.createDeploymentAliases(deploymentId, aliasToCreate)
                .enqueue(object : Callback<Alias> {
                    override fun onResponse(call: Call<Alias>, response: Response<Alias>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(response.body())
                        }

                    }

                    override fun onFailure(call: Call<Alias>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    override val secrets: List<Secret>
        @Throws(IOException::class)
        get() {
            val response = this.nowService!!.getSecrets().execute()
            if (!response.isSuccessful) {
                throw UnsuccessfulResponseException(response.code(), response.message())
            }
            return response.body()?.secrets ?: emptyList<Secret>()
        }

    @Throws(IOException::class)
    override fun getSecrets(callback: ClientCallback<List<Secret>>) {
        this.nowService!!.getSecrets()
                .enqueue(object : Callback<GetSecretsResponse> {
                    override fun onResponse(call: Call<GetSecretsResponse>, response: Response<GetSecretsResponse>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                            return
                        }
                        callback.onSuccess(response.body()?.secrets ?: emptyList<Secret>())
                    }

                    override fun onFailure(call: Call<GetSecretsResponse>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun createSecret(name: String, value: String): Secret {
        val request = CreateOrUpdateSecretRequest()
        request.name = name
        request.value = value
        val response = this.nowService!!.createSecret(request).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun createSecret(name: String, value: String, callback: ClientCallback<Secret>) {
        val request = CreateOrUpdateSecretRequest()
        request.name = name
        request.value = value
        this.nowService!!.createSecret(request)
                .enqueue(object : Callback<Secret> {
                    override fun onResponse(call: Call<Secret>, response: Response<Secret>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(response.body())
                        }
                    }

                    override fun onFailure(call: Call<Secret>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun renameSecret(uidOrName: String, newName: String): Secret {
        val request = CreateOrUpdateSecretRequest()
        request.name = newName
        val response = this.nowService!!.editSecret(uidOrName, request).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun renameSecret(uidOrName: String, newName: String, callback: ClientCallback<Secret>) {
        val request = CreateOrUpdateSecretRequest()
        request.name = newName
        this.nowService!!.editSecret(uidOrName, request)
                .enqueue(object : Callback<Secret> {
                    override fun onResponse(call: Call<Secret>, response: Response<Secret>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(response.body())
                        }
                    }

                    override fun onFailure(call: Call<Secret>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    @Throws(IOException::class)
    override fun deleteSecret(uidOrName: String): Secret {
        val response = this.nowService!!.deleteSecret(uidOrName).execute()
        if (!response.isSuccessful) {
            throw UnsuccessfulResponseException(response.code(), response.message())
        }
        return response.body()!!
    }

    @Throws(IOException::class)
    override fun deleteSecret(uidOrName: String, callback: ClientCallback<Secret>) {
        this.nowService!!.deleteSecret(uidOrName)
                .enqueue(object : Callback<Secret> {
                    override fun onResponse(call: Call<Secret>, response: Response<Secret>) {
                        if (!response.isSuccessful) {
                            this.onFailure(call, UnsuccessfulResponseException(response.code(), response.message()))
                        } else {
                            callback.onSuccess(response.body())
                        }
                    }

                    override fun onFailure(call: Call<Secret>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
    }

    companion object {

        private val NOW_TOKEN = "NOW_TOKEN"
        private val NOW_TEAM = "NOW_TEAM"
        private val TOKEN = "token"
        private val TEAM = "team"
        private val HOME_DIR = System.getProperty("user.home")
        private val NOW_JSON = ".now.json"
        private val BASE_API_URL = "https://api.zeit.co/"

        fun create(): NowClient {
            return NowClient()
        }

        fun create(token: String): NowClient {
            return NowClient(token)
        }

        fun create(token: String,
                   team: String?): NowClient {
            return NowClient(token, team)
        }

        fun create(httpClient: OkHttpClient): NowClient {
            return NowClient(httpClient)
        }
    }
}
