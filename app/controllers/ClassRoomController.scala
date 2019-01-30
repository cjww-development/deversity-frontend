/*
 * Copyright 2019 CJWW Development
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

package controllers

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.featuremanagement.services.FeatureService
import common.helpers.AuthController
import forms.CreateClassForm
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{ClassRoomService, EnrolmentService}
import views.html.{ManageClassroomView, ManageClassroomsView}

import scala.concurrent.ExecutionContext

class DefaultClassRoomController @Inject()(val controllerComponents: ControllerComponents,
                                           val featureService: FeatureService,
                                           val enrolmentService: EnrolmentService,
                                           val classRoomService: ClassRoomService,
                                           val authConnector: AuthConnector,
                                           implicit val ec: ExecutionContext) extends ClassRoomController

trait ClassRoomController extends AuthController {

  val classRoomService: ClassRoomService

  def manageClassRooms(): Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    featureGuard(classRoomManagement) {
      teacherGuard {
        classRoomService.getClassRooms map { classList =>
          Ok(ManageClassroomsView(CreateClassForm.form, classList))
        }
      }
    }
  }

  def createClassRoom(): Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    featureGuard(classRoomManagement) {
      teacherGuard {
        CreateClassForm.form.bindFromRequest.fold(
          errors => classRoomService.getClassRooms map { classList =>
            BadRequest(ManageClassroomsView(errors, classList))
          },
          name => classRoomService.createClassRoom(name) map { _ =>
            Redirect(routes.ClassRoomController.manageClassRooms())
          }
        )
      }
    }
  }

  def manageClassRoom(classId: String): Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    featureGuard(classRoomManagement) {
      teacherGuard {
        classRoomService.getClassRoom(classId) map { classRoom =>
          Ok(ManageClassroomView(classRoom))
        }
      }
    }
  }

  def deleteClassRoom(classId: String): Action[AnyContent] = isAuthorised { implicit req => implicit user =>
    featureGuard(classRoomManagement) {
      teacherGuard {
        classRoomService.deleteClassRoom(classId) map {
          _ => Redirect(routes.ClassRoomController.manageClassRooms())
        }
      }
    }
  }
}
