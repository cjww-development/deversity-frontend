/*
 * Copyright 2017 HM Revenue & Customs
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
package controllers.internal

import javax.inject.Inject

import common.{ApplicationConfiguration, Logging}
import play.api.mvc._
import services.SessionService

import scala.concurrent.ExecutionContext.Implicits.global

class SessionControllerImpl @Inject()(val sessionService: SessionService) extends SessionController

trait SessionController extends Controller with Logging with ApplicationConfiguration {
  val sessionService: SessionService

  def buildSession(sessionId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"[buildSession] - request was sent from ${request.headers("Referer")}")
    sessionService.fetchAuthContext(sessionId) map {
      _.fold(forbiddenResponse(sessionId))({ context =>
        val session = Session(sessionService.sessionMap(sessionId, context))
        Redirect(routes.SessionController.validateSession(sessionId)).withSession(session)
      })
    }
  }

  def validateSession(sessionId: String): Action[AnyContent] = Action { implicit request =>
    logger.info(s"[validateSession] - request was sent from ${request.headers("Referer")}")
    if(request.session("cookieId").equals(sessionId)) Redirect(SERVICE_DIRECTOR) else InternalServerError
  }

  private def forbiddenResponse(sessionId: String): Result = {
    logger.warn(s"[buildSession] - No AuthContext found matching sessionId $sessionId")
    Forbidden
  }
}
