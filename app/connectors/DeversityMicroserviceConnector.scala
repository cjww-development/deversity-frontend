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
package connectors

import com.cjwwdev.auth.models.CurrentUser
import javax.inject.Inject
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.implicits.ImplicitHandlers
import common.ApplicationConfiguration
import models.http.TeacherInformation
import models.{ClassRoom, DeversityEnrolment, SchoolDetails}
import play.api.libs.json._
import play.api.mvc.Request
import play.api.http.Status._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeversityMicroserviceConnectorImpl @Inject()(val http: Http) extends DeversityMicroserviceConnector with ApplicationConfiguration

trait DeversityMicroserviceConnector extends ImplicitHandlers with DefaultFormat {
  val http: Http

  val deversityMicroservice: String

  case class JS(value: Option[String] = None)
  implicit val format: OFormat[JS] = Json.format[JS]

  def getDeversityUserInfo(implicit user: CurrentUser, request: Request[_]): Future[Option[DeversityEnrolment]] = {
    http.get(s"$deversityMicroservice/enrolment/${user.id}/deversity") map { resp =>
      resp.status match {
        case OK         => Some(resp.body.decryptType[DeversityEnrolment])
        case NO_CONTENT => None
      }
    } recover {
      case _: NotFoundException => None
    }
  }

  def getTeacherDetails(teacherDevId: String, schoolDevId: String)(implicit user: CurrentUser, request: Request[_]): Future[Option[TeacherInformation]] = {
    http.get(s"$deversityMicroservice/user/${user.id}/teacher/${teacherDevId.encrypt}/school/${schoolDevId.encrypt}/details") map { resp =>
      Some(resp.body.decryptType[TeacherInformation])
    } recover {
      case _: NotFoundException => None
    }
  }

  def createDeversityId(implicit user: CurrentUser, request: Request[_]): Future[String] = {
    http.patch[JS](s"$deversityMicroservice/${user.id}/create-deversity-id", JS()) map {
      _.body.decrypt
    }
  }

  def initialiseDeversityEnrolment(deversityEnrolment: DeversityEnrolment)(implicit user: CurrentUser, request: Request[_]): Future[Int] = {
    http.patch[DeversityEnrolment](s"$deversityMicroservice/enrolment/${user.id}/deversity", deversityEnrolment) map(_.status)
  }

  def validateSchool(regCode: String)(implicit request: Request[_]): Future[String] = {
    http.get(s"$deversityMicroservice/validate/school/${regCode.encrypt}") map {
      _.body.decrypt
    }
  }

  def validateTeacher(regCode: String, schoolDevId: String)(implicit request: Request[_]): Future[String] = {
    http.get(s"$deversityMicroservice/validate/teacher/${regCode.encrypt}/school/${schoolDevId.encrypt}") map {
      _.body.decrypt
    }
  }

  def getSchoolDetails(orgDevId: String)(implicit user: CurrentUser, request: Request[_]): Future[Option[SchoolDetails]] = {
    http.get(s"$deversityMicroservice/user/${user.id}/school/${orgDevId.encrypt}/details") map { resp =>
      Some(resp.body.decryptType[SchoolDetails])
    } recover {
      case _: NotFoundException => None
    }
  }

  def lookupRegistrationCode(regCode: String)(implicit user: CurrentUser, request: Request[_]): Future[String] = {
    http.get(s"$deversityMicroservice/user/${user.id}/lookup/$regCode/lookup-reg-code") map {
      _.body.decrypt
    }
  }
}
