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
package controllers

import javax.inject.{Inject, Singleton}

import com.cjwwdev.auth.actions.Actions
import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.config.ConfigurationLoader
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.EnrolmentService
import utils.application.FrontendController
import views.html.misc.ServiceUnavailableView

import scala.concurrent.Future

@Singleton
class RedirectController @Inject()(val authConnector: AuthConnector,
                                   val enrolmentService: EnrolmentService,
                                   val config: ConfigurationLoader,
                                   implicit val messagesApi: MessagesApi) extends FrontendController with Actions {

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

  def redirectToUserDashboard: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        Future.successful(Redirect(DASHBOARD))
  }

  def redirectToSignOut: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        Future.successful(Redirect(SIGN_OUT))
  }

  def redirectToServiceOutage: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(ServiceUnavailable(ServiceUnavailableView()))
  }
}
