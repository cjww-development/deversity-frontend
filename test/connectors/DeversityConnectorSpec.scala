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

import com.cjwwdev.http.verbs.Http
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._
import com.cjwwdev.security.obfuscation.{Obfuscation, Obfuscator}
import enums.HttpResponse
import helpers.connectors.ConnectorSpec
import helpers.other.MockRequest
import models.{ClassRoom, RegistrationCode}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class DeversityConnectorSpec extends ConnectorSpec with MockRequest {

  val testConnector = new DeversityConnector {
    override val http: Http           = mockHttp
    override val deversityUrl: String = "/test/url"
  }

  implicit val classSeqObs: Obfuscator[Seq[ClassRoom]] = new Obfuscator[Seq[ClassRoom]] {
    override def encrypt(value: Seq[ClassRoom]): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }

  implicit val classObs: Obfuscator[ClassRoom] = new Obfuscator[ClassRoom] {
    override def encrypt(value: ClassRoom): String = Obfuscation.obfuscateJson(Json.toJson(value))
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

  "getRegistrationCode" should {
    "return a registration code" in {
      implicit val obfuscator: Obfuscator[RegistrationCode] = new Obfuscator[RegistrationCode] {
        override def encrypt(value: RegistrationCode): String = {
          Obfuscation.obfuscateJson(Json.toJson(value))
        }
      }

      val regCode = RegistrationCode(
        identifier = "testId",
        code       = "testRegCode",
        createdAt  = DateTime.now()
      )

      mockGet(OK, regCode.encrypt)

      awaitAndAssert(testConnector.getRegistrationCode(testCurrentUser, request, implicitly)) {
        _.code mustBe "testRegCode"
      }
    }
  }

  "generateRegistrationCode" should {
    "return a success" when {
      "a code has been generated" in {
        mockHead(response = fakeHttpResponse(OK))

        awaitAndAssert(testConnector.generateRegistrationCode(testCurrentUser, request, implicitly)) {
          _ mustBe HttpResponse.success
        }
      }
    }
  }

  "createClassroom" should {
    "return the name of the created classroom" in {
      mockHttpPostString(response = fakeHttpResponse(OK))

      awaitAndAssert(testConnector.createClassroom("testClassRoom")(testCurrentUser, request, implicitly)) {
        _ mustBe "testClassRoom"
      }
    }
  }

  "getClassrooms" should {
    "return a seq of class rooms" in {
      implicit val classRoomWrites: OWrites[ClassRoom] = new OWrites[ClassRoom] {
        override def writes(classRoom: ClassRoom): JsObject = Json.obj(
          "classId"      -> classRoom.classId,
          "schooldevId"  -> classRoom.schoolDevId,
          "teacherDevId" -> classRoom.teacherDevId,
          "name"         -> classRoom.name
        )
      }

      val classList = Json.toJson(List(Json.toJson(testClassroom), Json.toJson(testClassroom2)))

      mockGet(OK, classList.encrypt)

      awaitAndAssert(testConnector.getClassrooms(testCurrentUser, request, implicitly)) {
        _ mustBe testClassSeq
      }
    }

    "return an empty seq" in {
      mockGet(NO_CONTENT, "")

      awaitAndAssert(testConnector.getClassrooms(testCurrentUser, request, implicitly)) {
        _ mustBe Seq.empty[ClassRoom]
      }
    }
  }

  "getClassroom" should {
    "return a classroom" in {
      implicit val classRoomWrites: OWrites[ClassRoom] = new OWrites[ClassRoom] {
        override def writes(classRoom: ClassRoom): JsObject = Json.obj(
          "classId"      -> classRoom.classId,
          "schooldevId"  -> classRoom.schoolDevId,
          "teacherDevId" -> classRoom.teacherDevId,
          "name"         -> classRoom.name
        )
      }

      mockGet(OK, Json.toJson(testClassroom).encrypt)

      awaitAndAssert(testConnector.getClassroom(generateTestSystemId("class"))(testCurrentUser, request, implicitly)) {
        _ mustBe testClassroom
      }
    }
  }

  "deleteClassroom" should {
    "return a HttpSuccess" in {
      mockHttpDelete(response = fakeHttpResponse(OK))

      awaitAndAssert(testConnector.deleteClassroom(generateTestSystemId("class"))(testCurrentUser, request, implicitly)) {
        _ mustBe HttpResponse.success
      }
    }
  }
}
