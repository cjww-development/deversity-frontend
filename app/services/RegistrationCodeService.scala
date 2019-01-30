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

package services

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.logging.Logging
import common.responses.{CurrentEnrolmentResponse, InvalidEnrolments}
import connectors.DeversityConnector
import enums.{AccountTypes, HttpResponse, UserRoles}
import javax.inject.Inject
import models.{DeversityEnrolment, RegistrationCode}
import play.api.mvc.Request

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultRegistrationCodeService @Inject()(val devConnector: DeversityConnector) extends RegistrationCodeService

trait RegistrationCodeService extends Logging {

  val devConnector: DeversityConnector

  private val isTeacher: DeversityEnrolment => Boolean = _.role == UserRoles.TEACHER

  def getRegistrationCode(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[Either[CurrentEnrolmentResponse, RegistrationCode]] = {
    user.credentialType match {
      case AccountTypes.INDIVIDUAL   => devConnector.getDeversityUserInfo flatMap {
        case Some(enr) => if(isTeacher(enr)) {
          devConnector.getRegistrationCode.map { regCode =>
            logger.info(s"[getRegistrationCode] - Fetching registration code for user ${user.id}")
            Right(regCode)
          }
        } else {
          logger.warn(s"[getRegistrationCode] - User ${user.id} is not a teacher (${enr.role})")
          Future.successful(Left(InvalidEnrolments))
        }
        case None =>
          logger.warn(s"[getRegistrationCode] - User ${user.id} is not enrolled for deversity")
          Future.successful(Left(InvalidEnrolments))
      }
      case AccountTypes.ORGANISATION => devConnector.getRegistrationCode.map { regCode =>
        logger.info(s"[getRegistrationCode] - Fetching registration code for orgId ${user.id}")
        Right(regCode)
      }
    }
  }

  def generateRegistrationCode(implicit user: CurrentUser, req: Request[_], ec: ExC): Future[Either[CurrentEnrolmentResponse, HttpResponse.Value]] = {
    user.credentialType match {
      case AccountTypes.INDIVIDUAL   => devConnector.getDeversityUserInfo flatMap {
        case Some(enr) => if(isTeacher(enr)) {
          devConnector.generateRegistrationCode.map { resp =>
            logger.info(s"[generateRegistrationCode] - Generating registration code for userId ${user.id}")
            Right(resp)
          }
        } else {
          logger.warn(s"[generateRegistrationCode] - User ${user.id} is not a teacher (${enr.role})")
          Future.successful(Left(InvalidEnrolments))
        }
        case None =>
          logger.warn(s"[generateRegistrationCode] - User ${user.id} is not enrolled for deversity")
          Future.successful(Left(InvalidEnrolments))
      }
      case AccountTypes.ORGANISATION => devConnector.generateRegistrationCode map { resp =>
        logger.info(s"[generateRegistrationCode] - Generating registration code for orgId ${user.id}")
        Right(resp)
      }
    }
  }
}
