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

package utils.application

import com.cjwwdev.auth.models.AuthContext
import config.ApplicationConfiguration
import controllers.routes
import play.api.i18n.{I18NSupportLowPriorityImplicits, I18nSupport, Lang, Messages}
import play.api.mvc.{Controller, Request, Result}
import services._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait FrontendController extends Controller with ApplicationConfiguration with I18NSupportLowPriorityImplicits with I18nSupport {

  val enrolmentService: EnrolmentService

  def checkDeversityEnrolment(f: => Future[Result])(implicit authContext: AuthContext, request: Request[_]): Future[Result] = {
    enrolmentService.validateCurrentEnrolments flatMap {
      case ValidEnrolments    => f
      case InvalidEnrolments  => validateDevId
    }
  }

  private def validateDevId(implicit authContext: AuthContext, request: Request[_]): Future[Result] = {
    request.session.get("devId") match {
      case Some(_) => Future.successful(Redirect(routes.EnrolmentController.enrolmentWelcome()))
      case None    => enrolmentService.getOrGenerateDeversityId map {
        case Some(id) => Redirect(routes.EnrolmentController.enrolmentWelcome()).withSession(request.session. +("devId" -> id))
        case None     => throw new DevIdGetOrGenerationException(s"There was a problem getting or generating a dev id for user ${authContext.user.id}")
      }
    }
  }
}
