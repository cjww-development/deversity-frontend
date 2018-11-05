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
package controllers

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.config.ConfigurationLoader
import common.helpers.AuthController
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EnrolmentService
import views.html.misc.ServiceUnavailableView

import scala.concurrent.Future

class DefaultRedirectController @Inject()(val authConnector: AuthConnector,
                                          val controllerComponents: ControllerComponents,
                                          val config: ConfigurationLoader,
                                          val enrolmentService: EnrolmentService) extends RedirectController {
  override val deversityFrontend: String   = config.getServiceUrl("deversity-frontend")
  override val diagnosticsFrontend: String = config.getServiceUrl("diagnostics-frontend")
  override val hubFrontend: String         = config.getServiceUrl("hub-frontend")
}

trait RedirectController extends AuthController {

  val deversityFrontend: String
  val diagnosticsFrontend: String
  val hubFrontend: String

  def redirectToUserRegister: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(USER_REGISTER))
  }

  def redirectToOrgRegister: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(ORG_REGISTER))
  }

  def redirectToLogin: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(USER_LOGIN))
  }

  def redirectToUserDashboard: Action[AnyContent] = isAuthorised {
    implicit request =>
      implicit user =>
        Future.successful(Redirect(DASHBOARD))
  }

  def redirectToSignOut: Action[AnyContent] = isAuthorised {
    implicit request =>
      implicit user =>
        Future.successful(Redirect(SIGN_OUT))
  }

  def redirectToDeversity: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(deversityFrontend))
  }

  def redirectToDiagnostics: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(diagnosticsFrontend))
  }

  def redirectToHub: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(hubFrontend))
  }

  def redirectToServiceOutage: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(ServiceUnavailable(ServiceUnavailableView()))
  }
}
