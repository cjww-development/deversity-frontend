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

package services

import com.cjwwdev.auth.models.CurrentUser
import javax.inject.Inject
import connectors.DeversityConnector
import models.SchoolDetails
import play.api.mvc.Request

import scala.concurrent.Future

class DefaultSchoolDetailsService @Inject()(val deversityConnector: DeversityConnector) extends SchoolDetailsService

trait SchoolDetailsService{
  val deversityConnector: DeversityConnector

  def validateSchool(regCode: String)(implicit request: Request[_]): Future[String] = {
    deversityConnector.validateSchool(regCode)
  }

  def getSchoolDetails(orgName: String)(implicit user: CurrentUser, request: Request[_]): Future[Option[SchoolDetails]] = {
    deversityConnector.getSchoolDetails(orgName)
  }
}
