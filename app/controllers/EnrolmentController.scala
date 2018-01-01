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
package controllers

import javax.inject.Inject

import com.cjwwdev.auth.actions.Actions
import com.cjwwdev.auth.connectors.AuthConnector
import common.FrontendController
import connectors.SessionStoreConnector
import forms._
import models.SessionUpdateSet
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{EnrolmentService, SchoolDetailsService}
import views.html.enrolment._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentControllerImpl @Inject()(val schoolDetailsService: SchoolDetailsService,
                                        val sessionStoreConnector: SessionStoreConnector,
                                        val authConnector: AuthConnector,
                                        val enrolmentService: EnrolmentService,
                                        implicit val messagesApi: MessagesApi) extends EnrolmentController

trait EnrolmentController extends FrontendController with Actions {
  val schoolDetailsService: SchoolDetailsService
  val sessionStoreConnector: SessionStoreConnector

  def testController: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        checkDeversityEnrolment {
          Future.successful(Ok("TEST ROUTE"))
        }
  }

  def enrolmentWelcome: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        Future.successful(Ok(EnrolmentWelcome()))
  }

  def selectSchool: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        Logger.info(s"SELECT SCHOOOOOOOOOOL")
        Future.successful(Ok(SchoolSelector(SchoolRegCodeForm.form)))
  }

  def validateSchool: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        SchoolRegCodeForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(SchoolSelector(errors))),
          valid => schoolDetailsService.validateSchool(valid.regCode) flatMap { schoolDevId =>
            sessionStoreConnector.updateSession(SessionUpdateSet("schoolDevId", schoolDevId)) map {
              _ => Redirect(routes.EnrolmentController.confirmSchool())
            }
          } recover {
            case _ => Ok(InvalidSchool())
          }
        )
  }

  def confirmSchool: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(schoolDevId)     <- sessionStoreConnector.getDataElement("schoolDevId")
          Some(schoolDetails)   <- schoolDetailsService.getSchoolDetails(schoolDevId)
        } yield Ok(ConfirmSchool(schoolDetails))
  }

  def roleSelection: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(schoolDevId)   <- sessionStoreConnector.getDataElement("schoolDevId")
          Some(schoolDetails) <- schoolDetailsService.getSchoolDetails(schoolDevId)
        } yield Ok(RoleSelector(RoleForm.form, schoolDetails))
  }

  def confirmRole: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        RoleForm.form.bindFromRequest.fold(
          errors => {
            for {
              Some(schoolDevId)   <- sessionStoreConnector.getDataElement("schoolDevId")
              Some(schoolDetails) <- schoolDetailsService.getSchoolDetails(schoolDevId)
            } yield BadRequest(RoleSelector(errors, schoolDetails))
          },
          valid => sessionStoreConnector.updateSession(SessionUpdateSet("role", valid.role)) map {
            _ => valid.role match {
              case "teacher" => Redirect(routes.EnrolmentController.confirmAsTeacher())
              case "student" => Redirect(routes.EnrolmentController.confirmAsStudent())
            }
          }
        )
  }

  def confirmAsTeacher: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        Future.successful(Ok(TeacherDetailsEntry(TeacherDetailsForm.form)))
  }

  def cacheTeacherDetails: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        TeacherDetailsForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(TeacherDetailsEntry(errors))),
          valid => enrolmentService.cacheTeacherDetails(valid) map {
            _ => Redirect(routes.EnrolmentController.enrolmentConfirmation())
          }
        )
  }

  def confirmAsStudent: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        Future.successful(Ok(TeacherSelector(TeacherRegCodeForm.form)))
  }

  def validateTeacher: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        TeacherRegCodeForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(TeacherSelector(errors))),
          valid => enrolmentService.validateTeacher(valid.regCode) flatMap { teacherDevId =>
            sessionStoreConnector.updateSession(SessionUpdateSet("teacherDevId", teacherDevId)) map {
              _ => Redirect(routes.EnrolmentController.confirmTeacher())
            }
          } recover {
            case _ => Ok(InvalidTeacher())
          }
        )
  }

  def confirmTeacher: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(schoolDevId)   <- sessionStoreConnector.getDataElement("schoolDevId")
          Some(details)       <- enrolmentService.getTeacherDetails
          Some(schoolDetails) <- schoolDetailsService.getSchoolDetails(schoolDevId)
        } yield Ok(ConfirmTeacher(details, schoolDetails.orgName))
  }

  def enrolmentConfirmation: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(role)      <- sessionStoreConnector.getDataElement("role")
          schoolDetails   <- role match {
            case "teacher" => enrolmentService.fetchAndSubmitTeacherEnrolment
            case "student" => enrolmentService.fetchAndSubmitStudentEnrolment
          }
        } yield Ok(EnrolmentConfirmation(schoolDetails, role))
  }
}
