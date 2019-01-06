package com.yumtao.rpc.helloworld

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by yumtao on 2019/1/6.
  */
class Worker(val masterHost: String, val masterPort: Int) extends Actor {

  var masterActor: ActorSelection = _

  // 初始化方法：与master建立连接
  override def preStart(): Unit = {
    //通过context与master的Actor建立连接
    // TODO (url:akka.tcp://systemName@systemHost:systemPort/user/ActorName)
    masterActor = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")

    // 发送消息给master
    masterActor ! "connect"
  }

  /**
    * 接收消息
    */
  override def receive: Receive = {
    case "reply" => {
      println("worker: connected master")
    }

    // 发送消息至master，消息链路：main -> worker -> master -> worker
    case "communicate" => {
      println("worker: start to send message to master")
      masterActor ! "communicate"
    }

    case "communicated" => {
      println("worker: receive from master communicate msg")
    }

    case s: String => {
      println(s)
      masterActor ! s
    }
  }
}


object Worker {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1)
    val masterHost = args(2)
    val masterPort = args(3) toInt

    // akka配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr)

    // 创建ActorSystem，ActorSystem：单例对象，职责是创建Actor
    val actorSystem = ActorSystem("WorkerSystem", config)
    // 创建Actor
    val workerActor = actorSystem.actorOf(Props(new Worker(masterHost, masterPort)), "Worker")

    // 向masterActor发送消息
    workerActor ! "communicate"
    workerActor ! "hello master this is random string"

    // 优雅停止
    actorSystem.awaitTermination()
  }
}