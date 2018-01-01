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

import javax.inject.Inject

import com.cjwwdev.auth.models.AuthContext
import common.{DeversityCurrentEnrolmentResponse, InvalidEnrolments, ValidEnrolments}
import connectors.{AccountsMicroserviceConnector, DeversityMicroserviceConnector, SessionStoreConnector}
import enums.SessionCache
import models.forms.TeacherDetails
import models.http.TeacherInformation
import models.{DeversityEnrolment, SchoolDetails, SessionUpdateSet}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentServiceImpl @Inject()(val accountsConnector: AccountsMicroserviceConnector,
                                     val deversityConnector: DeversityMicroserviceConnector,
                                     val sessionStoreConnector: SessionStoreConnector) extends EnrolmentService

trait EnrolmentService {
  val accountsConnector: AccountsMicroserviceConnector
  val deversityConnector: DeversityMicroserviceConnector
  val sessionStoreConnector: SessionStoreConnector

  def validateCurrentEnrolments(implicit authContext: AuthContext, request: Request[_]): Future[DeversityCurrentEnrolmentResponse] = {
    deversityConnector.getDeversityUserInfo map {
      _.fold[DeversityCurrentEnrolmentResponse](InvalidEnrolments)(_ => ValidEnrolments)
    }
  }

  def getOrGenerateDeversityId(implicit authContext: AuthContext, request: Request[_]): Future[Option[String]] = {
    accountsConnector.getEnrolments flatMap {
      case Some(enrolments) => Future.successful(enrolments.deversityId)
      case None => deversityConnector.createDeversityId map {
        id => Some(id)
      } recover {
        case _ => None
      }
    }
  }

  def cacheTeacherDetails(teacherDetails: TeacherDetails)(implicit request: Request[_]): Future[SessionCache.Value] = {
    for {
      _           <- sessionStoreConnector.updateSession(SessionUpdateSet("title", teacherDetails.title))
      roomCache   <- sessionStoreConnector.updateSession(SessionUpdateSet("room", teacherDetails.room))
    } yield roomCache
  }

  def validateTeacher(regCode: String)(implicit request: Request[_]): Future[String] = {
    for {
      Some(schoolDevId)     <- sessionStoreConnector.getDataElement("schoolDevId")
      validationResponse    <- deversityConnector.validateTeacher(regCode, schoolDevId)
    } yield validationResponse
  }

  def getTeacherDetails(implicit authContext: AuthContext, request: Request[_]): Future[Option[TeacherInformation]] = {
    for {
      Some(schoolDevId)   <- sessionStoreConnector.getDataElement("schoolDevId")
      Some(teacherDevId)  <- sessionStoreConnector.getDataElement("teacherDevId")
      details             <- deversityConnector.getTeacherDetails(teacherDevId, schoolDevId)
    } yield details
  }

  def fetchAndSubmitTeacherEnrolment(implicit authContext: AuthContext, request: Request[_]): Future[SchoolDetails] = {
    for {
      Some(schoolDevId)     <- sessionStoreConnector.getDataElement("schoolDevId")
      title                 <- sessionStoreConnector.getDataElement("title")
      room                  <- sessionStoreConnector.getDataElement("room")
      Some(schoolDetails)   <- deversityConnector.getSchoolDetails(schoolDevId)
      _                     <- deversityConnector.initialiseDeversityEnrolment(
        DeversityEnrolment("pending", schoolDevId, "teacher", title, room, None)
      )
    } yield schoolDetails
  }

  def fetchAndSubmitStudentEnrolment(implicit authContext: AuthContext, request: Request[_]): Future[SchoolDetails] = {
    for {
      Some(schoolDevId)     <- sessionStoreConnector.getDataElement("schoolDevId")
      Some(teacherdevId)    <- sessionStoreConnector.getDataElement("teacherDevId")
      Some(schoolDetails)   <- deversityConnector.getSchoolDetails(schoolDevId)
      Some(teacherDetails)  <- deversityConnector.getTeacherDetails(teacherdevId, schoolDevId)
      _                     <- deversityConnector.initialiseDeversityEnrolment(
        DeversityEnrolment("pending", schoolDevId, "student", None, None, Some(s"${teacherDetails.title} ${teacherDetails.lastName}"))
      )
    } yield schoolDetails
  }
}
