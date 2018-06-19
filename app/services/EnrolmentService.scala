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
import common.{DeversityCurrentEnrolmentResponse, InvalidEnrolments, ValidEnrolments}
import connectors.{AccountsConnector, DeversityConnector, SessionStoreConnector}
import enums.SessionCache
import javax.inject.Inject
import models.enrolmentFlow.{EnrolmentSummary, TeacherInfo}
import models.forms.TeacherDetails
import models.http.TeacherInformation
import models.{DeversityEnrolment, SchoolDetails, SessionUpdateSet}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultEnrolmentService @Inject()(val accountsConnector: AccountsConnector,
                                        val deversityConnector: DeversityConnector,
                                        val sessionStoreConnector: SessionStoreConnector) extends EnrolmentService

trait EnrolmentService {
  val accountsConnector: AccountsConnector
  val deversityConnector: DeversityConnector
  val sessionStoreConnector: SessionStoreConnector

  def validateCurrentEnrolments(implicit user: CurrentUser, request: Request[_]): Future[DeversityCurrentEnrolmentResponse] = {
    deversityConnector.getDeversityUserInfo map {
      _.fold[DeversityCurrentEnrolmentResponse](InvalidEnrolments)(_ => ValidEnrolments)
    }
  }

  def getOrGenerateDeversityId(implicit user: CurrentUser, request: Request[_]): Future[Option[String]] = {
    accountsConnector.getEnrolments flatMap {
      case Some(enrolments) => Future.successful(enrolments.deversityId)
      case None => deversityConnector.createDeversityId map {
        Some(_)
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

  def getTeacherDetails(implicit user: CurrentUser, request: Request[_]): Future[Option[TeacherInformation]] = {
    for {
      Some(schoolDevId)   <- sessionStoreConnector.getDataElement("schoolDevId")
      Some(teacherDevId)  <- sessionStoreConnector.getDataElement("teacherDevId")
      details             <- deversityConnector.getTeacherDetails(teacherDevId, schoolDevId)
    } yield details
  }

  def buildSummaryData(implicit request: Request[_], currentUser: CurrentUser): Future[EnrolmentSummary] = {
    for {
      Some(schoolDevId)   <- sessionStoreConnector.getDataElement("schoolDevId")
      Some(schoolDetails) <- deversityConnector.getSchoolDetails(schoolDevId)
      Some(role)          <- sessionStoreConnector.getDataElement("role")
      teacher <- role match {
        case "teacher" => for {
          Some(title) <- sessionStoreConnector.getDataElement("title")
          Some(room)  <- sessionStoreConnector.getDataElement("room")
        } yield Left(TeacherInfo(title, room))
        case "student" => for {
          Some(teacherDevId) <- sessionStoreConnector.getDataElement("teacherDevId")
          Some(teacherName)  <- deversityConnector.getTeacherDetails(teacherDevId, schoolDevId) map(_.map(x => s"${x.title} ${x.lastName}"))
        } yield Right(teacherName)
      }
    } yield EnrolmentSummary(
      schoolName     = schoolDetails.orgName,
      schoolInitials = schoolDetails.initials,
      role           = role,
      teacherInfo    = teacher.fold(right => Some(right), _ => None),
      teacherName    = teacher.fold(_ => None, left => Some(left))
    )
  }

  def fetchAndSubmitTeacherEnrolment(implicit user: CurrentUser, request: Request[_]): Future[SchoolDetails] = {
    for {
      Some(schoolDevId)     <- sessionStoreConnector.getDataElement("schoolDevId")
      title                 <- sessionStoreConnector.getDataElement("title")
      room                  <- sessionStoreConnector.getDataElement("room")
      Some(schoolDetails)   <- deversityConnector.getSchoolDetails(schoolDevId)
      _                     <- deversityConnector.initialiseDeversityEnrolment(
        DeversityEnrolment(schoolDevId, "teacher", title, room, None)
      )
    } yield schoolDetails
  }

  def fetchAndSubmitStudentEnrolment(implicit user: CurrentUser, request: Request[_]): Future[SchoolDetails] = {
    for {
      Some(schoolDevId)     <- sessionStoreConnector.getDataElement("schoolDevId")
      Some(teacherDevId)    <- sessionStoreConnector.getDataElement("teacherDevId")
      Some(schoolDetails)   <- deversityConnector.getSchoolDetails(schoolDevId)
      Some(teacherDetails)  <- deversityConnector.getTeacherDetails(teacherDevId, schoolDevId)
      _                     <- deversityConnector.initialiseDeversityEnrolment(
        DeversityEnrolment(schoolDevId, "student", None, None, Some(s"${teacherDetails.title} ${teacherDetails.lastName}"))
      )
    } yield schoolDetails
  }
}
