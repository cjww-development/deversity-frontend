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

import com.cjwwdev.auth.models.CurrentUser
import javax.inject.Inject
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.verbs.Http
import common.ApplicationConfiguration
import models.Enrolments
import play.api.libs.json._
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait ValidOrg
case object Valid extends ValidOrg
case object Invalid extends ValidOrg

class DefaultAccountsMicroserviceConnector @Inject()(val http: Http) extends AccountsMicroserviceConnector with ApplicationConfiguration

trait AccountsMicroserviceConnector extends DefaultFormat with WsResponseHelpers {
  val http: Http
  val accountsMicroservice: String

  def getEnrolments(implicit user: CurrentUser, request: Request[_]): Future[Option[Enrolments]] = {
    http.get(s"$accountsMicroservice/account/${user.id}/enrolments") map { resp =>
      Some(resp.toDataType[Enrolments](needsDecrypt = true))
    } recover {
      case _: NotFoundException => None
    }
  }
}
