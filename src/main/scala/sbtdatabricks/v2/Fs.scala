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

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer
import java.nio.file.Files

class Fs(client: ShardClient) extends Dbfs(client) {

  val MAX_BLOCK_SIZE = 1048576
  val BLOCK_SIZE = MAX_BLOCK_SIZE

  def upload(src: String, dst: String, overwrite: Boolean = true): Unit = {
    val localFile = new File(src)
    val fileSize = localFile.length()
    if (fileSize <= MAX_BLOCK_SIZE) {
      val data = Files.readAllBytes(localFile.toPath)
      val block = WriteBlock(ByteBuffer.wrap(data))

      put(dst, block, overwrite)
    } else {
      val bb = new Array[Byte](BLOCK_SIZE)
      val is = new FileInputStream(localFile)
      val bis = new java.io.BufferedInputStream(is)
      try {
        var bytesRead = bis.read(bb, 0, BLOCK_SIZE)

        val streamId = create(dst, overwrite)
        try {
          while (bytesRead > 0) {
            val block = WriteBlock(ByteBuffer.wrap(bb, 0, bytesRead))
            addBlock(streamId, block)
            bytesRead = bis.read(bb, 0, BLOCK_SIZE)
          }
        } finally {
          close(streamId)
        }
      } finally {
        bis.close()
      }
    }
  }
}
