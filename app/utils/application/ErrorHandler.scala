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
package utils.application

import javax.inject._

import com.cjwwdev.config.ConfigurationLoader
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.MessagesApi
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.api.Logger
import play.api.{Configuration, Environment, OptionalSourceMapper}
import play.api.http.Status.NOT_FOUND
import play.api.mvc.Results.{InternalServerError, NotFound, Status}
import com.cjwwdev.views.html.templates.errors.{NotFoundView, ServerErrorView, StandardErrorView}
import config.ApplicationConfiguration

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(env: Environment,
                             sm: OptionalSourceMapper,
                             router: Provider[Router],
                             val config: ConfigurationLoader,
                             implicit val messagesApi: MessagesApi)
  extends DefaultHttpErrorHandler(env, config.loadedConfig, sm, router) with RequestBuilder with ApplicationConfiguration {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger.error(s"[ErrorHandler] - [onClientError] - Url: ${request.uri}, status code: $statusCode")
    implicit val req = buildNewRequest[String](request, "")
    statusCode match {
      case NOT_FOUND  => Future.successful(NotFound(NotFoundView()))
      case _          => Future.successful(Status(statusCode)(StandardErrorView(messagesApi("errors.standard-error.message"))))
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger.error(s"[ErrorHandler] - [onServerError] - exception : $exception")
    exception.printStackTrace()
    implicit val req = buildNewRequest[String](request, "")
    Future.successful(InternalServerError(ServerErrorView()))
  }
}
