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
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import javax.inject.Inject
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future

class HeadersFilter @Inject()(implicit val mat: Materializer, configLoader: ConfigurationLoader) extends Filter {
  def initialiseHeaderPackage(rh: RequestHeader): (String, String) = {
    "cjww-headers" -> HeaderPackage(
      configLoader.getApplicationId("auth-service"),
      rh.session.data.getOrElse("cookieId", "")).encryptType
  }

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    val appliedHeaders = rh.copy(headers = rh.headers.add(initialiseHeaderPackage(rh)))
    f(appliedHeaders)
  }
}
