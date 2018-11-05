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
package models

import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SchoolDetails(orgName: String,
                         initials: String,
                         location: String)

object SchoolDetails {
  implicit val standardFormat: OFormat[SchoolDetails] = (
    (__ \ "orgName").format[String] and
    (__ \ "initials").format[String] and
    (__ \ "location").format[String]
  )(SchoolDetails.apply, unlift(SchoolDetails.unapply))

  implicit val deObfuscator: DeObfuscator[SchoolDetails] = new DeObfuscator[SchoolDetails] {
    override def decrypt(value: String): Either[SchoolDetails, DecryptionError] = {
      DeObfuscation.deObfuscate(value)
    }
  }
}
