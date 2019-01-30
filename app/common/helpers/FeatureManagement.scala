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

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.views.html.templates.errors.NotFoundView
import com.cjwwdev.featuremanagement.services.FeatureService
import common.{Features, ViewConfiguration}
import enums.UserRoles
import play.api.i18n.Lang
import play.api.mvc.{BaseController, Request, Result}

import scala.concurrent.Future

trait FeatureManagement extends ViewConfiguration {
  self: BaseController =>

  val featureService: FeatureService

  def codeGeneration: Boolean      = featureService.getState(Features.codeGeneration).state
  def classRoomManagement: Boolean = featureService.getState(Features.classRoomManagement).state
  def enrolmentsReview: Boolean    = featureService.getState(Features.enrolmentsReview).state

  private val isTeacher: CurrentUser => Boolean = _.role.contains(UserRoles.TEACHER)

  def featureGuard(featureState: Boolean)(result: => Future[Result])(implicit req: Request[_], lang: Lang): Future[Result] = {
    if(featureState) result else Future.successful(NotFound(NotFoundView()))
  }

  def teacherGuard(result: => Future[Result])(implicit req: Request[_], user: CurrentUser, lang: Lang): Future[Result] = {
    if(isTeacher(user)) result else Future.successful(NotFound(NotFoundView()))
  }
}
