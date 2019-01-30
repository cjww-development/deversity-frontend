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

package helpers.auth

import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Action, Request, Result}

import scala.concurrent.Future

trait AuthBuilder extends MockAuthConnector {
  self: PlaySpec =>

  def runActionWithAuth[A](action: Action[A], request: Request[A], authorisedAs: String)(test: Future[Result] => Assertion): Assertion = {
    authorisedAs match {
      case "individual"   => mockGetIndCurrentUser(true)
      case "organisation" => mockGetOrgCurrentUser(true)
    }

    test(action(request))
  }

  def runActionWithoutAuth[A](action: Action[A], request: Request[A])(test: Future[Result] => Assertion): Assertion = {
    test(action(request))
  }
}
