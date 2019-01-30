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

package helpers.connectors

import connectors.DeversityConnector
import enums.{HttpResponse, UserRoles}
import helpers.other.Fixtures
import models.{ClassRoom, DeversityEnrolment, RegistrationCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future

trait MockDeversityConnector extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockDeversityConector = mock[DeversityConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDeversityConector)
  }

  def mockGetDeversityUserInfo(enroled: Boolean, as: String = UserRoles.TEACHER): OngoingStubbing[Future[Option[DeversityEnrolment]]] = {
    val enr = if(enroled) {
      as match {
        case UserRoles.TEACHER => Some(testTeacherEnrolment)
        case UserRoles.STUDENT => Some(testStudentEnrolment)
      }
    } else {
      None
    }

    when(mockDeversityConector.getDeversityUserInfo(any(), any(), any()))
      .thenReturn(Future.successful(enr))
  }

  def mockGetRegistrationCode(regCode: RegistrationCode): OngoingStubbing[Future[RegistrationCode]] = {
    when(mockDeversityConector.getRegistrationCode(any(), any(), any()))
      .thenReturn(Future.successful(regCode))
  }

  def mockGenerateRegistrationCode(success: Boolean): OngoingStubbing[Future[HttpResponse.Value]] = {
    when(mockDeversityConector.generateRegistrationCode(any(), any(), any()))
      .thenReturn(if(success) Future.successful(HttpResponse.success) else Future.successful(HttpResponse.failed))
  }

  def mockCreateClassRoom(name: String): OngoingStubbing[Future[String]] = {
    when(mockDeversityConector.createClassroom(any())(any(), any(), any()))
      .thenReturn(Future.successful(name))
  }

  def mockGetClassRooms(classes: Seq[ClassRoom]): OngoingStubbing[Future[Seq[ClassRoom]]] = {
    when(mockDeversityConector.getClassrooms(any(), any(), any()))
      .thenReturn(Future.successful(classes))
  }

  def mockGetClassRoom(classRoom: ClassRoom): OngoingStubbing[Future[ClassRoom]] = {
    when(mockDeversityConector.getClassroom(any())(any(), any(), any()))
      .thenReturn(Future.successful(classRoom))
  }

  def mockDeleteClassRoom(success: Boolean): OngoingStubbing[Future[HttpResponse.Value]] = {
    when(mockDeversityConector.deleteClassroom(any())(any(), any(), any()))
      .thenReturn(if(success) Future.successful(HttpResponse.success) else Future.successful(HttpResponse.failed))
  }
}
