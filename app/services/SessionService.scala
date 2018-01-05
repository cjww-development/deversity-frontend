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
package services

import java.util.UUID
import javax.inject.Inject

import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.exceptions.{ClientErrorException, NotFoundException}
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import play.api.libs.json.JsValue
import play.api.mvc.Request

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SessionServiceImpl @Inject()(val http: Http, configurationLoader: ConfigurationLoader) extends SessionService {
  override val sessionStore     = configurationLoader.buildServiceUrl("session-store")
  override val authMicroservice = configurationLoader.buildServiceUrl("auth-microservice")
}

trait SessionService {
  val http: Http

  val sessionStore: String
  val authMicroservice: String

  def sessionMap(sessionId: String, context: AuthContext): Map[String, String] = {
    context.user.credentialType match {
      case "organisation" => Map(
        "cookieId"        -> sessionId,
        "orgName"         -> context.user.orgName.get,
        "credentialType"  -> context.user.credentialType,
        "testKey"         -> "DEVERSITY_UNIQUE"
      )
      case "individual" => Map(
        "cookieId"        -> sessionId,
        "firstName"       -> context.user.firstName.get,
        "lastName"        -> context.user.lastName.get,
        "credentialType"  -> context.user.credentialType,
        "testKey"         -> "DEVERSITY_UNIQUE",
        if(context.user.role.isDefined) "role" -> context.user.role.get else "" -> ""
      )
    }
  }

  def fetchAuthContext(sessionId: String)(implicit request: Request[_]): Future[Option[AuthContext]] = for {
    contextId <- getContextId(sessionId)
    context   <- getAuthContext(contextId)
  } yield context

  private def getContextId(sessionId: String)(implicit request: Request[_]): Future[String] = {
    http.GET[JsValue](s"$sessionStore/session/$sessionId/context") map { response =>
      DataSecurity.decryptString(response.\("contextId").as[String])
    }
  }

  private def getAuthContext(contextId: String)(implicit request: Request[_]): Future[Option[AuthContext]] = {
    http.GET[AuthContext](s"$authMicroservice/get-context/$contextId") map {
      Some(_)
    } recover {
      case _: NotFoundException    => None
      case _: ClientErrorException => None
    }
  }
}
