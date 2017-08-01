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

import com.cjwwdev.http.exceptions.{ForbiddenException, NotFoundException, ServerErrorException}
import com.cjwwdev.http.utils.SessionUtils
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import com.google.inject.{Inject, Singleton}
import config.ApplicationConfiguration
import enums.SessionCache
import models.SessionUpdateSet
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SessionStoreConnector @Inject()(http : Http) extends ApplicationConfiguration with SessionUtils {
  implicit val jsValueReads: Reads[JsValue] = new Reads[JsValue] {
    override def reads(json: JsValue): JsResult[JsValue] = JsSuccess(json)
  }

  def getDataElement(key : String)(implicit request: Request[_]) : Future[Option[String]] = {
    http.GET[JsValue](s"$sessionStore/session/$getCookieId/data/$key")(request, jsValueReads) map {
      data => Some(DataSecurity.decryptString((data \ "data").as[String]))
    } recover {
      case _: NotFoundException   => None
      case e: ForbiddenException  => throw e
    }
  }

  def updateSession(updateSet : SessionUpdateSet)(implicit writes: OWrites[SessionUpdateSet], request: Request[_]) : Future[SessionCache.Value] = {
    http.PUT[SessionUpdateSet](s"$sessionStore/session/$getCookieId", updateSet) map {
      _.status match {
        case OK => SessionCache.cacheUpdated
      }
    } recover {
      case _: ServerErrorException => SessionCache.cacheUpdateFailure
    }
  }
}
