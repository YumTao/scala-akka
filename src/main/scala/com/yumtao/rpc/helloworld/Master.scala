package com.yumtao.rpc.helloworld

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by yumtao on 2019/1/6.
  */
class Master extends Actor {

  /**
    * 初始化方法(客户端处可用于建立连接)
    */
  override def preStart(): Unit = {
    println("preStart() start")
  }

  /**
    * 接收消息
    */
  override def receive: Receive = {
    case "connect" => {
      println("connected successful")
      sender ! "reply"
    }
    case "hello" => {
      println("I am starting")
    }
    case "communicate" => {
      println("master: i am get message from worker")
      sender ! "communicated"
    }
    case s: String => {
      println(s)
    }
  }
}

object Master {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1)

    // akka配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    // 创建ActorSystem，ActorSystem：单例对象，职责是创建Actor，
    val actorSystem = ActorSystem("MasterSystem", config)
    // 创建Actor
    val masterActor = actorSystem.actorOf(Props[Master], "Master")
    // 向masterActor发送消息
    masterActor ! "hello"

    // 等待，不结束进程
    actorSystem.awaitTermination()
  }
}