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

package common

import com.cjwwdev.frontendUI.builders.NavBarLinkBuilder
import controllers.routes
import play.api.mvc.{Call, RequestHeader}

trait ViewConfiguration {

  implicit def serviceLinks(implicit rh: RequestHeader): Seq[NavBarLinkBuilder] = Seq(
    NavBarLinkBuilder("/", "glyphicon-home", "Home", "home"),
    NavBarLinkBuilder(routes.RedirectController.redirectToDiagnostics().absoluteURL(), "glyphicon-wrench", "Diagnostics", "diagnostics"),
    NavBarLinkBuilder(routes.RedirectController.redirectToDeversity().absoluteURL(), "glyphicon-education", "Deversity", "deversity"),
    NavBarLinkBuilder("/", "glyphicon-asterisk", "Hub", "the-hub")
  )

  implicit def standardNavBarRoutes(implicit rh: RequestHeader): Map[String, Call] = Map(
    "navBarLogo"   -> routes.Assets.versioned("images/logo.png"),
    "globalAssets" -> routes.Assets.versioned("stylesheets/global-assets.css"),
    "favicon"      -> routes.Assets.versioned("images/favicon.ico"),
    "userRegister" -> routes.RedirectController.redirectToUserRegister(),
    "orgRegister"  -> routes.RedirectController.redirectToOrgRegister(),
    "login"        -> routes.RedirectController.redirectToLogin(),
    "dashboard"    -> routes.RedirectController.redirectToUserDashboard(),
    "signOut"      -> routes.RedirectController.redirectToSignOut()
  )
}
