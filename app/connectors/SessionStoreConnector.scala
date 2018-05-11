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

package connectors

import com.cjwwdev.http.exceptions.{ForbiddenException, NotFoundException, ServerErrorException}
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.session.SessionUtils
import com.cjwwdev.http.verbs.Http
import com.google.inject.Inject
import common.ApplicationConfiguration
import enums.SessionCache
import models.SessionUpdateSet
import play.api.libs.json._
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionStoreConnectorImpl @Inject()(val http : Http) extends SessionStoreConnector with ApplicationConfiguration

trait SessionStoreConnector extends SessionUtils with WsResponseHelpers {
  val http: Http

  val sessionStore: String

  implicit val jsValueReads: Reads[JsValue] = new Reads[JsValue] {
    def reads(json: JsValue): JsResult[JsValue] = JsSuccess(json)
  }

  def getDataElement(key : String)(implicit request: Request[_]) : Future[Option[String]] = {
    http.get(s"$sessionStore/session/$getCookieId/data?key=$key") map { resp =>
      Some(resp.toResponseString(needsDecrypt = true))
    } recover {
      case _: NotFoundException   => None
      case e: ForbiddenException  => throw e
    }
  }

  def updateSession(updateSet : SessionUpdateSet)(implicit format: OFormat[SessionUpdateSet], request: Request[_]) : Future[SessionCache.Value] = {
    http.patch[SessionUpdateSet](s"$sessionStore/session/$getCookieId", updateSet, secure = false) map {
      _ => SessionCache.cacheUpdated
    } recover {
      case _: ServerErrorException => SessionCache.cacheUpdateFailure
    }
  }
}
