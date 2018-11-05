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
import common.helpers.FrontendController
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EnrolmentService
import views.html.index

import scala.concurrent.Future

class DefaultHomeController @Inject()(val enrolmentService: EnrolmentService,
                                      val authConnector: AuthConnector,
                                      val controllerComponents: ControllerComponents) extends HomeController

trait HomeController extends FrontendController {

  def showHome: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(index()))
  }
}
