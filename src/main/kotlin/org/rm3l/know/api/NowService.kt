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
package org.rm3l.know.api

import okhttp3.ResponseBody
import org.rm3l.know.resources.domains.*
import org.rm3l.know.resources.aliases.Alias
import org.rm3l.know.resources.aliases.Aliases
import org.rm3l.know.resources.aliases.DeleteAliasResponse
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
import retrofit2.http.*

/**
 * Connector to Now's REST API
 */
interface NowService {

    @GET("now/deployments")
    fun getDeployments(): Call<GetDeploymentsResponse>

    @GET("now/deployments/{deployment_id}")
    fun getDeployment(@Path("deployment_id") deploymentId: String): Call<Deployment>

    @DELETE("now/deployments/{deployment_id}")
    fun deleteDeployment(@Path("deployment_id") deploymentId: String): Call<ResponseBody>

    @POST("now/deployments")
    fun createDeployment(@Body body: Map<String, Any?>): Call<Deployment>

    @GET("now/deployments/{deployment_id}/files")
    fun getFiles(@Path("deployment_id") deploymentId: String): Call<List<DeploymentFileStructure>>

    @GET("now/deployments/{deployment_id}/files/{file_id}")
    fun getFile(
            @Path("deployment_id") deploymentId: String,
            @Path("file_id") fileId: String): Call<ResponseBody>

    @GET("domains")
    fun getDomains(): Call<Domains>

    @POST("domains")
    fun createDomain(@Body domain: Domain): Call<Domain>

    @DELETE("domains/{domain_name}")
    fun deleteDomain(@Path("domain_name") domainName: String): Call<Domain>

    @GET("domains/{domain_name}/records")
    fun getDomainRecords(@Path("domain_name") domainName: String): Call<DomainRecords>

    @POST("domains/{domain_name}/records")
    fun createDomainRecord(@Path("domain_name") domainName: String,
                           @Body body: DomainRecordCreationRequest): Call<DomainRecord>

    @DELETE("domains/{domain_name}/records/{record_id}")
    fun deleteDomainRecord(@Path("domain_name") domainName: String,
                           @Path("record_id") recordId: String): Call<ResponseBody>

    @GET("now/certs/{common_name}")
    fun getCertificates(@Path("common_name") commonName: String): Call<Certificates>

    @POST("now/certs")
    fun issueCertificate(
            @Body request: CertificateCreationOrUpdateRequest): Call<CertificateCreationOrUpdateResponse>

    @PUT("now/certs")
    fun createOrReplaceCertificate(
            @Body request: CertificateCreationOrUpdateRequest): Call<CertificateCreationOrUpdateResponse>

    @DELETE("now/certs/{common_name}")
    fun deleteCertificate(@Path("common_name") commonName: String): Call<ResponseBody>

    @GET("now/aliases")
    fun getAliases(): Call<Aliases>

    @DELETE("now/aliases/{alias_id}")
    fun deleteAlias(@Path("alias_id") aliasId: String): Call<DeleteAliasResponse>


    @GET("deployments/{deployment_id}/aliases")
    fun getDeploymentAliases(@Path("deployment_id") deploymentId: String): Call<Aliases>

    @POST("deployments/{deployment_id}/aliases")
    fun createDeploymentAliases(@Path("deployment_id") deploymentId: String,
                                @Body alias: Alias): Call<Alias>

    @GET("now/secrets")
    fun getSecrets(): Call<GetSecretsResponse>

    @POST("now/secrets")
    fun createSecret(@Body request: CreateOrUpdateSecretRequest): Call<Secret>

    @PATCH("now/secrets/{secret_uid_or_name}")
    fun editSecret(@Path("secret_uid_or_name") secretUidOrName: String,
                   @Body request: CreateOrUpdateSecretRequest): Call<Secret>

    @DELETE("now/secrets/{secret_uid_or_name}")
    fun deleteSecret(@Path("secret_uid_or_name") secretUidOrName: String): Call<Secret>
}
