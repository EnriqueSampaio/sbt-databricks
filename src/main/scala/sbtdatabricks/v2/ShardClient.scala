/*
 * Copyright 2018 Databricks
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
package sbtdatabricks.v2

import java.net.URI

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.http.client.HttpClient
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils

/**
  * Databricks REST API client - entry point of all REST API calls
  * @param client - http request executor
  * @param shard - url of the shard like https://my-shard.cloud.databricks.com:443
  */
case class ShardClient(client: HttpClient, shard: String) extends Endpoint {
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  /** Common suffix of all endpoints of Databricks public API */
  override lazy val url: String = {
    val https = "https://"
    val withProtocol = if (shard.startsWith(https)) shard else https + shard
    withProtocol + "/api"
  }

  /**
   * Entry point for Databricks Dbfs API:
   * https://docs.databricks.com/api/latest/dbfs.html
   * */
  lazy val dbfs = new Dbfs(this)
  /** An extension of DBFS API */
  lazy val fs = new Fs(this)
  /**
   * Entry point of Library API:
   * https://docs.databricks.com/api/latest/libraries.html#library-api
   * */
  lazy val lib = new Libraries(this)

  /**
    * Makes a REST request to specific endpoint
    *
    * @param endpoint - url like https://my-shard.cloud.databricks.com:443/api/2.0/token/list
    * @param httpMethod - "get" or "post"
    * @param data - entity of the https request. For example, in json format: {"token_id": 42}
    * @return a string with json if http status is 200 otherwise throws an exception
    */
  def req(
    endpoint: String,
    httpMethod: String,
    data: String = "",
    expect100Continue: Boolean = false
  ): String = {
    val request = httpMethod.toUpperCase match {
      case "POST" => new org.apache.http.client.methods.HttpPost(endpoint)
      case _ =>
        new org.apache.http.client.methods.HttpEntityEnclosingRequestBase() {
          setURI(URI.create(endpoint))
          override def getMethod(): String = httpMethod.toUpperCase
        }
    }
    if (expect100Continue) {
      request.addHeader("Expect", "100-continue")
    }
    request.setEntity(new StringEntity(data))

    val response = client.execute(request)
    val statusCode = response.getStatusLine.getStatusCode

    statusCode match {
      case 200 => EntityUtils.toString(response.getEntity)
      case 400 =>
        val body = EntityUtils.toString(response.getEntity)
        mapper.readValue[BricksException](body).throwException
      case _ => throw new HttpException(statusCode)
    }
  }

  def extract[A](json: String)(implicit mf: scala.reflect.Manifest[A]): A = {
    mapper.readValue[A](json)
  }
}
