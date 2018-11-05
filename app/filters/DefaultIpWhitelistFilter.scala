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
package filters

import akka.stream.Materializer
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.filters.IpWhitelistFilter
import controllers.routes
import javax.inject.Inject
import play.api.mvc.Call

class DefaultIpWhitelistFilter @Inject()(implicit val mat: Materializer,
                                         val config: ConfigurationLoader) extends IpWhitelistFilter {
  override val enabled: Boolean      = config.get[Boolean]("whitelist.enabled")
  override lazy val whitelistIps: String  = config.get[String]("whitelist.ip")
  override lazy val excludedPaths: String = config.get[String]("whitelist.excluded")
  override lazy val baseAppUri: String    = config.getServiceUrl(config.get[String]("appName"))
  override lazy val serviceOutage: Call   = routes.RedirectController.redirectToServiceOutage()
}
