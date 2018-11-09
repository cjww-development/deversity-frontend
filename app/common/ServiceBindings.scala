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

import com.cjwwdev.config.{ConfigurationLoader, DefaultConfigurationLoader}
import com.cjwwdev.featuremanagement.models.Features
import com.cjwwdev.filters.IpWhitelistFilter
import com.cjwwdev.health.{DefaultHealthController, HealthController}
import com.cjwwdev.http.headers.filters.{DefaultHeadersFilter, HeadersFilter}
import com.cjwwdev.shuttering.filters.FrontendShutteringFilter
import connectors._
import controllers._
import controllers.internal._
import filters.{DefaultIpWhitelistFilter, DefaultShutteringFilter}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import services._

class ServiceBindings extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    bindOther() ++ bindConnectors() ++ bindServices() ++ bindControllers()

  private def bindConnectors(): Seq[Binding[_]] = Seq(
    bind(classOf[AccountsConnector]).to(classOf[DefaultAccountsConnector]).eagerly(),
    bind(classOf[DeversityConnector]).to(classOf[DefaultDeversityConnector]).eagerly(),
    bind(classOf[SessionStoreConnector]).to(classOf[DefaultSessionStoreConnector]).eagerly()
  )

  private def bindServices(): Seq[Binding[_]] = Seq(
    bind(classOf[EnrolmentService]).to(classOf[DefaultEnrolmentService]).eagerly(),
    bind(classOf[SchoolDetailsService]).to(classOf[DefaultSchoolDetailsService]).eagerly(),
    bind(classOf[SessionService]).to(classOf[DefaultSessionService]).eagerly()
  )

  private def bindControllers(): Seq[Binding[_]] = Seq(
    bind(classOf[EnrolmentController]).to(classOf[DefaultEnrolmentController]).eagerly(),
    bind(classOf[HomeController]).to(classOf[DefaultHomeController]).eagerly(),
    bind(classOf[RedirectController]).to(classOf[DefaultRedirectController]).eagerly(),
    bind(classOf[SessionController]).to(classOf[DefaultSessionController]).eagerly(),
    bind(classOf[HealthController]).to(classOf[DefaultHealthController]).eagerly()
  )

  private def bindOther(): Seq[Binding[_]] = Seq(
    bind(classOf[ConfigurationLoader]).to(classOf[DefaultConfigurationLoader]).eagerly(),
    bind(classOf[HeadersFilter]).to(classOf[DefaultHeadersFilter]).eagerly(),
    bind(classOf[IpWhitelistFilter]).to(classOf[DefaultIpWhitelistFilter]).eagerly(),
    bind(classOf[FrontendShutteringFilter]).to(classOf[DefaultShutteringFilter]).eagerly(),
    bind(classOf[Features]).to(classOf[FeatureDef]).eagerly()
  )
}
