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

import connectors.SessionStoreConnector
import enums.SessionCache
import helpers.other.Fixtures
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future

trait MockSessionStoreConnector extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockSessionStoreConnector = mock[SessionStoreConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionStoreConnector)
  }

  def mockGetDataElement(returned: Option[String]): OngoingStubbing[Future[Option[String]]] = {
    when(mockSessionStoreConnector.getDataElement(any())(any(), any()))
      .thenReturn(Future.successful(returned))
  }

  def mockUpdateSession(updated: Boolean): OngoingStubbing[Future[SessionCache.Value]] = {
    when(mockSessionStoreConnector.updateSession(any())(any(), any()))
      .thenReturn(Future.successful(if(updated) SessionCache.cacheUpdated else SessionCache.cacheUpdateFailure))
  }
}
