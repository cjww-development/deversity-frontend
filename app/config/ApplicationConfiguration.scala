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

import com.cjwwdev.bootstrap.config.BaseConfiguration
import play.api.mvc.Call

trait ApplicationConfiguration extends BaseConfiguration {

  val auth_service_route        = config.getString("routes.auth-service")
  val session_store_route       = config.getString("routes.session-store")

  val USER_LOGIN                = s"$auth_service_route/login?redirect=deversity"
  val USER_REGISTER             = s"$auth_service_route/create-an-acount"
  val DASHBOARD                 = s"$auth_service_route/dashboard"
  val SIGN_OUT                  = s"$auth_service_route/goodbye"

  val USER_LOGIN_CALL           = controllers.routes.RedirectController.redirectToUserLogin()

  val account_service_route     = config.getString("routes.accounts-microservice")
}
