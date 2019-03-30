package in.varadharajan.accountservice

import java.util.UUID

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

sealed trait TransactionStatus
case object TransactionSuccess            extends TransactionStatus
case class TransactionFailed(msg: String) extends TransactionStatus

case class Transaction(from: Account, to: Account, date: DateTime, amount: BigDecimal, status: TransactionStatus)
case class Account(id: UUID, name: String, balance: BigDecimal = 0, transactions: Iterable[Transaction] = List.empty)

object APIRequest {
  case class Account(name: String)
  case class TransferReq(from: String, to: String, amount: BigDecimal)
}

object APIResponse {
  object Account {
    val dateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.getDefault())
    case class Transaction(from: String, to: String, dateTime: String, amount: BigDecimal, status: String)
    case class Account(id: String, name: String, balance: BigDecimal, transactions: Iterable[Transaction])
    def apply(account: in.varadharajan.accountservice.Account): Account =
      Account(
        account.id.toString,
        account.name,
        account.balance,
        account.transactions.map { txn =>
          renderTransaction(txn)
        }
      )
    def renderTransaction(txn: in.varadharajan.accountservice.Transaction): APIResponse.Account.Transaction = Transaction(
      txn.from.id.toString,
      txn.to.id.toString,
      txn.date.toString(dateTimeFormatter),
      txn.amount,
      txn.status match {
        case TransactionSuccess     => "SUCCESS"
        case TransactionFailed(msg) => s"Failed : $msg"
      }
    )
  }
}
