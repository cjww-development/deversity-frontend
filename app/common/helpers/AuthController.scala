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

package common.helpers

import com.cjwwdev.auth.frontend.AuthorisedAction
import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.logging.Logging
import common.responses.{DevIdGetOrGenerationException, InvalidEnrolments, ValidEnrolments}
import controllers.routes
import enums.AccountTypes
import play.api.i18n.Lang
import play.api.mvc.{BaseController, Call, Request, Result}
import services.EnrolmentService

import scala.concurrent.{Future, ExecutionContext => ExC}

trait AuthController
  extends BaseController
    with AuthorisedAction
    with Logging
    with FeatureManagement {

  implicit val ec: ExC

  implicit def getLang(implicit request: Request[_]): Lang = supportedLangs.preferred(request.acceptLanguages)

  override protected def unauthorisedRedirect: Call = controllers.routes.RedirectController.redirectToLogin()

  val enrolmentService: EnrolmentService

  def checkDeversityEnrolment(f: => Future[Result])(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[Result] = {
    user.credentialType match {
      case AccountTypes.INDIVIDUAL   => enrolmentService.validateCurrentEnrolments flatMap {
        case ValidEnrolments   => f
        case InvalidEnrolments => validateDevId
      }
      case AccountTypes.ORGANISATION => f
    }
  }

  private def validateDevId(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[Result] = {
    req.session.get("devId") match {
      case Some(_) => Future.successful(Redirect(routes.EnrolmentController.enrolmentWelcome()))
      case None    => enrolmentService.getOrGenerateDeversityId map {
        case Some(id) => Redirect(routes.EnrolmentController.enrolmentWelcome()).withSession(req.session. +("devId" -> id))
        case None     => throw new DevIdGetOrGenerationException(s"There was a problem getting or generating a dev id for user ${user.id}")
      }
    }
  }
}
