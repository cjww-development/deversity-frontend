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
import connectors.{EnrolmentConnector, SchoolDetailsConnector, SessionStoreConnector, ValidOrg}
import models.{DeversityEnrolment, SchoolDetails, SessionUpdateSet}
import models.forms.TeacherDetails
import models.http.TeacherInformation
import play.api.mvc.Request

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NoStackTrace

sealed trait DeversityIdResponse
case object ValidId extends DeversityIdResponse
case object InvalidId extends DeversityIdResponse
case object NoIdPresent extends DeversityIdResponse

sealed trait DeversityCurrentEnrolmentResponse
case object ValidEnrolments extends DeversityCurrentEnrolmentResponse
case object InvalidEnrolments extends DeversityCurrentEnrolmentResponse

class NoEnrolmentsException(msg: String) extends NoStackTrace
class ExistingDeversityIdException(msg: String) extends NoStackTrace
class DevIdGetOrGenerationException(msg: String) extends NoStackTrace

@Singleton
class EnrolmentService @Inject()(schoolDetailsConnector: SchoolDetailsConnector,
                                 enrolmentConnector: EnrolmentConnector,
                                 sessionStoreConnector: SessionStoreConnector) {

  def validateCurrentEnrolments(implicit authContext: AuthContext, request: Request[_]): Future[DeversityCurrentEnrolmentResponse] = {
    enrolmentConnector.getDeversityUserInfo map {
      case Some(_) => ValidEnrolments
      case None =>
        InvalidEnrolments
    }
  }

  def getOrGenerateDeversityId(implicit authContext: AuthContext, request: Request[_]): Future[Option[String]] = {
    enrolmentConnector.getEnrolments flatMap {
      case Some(enrolments) => Future.successful(enrolments.deversityId)
      case None => enrolmentConnector.createDeversityId
    }
  }

  def cacheTeacherDetails(teacherDetails: TeacherDetails)(implicit request: Request[_]): Future[Int] = {
    for {
      _           <- sessionStoreConnector.updateSession(SessionUpdateSet("title", teacherDetails.title))
      roomCache   <- sessionStoreConnector.updateSession(SessionUpdateSet("room", teacherDetails.room))
    } yield roomCache
  }

  def validateTeacher(userName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    for {
      schoolName            <- sessionStoreConnector.getDataElement("schoolName")
      validationResponse    <- enrolmentConnector.validateTeacher(userName, schoolName)
    } yield validationResponse
  }

  def getTeacherDetails(implicit request: Request[_]): Future[Option[TeacherInformation]] = {
    for {
      schoolName    <- sessionStoreConnector.getDataElement("schoolName")
      teacherName   <- sessionStoreConnector.getDataElement("teacher")
      details       <- enrolmentConnector.getTeacherDetails(teacherName, schoolName)
    } yield details
  }

  def fetchAndSubmitTeacherEnrolment(implicit authContext: AuthContext, request: Request[_]): Future[SchoolDetails] = {
    for {
      schoolName            <- sessionStoreConnector.getDataElement("schoolName")
      title                 <- sessionStoreConnector.getDataElement("title")
      room                  <- sessionStoreConnector.getDataElement("room")
      Some(schoolDetails)   <- schoolDetailsConnector.getSchoolDetails(schoolName)
      _                     <- enrolmentConnector.initialiseDeversityEnrolment(
        DeversityEnrolment("pending", schoolName, "teacher", Some(title), Some(room), None)
      )
    } yield schoolDetails
  }

  def fetchAndSubmitStudentEnrolment(implicit authContext: AuthContext, request: Request[_]): Future[SchoolDetails] = {
    for {
      schoolName            <- sessionStoreConnector.getDataElement("schoolName")
      teacher               <- sessionStoreConnector.getDataElement("teacher")
      Some(schoolDetails)   <- schoolDetailsConnector.getSchoolDetails(schoolName)
      _                     <- enrolmentConnector.initialiseDeversityEnrolment(
        DeversityEnrolment("pending", schoolName, "student", None, None, Some(teacher))
      )
    } yield schoolDetails
  }
}
