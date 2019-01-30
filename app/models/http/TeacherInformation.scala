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
package models.http

import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TeacherInformation(title: String, lastName: String, room: String)

object TeacherInformation {
  implicit val standardFormat: OFormat[TeacherInformation] = (
    (__ \ "title").format[String] and
    (__ \ "lastName").format[String] and
    (__ \ "room").format[String]
  )(TeacherInformation.apply, unlift(TeacherInformation.unapply))

  implicit val deObfuscator: DeObfuscator[TeacherInformation] = new DeObfuscator[TeacherInformation] {
    override def decrypt(value: String): Either[TeacherInformation, DecryptionError] = {
      DeObfuscation.deObfuscate[TeacherInformation](value)
    }
  }
}
