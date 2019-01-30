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
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.featuremanagement.services.FeatureService
import common.ViewConfiguration
import common.helpers.AuthController
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EnrolmentService
import views.html.misc.ServiceUnavailableView

import scala.concurrent.{ExecutionContext, Future}

class DefaultRedirectController @Inject()(val authConnector: AuthConnector,
                                          val controllerComponents: ControllerComponents,
                                          val config: ConfigurationLoader,
                                          val featureService: FeatureService,
                                          val enrolmentService: EnrolmentService,
                                          implicit val ec: ExecutionContext) extends RedirectController {
  override val deversityFrontend: String   = config.getServiceUrl("deversity-frontend")
  override val diagnosticsFrontend: String = config.getServiceUrl("diagnostics-frontend")
  override val hubFrontend: String         = config.getServiceUrl("hub-frontend")

  private val authService: String          = config.getServiceUrl("auth-service")

  override val userRegister: String        = s"$authService/create-an-account"
  override val orgRegister: String         = s"$authService/create-an-organisation-account"
  override val userLogin: String           = s"$authService/login?redirect=deversity"
  override val dashboard: String           = s"$authService/dashboard"
  override val signOut: String             = s"$authService/goodbye"
}

trait RedirectController extends AuthController with ViewConfiguration {

  val deversityFrontend: String
  val diagnosticsFrontend: String
  val hubFrontend: String

  val userRegister: String
  val orgRegister: String
  val userLogin: String
  val dashboard: String
  val signOut: String

  def redirectToUserRegister: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(Redirect(userRegister))
  }

  def redirectToOrgRegister: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(Redirect(orgRegister))
  }

  def redirectToLogin: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(Redirect(userLogin))
  }

  def redirectToUserDashboard: Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    Future.successful(Redirect(dashboard))
  }

  def redirectToSignOut: Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    Future.successful(Redirect(signOut))
  }

  def redirectToDeversity: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(Redirect(deversityFrontend))
  }

  def redirectToDiagnostics: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(Redirect(diagnosticsFrontend))
  }

  def redirectToHub: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(Redirect(hubFrontend))
  }

  def redirectToServiceOutage: Action[AnyContent] = Action.async { implicit req =>
    Future.successful(ServiceUnavailable(ServiceUnavailableView()))
  }
}
