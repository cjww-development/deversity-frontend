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

package helpers.controllers

import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import helpers.auth.AuthBuilder
import helpers.connectors.MockSessionStoreConnector
import helpers.other.{Fixtures, FutureAsserts}
import helpers.services.{MockEnrolmentService, MockFeatureService, MockRegistrationCodeService}
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

trait ControllerSpec
  extends PlaySpec
    with FutureAsserts
    with Fixtures
    with MockFeatureService
    with MockSessionStoreConnector
    with MockEnrolmentService
    with MockRegistrationCodeService
    with AuthBuilder {

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest()
      .withHeaders("cjww-headers" -> HeaderPackage("testAppId", Some(generateTestSystemId(SESSION))).encrypt)

  def assertFutureResult(futureResult: Future[Result])(assertions: Future[Result] => Assertion): Assertion = {
    assertions(futureResult)
  }
}
