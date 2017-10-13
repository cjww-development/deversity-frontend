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

import javax.inject.{Inject, Singleton}

import com.cjwwdev.auth.actions.Actions
import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.config.ConfigurationLoader
import connectors.{Invalid, SessionStoreConnector, Valid}
import play.api.mvc.{Action, AnyContent}
import services.{EnrolmentService, SchoolDetailsService}
import utils.application.FrontendController
import views.html.enrolment._
import forms.{RoleForm, SchoolNameForm, TeacherDetailsForm, TeacherNameForm}
import models.SessionUpdateSet

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EnrolmentController @Inject()(val authConnector: AuthConnector,
                                    schoolDetailsService: SchoolDetailsService,
                                    sessionStoreConnector: SessionStoreConnector,
                                    val enrolmentService: EnrolmentService,
                                    val config: ConfigurationLoader) extends FrontendController with Actions {

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
        Future.successful(Ok(SchoolSelector(SchoolNameForm.form)))
  }

  def validateSchool: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        SchoolNameForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(SchoolSelector(errors))),
          valid => schoolDetailsService.validateSchool(valid.schoolName) flatMap  {
            case Valid => sessionStoreConnector.updateSession(SessionUpdateSet("schoolName", valid.schoolName)) map {
              _ => Redirect(routes.EnrolmentController.confirmSchool())
            }
            case Invalid => Future.successful(Ok(InvalidSchool()))
          }
        )
  }

  def confirmSchool: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(schoolName)      <- sessionStoreConnector.getDataElement("schoolName")
          Some(schoolDetails)   <- schoolDetailsService.getSchoolDetails(schoolName)
        } yield Ok(ConfirmSchool(schoolDetails))
  }

  def roleSelection: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(schoolName)    <- sessionStoreConnector.getDataElement("schoolName")
          Some(schoolDetails) <- schoolDetailsService.getSchoolDetails(schoolName)
        } yield Ok(RoleSelector(RoleForm.form, schoolDetails))
  }

  def confirmRole: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        RoleForm.form.bindFromRequest.fold(
          errors => {
            for {
              Some(schoolName)    <- sessionStoreConnector.getDataElement("schoolName")
              Some(schoolDetails) <- schoolDetailsService.getSchoolDetails(schoolName)
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
        Future.successful(Ok(TeacherSelector(TeacherNameForm.form)))
  }

  def validateTeacher: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        TeacherNameForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(TeacherSelector(errors))),
          valid => enrolmentService.validateTeacher(valid.userName) flatMap {
            case Valid => sessionStoreConnector.updateSession(SessionUpdateSet("teacher", valid.userName)) map {
              _ => Redirect(routes.EnrolmentController.confirmTeacher())
            }
            case Invalid => Future.successful(Ok(InvalidTeacher()))
          }
        )
  }

  def confirmTeacher: Action[AnyContent] = authorisedFor(USER_LOGIN_CALL).async {
    implicit user =>
      implicit request =>
        for {
          Some(schoolName)    <- sessionStoreConnector.getDataElement("schoolName")
          Some(details)       <- enrolmentService.getTeacherDetails
        } yield Ok(ConfirmTeacher(details, schoolName))
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
