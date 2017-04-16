// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package utils.application

import java.security.cert.X509Certificate

import play.api.mvc.{Headers, Request, RequestHeader}

trait RequestBuilder {
  def buildNewRequest[T](requestHeader: RequestHeader, requestBody: T): Request[T] = new Request[T] {
    override def body: T = requestBody
    override def clientCertificateChain: Option[Seq[X509Certificate]] = requestHeader.clientCertificateChain
    override def secure: Boolean = requestHeader.secure
    override def path: String = requestHeader.path
    override def id: Long = requestHeader.id
    override def remoteAddress: String = requestHeader.remoteAddress
    override def headers: Headers = requestHeader.headers
    override def method: String = requestHeader.method
    override def queryString: Map[String, Seq[String]] = requestHeader.queryString
    override def uri: String = requestHeader.uri
    override def version: String = requestHeader.version
    override def tags: Map[String, String] = requestHeader.tags
  }
}
