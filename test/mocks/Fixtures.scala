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
 *
 */
package mocks

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.implicits.ImplicitHandlers
import org.joda.time.DateTime
import play.api.libs.json.Json

trait Fixtures extends TestDataGenerator with ImplicitHandlers {
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

  implicit val testCurrentUser = CurrentUser(
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
}
