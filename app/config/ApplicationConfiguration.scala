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
package config

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.frontendUI.builders.NavBarLinkBuilder
import controllers.routes
import play.api.mvc.Call
import play.api.mvc.RequestHeader

trait ApplicationConfiguration {

  val config: ConfigurationLoader

  val authService               = config.buildServiceUrl("auth-service")
  val sessionStore              = config.buildServiceUrl("session-store")
  val accountMicroservice       = config.buildServiceUrl("accounts-microservice")
  val deversityMicroservice     = config.buildServiceUrl("deversity")
  
  val USER_LOGIN                = s"$authService/login?redirect=deversity"
  val USER_REGISTER             = s"$authService/create-an-account"
  val ORG_REGISTER              = s"$authService/create-an-organisation-account"
  val DASHBOARD                 = s"$authService/dashboard"
  val SIGN_OUT                  = s"$authService/goodbye"

  val USER_LOGIN_CALL           = Call("GET", USER_LOGIN)

  implicit def serviceLinks(implicit requestHeader: RequestHeader): Seq[NavBarLinkBuilder] = Seq(
    NavBarLinkBuilder("/", "glyphicon-home", "Home"),
    NavBarLinkBuilder("/", "glyphicon-wrench", "Diagnostics"),
    NavBarLinkBuilder("/deversity", "glyphicon-education", "Deversity"),
    NavBarLinkBuilder("/", "glyphicon-asterisk", "Hub")
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
