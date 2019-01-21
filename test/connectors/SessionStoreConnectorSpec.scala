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
import com.cjwwdev.security.obfuscation.Obfuscation._
import helpers.connectors.ConnectorSpec
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class SessionStoreConnectorSpec extends ConnectorSpec {

  private val testConnector = new SessionStoreConnector {
    override val http: Http           = mockHttp
    override val sessionStore: String = "/test/url"
  }

  "getDataElement" should {
    "return testString" in {
      mockGet(statusCode = OK, body = "testString".encrypt)

      awaitAndAssert(testConnector.getDataElement("testKey")(request, implicitly)) {
        _ mustBe Some("testString")
      }
    }

    "return no string" in {
      mockGet(statusCode = NOT_FOUND, "Data for key not found")

      awaitAndAssert(testConnector.getDataElement("testKey")(request, implicitly)) {
        _ mustBe None
      }
    }
  }
}
