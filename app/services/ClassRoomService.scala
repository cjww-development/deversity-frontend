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

import com.cjwwdev.auth.models.CurrentUser
import connectors.DeversityConnector
import javax.inject.Inject
import models.ClassRoom
import play.api.mvc.Request

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultClassRoomService @Inject()(val devConnector: DeversityConnector) extends ClassRoomService

trait ClassRoomService {

  val devConnector: DeversityConnector

  def createClassRoom(classRoomName: String)(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[String] = {
    devConnector.createClassroom(classRoomName)
  }

  def getClassRooms(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[Seq[ClassRoom]] = {
    devConnector.getClassrooms
  }

  def getClassRoom(classId: String)(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[ClassRoom] = {
    devConnector.getClassroom(classId)
  }

  def deleteClassRoom(classId: String)(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[String] = {
    devConnector.deleteClassroom(classId) map(_ => classId)
  }
}
