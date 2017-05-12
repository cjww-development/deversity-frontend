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
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.logging.Logger
import com.cjwwdev.security.encryption.DataSecurity
import config.ApplicationConfiguration
import models.forms.TeacherDetails
import models.http.TeacherInformation
import models.{DeversityEnrolment, Enrolments}
import play.api.mvc.Request
import play.api.http.Status.{NOT_FOUND, OK}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EnrolmentConnector @Inject()(http: Http) extends ApplicationConfiguration {
  def getEnrolments(implicit authContext: AuthContext, request: Request[_]): Future[Option[Enrolments]] = {
    http.GET(s"$account_service_route${authContext.enrolmentsUri}") map { resp =>
      resp.status match {
        case OK => DataSecurity.decryptInto[Enrolments](resp.body)
        case NOT_FOUND => None
      }
    }
  }

  def getDeversityUserInfo(implicit authContext: AuthContext, request: Request[_]): Future[Option[DeversityEnrolment]] = {
    http.GET(s"$account_service_route/account/${authContext.user.userId}/deversity-info") map { resp =>
      Logger.warn(s"[EnrolmentConnector] - [getDeversityUserInfo] : Response from /account/${authContext.user.userId}/deversity-info = ${resp.status}")
      resp.status match {
        case OK         => DataSecurity.decryptInto[DeversityEnrolment](resp.body)
        case NOT_FOUND  => None
      }
    }
  }

  def validateTeacher(userName: String, schoolName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    val un = DataSecurity.encryptData[String](userName).get
    val sn = DataSecurity.encryptData[String](schoolName).get
    http.GET(s"$account_service_route/validate/teacher/$un/school/$sn") map {
      _.status match {
        case OK => Valid
        case NOT_FOUND => Invalid
      }
    }
  }

  def getTeacherDetails(userName: String, schoolName: String)(implicit request: Request[_]): Future[Option[TeacherInformation]] = {
    val un = DataSecurity.encryptData[String](userName).get
    val sn = DataSecurity.encryptData[String](schoolName).get
    http.GET(s"$account_service_route/teacher/$un/school/$sn/details") map { resp =>
      Logger.info(s"RESPONSE CODE ${resp.status}")
      resp.status match {
        case OK => DataSecurity.decryptInto[TeacherInformation](resp.body)
        case NOT_FOUND => None
      }
    }
  }

  def createDeversityId(implicit authContext: AuthContext, request: Request[_]): Future[Option[String]] = {
    http.PATCH(s"$account_service_route/account/${authContext.user.userId}/deversity-id", "") map { resp =>
      Logger.warn(s"[EnrolmentConnector] - [createDeversityId] : Response from /account/${authContext.user.userId}/deversity-id = ${resp.status}")
      Logger.info(s"RESP BODY: ${resp.body}")
      DataSecurity.decryptInto[String](resp.body)
    }
  }

  def initialiseDeversityEnrolment(deversityEnrolment: DeversityEnrolment)(implicit authContext: AuthContext, request: Request[_]): Future[Int] = {
    http.PATCH[DeversityEnrolment](s"$account_service_route/account/${authContext.user.userId}/deversity-info", deversityEnrolment) map {
      resp => resp.status
    }
  }
}
