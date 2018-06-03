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

import com.cjwwdev.frontendUI.builders.NavBarLinkBuilder
import com.typesafe.config.ConfigFactory
import controllers.routes
import play.api.mvc.{Call, RequestHeader}

trait ApplicationConfiguration {
  private def buildServiceUrl(service: String): String = ConfigFactory.load.getString(s"microservice.external-services.$service.domain")

  val authService               = buildServiceUrl("auth-service")
  val accountsMicroservice      = buildServiceUrl("accounts-microservice")
  val authMicroservice          = buildServiceUrl("auth-microservice")
  val sessionStore              = buildServiceUrl("session-store")
  val diagnosticsFrontend       = buildServiceUrl("diagnostics-frontend")
  val deversityFrontend         = buildServiceUrl("deversity-frontend")
  val deversityMicroservice     = buildServiceUrl("deversity")
  val hubFrontend               = buildServiceUrl("hub-frontend")

  val USER_LOGIN                = s"$authService/login?redirect=deversity"
  val SERVICE_DIRECTOR          = s"$authService/where-do-you-want-to-go"
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
    "userRegister"  -> routes.RedirectController.redirectToUserRegister(),
    "orgRegister"   -> routes.RedirectController.redirectToOrgRegister(),
    "login"         -> routes.RedirectController.redirectToLogin(),
    "dashboard"     -> routes.RedirectController.redirectToUserDashboard(),
    "signOut"       -> routes.RedirectController.redirectToSignOut()
  )
}
