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

import javax.inject.Inject

import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import common.ApplicationConfiguration
import models.http.TeacherInformation
import models.{DeversityEnrolment, SchoolDetails}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeversityMicroserviceConnectorImpl @Inject()(val http: Http) extends DeversityMicroserviceConnector with ApplicationConfiguration

trait DeversityMicroserviceConnector {
  val http: Http

  val deversityMicroservice: String

  def getDeversityUserInfo(implicit authContext: AuthContext, request: Request[_]): Future[Option[DeversityEnrolment]] = {
    http.GET[DeversityEnrolment](s"$deversityMicroservice/enrolment/${authContext.user.id}/deversity") map {
      deversityEnrolment => Some(deversityEnrolment)
    } recover {
      case _: NotFoundException => None
    }
  }

  def getTeacherDetails(teacherDevId: String, schoolDevId: String)
                       (implicit authContext: AuthContext, request: Request[_]): Future[Option[TeacherInformation]] = {
    val tDiD = DataSecurity.encryptString(teacherDevId)
    val sDiD = DataSecurity.encryptString(schoolDevId)
    http.GET[TeacherInformation](s"$deversityMicroservice/user/${authContext.user.id}/teacher/$tDiD/school/$sDiD/details") map {
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

  def validateSchool(regCode: String)(implicit request: Request[_]): Future[String] = {
    val orgRegCode = DataSecurity.encryptString(regCode)
    http.GET[String](s"$deversityMicroservice/validate/school/$orgRegCode")
  }

  def validateTeacher(regCode: String, schoolDevId: String)(implicit request: Request[_]): Future[String] = {
    val rc   = DataSecurity.encryptString(regCode)
    val sDId = DataSecurity.encryptString(schoolDevId)
    http.GET[String](s"$deversityMicroservice/validate/teacher/$rc/school/$sDId")
  }

  def getSchoolDetails(orgDevId: String)(implicit authContext: AuthContext, request: Request[_]): Future[Option[SchoolDetails]] = {
    val encOrgDevId = DataSecurity.encryptString(orgDevId)
    http.GET[SchoolDetails](s"$deversityMicroservice/user/${authContext.user.id}/school/$encOrgDevId/details") map {
      schoolDeets => Some(schoolDeets)
    } recover {
      case _: NotFoundException => None
    }
  }

  def lookupRegistrationCode(regCode: String)(implicit authContext: AuthContext, request: Request[_]): Future[String] = {
    http.GET[String](s"$deversityMicroservice/user/${authContext.user.id}/lookup/$regCode/lookup-reg-code")
  }
}
