package com.dist.consensus

import java.time.Duration

import com.dist.consensus.network.{Config, InetAddressAndPort, Peer}
import org.scalatest.FunSuite
import scala.concurrent.duration._
import scala.concurrent.Await

class ConsensusTest extends FunSuite {

    test("should update commitIndex after quorum writes") {
      val address = new Networks().ipv4Address
      val ports = TestUtils.choosePorts(3)
      val peerAddr1 = InetAddressAndPort(address, ports(0))
      val peerAddr2 = InetAddressAndPort(address, ports(1))
      val peerAddr3 = InetAddressAndPort(address, ports(2))


      val serverList = List(Peer(1, peerAddr1), Peer(2, peerAddr2), Peer(3, peerAddr3))

      val config1 = Config(1, peerAddr1, serverList, TestUtils.tempDir())
      val peer1 = new Server(config1)

      val config2 = Config(2, peerAddr2, serverList, TestUtils.tempDir())
      val peer2 = new Server(config2)

      val config3 = Config(3, peerAddr3, serverList, TestUtils.tempDir())
      val peer3 = new Server(config3)

      peer1.startListening()
      peer2.startListening()
      peer3.startListening()

      peer1.start()
      peer2.start()
      peer3.start()

      TestUtils.waitUntilTrue(()⇒ {
        peer3.state == ServerState.LEADING && peer1.state == ServerState.FOLLOWING && peer2.state == ServerState.FOLLOWING
      }, "Waiting for leader to be selected")

      val future = peer3.put("k1", "v1")
      Await.ready(future, 5.second)
      val value = peer3.get("k1")
      assert(value == Some("v1"))
    }

}
