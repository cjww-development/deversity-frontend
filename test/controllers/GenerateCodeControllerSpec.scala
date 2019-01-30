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
import com.cjwwdev.featuremanagement.models.Feature
import com.cjwwdev.featuremanagement.services.FeatureService
import common.Features
import enums.AccountTypes
import helpers.controllers.ControllerSpec
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import services.{EnrolmentService, RegistrationCodeService}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits

class GenerateCodeControllerSpec extends ControllerSpec {

  val testController = new GenerateCodeController {
    override val registrationCodeService: RegistrationCodeService     = mockRegistrationCodeService
    override implicit val ec: ExecutionContext                        = Implicits.global
    override val enrolmentService: EnrolmentService                   = mockEnrolmentService
    override protected val authConnector: AuthConnector               = mockAuthConnector
    override protected def controllerComponents: ControllerComponents = stubControllerComponents()
    override val featureService: FeatureService                       = mockFeatureService
  }

  "registrationCodeShow" should {
    "return an Ok" when {
      "authorised as a teacher" in {
        mockGetState(feature = Feature(Features.codeGeneration, state = true))

        mockValidateCurrentEnrolments(valid = true)

        mockGetRegistrationCode(fetched = true)

        runActionWithAuth(testController.registrationCodeShow(), request, AccountTypes.INDIVIDUAL) { res =>
          status(res) mustBe OK
        }
      }

      "authorised as an organisation" in {
        mockGetState(feature = Feature(Features.codeGeneration, state = true))

        mockValidateCurrentEnrolments(valid = true)

        mockGetRegistrationCode(fetched = true)

        runActionWithAuth(testController.registrationCodeShow(), request, AccountTypes.ORGANISATION) { res =>
          status(res) mustBe OK
        }
      }
    }
  }

  "generateRegistrationCode" should {
    "return a See other" when {
      "authorised as a teacher" in {
        mockGetState(feature = Feature(Features.codeGeneration, state = true))

        mockValidateCurrentEnrolments(valid = true)

        mockGenerateRegistrationCode(generated = true)

        runActionWithAuth(testController.generateRegistrationCode(), request, AccountTypes.INDIVIDUAL) { res =>
          status(res) mustBe SEE_OTHER
        }
      }

      "authorised as an organisation" in {
        mockGetState(feature = Feature(Features.codeGeneration, state = true))

        mockValidateCurrentEnrolments(valid = true)

        mockGenerateRegistrationCode(generated = true)

        runActionWithAuth(testController.generateRegistrationCode(), request, AccountTypes.ORGANISATION) { res =>
          status(res) mustBe SEE_OTHER
        }
      }
    }
  }
}
