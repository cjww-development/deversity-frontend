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

import play.api.mvc.{Action, AnyContent}
import com.cjwwdev.auth.connectors.AuthConnector
import com.kenshoo.play.metrics.MetricsDisabledException
import services.{EnrolmentService, MetricsService}
import utils.application.FrontendController
import views.html.index

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(authConnect: AuthConnector,
                               enrolmentsSrv: EnrolmentService,
                               metricsService: MetricsService) extends FrontendController {

  val authConnector = authConnect
  val enrolmentService = enrolmentsSrv

  def showHome: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(index()))
  }
}
