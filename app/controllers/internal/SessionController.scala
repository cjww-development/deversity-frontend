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

package controllers.internal

import com.cjwwdev.config.ConfigurationLoader
import common.helpers.FrontendController
import javax.inject.Inject
import play.api.mvc._
import services.SessionService

import scala.concurrent.ExecutionContext

class DefaultSessionController @Inject()(val sessionService: SessionService,
                                         val controllerComponents: ControllerComponents,
                                         val config: ConfigurationLoader,
                                         implicit val ec: ExecutionContext) extends SessionController {
  private val authService: String      = config.getServiceUrl("auth-service")
  override val serviceDirector: String = s"$authService/where-do-you-want-to-go"
}

trait SessionController extends FrontendController {
  val sessionService: SessionService

  val serviceDirector: String

  def buildSession(sessionId: String): Action[AnyContent] = Action.async { implicit req =>
    logger.info(s"[buildSession] - request was sent from ${req.headers("Referer")}")
    logger.info(s"[buildSession] - attempting to build session")
    sessionService.fetchAuthContext(sessionId) map {
      _.fold(forbiddenResponse(sessionId))({ context =>
        val session = Session(sessionService.sessionMap(sessionId, context))
        Redirect(routes.SessionController.validateSession(sessionId)).withSession(session)
      })
    }
  }

  def validateSession(sessionId: String): Action[AnyContent] = Action { implicit req =>
    logger.info(s"[validateSession] - request was sent from ${req.headers("Referer")}")
    if(req.session("cookieId").equals(sessionId)) Redirect(serviceDirector) else InternalServerError
  }

  private def forbiddenResponse(sessionId: String): Result = {
    logger.warn(s"[buildSession] - No AuthContext found matching sessionId $sessionId")
    Forbidden
  }
}
