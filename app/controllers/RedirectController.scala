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
import common.FrontendController
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EnrolmentService
import views.html.misc.ServiceUnavailableView

import scala.concurrent.Future

class DefaultRedirectController @Inject()(val authConnector: AuthConnector,
                                          val controllerComponents: ControllerComponents,
                                          val enrolmentService: EnrolmentService) extends RedirectController

trait RedirectController extends FrontendController {
  def redirectToUserRegister: Action[AnyContent] = action.async {
    implicit request =>
      Future.successful(Redirect(USER_REGISTER))
  }

  def redirectToOrgRegister: Action[AnyContent] = action.async {
    implicit request =>
      Future.successful(Redirect(ORG_REGISTER))
  }

  def redirectToLogin: Action[AnyContent] = action.async {
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

  def redirectToDeversity: Action[AnyContent] = action.async {
    implicit request =>
      Future.successful(Redirect(deversityFrontend))
  }

  def redirectToDiagnostics: Action[AnyContent] = action.async {
    implicit request =>
      Future.successful(Redirect(diagnosticsFrontend))
  }

  def redirectToHub: Action[AnyContent] = action.async {
    implicit request =>
      Future.successful(Redirect(hubFrontend))
  }

  def redirectToServiceOutage: Action[AnyContent] = action.async {
    implicit request =>
      Future.successful(ServiceUnavailable(ServiceUnavailableView()))
  }
}
