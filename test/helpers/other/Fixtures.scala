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
package helpers.other

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.security.obfuscation.{Obfuscation, Obfuscator}
import models._
import models.http.TeacherInformation
import play.api.libs.json.Json

trait Fixtures extends TestDataGenerator {
  val testOrgDevId = generateTestSystemId(DEVERSITY)

  val testOrgCurrentUser = CurrentUser(
    contextId       = generateTestSystemId(CONTEXT),
    id              = generateTestSystemId(ORG),
    orgDeversityId  = Some(generateTestSystemId(DEVERSITY)),
    credentialType  = "organisation",
    orgName         = None,
    firstName       = None,
    lastName        = None,
    role            = None,
    enrolments      = None
  )

  val testCurrentUser = CurrentUser(
    contextId       = generateTestSystemId(CONTEXT),
    id              = generateTestSystemId(USER),
    orgDeversityId  = Some(generateTestSystemId(DEVERSITY)),
    credentialType  = "individual",
    orgName         = None,
    firstName       = Some("testFirstName"),
    lastName        = Some("testLastName"),
    role            = None,
    enrolments      = Some(Json.obj(
      "deversityId" -> generateTestSystemId(DEVERSITY)
    ))
  )

  def testTeacherEnrolment: DeversityEnrolment = {
    DeversityEnrolment(
      schoolDevId = testOrgDevId,
      role        = "teacher",
      title       = Some("testTitle"),
      room        = Some("testRoom"),
      teacher     = None
    )
  }

  def testStudentEnrolment: DeversityEnrolment = {
    DeversityEnrolment(
      schoolDevId = testOrgDevId,
      role        = "student",
      title       = None,
      room        = None,
      teacher     = Some(createTestUserName)
    )
  }

  def testEnrolments: Enrolments = Enrolments(
    hubId       = None,
    deversityId = Some(generateTestSystemId(DEVERSITY)),
    diagId      = None
  )

  def testTeacherDetails: TeacherInformation = TeacherInformation(
    title     = "testTitle",
    lastName  = "testName",
    room      = "testRoom"
  )

  val testSchoolDetails: SchoolDetails = SchoolDetails(
    orgName  = "testOrgName",
    initials = "testInitials",
    location = "testLocation"
  )

  implicit val enrolmentObfuscator: Obfuscator[Enrolments] = new Obfuscator[Enrolments] {
    override def encrypt(value: Enrolments): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }

  implicit val teacherDetailsObfuscator: Obfuscator[TeacherInformation] = new Obfuscator[TeacherInformation] {
    override def encrypt(value: TeacherInformation): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }

  implicit val schoolDetailsObfuscator: Obfuscator[SchoolDetails] = new Obfuscator[SchoolDetails] {
    override def encrypt(value: SchoolDetails): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }
}
