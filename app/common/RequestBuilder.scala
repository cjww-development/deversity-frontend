/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package common

import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{RemoteConnection, RequestTarget}
import play.api.mvc.{Headers, Request, RequestHeader}

trait RequestBuilder {
  def buildNewRequest[T](requestHeader : RequestHeader, requestBody : T) : Request[T] = new Request[T] {
    override def body: T                                              = requestBody
    override def method: String                                       = requestHeader.method
    override def headers: Headers                                     = requestHeader.headers
    override def version: String                                      = requestHeader.version
    override def connection: RemoteConnection                         = requestHeader.connection
    override def target: RequestTarget                                = requestHeader.target
    override def attrs: TypedMap                                      = requestHeader.attrs
  }
}
