/*
 * Copyright 2019 HM Revenue & Customs
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

package support

import java.net.URLEncoder

import play.api.mvc.Call

object CallOps {

  def addParamsToUrl(url: String, params: (String, Option[String])*): String = {
    val query = params collect { case (k, Some(v)) => s"$k=${URLEncoder.encode(v, "UTF-8")}" } mkString "&"
    if (query.isEmpty) {
      url
    } else if (url.endsWith("?") || url.endsWith("&")) {
      url + query
    } else {
      val join = if (url.contains("?")) "&" else "?"
      url + join + query
    }
  }
}
