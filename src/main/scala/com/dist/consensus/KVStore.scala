package com.dist.consensus

import java.io.{ByteArrayInputStream, File}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class KVStore(walDir:File) {
  val kv = new mutable.HashMap[String, String]()
  val wal = WriteAheadLog.create(walDir)
  applyLog()

  def put(key:String, value:String): Unit = {
    wal.writeEntry(SetValueCommand(key, value).serialize())

    kv.put(key, value)
  }

  def get(key: String): Option[String] = kv.get(key)

  def close = {
    kv.clear()
  }


  def applyLog() = {
    val entries: ListBuffer[WalEntry] = wal.readAll()
    entries.foreach(entry ⇒ {
      val command = SetValueCommand.deserialize(new ByteArrayInputStream(entry.data))
      kv.put(command.key, command.value)
    })
  }
}
