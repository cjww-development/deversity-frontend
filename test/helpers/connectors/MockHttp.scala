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

package helpers.connectors

import com.cjwwdev.http.verbs.Http
import com.cjwwdev.http.responses.EvaluateResponse._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import org.mockito.stubbing.OngoingStubbing
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK

import scala.concurrent.Future

trait MockHttp extends BeforeAndAfterEach with MockitoSugar with MockResponse {
  self: PlaySpec =>

  val mockHttp: Http = mock[Http]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp)
  }

  def mockGet(statusCode: Int, body: String): OngoingStubbing[Future[ConnectorResponse]] = {
    val result = if((200 to 299).contains(statusCode)) {
      SuccessResponse(FakeResponse(statusCode, body))
    } else {
      ErrorResponse(FakeResponse(statusCode, body))
    }

    when(mockHttp.get(any(), any())(any(), any()))
      .thenReturn(Future.successful(result))
  }

  def mockHttpPost(response: WSResponse): OngoingStubbing[Future[ConnectorResponse]] = {
    when(mockHttp.post(any(), any(), any(),any())(any(), any(), any(), any()))
      .thenReturn(if(response.status >= 200 && response.status <= 299) {
        Future.successful(SuccessResponse(response))
      } else {
        Future.successful(ErrorResponse(response))
      })
  }

  def mockHttpPostString(response: WSResponse): OngoingStubbing[Future[ConnectorResponse]] = {
    when(mockHttp.postString(any(), any(), any(), any())(any(), any()))
      .thenReturn(if(response.status >= 200 && response.status <= 299) {
        Future.successful(SuccessResponse(response))
      } else {
        Future.successful(ErrorResponse(response))
      })
  }

  def mockPatch(statusCode: Int): OngoingStubbing[Future[ConnectorResponse]] = {
    val result = if((200 to 299).contains(statusCode)) {
      SuccessResponse(FakeResponse(statusCode))
    } else {
      ErrorResponse(FakeResponse(statusCode))
    }

    when(mockHttp.patch(any(), any(), any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(result))
  }

  def mockPatchString(body: String): OngoingStubbing[Future[ConnectorResponse]] = {
    when(mockHttp.patchString(any(), any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(SuccessResponse(FakeResponse(OK, body))))
  }

  def mockHead(response: WSResponse): OngoingStubbing[Future[ConnectorResponse]] = {
    when(mockHttp.head(any(), any())(any(), any()))
      .thenReturn(if(response.status >= 200 && response.status <= 299) {
        Future.successful(SuccessResponse(response))
      } else {
        Future.successful(ErrorResponse(response))
      })
  }

  def mockHttpDelete(response: WSResponse): OngoingStubbing[Future[ConnectorResponse]] = {
    when(mockHttp.delete(any(), any())(any(), any()))
      .thenReturn(if(response.status >= 200 && response.status <= 299) {
        Future.successful(SuccessResponse(response))
      } else {
        Future.successful(ErrorResponse(response))
      })
  }
}
