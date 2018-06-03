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

package common

import java.util.Locale

import com.cjwwdev.views.html.templates.errors.{NotFoundView, ServerErrorView, StandardErrorView}
import javax.inject.{Inject, Provider, Singleton}
import play.api.{Environment, OptionalSourceMapper}
import play.api.http.HttpErrorHandler
import play.api.http.Status.{FORBIDDEN, NOT_FOUND}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.mvc.Results.{InternalServerError, NotFound, Redirect, Status}
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(env: Environment,
                             sm: OptionalSourceMapper,
                             router: Provider[Router],
                             messagesApi: MessagesApi) extends HttpErrorHandler with RequestBuilder with ApplicationConfiguration with Logging {

  implicit val messages: Messages = MessagesImpl(Lang(Locale.ENGLISH), messagesApi)

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.error(s"[ErrorHandler] - [onClientError] - Url: ${request.uri}, status code: $statusCode")
    implicit val req: Request[String] = buildNewRequest[String](request, "")
    statusCode match {
      case NOT_FOUND  => Future.successful(NotFound(NotFoundView()))
      case FORBIDDEN  => Future.successful(Redirect(USER_LOGIN_CALL))
      case _          => Future.successful(Status(statusCode)(StandardErrorView(messages("errors.standard-error.message"))))
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error(s"[ErrorHandler] - [onServerError] - exception : $exception")
    exception.printStackTrace()
    implicit val req = buildNewRequest[String](request, "")
    Future.successful(InternalServerError(ServerErrorView()))
  }
}
