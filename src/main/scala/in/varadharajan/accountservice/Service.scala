package in.varadharajan.accountservice

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import AccountServiceRuntime._

trait AccountService {
  val accountRepository: AccountRepository
  def createAccount(account: APIRequest.Account): Future[APIResponse.Account.Account] = Future {
    val newAccount = Account(java.util.UUID.randomUUID(), account.name)
    accountRepository.persist(newAccount)
    APIResponse.Account(newAccount)
  }
  def findAccount(id: UUID): Future[Option[APIResponse.Account.Account]] = accountRepository.find(id).map(_.map(APIResponse.Account(_)))
  def allAccounts(): Future[Iterable[APIResponse.Account.Account]]       = accountRepository.all().map(_.map(APIResponse.Account(_)))
  def transfer(transfer: APIRequest.TransferReq): Future[APIResponse.Account.Transaction] = {
    accountRepository
      .transfer(UUID.fromString(transfer.from), UUID.fromString(transfer.to), transfer.amount)
      .map(x => APIResponse.Account.renderTransaction(x))
  }
  def deposit(id: UUID, amount: BigDecimal): Future[Option[APIResponse.Account.Account]] =
    accountRepository.deposit(id, amount).map(_.map(APIResponse.Account(_)))
  def withdraw(id: UUID, amount: BigDecimal): Future[Option[APIResponse.Account.Account]] =
    accountRepository.withdraw(id, amount).map(_.map(APIResponse.Account(_)))
}

case class AccountServiceImpl(accountRepository: AccountRepository) extends AccountService

trait AccountServiceJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val transactionFormat     = jsonFormat5(APIResponse.Account.Transaction)
  implicit val responseAccountFormat = jsonFormat4(APIResponse.Account.Account)
  implicit val requestAccountFormat  = jsonFormat1(APIRequest.Account)
  implicit val requestTransferFormat = jsonFormat3(APIRequest.TransferReq)
}

case class AccountServiceRouter(accountService: AccountService) extends AccountServiceJsonSupport {

  implicit def exceptionHandler = ExceptionHandler {
    case ex: Throwable => complete(500 -> Map("error" -> ex.getMessage))
  }

  private val topLevelRoute = "v1" / "accounts"
  val routes = Route.seal(path(topLevelRoute) {
    get {
      complete(accountService.allAccounts())
    }
  } ~ path(topLevelRoute) {
    post {
      entity(as[APIRequest.Account]) { account =>
        complete(accountService.createAccount(account))
      }
    }
  } ~ path(topLevelRoute / JavaUUID) { accountId =>
    get {
      complete(accountService.findAccount(accountId))
    }
  } ~ path(topLevelRoute / JavaUUID / "deposit") { accountId =>
    put {
      parameters('amount) { (amount) =>
        complete(accountService.deposit(accountId, BigDecimal(amount)))
      }
    }
  } ~ path(topLevelRoute / JavaUUID / "withdraw") { accountId =>
    put {
      parameters('amount) { (amount) =>
        complete(accountService.withdraw(accountId, BigDecimal(amount)))
      }
    }
  } ~ path(topLevelRoute / "transfer") {
    post {
      entity(as[APIRequest.TransferReq]) { transferReq =>
        complete(accountService.transfer(transferReq))
      }
    }
  })
}
