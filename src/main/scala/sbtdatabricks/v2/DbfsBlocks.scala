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

import java.nio.ByteBuffer
import java.util.Base64
import java.nio.charset.StandardCharsets

sealed trait Block {
  def raw: Array[Byte]
  def str: String = new String(raw)
  def base64: String
}

/**
  * Content of a file read from DBFS
  * @param bytes_read - length of un-encoded data
  * @param data - base64 encoded content
  */
case class ReadBlock(bytes_read: Long, data: String) extends Block {
  override def raw: Array[Byte] = {
    Base64.getDecoder.decode(data.getBytes(StandardCharsets.UTF_8))
  }
  override def base64 = data
}

/**
 * A binary block to upload
 * @param buffer - the buffer should be encoded to base64 and uploaded
 */
case class WriteBlock(buffer: ByteBuffer) extends Block {
  override def raw: Array[Byte] = buffer.array()
  override def base64: String = {
    new String(Base64.getEncoder.encode(buffer).array())
  }
}

case class StrBlock(override val str: String) extends Block {
  override def raw: Array[Byte] = str.map(_.toByte).toArray
  override def base64: String = {
    Base64.getEncoder.encodeToString(str.getBytes(StandardCharsets.UTF_8))
  }
}

