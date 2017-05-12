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
package connectors

import javax.inject.{Inject, Singleton}

import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import config.ApplicationConfiguration
import models.SchoolDetails
import play.api.mvc.Request
import play.api.http.Status.{NOT_FOUND, OK}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait ValidOrg
case object Valid extends ValidOrg
case object Invalid extends ValidOrg

@Singleton
class SchoolDetailsConnector @Inject()(http: Http) extends ApplicationConfiguration {

  def validateSchoolName(orgName: String)(implicit request: Request[_]): Future[ValidOrg] = {
    val orgUserName = DataSecurity.encryptData[String](orgName).get
    http.GET(s"$account_service_route/validate/school/$orgUserName") map {
      _.status match {
        case OK => Valid
        case NOT_FOUND => Invalid
      }
    }
  }

  def getSchoolDetails(orgName: String)(implicit request: Request[_]): Future[Option[SchoolDetails]] = {
    val orgUserName = DataSecurity.encryptData[String](orgName).get
    http.GET(s"$account_service_route/school/$orgUserName/details") map { resp =>
      resp.status match {
        case OK => DataSecurity.decryptInto[SchoolDetails](resp.body)
        case NOT_FOUND => None
      }
    }
  }
}
