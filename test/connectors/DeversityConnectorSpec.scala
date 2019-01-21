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

import com.cjwwdev.http.verbs.Http
import helpers.connectors.ConnectorSpec
import play.api.test.Helpers._
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._

import scala.concurrent.ExecutionContext.Implicits.global

class DeversityConnectorSpec extends ConnectorSpec {

  val testConnector = new DeversityConnector {
    override val http: Http           = mockHttp
    override val deversityUrl: String = "/test/url"
  }

  "getDeversityUserInfo" should {
    "return a deversity enrolment" when {
      "the response is Ok" in {
        mockGet(statusCode = OK, body = testStudentEnrolment.encrypt)

        awaitAndAssert(testConnector.getDeversityUserInfo(testCurrentUser, request, implicitly)) {
          _ mustBe Some(testStudentEnrolment)
        }
      }
    }

    "return no deversity enrolment" when {
      "the response is No content" in {
        mockGet(statusCode = NO_CONTENT, body = "")

        awaitAndAssert(testConnector.getDeversityUserInfo(testCurrentUser, request, implicitly)) {
          _ mustBe None
        }
      }

      "response is Not found" in {
        mockGet(statusCode = NOT_FOUND, "No enrolment")

        awaitAndAssert(testConnector.getDeversityUserInfo(testCurrentUser, request, implicitly)) {
          _ mustBe None
        }
      }
    }
  }

  "getTeacherDetails" should {
    "return a teacher details" in {
      mockGet(statusCode = OK, body = testTeacherDetails.encrypt)

      awaitAndAssert(testConnector.getTeacherDetails("testId", "testId")(testCurrentUser, request, implicitly)) {
        _ mustBe Some(testTeacherDetails)
      }
    }

    "return no teacher details" in {
      mockGet(statusCode = NOT_FOUND, "No teacher info")

      awaitAndAssert(testConnector.getTeacherDetails("testId", "testId")(testCurrentUser, request, implicitly)) {
        _ mustBe None
      }
    }
  }

  "createDeversityId" should {
    "return a new deversity id" in {
      mockPatchString(body = generateTestSystemId(DEVERSITY).encrypt)

      awaitAndAssert(testConnector.createDeversityId(testCurrentUser, request, implicitly)) {
        _ mustBe Some(generateTestSystemId(DEVERSITY))
      }
    }
  }

  "initialiseDeversityEnrolment" should {
    "return an Ok" in {
      mockPatch(statusCode = OK)

      awaitAndAssert(testConnector.initialiseDeversityEnrolment(testStudentEnrolment)(testCurrentUser, request, implicitly)) {
        _ mustBe OK
      }
    }
  }

  "validateSchool" should {
    "return a school name" in {
      mockGet(statusCode = OK, body = "testSchool".encrypt)

      awaitAndAssert(testConnector.validateSchool("testRegCode")(request, implicitly)) {
        _ mustBe Some("testSchool")
      }
    }
  }

  "validateTeacher" should {
    "return a school name" in {
      mockGet(statusCode = OK, body = "testTeacher".encrypt)

      awaitAndAssert(testConnector.validateTeacher("testRegCode", "testOrgId")(request, implicitly)) {
        _ mustBe Some("testTeacher")
      }
    }
  }

  "getSchoolDetails" should {
    "return a school details" in {
      mockGet(statusCode = OK, body = testSchoolDetails.encrypt)

      awaitAndAssert(testConnector.getSchoolDetails("testOrgId")(testCurrentUser, request, implicitly)) {
        _ mustBe Some(testSchoolDetails)
      }
    }

    "return no school details" in {
      mockGet(statusCode = NOT_FOUND, "No school details")

      awaitAndAssert(testConnector.getSchoolDetails("testOrgId")(testCurrentUser, request, implicitly)) {
        _ mustBe None
      }
    }
  }
}
