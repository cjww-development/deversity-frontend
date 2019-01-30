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

package controllers

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.featuremanagement.services.FeatureService
import com.cjwwdev.views.html.templates.errors.NotFoundView
import common.ViewConfiguration
import common.helpers.AuthController
import common.responses.InvalidEnrolments
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{EnrolmentService, RegistrationCodeService}
import views.html.GenerateCodeView

import scala.concurrent.ExecutionContext

class DefaultGenerateCodeController @Inject()(val controllerComponents: ControllerComponents,
                                              val authConnector: AuthConnector,
                                              val registrationCodeService: RegistrationCodeService,
                                              val enrolmentService: EnrolmentService,
                                              val featureService: FeatureService,
                                              implicit val ec: ExecutionContext) extends GenerateCodeController

trait GenerateCodeController extends AuthController with ViewConfiguration {

  val registrationCodeService: RegistrationCodeService

  def registrationCodeShow(): Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    featureGuard(codeGeneration) {
      registrationCodeService.getRegistrationCode map {
        case Right(regCode)          => Ok(GenerateCodeView(regCode))
        case Left(InvalidEnrolments) => NotFound(NotFoundView())
      }
    }
  }

  def generateRegistrationCode(): Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    featureGuard(codeGeneration) {
      registrationCodeService.generateRegistrationCode map {
        case Right(_)                => Redirect(routes.GenerateCodeController.registrationCodeShow())
        case Left(InvalidEnrolments) => NotFound(NotFoundView())
      }
    }
  }
}
