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

package services

import connectors.DeversityConnector
import helpers.services.ServiceSpec

import scala.concurrent.ExecutionContext.Implicits.global

class ClassRoomServiceSpec extends ServiceSpec {

  val testService = new ClassRoomService {
    override val devConnector: DeversityConnector = mockDeversityConector
  }

  "createClassRoom" should {
    "return the classroom name" in {
      mockCreateClassRoom(name = "testClassRoom")

      awaitAndAssert(testService.createClassRoom("testClassRoom")(testCurrentUser, request, implicitly)) {
        _ mustBe "testClassRoom"
      }
    }
  }

  "getClassRooms" should {
    "return a sequence of classrooms" in {
      mockGetClassRooms(classes = testClassSeq)

      awaitAndAssert(testService.getClassRooms(testCurrentUser, request, implicitly)) {
        _ mustBe testClassSeq
      }
    }
  }

  "getClassRoom" should {
    "return a classroom" in {
      mockGetClassRoom(classRoom = testClassroom)

      awaitAndAssert(testService.getClassRoom(generateTestSystemId("class"))(testCurrentUser, request, implicitly)) {
        _ mustBe testClassroom
      }
    }
  }

  "deleteClassRoom" should {
    "return the class id" in {
      mockDeleteClassRoom(success = true)

      awaitAndAssert(testService.deleteClassRoom(generateTestSystemId("class"))(testCurrentUser, request, implicitly)) {
        _ mustBe generateTestSystemId("class")
      }
    }
  }
}
