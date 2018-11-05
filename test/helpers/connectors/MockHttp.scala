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

package helpers.connectors

import com.cjwwdev.http.verbs.Http
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import org.mockito.stubbing.OngoingStubbing
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockHttp extends BeforeAndAfterEach with MockitoSugar with MockResponse {
  self: PlaySpec =>

  val mockHttp: Http = mock[Http]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp)
  }

  def mockGet(statusCode: Int, body: String): OngoingStubbing[Future[WSResponse]] = {
    when(mockHttp.get(any(), any())(any()))
      .thenReturn(Future(FakeResponse(statusCode, body)))
  }

  def mockFailedGet(exception: Exception): OngoingStubbing[Future[WSResponse]] = {
    when(mockHttp.get(any(), any())(any()))
      .thenReturn(Future.failed(exception))
  }

  def mockPatch(statusCode: Int): OngoingStubbing[Future[WSResponse]] = {
    when(mockHttp.patch(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Future(FakeResponse(statusCode)))
  }

  def mockPatchString(body: String): OngoingStubbing[Future[WSResponse]] = {
    when(mockHttp.patchString(any(), any(), any(), any())(any()))
      .thenReturn(Future(FakeResponse(OK, body)))
  }
}
