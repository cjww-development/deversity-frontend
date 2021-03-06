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

package connectors

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.responses.EvaluateResponse._
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.verbs.Http
import javax.inject.Inject
import models.Enrolments
import play.api.libs.json._
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext => ExC, Future}

sealed trait ValidOrg
case object Valid extends ValidOrg
case object Invalid extends ValidOrg

class DefaultAccountsConnector @Inject()(val http: Http,
                                         val config: ConfigurationLoader) extends AccountsConnector {
  override val accountsUrl: String = config.getServiceUrl("accounts-microservice")
}

trait AccountsConnector extends DefaultFormat with WsResponseHelpers {
  val http: Http
  val accountsUrl: String

  def getEnrolments(implicit user: CurrentUser, request: Request[_], ec: ExC): Future[Option[Enrolments]] = {
    http.get(s"$accountsUrl/account/${user.id}/enrolments") map {
      case SuccessResponse(resp) => resp.toDataType[Enrolments](needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }
}
