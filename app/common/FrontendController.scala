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

import java.util.Locale

import com.cjwwdev.auth.frontend.AuthorisedAction
import com.cjwwdev.auth.models.CurrentUser
import controllers.routes
import play.api.i18n._
import play.api.mvc._
import services.EnrolmentService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait FrontendController
  extends BaseController
    with ApplicationConfiguration
    with I18nSupport
    with AuthorisedAction {

  override def unauthorisedRedirect: Call = USER_LOGIN_CALL

  implicit val messages: Messages = MessagesImpl(Lang(Locale.ENGLISH), controllerComponents.messagesApi)

  protected val action: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder

  val enrolmentService: EnrolmentService

  def checkDeversityEnrolment(f: => Future[Result])(implicit user: CurrentUser, request: Request[_]): Future[Result] = {
    enrolmentService.validateCurrentEnrolments flatMap {
      case ValidEnrolments    => f
      case InvalidEnrolments  => validateDevId
    }
  }

  private def validateDevId(implicit user: CurrentUser, request: Request[_]): Future[Result] = {
    request.session.get("devId") match {
      case Some(_) => Future.successful(Redirect(routes.EnrolmentController.enrolmentWelcome()))
      case None    => enrolmentService.getOrGenerateDeversityId map {
        case Some(id) => Redirect(routes.EnrolmentController.enrolmentWelcome()).withSession(request.session. +("devId" -> id))
        case None     => throw new DevIdGetOrGenerationException(s"There was a problem getting or generating a dev id for user ${user.id}")
      }
    }
  }
}
