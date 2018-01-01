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
package common

import java.security.cert.X509Certificate
import javax.inject.{Inject, Provider, Singleton}

import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.filters.RequestLoggingFilter
import com.cjwwdev.frontendUI.builders.NavBarLinkBuilder
import com.cjwwdev.views.html.templates.errors.{NotFoundView, ServerErrorView, StandardErrorView}
import com.kenshoo.play.metrics.MetricsFilter
import controllers.routes
import filters.IPWhitelistFilter
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status.NOT_FOUND
import play.api.{Environment, OptionalSourceMapper}
import play.api.http.{DefaultHttpFilters, HttpErrorHandler}
import play.api.i18n.{I18NSupportLowPriorityImplicits, I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.mvc.Results.{InternalServerError, NotFound, Status}
import play.api.routing.Router
import services.EnrolmentService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NoStackTrace

trait ApplicationConfiguration extends ConfigurationLoader {
  val authService               = buildServiceUrl("auth-service")
  val accountsMicroservice      = buildServiceUrl("accounts-microservice")
  val authMicroservice          = buildServiceUrl("auth-microservice")
  val sessionStore              = buildServiceUrl("session-store")
  val diagnosticsFrontend       = buildServiceUrl("diagnostics-frontend")
  val deversityFrontend         = buildServiceUrl("deversity-frontend")
  val deversityMicroservice     = buildServiceUrl("deversity")
  val hubFrontend               = buildServiceUrl("hub-frontend")

  val USER_LOGIN                = s"$authService/login?redirect=deversity"
  val USER_REGISTER             = s"$authService/create-an-account"
  val ORG_REGISTER              = s"$authService/create-an-organisation-account"
  val DASHBOARD                 = s"$authService/dashboard"
  val SIGN_OUT                  = s"$authService/goodbye"

  val USER_LOGIN_CALL           = Call("GET", USER_LOGIN)

  implicit def serviceLinks(implicit requestHeader: RequestHeader): Seq[NavBarLinkBuilder] = Seq(
    NavBarLinkBuilder("/", "glyphicon-home", "Home", "home"),
    NavBarLinkBuilder(routes.RedirectController.redirectToDiagnostics().absoluteURL(), "glyphicon-wrench", "Diagnostics", "diagnostics"),
    NavBarLinkBuilder(routes.RedirectController.redirectToDeversity().absoluteURL(), "glyphicon-education", "Deversity", "deversity"),
    NavBarLinkBuilder("/", "glyphicon-asterisk", "Hub", "the-hub")
  )

  implicit def standardNavBarRoutes(implicit requestHeader: RequestHeader): Map[String, Call] = Map(
    "navBarLogo"    -> routes.Assets.versioned("images/logo.png"),
    "globalAssets"  -> routes.Assets.versioned("stylesheets/global-assets.css"),
    "favicon"       -> routes.Assets.versioned("images/favicon.ico"),
    "userRegister"  -> Call("GET", routes.RedirectController.redirectToUserRegister().absoluteURL()),
    "orgRegister"   -> Call("GET", routes.RedirectController.redirectToOrgRegister().absoluteURL()),
    "login"         -> Call("GET", routes.RedirectController.redirectToLogin().absoluteURL()),
    "dashboard"     -> Call("GET", routes.RedirectController.redirectToUserDashboard().absoluteURL()),
    "signOut"       -> Call("GET", routes.RedirectController.redirectToSignOut().absoluteURL())
  )
}

trait Logging {
  val logger: Logger = LoggerFactory.getLogger(getClass)
}

trait FrontendController extends Controller with ApplicationConfiguration with I18NSupportLowPriorityImplicits with I18nSupport with Logging {
  implicit val messagesApi: MessagesApi

  val enrolmentService: EnrolmentService

  def checkDeversityEnrolment(f: => Future[Result])(implicit authContext: AuthContext, request: Request[_]): Future[Result] = {
    enrolmentService.validateCurrentEnrolments flatMap {
      case ValidEnrolments    => f
      case InvalidEnrolments  => validateDevId
    }
  }

  private def validateDevId(implicit authContext: AuthContext, request: Request[_]): Future[Result] = {
    request.session.get("devId") match {
      case Some(_) => Future.successful(Redirect(routes.EnrolmentController.enrolmentWelcome()))
      case None    => enrolmentService.getOrGenerateDeversityId map {
        case Some(id) => Redirect(routes.EnrolmentController.enrolmentWelcome()).withSession(request.session. +("devId" -> id))
        case None     => throw new DevIdGetOrGenerationException(s"There was a problem getting or generating a dev id for user ${authContext.user.id}")
      }
    }
  }
}

trait RequestBuilder {
  def buildNewRequest[T](requestHeader : RequestHeader, requestBody : T) : Request[T] = {
    new Request[T] {
      override def body: T                                              = requestBody
      override def secure: Boolean                                      = requestHeader.secure
      override def uri: String                                          = requestHeader.uri
      override def queryString: Map[String, Seq[String]]                = requestHeader.queryString
      override def remoteAddress: String                                = requestHeader.remoteAddress
      override def method: String                                       = requestHeader.method
      override def headers: Headers                                     = requestHeader.headers
      override def path: String                                         = requestHeader.path
      override def clientCertificateChain: Option[Seq[X509Certificate]] = requestHeader.clientCertificateChain
      override def version: String                                      = requestHeader.version
      override def tags: Map[String, String]                            = requestHeader.tags
      override def id: Long                                             = requestHeader.id
    }
  }
}

@Singleton
class ErrorHandler @Inject()(env: Environment,
                             sm: OptionalSourceMapper,
                             router: Provider[Router],
                             implicit val messagesApi: MessagesApi)
  extends HttpErrorHandler with RequestBuilder with ApplicationConfiguration with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.error(s"[ErrorHandler] - [onClientError] - Url: ${request.uri}, status code: $statusCode")
    implicit val req = buildNewRequest[String](request, "")
    statusCode match {
      case NOT_FOUND  => Future.successful(NotFound(NotFoundView()))
      case _          => Future.successful(Status(statusCode)(StandardErrorView(messagesApi("errors.standard-error.message"))))
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error(s"[ErrorHandler] - [onServerError] - exception : $exception")
    exception.printStackTrace()
    implicit val req = buildNewRequest[String](request, "")
    Future.successful(InternalServerError(ServerErrorView()))
  }
}

class EnabledFilters @Inject()(requestLoggingFilter: RequestLoggingFilter, metricsFilter: MetricsFilter, iPWhitelistFilter: IPWhitelistFilter)
  extends DefaultHttpFilters(requestLoggingFilter, metricsFilter, iPWhitelistFilter)

sealed trait DeversityIdResponse
case object ValidId extends DeversityIdResponse
case object InvalidId extends DeversityIdResponse
case object NoIdPresent extends DeversityIdResponse

sealed trait DeversityCurrentEnrolmentResponse
case object ValidEnrolments extends DeversityCurrentEnrolmentResponse
case object InvalidEnrolments extends DeversityCurrentEnrolmentResponse

class NoEnrolmentsException(msg: String) extends NoStackTrace
class ExistingDeversityIdException(msg: String) extends NoStackTrace
class DevIdGetOrGenerationException(msg: String) extends NoStackTrace