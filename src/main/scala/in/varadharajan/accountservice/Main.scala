package in.varadharajan.accountservice

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

object AccountServiceRuntime {
  implicit val system           = ActorSystem("account-service")
  implicit val materializer     = ActorMaterializer()
  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(AccountServiceConfig.threadPoolSize))
}

object AccountServiceConfig {
  val config: Config = ConfigFactory.load()
  val socket         = (config.getString("account_service.server.host"), config.getInt("account_service.server.port"))
  val threadPoolSize = config.getInt("account_service.server.thread_pool_size")
}

object Main {
  import AccountServiceRuntime._

  def main(args: Array[String]): Unit = {
    val accountRepository = STMAccountRepository()
    val accountService    = AccountServiceImpl(accountRepository)
    val router            = DebuggingDirectives.logRequest("APIRequest", Logging.InfoLevel)(AccountServiceRouter(accountService).routes)

    val bindingFuture = Http().bindAndHandle(router, interface = AccountServiceConfig.socket._1, port = AccountServiceConfig.socket._2)

    println(s"Server started at http://${AccountServiceConfig.socket._1}:${AccountServiceConfig.socket._2}")

    sys.ShutdownHookThread { system.terminate() }
  }
}
