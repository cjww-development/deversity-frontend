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

package connectors

import javax.inject.{Inject, Singleton}

import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import config.ApplicationConfiguration
import models.http.TeacherInformation
import models.{DeversityEnrolment, Enrolments, SchoolDetails}
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.mvc.Request

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait ValidOrg
case object Valid extends ValidOrg
case object Invalid extends ValidOrg

@Singleton
class AccountsMicroserviceConnector @Inject()(http: Http, val config: ConfigurationLoader) extends ApplicationConfiguration with DefaultFormat {
  def getEnrolments(implicit authContext: AuthContext, request: Request[_]): Future[Option[Enrolments]] = {
    http.GET[Enrolments](s"$accountMicroservice${authContext.enrolmentsUri}") map {
      enrolments => Some(enrolments)
    } recover {
      case _: NotFoundException => None
    }
  }
}
