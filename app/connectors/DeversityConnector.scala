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
package connectors

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.responses.EvaluateResponse._
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._
import enums.HttpResponse
import javax.inject.Inject
import models.http.TeacherInformation
import models.{ClassRoom, DeversityEnrolment, RegistrationCode, SchoolDetails}
import play.api.libs.json._
import play.api.mvc.Request
import play.api.http.Status.{NO_CONTENT, OK}

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultDeversityConnector @Inject()(val http: Http,
                                          val config: ConfigurationLoader) extends DeversityConnector {
  override val deversityUrl: String = config.getServiceUrl("deversity")
}

trait DeversityConnector extends DefaultFormat with WsResponseHelpers {
  val http: Http

  val deversityUrl: String

  def getDeversityUserInfo(implicit user: CurrentUser, request: Request[_], ec: ExC): Future[Option[DeversityEnrolment]] = {
    http.get(s"$deversityUrl/user/${user.id}/enrolment") map {
      case SuccessResponse(resp) => resp.status match {
        case OK         => resp.toDataType[DeversityEnrolment](needsDecrypt = true).fold(Some(_), _ => None)
        case NO_CONTENT => None
      }
      case ErrorResponse(_)      => None
    }
  }

  def getTeacherDetails(teacherDevId: String, schoolDevId: String)
                       (implicit user: CurrentUser, request: Request[_], ec: ExC): Future[Option[TeacherInformation]] = {
    http.get(s"$deversityUrl/user/${user.id}/teacher/${teacherDevId.encrypt}/school/${schoolDevId.encrypt}/details") map {
      case SuccessResponse(resp) => resp.toDataType[TeacherInformation](needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }

  def createDeversityId(implicit user: CurrentUser, request: Request[_], ec: ExC): Future[Option[String]] = {
    http.patchString(s"$deversityUrl/user/${user.id}/create-deversity-id", "") collect {
      case SuccessResponse(resp) => resp.toResponseString(needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }

  def initialiseDeversityEnrolment(devEnrolment: DeversityEnrolment)(implicit user: CurrentUser, request: Request[_], ec: ExC): Future[Int] = {
    http.patch[DeversityEnrolment](s"$deversityUrl/user/${user.id}/enrolment", devEnrolment) map {
      case SuccessResponse(resp) => resp.status
      case ErrorResponse(resp)   => resp.status
    }
  }

  def validateSchool(regCode: String)(implicit request: Request[_], ec: ExC): Future[Option[String]] = {
    http.get(s"$deversityUrl/validation/school/${regCode.encrypt}") map {
      case SuccessResponse(resp) => resp.toResponseString(needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }

  def validateTeacher(regCode: String, schoolDevId: String)(implicit request: Request[_], ec: ExC): Future[Option[String]] = {
    http.get(s"$deversityUrl/validation/teacher/${regCode.encrypt}/school/${schoolDevId.encrypt}") map {
      case SuccessResponse(resp) => resp.toResponseString(needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }

  def getSchoolDetails(orgDevId: String)(implicit user: CurrentUser, request: Request[_], ec: ExC): Future[Option[SchoolDetails]] = {
    http.get(s"$deversityUrl/user/${user.id}/school/${orgDevId.encrypt}/details") map {
      case SuccessResponse(resp) => resp.toDataType[SchoolDetails](needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }

  def getRegistrationCode(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[RegistrationCode] = {
    http.get(s"$deversityUrl/user/${user.id}/fetch-registration-code") collect {
      case SuccessResponse(resp) => resp.toDataType[RegistrationCode](needsDecrypt = true).left.get
    }
  }

  def generateRegistrationCode(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[HttpResponse.Value] = {
    http.head(s"$deversityUrl/user/${user.id}/generate-registration-code") map {
      case SuccessResponse(_) => HttpResponse.success
      case ErrorResponse(_)   => HttpResponse.failed
    }
  }

  def createClassroom(classRoomName: String)(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[String] = {
    http.postString(s"$deversityUrl/teacher/${user.id}/create-classroom", classRoomName) map {
      _ => classRoomName
    }
  }

  def getClassrooms(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[Seq[ClassRoom]] = {
    http.get(s"$deversityUrl/teacher/${user.id}/classrooms") map {
      case SuccessResponse(resp) => resp.status match {
        case OK         => resp.toDataType[Seq[ClassRoom]](needsDecrypt = true).fold(identity, _ => Seq.empty[ClassRoom])
        case NO_CONTENT => Seq.empty[ClassRoom]
      }
      case ErrorResponse(_) => Seq.empty[ClassRoom]
    }
  }

  def getClassroom(classId: String)(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[ClassRoom] = {
    http.get(s"$deversityUrl/teacher/${user.id}/classroom/$classId") collect {
      case SuccessResponse(resp) => resp.toDataType[ClassRoom](needsDecrypt = true).left.get
    }
  }

  def deleteClassroom(classId: String)(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[HttpResponse.Value] = {
    http.delete(s"$deversityUrl/teacher/${user.id}/classroom/$classId") map {
      case SuccessResponse(_) => HttpResponse.success
      case ErrorResponse(_)   => HttpResponse.failed
    }
  }
}
