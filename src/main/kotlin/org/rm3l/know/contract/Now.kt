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
package org.rm3l.know.contract

import org.rm3l.know.exceptions.UnsuccessfulResponseException
import org.rm3l.know.resources.aliases.Alias
import org.rm3l.know.resources.certs.Certificate
import org.rm3l.know.resources.deployments.Deployment
import org.rm3l.know.resources.deployments.DeploymentFileStructure
import org.rm3l.know.resources.domains.Domain
import org.rm3l.know.resources.domains.DomainRecord
import org.rm3l.know.resources.secrets.Secret

import java.io.IOException
import java.io.InputStream
import java.time.ZonedDateTime

/**
 * Contract API for Now Clients
 */
interface Now {

    /**
     * Get all deployments

     * @return a list of all deployments
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    fun getDeployments(): List<Deployment>

    /**
     * Get all deployments, asynchronously.
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDeployments(callback: ClientCallback<List<Deployment>>)

    /**
     * Get a given deployment

     * @param deploymentId ID of deployment
     * *
     * @return the deployment
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDeployment(deploymentId: String): Deployment

    /**
     * Returns a given deployment, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDeployment(deploymentId: String,
                      callback: ClientCallback<Deployment>)

    /**
     * Creates a new deployment and returns its data

     * @param body The keys should represent a file path, with their respective values containing the file contents.
     * *
     * @return the deployment created
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createDeployment(body: Map<String, Any>): Deployment

    /**
     * Creates a new deployment and returns its data, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param body The keys should represent a file path, with their respective values containing the file contents.
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createDeployment(body: Map<String, Any>,
                         callback: ClientCallback<Deployment>)

    /**
     * Deletes a deployment

     * @param deploymentId ID of deployment
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteDeployment(deploymentId: String)

    /**
     * Deletes a deployment, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteDeployment(deploymentId: String,
                         callback: ClientCallback<Void>)

    /**
     * Returns a list with the deployment file structure
     * @param deploymentId ID of deployment
     * *
     * @return a list with the deployment file structure
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getFiles(deploymentId: String): List<DeploymentFileStructure>

    /**
     * Returns a list with the deployment file structure, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getFiles(deploymentId: String,
                 callback: ClientCallback<List<DeploymentFileStructure>>)

    /**
     * Returns the content of a file as a [String]

     * @param deploymentId ID of deployment
     * *
     * @param fileId ID of the file
     * *
     * @return the content of the file as an [String]
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getFileAsString(deploymentId: String,
                        fileId: String): String

    /**
     * Returns the content of a file as a [String], asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param fileId ID of the file
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getFileAsString(deploymentId: String,
                        fileId: String,
                        callback: ClientCallback<String>)

    /**
     * Returns the content of a file as an [InputStream]

     * @param deploymentId ID of deployment
     * *
     * @param fileId ID of the file
     * *
     * @return the content of the file as an [InputStream]
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getFileAsInputStream(deploymentId: String,
                             fileId: String): InputStream

    /**
     * Returns the content of a file as an [InputStream], asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param fileId ID of the file
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getFileAsInputStream(deploymentId: String,
                             fileId: String,
                             callback: ClientCallback<InputStream>)

    /**
     * Returns a list with all domain names and related aliases

     * @return a list with all domain names and related aliases
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDomains(): List<Domain>

    /**
     * Returns a list with all domain names and related aliases, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDomains(callback: ClientCallback<List<Domain>>)

    /**
     * Adds a new domain and returns its data
     * @param name the domain name
     * *
     * @param isExternalDNS whether this is an external DNS or not
     * *
     * @return the domain added
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun addDomain(name: String, isExternalDNS: Boolean): Domain

    /**
     * Adds a new domain and returns its data, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param name the domain name
     * *
     * @param isExternalDNS whether this is an external DNS or not
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun addDomain(name: String,
                  isExternalDNS: Boolean,
                  callback: ClientCallback<Domain>)

    /**
     * Deletes a domain name

     * @param name Domain name
     * *
     * @return the domain ID
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteDomain(name: String): String

    /**
     * Deletes a domain name, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param name Domain name
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteDomain(name: String,
                     callback: ClientCallback<String>)

    /**
     * Returns a list with all DNS records configured for a domain name

     * @param name Domain name
     * *
     * @return a list with all DNS records configured for a domain name
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDomainRecords(name: String): List<DomainRecord>

    /**
     * Returns a list with all DNS records configured for a domain name, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param name Domain name
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDomainRecords(name: String,
                         callback: ClientCallback<List<DomainRecord>>)

    /**
     * Adds a new DNS record for a domain

     * @param name Domain name
     * *
     * @param record the record data
     * *
     * @return the record created
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun addDomainRecord(name: String,
                        record: DomainRecord): DomainRecord

    /**
     * Adds a new DNS record for a domain, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param name Domain name
     * *
     * @param record the record data
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun addDomainRecord(name: String,
                        record: DomainRecord,
                        callback: ClientCallback<DomainRecord>)

    /**
     * Deletes a DNS record associated with a domain

     * @param domainName Domain name
     * *
     * @param recordId Record ID
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteDomainRecord(domainName: String,
                           recordId: String)

    /**
     * Deletes a DNS record associated with a domain, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param domainName Domain name
     * *
     * @param recordId Record ID
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteDomainRecord(domainName: String,
                           recordId: String,
                           callback: ClientCallback<Void>)

    /**
     * Returns a list of all certificates

     * @param commonName Common Name
     * *
     * @return the list of all certificates
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getCertificates(commonName: String): List<Certificate>

    /**
     * Returns a list of all certificates, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param commonName Common Name
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getCertificates(commonName: String,
                        callback: ClientCallback<List<Certificate>>)

    /**
     * Creates a new certificate for the given domains registered to the user

     * @param domains the list of domains
     * *
     * @return the certificate ID
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createCertificate(domains: List<String>): String

    /**
     * Creates a new certificate for the given domains registered to the user, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param domains the list of domains
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createCertificate(domains: List<String>,
                          callback: ClientCallback<String>)

    /**
     * Renews an existing certificate
     * @param domains the list of domains
     * *
     * @return the certificate ID
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun renewCertificate(domains: List<String>): String

    /**
     * Renews an existing certificate, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param domains the list of domains
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun renewCertificate(domains: List<String>,
                         callback: ClientCallback<String>)

    /**
     * Replace an existing certificate

     * @param domains the list of domains
     * *
     * @param ca X.509 certificate
     * *
     * @param cert Private key for the certificate
     * *
     * @param key CA certificate chain
     * *
     * @return the date at which the certificate has been replaced
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun replaceCertificate(domains: List<String>,
                           ca: String,
                           cert: String,
                           key: String): ZonedDateTime

    /**
     * Replace an existing certificate, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param domains the list of domains
     * *
     * @param ca X.509 certificate
     * *
     * @param cert Private key for the certificate
     * *
     * @param key CA certificate chain
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun replaceCertificate(domains: List<String>,
                           ca: String,
                           cert: String,
                           key: String,
                           callback: ClientCallback<ZonedDateTime>)

    /**
     * Deletes a certificate
     * @param commonName Common Name
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteCertificate(commonName: String)

    /**
     * Deletes a certificate, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param commonName Common Name
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteCertificate(commonName: String,
                          callback: ClientCallback<Void>)

    /**
     * Gets all aliases

     * @return a list of all aliases
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getAliases(): List<Alias>

    /**
     * Gets all aliases, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getAliases(callback: ClientCallback<List<Alias>>)

    /**
     * Delete an alias

     * @param aliasId the alias ID
     * *
     * @return the alias ID
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteAlias(aliasId: String): String

    /**
     * Delete an alias, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param aliasId the alias ID
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteAlias(aliasId: String,
                    callback: ClientCallback<String>)

    /**
     * Gets the list of aliases for the given deployment

     * @param deploymentId ID of deployment
     * *
     * @return the list of aliases for the given deployment
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDeploymentAliases(deploymentId: String): List<Alias>

    /**
     * Gets the list of aliases for the given deployment, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getDeploymentAliases(deploymentId: String,
                             callback: ClientCallback<List<Alias>>)

    /**
     * Creates an alias for the given deployment

     * @param deploymentId ID of deployment
     * *
     * @param alias Hostname or custom url for the alias
     * *
     * @return the alias created for the given deployment
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createDeploymentAlias(deploymentId: String,
                              alias: String): Alias

    /**
     * Creates an alias for the given deployment, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param deploymentId ID of deployment
     * *
     * @param alias Hostname or custom url for the alias
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createDeploymentAlias(deploymentId: String,
                              alias: String,
                              callback: ClientCallback<Alias>)

    /**
     * Returns a list with all secrets

     * @return a list of all secrets
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getSecrets(): List<Secret>

    /**
     * Returns a list with all secrets, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun getSecrets(callback: ClientCallback<List<Secret>>)

    /**
     * Creates a secret and returns it

     * @param name name for the secret
     * *
     * @param value value for the secret
     * *
     * @return the secret created
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createSecret(name: String,
                     value: String): Secret

    /**
     * Creates a secret and returns it, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param name name for the secret
     * *
     * @param value value for the secret
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun createSecret(name: String,
                     value: String,
                     callback: ClientCallback<Secret>)

    /**
     * Changes the name of the given secret and returns it

     * @param uidOrName id or name of the secret
     * *
     * @param newName new name for the secret
     * *
     * @return the secret updated
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun renameSecret(uidOrName: String,
                     newName: String): Secret

    /**
     * Changes the name of the given secret and returns it, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param uidOrName id or name of the secret
     * *
     * @param newName new name for the secret
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun renameSecret(uidOrName: String,
                     newName: String,
                     callback: ClientCallback<Secret>)

    /**
     * Deletes a secret and returns it

     * @param uidOrName ID or name of the secret
     * *
     * @return the secret, with its ID
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteSecret(uidOrName: String): Secret

    /**
     * Deletes a secret and returns it, asynchronously
     *
     *
     * You are notified (with either a result or an exception)
     * via the callback provided.

     * @param uidOrName ID or name of the secret
     * *
     * @param callback Callback object will be called asynchronously
     * *
     * @throws IOException if a problem occurred talking to the server.
     * *
     * @throws UnsuccessfulResponseException if response code got from the server was not successful
     */
    @Throws(IOException::class)
    fun deleteSecret(uidOrName: String,
                     callback: ClientCallback<Secret>)
}
