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

package services

import common.responses.InvalidEnrolments
import connectors.DeversityConnector
import enums.{HttpResponse, UserRoles}
import helpers.services.ServiceSpec

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationCodeServiceSpec extends ServiceSpec {

  val testService = new RegistrationCodeService {
    override val devConnector: DeversityConnector = mockDeversityConector
  }

  "getRegistrationCode" should {
    "return a registration code" when {
      "the user has an organisation account" in {
        mockGetRegistrationCode(regCode = testRegistrationCode)

        awaitAndAssert(testService.getRegistrationCode(testOrgCurrentUser, request, implicitly)) {
          _ mustBe Right(testRegistrationCode)
        }
      }

      "the user is an individual and is a teacher" in {
        mockGetDeversityUserInfo(enroled = true)

        mockGetRegistrationCode(regCode = testRegistrationCode)

        awaitAndAssert(testService.getRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Right(testRegistrationCode)
        }
      }
    }

    "return invalid enrolments" when {
      "the user is an individual but is a student" in {
        mockGetDeversityUserInfo(enroled = true, as = UserRoles.STUDENT)

        awaitAndAssert(testService.getRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Left(InvalidEnrolments)
        }
      }

      "the user doesn't have a deversity enrolment" in {
        mockGetDeversityUserInfo(enroled = false)

        awaitAndAssert(testService.getRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Left(InvalidEnrolments)
        }
      }
    }
  }

  "generateRegistrationCode" should {
    "return a success HttpResponse" when {
      "a registration code is successfully generated for an org user" in {
        mockGenerateRegistrationCode(success = true)

        awaitAndAssert(testService.generateRegistrationCode(testOrgCurrentUser, request, implicitly)) {
          _ mustBe Right(HttpResponse.success)
        }
      }

      "a registration code is successfully generated for a individual who is a teacher" in {
        mockGetDeversityUserInfo(enroled = true)

        mockGenerateRegistrationCode(success = true)

        awaitAndAssert(testService.generateRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Right(HttpResponse.success)
        }
      }
    }

    "return a failed HttpResponse" when {
      "a registration code wasn't successfully generated for an org user" in {
        mockGenerateRegistrationCode(success = false)

        awaitAndAssert(testService.generateRegistrationCode(testOrgCurrentUser, request, implicitly)) {
          _ mustBe Right(HttpResponse.failed)
        }
      }

      "a registration code wasn't successfully generated for a individual who is a teacher" in {
        mockGetDeversityUserInfo(enroled = true)

        mockGenerateRegistrationCode(success = false)

        awaitAndAssert(testService.generateRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Right(HttpResponse.failed)
        }
      }
    }

    "return an InvalidEnrolment" when {
      "the user is an individual but is a student" in {
        mockGetDeversityUserInfo(enroled = true, as = UserRoles.STUDENT)

        awaitAndAssert(testService.generateRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Left(InvalidEnrolments)
        }
      }

      "the user doesn't have a deversity enrolment" in {
        mockGetDeversityUserInfo(enroled = false)

        awaitAndAssert(testService.generateRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe Left(InvalidEnrolments)
        }
      }
    }
  }
}
