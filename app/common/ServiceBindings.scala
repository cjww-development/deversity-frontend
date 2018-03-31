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

import com.cjwwdev.config.{ConfigurationLoader, ConfigurationLoaderImpl}
import com.google.inject.AbstractModule
import connectors._
import controllers._
import controllers.internal._
import services._

class ServiceBindings extends AbstractModule {
  override def configure(): Unit = {
    bindOther()
    bindConnectors()
    bindServices()
    bindControllers()
  }

  private def bindConnectors(): Unit = {
    bind(classOf[AccountsMicroserviceConnector]).to(classOf[AccountsMicroserviceConnectorImpl]).asEagerSingleton()
    bind(classOf[DeversityMicroserviceConnector]).to(classOf[DeversityMicroserviceConnectorImpl]).asEagerSingleton()
    bind(classOf[SessionStoreConnector]).to(classOf[SessionStoreConnectorImpl]).asEagerSingleton()
  }

  private def bindServices(): Unit = {
    bind(classOf[EnrolmentService]).to(classOf[EnrolmentServiceImpl]).asEagerSingleton()
    bind(classOf[SchoolDetailsService]).to(classOf[SchoolDetailsServiceImpl]).asEagerSingleton()
    bind(classOf[SessionService]).to(classOf[SessionServiceImpl]).asEagerSingleton()
  }

  private def bindControllers(): Unit = {
    bind(classOf[EnrolmentController]).to(classOf[EnrolmentControllerImpl]).asEagerSingleton()
    bind(classOf[HomeController]).to(classOf[HomeControllerImpl]).asEagerSingleton()
    bind(classOf[RedirectController]).to(classOf[RedirectControllerImpl]).asEagerSingleton()
    bind(classOf[SessionController]).to(classOf[SessionControllerImpl]).asEagerSingleton()
  }

  private def bindOther(): Unit = {
    bind(classOf[ConfigurationLoader]).to(classOf[ConfigurationLoaderImpl]).asEagerSingleton()
  }
}
