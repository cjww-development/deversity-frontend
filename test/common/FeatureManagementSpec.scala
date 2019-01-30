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

package common

import com.cjwwdev.testing.unit.UnitTestSpec
import _root_.helpers.services.MockFeatureService
import com.cjwwdev.featuremanagement.models.Feature
import com.cjwwdev.featuremanagement.services.FeatureService
import common.helpers.FeatureManagement
import play.api.i18n.Lang
import play.api.mvc.{BaseController, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Results.Ok

import scala.concurrent.Future

class FeatureManagementSpec extends UnitTestSpec with MockFeatureService {

  val testFeatureManager = new FeatureManagement with BaseController {
    override protected def controllerComponents: ControllerComponents = stubControllerComponents()
    override val featureService: FeatureService                       = mockFeatureService
  }

  implicit val req = FakeRequest()
  implicit val lang = Lang("en")

  "featureGuard" should {
    "return the result" when {
      "code generation is enabled" in {
        mockGetState(feature = Feature("code-generator", state = true))

        val result = testFeatureManager.featureGuard(testFeatureManager.codeGeneration) {
          Future.successful(Ok("Hello"))
        }

        status(result)          mustBe OK
        contentAsString(result) mustBe "Hello"
      }

      "classroom management is enabled" in {
        mockGetState(feature = Feature("classroom-management", state = true))

        val result = testFeatureManager.featureGuard(testFeatureManager.classRoomManagement) {
          Future.successful(Ok("Hello"))
        }

        status(result)          mustBe OK
        contentAsString(result) mustBe "Hello"
      }

      "enrolments review is enabled" in {
        mockGetState(feature = Feature("enrolments-review", state = true))

        val result = testFeatureManager.featureGuard(testFeatureManager.enrolmentsReview) {
          Future.successful(Ok("Hello"))
        }

        status(result)          mustBe OK
        contentAsString(result) mustBe "Hello"
      }
    }

    "return not found" when {
      "code generation is disabled" in {
        mockGetState(feature = Feature("code-generator", state = false))

        val result = testFeatureManager.featureGuard(testFeatureManager.codeGeneration) {
          Future.successful(Ok("Hello"))
        }

        status(result)          mustBe NOT_FOUND
      }

      "classroom management is disabled" in {
        mockGetState(feature = Feature("classroom-management", state = false))

        val result = testFeatureManager.featureGuard(testFeatureManager.classRoomManagement) {
          Future.successful(Ok("Hello"))
        }

        status(result) mustBe NOT_FOUND
      }

      "enrolments review is disabled" in {
        mockGetState(feature = Feature("enrolments-review", state = false))

        val result = testFeatureManager.featureGuard(testFeatureManager.enrolmentsReview) {
          Future.successful(Ok("Hello"))
        }

        status(result) mustBe NOT_FOUND
      }
    }
  }
}
