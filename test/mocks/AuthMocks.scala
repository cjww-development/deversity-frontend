// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package mocks

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.{Args, BeforeAndAfterEach, Status, Suite}
import org.scalatest.mock.MockitoSugar
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

trait AuthMocks
  extends SessionBuild
    with MockitoSugar
    with MockResponse
    with ComponentMocks
    with BeforeAndAfterEach
    with Suite {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val ws = AhcWSClient()

  abstract override def runTest(testName: String, args: Args): Status = super.runTest(testName, args)

  override def beforeEach() : Unit = {
    resetMocks()
  }

  def showWithAuthorisedUser(action: Action[AnyContent],
                             mockAuthConnector: AuthConnector,
                             mockHttp : Http,
                             context: Option[AuthContext])(test: Future[Result] => Any) {
    val request = buildRequestWithSession

    val encValue: String = {
      context match {
        case Some(user) => DataSecurity.encryptData[AuthContext](user).get
        case None       => ""
      }
    }

    val mockResponse = mockWSResponseWithBody(encValue)

    when(mockHttp.GET(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(mockResponse))

    when(mockAuthConnector.getContext(ArgumentMatchers.any()))
      .thenReturn(Future.successful(context))

    val result = action.apply(request)
    test(result.run())
  }

  def submitWithAuthorisedUser(action: Action[AnyContent],
                               mockAuthConnector: AuthConnector,
                               request: FakeRequest[AnyContentAsFormUrlEncoded],
                               mockHttp : Http,
                               context : Option[AuthContext])(test: Future[Result] => Any) {

    val encValue: String = {
      context match {
        case Some(user) => DataSecurity.encryptData[AuthContext](user).get
        case None       => ""
      }
    }

    val mockResponse = mockWSResponseWithBody(encValue)

    when(mockHttp.GET(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(mockResponse))

    when(mockAuthConnector.getContext(ArgumentMatchers.any()))
      .thenReturn(Future.successful(context))

    val result = action.apply(request)
    test(result)
  }
}
