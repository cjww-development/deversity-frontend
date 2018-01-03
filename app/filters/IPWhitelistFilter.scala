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
package filters

import java.util.Base64
import javax.inject.Inject

import akka.stream.Materializer
import com.cjwwdev.config.ConfigurationLoader
import controllers.routes
import play.api.Logger
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Filter, RequestHeader, Result}

import scala.concurrent.Future

class IPWhitelistFilter @Inject()(implicit val mat: Materializer, configurationLoader: ConfigurationLoader) extends Filter {

  private val headerKey = "X-Forwarded-For"

  private def decodeIntoList(encodedString: String): Seq[String] = {
    Some(new String(Base64.getDecoder.decode(encodedString), "UTF-8")).map(_.split(",")).getOrElse(Array.empty).toSeq
  }

  private lazy val whitelistSeq = decodeIntoList(configurationLoader.loadedConfig.underlying.getString("whitelist.ip"))

  private lazy val excludedPathSeq: Seq[Call] = decodeIntoList(configurationLoader.loadedConfig.underlying.getString("whitelist.excluded")) map(Call("GET", _))

  private def uriIsWhitelisted(rh: RequestHeader): Boolean = excludedPathSeq contains Call(rh.method, rh.uri)
  private def isAssetRoute(rh: RequestHeader): Boolean     = rh.uri contains "/account-services/assets/"

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if(configurationLoader.loadedConfig.underlying.getBoolean("whitelist.enabled")) {
      if(uriIsWhitelisted(rh) | isAssetRoute(rh)) {
        f(rh)
      } else {
        rh.headers.get(headerKey) match {
          case Some(ip) => if(whitelistSeq contains ip) f(rh) else Future.successful(Redirect(routes.RedirectController.redirectToServiceOutage()))
          case None     =>
            Logger.warn(s"[Filters] - [IPWhitelistFilter] - No X-Forwarded-For header present blocking request")
            Future.successful(Redirect(routes.RedirectController.redirectToServiceOutage()))
        }
      }
    } else {
      f(rh)
    }
  }
}
