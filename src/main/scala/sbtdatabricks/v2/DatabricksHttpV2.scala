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

import java.io.{File, PrintStream}

import org.apache.http.client.HttpClient
import sbtdatabricks._

class DatabricksHttpV2(
    shardClient: ShardClient,
    _endpoint: String,
    httpClientV1: HttpClient,
    outputStream: PrintStream = System.out
  ) extends DatabricksHttp(_endpoint, httpClientV1, outputStream) {

  override private[sbtdatabricks] def uploadJar(
    name: String,
    file: File,
    folder: String
  ): UploadedLibraryId = {
    val fileName = file.getName
    val dstPath = folder + "/" + fileName
    outputStream.println(s"Upload jar: ${file.getCanonicalPath} to ${dstPath}")
    shardClient.fs.upload(file.getCanonicalPath, dstPath, overwrite = true)

    UploadedLibraryId(dstPath)
  }

  override private[sbtdatabricks] def attachToCluster(
    library: UploadedLibrary,
    cluster: Cluster): ClusterId = {
    outputStream.println(s"Install ${library.remotePath} to ${cluster.id}")
    shardClient.lib.install(cluster.id, List(Jar(library.remotePath)))

    ClusterId(cluster.id)
  }
}
