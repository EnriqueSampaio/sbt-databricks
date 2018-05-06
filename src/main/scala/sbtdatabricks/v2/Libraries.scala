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

case class Jar(jar: String)

case class LibraryFullStatus(
  library: Jar,
  status: String,
  messages: Array[String],
  is_library_for_all_clusters: Boolean
)

case class ClusterLibraryStatuses(
  cluster_id: String,
  library_statuses: List[LibraryFullStatus]
)
case class ClusterLibraryStatusesList(
  statuses: List[ClusterLibraryStatuses]
)

class Libraries(client: ShardClient) extends Endpoint {
  /** Common suffix of paths to libraries endpoints */
  override def url: String = client.url + "/2.0/libraries"

  def install(clusterId: String, libraries: List[Jar]): Unit = {
    libraries foreach {lib =>
      val resp = client.req(s"$url/install", "post",
        s"""|{
            |  "cluster_id":"$clusterId",
            |  "libraries": [{
            |    "jar": "${lib.jar}"
            |  }]
            |}""".stripMargin
      )
      client.extract[Response](resp)
    }
  }

  def uninstall(clusterId: String, libraries: List[Jar]): Unit = {
    libraries foreach {lib =>
      val resp = client.req(s"$url/uninstall", "post",
        s"""|{
            |  "cluster_id":"$clusterId",
            |  "libraries": [{
            |    "jar": "${lib.jar}"
            |  }]
            |}""".stripMargin
      )
      client.extract[Response](resp)
    }
  }

  def clusterStatus(clusterId: String): List[LibraryFullStatus] = {
    val resp = client.req(s"$url/cluster-status", "get",
      s"""{"cluster_id": "$clusterId"}"""
    )

    client.extract[ClusterLibraryStatuses](resp).library_statuses
  }

  def allClusterStatuses: List[ClusterLibraryStatuses] = {
    val resp = client.req(s"$url/all-cluster-statuses", "get")

    client.extract[ClusterLibraryStatusesList](resp).statuses
  }
}
