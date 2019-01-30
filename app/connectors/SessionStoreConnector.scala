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

package connectors

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.responses.EvaluateResponse._
import com.cjwwdev.http.session.SessionUtils
import com.cjwwdev.http.verbs.Http
import enums.SessionCache
import javax.inject.Inject
import models.SessionUpdateSet
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext => ExC, Future}

class DefaultSessionStoreConnector @Inject()(val http : Http,
                                             val config: ConfigurationLoader) extends SessionStoreConnector {
  override val sessionStore: String = config.getServiceUrl("session-store")
}

trait SessionStoreConnector extends SessionUtils with WsResponseHelpers {
  val http: Http

  val sessionStore: String

  def getDataElement(key : String)(implicit request: Request[_], ec: ExC) : Future[Option[String]] = {
    http.get(s"$sessionStore/session/$getCookieId/data?key=$key") map {
      case SuccessResponse(resp) => resp.toResponseString(needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }

  def updateSession(updateSet : SessionUpdateSet)(implicit request: Request[_], ec: ExC): Future[SessionCache.Value] = {
    http.patch[SessionUpdateSet](s"$sessionStore/session/$getCookieId", updateSet, secure = false) map {
      case SuccessResponse(_) => SessionCache.cacheUpdated
      case ErrorResponse(_)   => SessionCache.cacheUpdateFailure
    }
  }
}
