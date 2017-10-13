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
package services

import javax.inject.{Inject, Singleton}

import com.cjwwdev.auth.models.AuthContext
import connectors.{DeversityMicroserviceConnector, ValidOrg}
import models.SchoolDetails
import play.api.mvc.Request

import scala.concurrent.Future

@Singleton
class SchoolDetailsService @Inject()(deversityConnector: DeversityMicroserviceConnector) {

  def validateSchool(orgName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    deversityConnector.validateSchoolName(orgName)
  }

  def getSchoolDetails(orgName: String)(implicit authContext: AuthContext, request: Request[_]): Future[Option[SchoolDetails]] = {
    deversityConnector.getSchoolDetails(orgName)
  }
}
