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

package helpers.connectors

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSCookie, WSResponse}
import play.api.test.Helpers.OK

import scala.xml.Elem

trait MockResponse {

  object FakeResponse {

    implicit class IntOps(int: Int) {
      def isBetween(range: Range): Boolean = range contains int
    }

    private val bodyKey: Int => String = statusCode => if(statusCode.isBetween(200 to 299)) "body" else "errorMessage"

    private def buildApiResponse(statusCode: Int, body: String): JsValue = Json.obj(
      "uri"                     -> "/test/uri",
      "method"                  -> "TEST",
      "status"                  -> statusCode,
      s"${bodyKey(statusCode)}" -> s"$body",
      "stats"                   -> Json.obj(
        "requestCompletedAt" -> s"${LocalDateTime.now}"
      )
    )

    private def buildResponse(statusCode: Int = OK, responseBody: String = "") = new WSResponse {
      override def status: Int                            = statusCode
      override def statusText: String                     = ???
      override def headers: Map[String, Seq[String]]      = ???
      override def underlying[T]: T                       = ???
      override def cookies: Seq[WSCookie]                 = ???
      override def cookie(name: String): Option[WSCookie] = ???
      override def body: String                           = ???
      override def bodyAsBytes: ByteString                = ???
      override def bodyAsSource: Source[ByteString, _]    = ???
      override def allHeaders: Map[String, Seq[String]]   = ???
      override def xml: Elem                              = ???
      override def json: JsValue                          = buildApiResponse(statusCode, responseBody)
    }

    def apply(): WSResponse = buildResponse()

    def apply(statusCode: Int): WSResponse = buildResponse(statusCode = statusCode)

    def apply(statusCode: Int, body: String): WSResponse = buildResponse(statusCode, body)
  }
}
