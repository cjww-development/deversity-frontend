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
package models

import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import com.cjwwdev.implicits.ImplicitJsValues._
import play.api.libs.json._

case class ClassRoom(classId: String,
                     schoolDevId: String,
                     teacherDevId: String,
                     name: String)

object ClassRoom {
  implicit val writes: OWrites[ClassRoom] = Json.writes[ClassRoom]

  implicit val reads: Reads[ClassRoom] = new Reads[ClassRoom] {
    override def reads(json: JsValue): JsResult[ClassRoom] = JsSuccess(ClassRoom(
      classId      = json.get[String]("classId"),
      schoolDevId  = json.get[String]("schooldevId"),
      teacherDevId = json.get[String]("teacherDevId"),
      name         = json.get[String]("name")
    ))
  }

  implicit val deObfuscator: DeObfuscator[ClassRoom] = new DeObfuscator[ClassRoom] {
    override def decrypt(value: String): Either[ClassRoom, DecryptionError] = {
      DeObfuscation.deObfuscate[ClassRoom](value)
    }
  }

  implicit val deObfuscatorSeq: DeObfuscator[Seq[ClassRoom]] = new DeObfuscator[Seq[ClassRoom]] {
    override def decrypt(value: String): Either[Seq[ClassRoom], DecryptionError] = {
      DeObfuscation.deObfuscate[Seq[ClassRoom]](value)
    }
  }
}
