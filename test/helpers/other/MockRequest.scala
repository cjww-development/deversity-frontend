/*
 * Copyright 2019 CJWW Development
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

package helpers.other

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.cjwwdev.responses.ApiResponse
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSCookie, WSResponse}

import scala.xml.Elem

trait MockRequest extends ApiResponse {
  def fakeHttpResponse(statusCode: Int, bodyContents: String = ""): WSResponse = new WSResponse {
    override def headers: Map[String, Seq[String]]      = ???
    override def bodyAsSource: Source[ByteString, _]    = ???
    override def cookie(name: String): Option[WSCookie] = ???
    override def underlying[T]: T                       = ???
    override def body: String                           = bodyContents
    override def bodyAsBytes: ByteString                = ???
    override def cookies: Seq[WSCookie]                 = ???
    override def allHeaders: Map[String, Seq[String]]   = ???
    override def xml: Elem                              = ???
    override def statusText: String                     = ???
    override def json: JsValue                          = {
      requestProperties(statusCode) ++
        Json.obj(bodyKey(statusCode) -> bodyContents) ++
        requestStats
    }
    override def header(key: String): Option[String]    = ???
    override def status: Int                            = statusCode
  }

  private def requestProperties(statusCode: Int): JsObject = Json.obj(
    "uri"    -> "/test/uri",
    "method" -> s"GET",
    "status" -> statusCode
  )

  private def requestStats: JsObject = Json.obj(
    "stats" -> Json.obj(
      "requestCompletedAt" -> s"${LocalDateTime.now}"
    )
  )

  private val bodyKey: Int => String = statusCode => if(statusCode.isBetween(200 to 299)) "body" else "errorMessage"
}
