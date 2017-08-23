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
  def validateSchoolName(orgName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    val orgUserName = DataSecurity.encryptString(orgName)
    http.HEAD(s"$accountMicroservice/validate/school/$orgUserName") map {
      _.status match {
        case OK => Valid
      }
    } recover {
      case _: NotFoundException => Invalid
    }
  }

  def getSchoolDetails(orgName: String)(implicit request: Request[_]): Future[Option[SchoolDetails]] = {
    val orgUserName = DataSecurity.encryptString(orgName)
    http.GET[SchoolDetails](s"$accountMicroservice/school/$orgUserName/details") map {
      schoolDeets => Some(schoolDeets)
    } recover {
      case _: NotFoundException => None
    }
  }

  def getEnrolments(implicit authContext: AuthContext, request: Request[_]): Future[Option[Enrolments]] = {
    http.GET[Enrolments](s"$accountMicroservice${authContext.enrolmentsUri}") map {
      enrolments => Some(enrolments)
    } recover {
      case _: NotFoundException => None
    }
  }

  def getDeversityUserInfo(implicit authContext: AuthContext, request: Request[_]): Future[Option[DeversityEnrolment]] = {
    http.GET[DeversityEnrolment](s"$accountMicroservice/account/${authContext.user.userId}/deversity-info") map {
      deversityEnrolment => Some(deversityEnrolment)
    } recover {
      case _: NotFoundException => None
    }
  }

  def validateTeacher(userName: String, schoolName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    val un = DataSecurity.encryptString(userName)
    val sn = DataSecurity.encryptString(schoolName)
    http.HEAD(s"$accountMicroservice/validate/teacher/$un/school/$sn") map {
      _.status match {
        case OK => Valid
      }
    } recover {
      case _: NotFoundException => Invalid
    }
  }

  def getTeacherDetails(userName: String, schoolName: String)(implicit request: Request[_]): Future[Option[TeacherInformation]] = {
    val un = DataSecurity.encryptString(userName)
    val sn = DataSecurity.encryptString(schoolName)
    http.GET[TeacherInformation](s"$accountMicroservice/teacher/$un/school/$sn/details") map {
      teacherInfo => Some(teacherInfo)
    } recover {
      case _: NotFoundException => None
    }
  }

  def createDeversityId(implicit authContext: AuthContext, request: Request[_]): Future[String] = {
    implicit val stringWriter = OWrites[String](str => Json.obj())
    http.PATCH[String](s"$accountMicroservice/account/${authContext.user.userId}/deversity-id", "") map {
      resp => DataSecurity.decryptString(resp.body)
    }
  }

  def initialiseDeversityEnrolment(deversityEnrolment: DeversityEnrolment)(implicit authContext: AuthContext, request: Request[_]): Future[Int] = {
    http.PATCH[DeversityEnrolment](s"$accountMicroservice/account/${authContext.user.userId}/deversity-info", deversityEnrolment) map(_.status)
  }
}
