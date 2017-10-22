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

  implicit val serviceLinks: Seq[NavBarLinkBuilder] = Seq(
    NavBarLinkBuilder("/", "glyphicon-home", "Home"),
    NavBarLinkBuilder("/", "glyphicon-wrench", "Diagnostics"),
    NavBarLinkBuilder("/", "glyphicon-education", "Deversity"),
    NavBarLinkBuilder("/", "glyphicon-asterisk", "Hub")
  )

  implicit val standardNavBarRoutes: Map[String, Call] = Map(
    "navBarLogo"    -> Call("GET", "/deversity/assets/images/logo.png"),
    "globalAssets"  -> Call("GET", "/deversity/assets/stylesheets/global-assets.css"),
    "favicon"       -> Call("GET", "/deversity/assets/images/favicon.ico"),
    "userRegister"  -> Call("GET", USER_REGISTER),
    "orgRegister"   -> Call("GET", ORG_REGISTER),
    "login"         -> Call("GET", USER_LOGIN),
    "dashboard"     -> Call("GET", DASHBOARD),
    "signOut"       -> Call("GET", SIGN_OUT)
  )
}
