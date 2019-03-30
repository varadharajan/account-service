package in.varadharajan.accountservice

import java.util.UUID

import com.github.nscala_time.time.Imports._

import scala.concurrent.Future
import scala.concurrent.stm._
import AccountServiceRuntime._

trait AccountRepository {
  def persist(account: Account): Future[Unit]
  def find(id: UUID): Future[Option[Account]]
  def transfer(from: UUID, to: UUID, amount: BigDecimal): Future[Transaction]
  def all(): Future[Iterable[Account]]
  def deposit(id: UUID, amount: BigDecimal): Future[Option[Account]]
  def withdraw(id: UUID, amount: BigDecimal): Future[Option[Account]]
}

case class STMAccountRepository() extends AccountRepository {
  private val accountStore = TMap[UUID, Account]()

  override def persist(account: Account): Future[Unit] = Future {
    atomic { implicit txn =>
      accountStore += (account.id -> account)
    }
  }
  override def find(id: UUID): Future[Option[Account]] = Future { accountStore.single.get(id) }
  override def transfer(from: UUID, to: UUID, amount: BigDecimal): Future[Transaction] = Future {
    atomic { implicit txn =>
      amount match {
        case _ if !accountStore.contains(from)                => throw new IllegalArgumentException(s"UUID: $from not found")
        case _ if !accountStore.contains(to)                  => throw new IllegalArgumentException(s"UUID: $to not found")
        case _ if from == to                                  => throw new IllegalArgumentException(s"Cannot transfer between same accounts")
        case _ if amount <= 0                                 => throw new IllegalArgumentException("Transaction amount cannot be zero or negative")
        case _ if accountStore.get(from).get.balance < amount => throw new IllegalArgumentException(s"Not enough money with UUID: ${from}")
        case _ => {
          val fromAccount = accountStore.get(from).get
          val toAccount   = accountStore.get(to).get
          val transaction = Transaction(fromAccount, toAccount, DateTime.now, amount, TransactionSuccess)
          accountStore.put(
            fromAccount.id,
            fromAccount.copy(balance = fromAccount.balance - amount, transactions = transaction :: fromAccount.transactions.toList)
          )
          accountStore.put(
            toAccount.id,
            toAccount.copy(balance = toAccount.balance + amount, transactions = transaction :: toAccount.transactions.toList)
          )
          transaction
        }
      }
    }
  }
  override def all(): Future[Iterable[Account]] = Future { accountStore.single.values }
  override def deposit(id: UUID, amount: BigDecimal): Future[Option[Account]] = Future {
    atomic { implicit txn =>
      amount match {
        case _ if !accountStore.contains(id) => throw new IllegalArgumentException(s"UUID: $id not found")
        case _ => {
          val account    = accountStore.get(id).get
          val newAccount = account.copy(balance = account.balance + amount)
          accountStore.put(id, newAccount)
          Some(newAccount)
        }
      }
    }
  }
  override def withdraw(id: UUID, amount: BigDecimal): Future[Option[Account]] = Future {
    atomic { implicit txn =>
      amount match {
        case _ if !accountStore.contains(id)                => throw new IllegalArgumentException(s"UUID: $id not found")
        case _ if accountStore.get(id).get.balance < amount => throw new IllegalArgumentException(s"Not enough money with UUID: $id")
        case _ => {
          val account    = accountStore.get(id).get
          val newAccount = account.copy(balance = account.balance - amount)
          accountStore.put(id, newAccount)
          Some(newAccount)
        }
      }
    }
  }
}
