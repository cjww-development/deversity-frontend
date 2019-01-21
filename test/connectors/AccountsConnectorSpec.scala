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
import com.cjwwdev.implicits.ImplicitDataSecurity._
import helpers.connectors.ConnectorSpec
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class AccountsConnectorSpec extends ConnectorSpec {

  private val testConnector = new AccountsConnector {
    override val http: Http           = mockHttp
    override val accountsUrl: String  = "/test/url"
  }

  "getEnrolments" should {
    "return an Enrolment" in {
      mockGet(statusCode = OK, body = testEnrolments.encrypt)

      awaitAndAssert(testConnector.getEnrolments(testCurrentUser, request, implicitly)) {
        _ mustBe Some(testEnrolments)
      }
    }

    "return no enrolment" in {
      mockGet(statusCode = NOT_FOUND, "Enrolment not found")

      awaitAndAssert(testConnector.getEnrolments(testCurrentUser, request, implicitly)) {
        _ mustBe None
      }
    }
  }
}
