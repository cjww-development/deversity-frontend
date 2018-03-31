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

package services

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.exceptions.{ClientErrorException, NotFoundException}
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.implicits.ImplicitHandlers
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionServiceImpl @Inject()(val http: Http, configurationLoader: ConfigurationLoader) extends SessionService {
  override val sessionStore     = configurationLoader.buildServiceUrl("session-store")
  override val authMicroservice = configurationLoader.buildServiceUrl("auth-microservice")
}

trait SessionService extends ImplicitHandlers {
  val http: Http

  val sessionStore: String
  val authMicroservice: String

  def sessionMap(sessionId: String, user: CurrentUser): Map[String, String] = {
    user.credentialType match {
      case "organisation" => Map(
        "cookieId"        -> sessionId,
        "orgName"         -> user.orgName.get,
        "credentialType"  -> user.credentialType
      )
      case "individual" => Map(
        "cookieId"        -> sessionId,
        "firstName"       -> user.firstName.get,
        "lastName"        -> user.lastName.get,
        "credentialType"  -> user.credentialType,
        if(user.role.isDefined) "role" -> user.role.get else "" -> ""
      )
    }
  }

  def fetchAuthContext(sessionId: String)(implicit request: Request[_]): Future[Option[CurrentUser]] = for {
    contextId <- getContextId(sessionId)
    context   <- getAuthContext(contextId)
  } yield context

  private def getContextId(sessionId: String)(implicit request: Request[_]): Future[String] = {
    http.get(s"$sessionStore/session/$sessionId/context") map {
      _.body.decryptType[JsValue].\("contextId").as[String]
    }
  }

  private def getAuthContext(contextId: String)(implicit request: Request[_]): Future[Option[CurrentUser]] = {
    http.get(s"$authMicroservice/get-current-user/$contextId") map { resp =>
      Some(resp.body.decryptType[CurrentUser])
    } recover {
      case _: NotFoundException    => None
      case _: ClientErrorException => None
    }
  }
}
