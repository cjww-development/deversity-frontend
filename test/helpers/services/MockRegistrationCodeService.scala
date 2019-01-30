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

package helpers.services

import common.responses.{CurrentEnrolmentResponse, InvalidEnrolments}
import enums.HttpResponse
import helpers.other.Fixtures
import models.RegistrationCode
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.RegistrationCodeService

import scala.concurrent.Future

trait MockRegistrationCodeService extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockRegistrationCodeService = mock[RegistrationCodeService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRegistrationCodeService)
  }

  def mockGetRegistrationCode(fetched: Boolean): OngoingStubbing[Future[Either[CurrentEnrolmentResponse, RegistrationCode]]] = {
    when(mockRegistrationCodeService.getRegistrationCode(any(), any(), any()))
      .thenReturn(if(fetched) Future.successful(Right(testRegistrationCode)) else Future.successful(Left(InvalidEnrolments)))
  }

  def mockGenerateRegistrationCode(generated: Boolean): OngoingStubbing[Future[Either[CurrentEnrolmentResponse, HttpResponse.Value]]] = {
    when(mockRegistrationCodeService.generateRegistrationCode(any(), any(), any()))
      .thenReturn(if(generated) Future.successful(Right(HttpResponse.success)) else Future.successful(Left(InvalidEnrolments)))
  }
}
