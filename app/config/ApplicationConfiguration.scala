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
import controllers.routes

trait ApplicationConfiguration {

  val config: ConfigurationLoader

  val authService               = config.buildServiceUrl("auth-service")
  val sessionStore              = config.buildServiceUrl("session-store")
  val accountMicroservice       = config.buildServiceUrl("accounts-microservice")

  val USER_LOGIN                = s"$authService/login?redirect=deversity"
  val USER_REGISTER             = s"$authService/create-an-acount"
  val DASHBOARD                 = s"$authService/dashboard"
  val SIGN_OUT                  = s"$authService/goodbye"

  val USER_LOGIN_CALL           = routes.RedirectController.redirectToUserLogin()
}
