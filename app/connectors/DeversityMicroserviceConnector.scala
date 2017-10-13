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
import models.{DeversityEnrolment, SchoolDetails}
import models.http.TeacherInformation
import play.api.http.Status.OK
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.Request

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DeversityMicroserviceConnector @Inject()(http: Http, val config: ConfigurationLoader) extends ApplicationConfiguration {
  def getDeversityUserInfo(implicit authContext: AuthContext, request: Request[_]): Future[Option[DeversityEnrolment]] = {
    http.GET[DeversityEnrolment](s"$deversityMicroservice/enrolment/${authContext.user.id}/deversity") map {
      deversityEnrolment => Some(deversityEnrolment)
    } recover {
      case _: NotFoundException => None
    }
  }

  def getTeacherDetails(userName: String, schoolName: String)(implicit authContext: AuthContext, request: Request[_]): Future[Option[TeacherInformation]] = {
    val un = DataSecurity.encryptString(userName)
    val sn = DataSecurity.encryptString(schoolName)
    http.GET[TeacherInformation](s"$deversityMicroservice/user/${authContext.user.id}/teacher/$un/school/$sn/details") map {
      teacherInfo => Some(teacherInfo)
    } recover {
      case _: NotFoundException => None
    }
  }

  def createDeversityId(implicit authContext: AuthContext, request: Request[_]): Future[String] = {
    implicit val stringWriter: OWrites[String] = OWrites[String](str => Json.obj())
    http.PATCH[String](s"$deversityMicroservice/${authContext.user.id}/create-deversity-id", "") map {
      resp => DataSecurity.decryptString(resp.body)
    }
  }

  def initialiseDeversityEnrolment(deversityEnrolment: DeversityEnrolment)(implicit authContext: AuthContext, request: Request[_]): Future[Int] = {
    http.PATCH[DeversityEnrolment](s"$deversityMicroservice/enrolment/${authContext.user.id}/deversity", deversityEnrolment) map(_.status)
  }

  def validateSchoolName(orgName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    val orgUserName = DataSecurity.encryptString(orgName)
    http.HEAD(s"$deversityMicroservice/validate/school/$orgUserName") map {
      _.status match {
        case OK => Valid
      }
    } recover {
      case _: NotFoundException => Invalid
    }
  }

  def validateTeacher(userName: String, schoolName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    val un = DataSecurity.encryptString(userName)
    val sn = DataSecurity.encryptString(schoolName)
    http.HEAD(s"$deversityMicroservice/validate/teacher/$un/school/$sn") map {
      _.status match {
        case OK => Valid
      }
    } recover {
      case _: NotFoundException => Invalid
    }
  }

  def getSchoolDetails(orgName: String)(implicit authContext: AuthContext, request: Request[_]): Future[Option[SchoolDetails]] = {
    val orgUserName = DataSecurity.encryptString(orgName)
    http.GET[SchoolDetails](s"$deversityMicroservice/user/${authContext.user.id}/school/$orgUserName/details") map {
      schoolDeets => Some(schoolDeets)
    } recover {
      case _: NotFoundException => None
    }
  }
}
