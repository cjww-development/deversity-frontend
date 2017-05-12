// Copyright (C) 2016-2017 the original author or authors.
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

package connectors

import com.cjwwdev.http.verbs.Http
import com.cjwwdev.logging.Logger
import com.cjwwdev.security.encryption.DataSecurity
import com.google.inject.{Inject, Singleton}
import config.ApplicationConfiguration
import models.SessionUpdateSet
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SessionStoreConnector @Inject()(http : Http) extends ApplicationConfiguration{
  def getDataElement(key : String)(implicit request: Request[_]) : Future[String] = {
    http.GET(s"$session_store_route/session/${request.session("cookieId")}/data/$key") map(resp => resp.body)
  }

  def updateSession(updateSet : SessionUpdateSet)(implicit format : Format[SessionUpdateSet], request: Request[_]) : Future[Int] = {
    http.PUT[SessionUpdateSet](s"$session_store_route/session/${request.session("cookieId")}", updateSet) map(_.status)
  }
}
