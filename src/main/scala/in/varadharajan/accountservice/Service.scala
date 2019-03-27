package in.varadharajan.accountservice

import akka.http.scaladsl.server.Directives._

case class AccountServiceRouter() {
  private val topLevelRoute = "v1" / "accounts"
  val routes = path(topLevelRoute) {
    get { complete("Hello World!") }
  }
}
