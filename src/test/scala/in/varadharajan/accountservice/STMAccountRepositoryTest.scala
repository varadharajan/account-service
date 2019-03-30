package in.varadharajan.accountservice
import java.util.UUID

import org.scalatest.{FlatSpec, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class STMAccountRepositoryTest extends FlatSpec with Matchers {
  "persist" should "store account in STM Map" in {
    val account    = Account(UUID.randomUUID(), "varadha")
    val repository = STMAccountRepository()
    Await.result(repository.all(), 10 seconds).toList.length shouldBe 0

    Await.result(repository.persist(account), 10 seconds)
    Await.result(repository.all(), 10 seconds).toList.length shouldBe 1
    Await.result(repository.find(account.id), 10 seconds) shouldBe Some(account)
  }

  "find" should "retrieve valid accounts" in {
    val account    = Account(UUID.randomUUID(), "varadha")
    val repository = STMAccountRepository()
    Await.result(repository.persist(account), 10 seconds)

    Await.result(repository.find(account.id), 10 seconds) shouldBe Some(account)
  }

  "find" should "handle invalid accounts gracefully" in {
    val repository = STMAccountRepository()
    Await.result(repository.find(UUID.randomUUID()), 10 seconds) shouldBe None
  }

  "all" should "return all available accounts in repository" in {
    val account1   = Account(UUID.randomUUID(), "varadha")
    val account2   = Account(UUID.randomUUID(), "srinath")
    val repository = STMAccountRepository()
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.persist(account2), 10 seconds)
    Await.result(repository.all(), 10 seconds).toSet shouldBe Set(account1, account2)
  }

  "deposit" should "deposit money on valid accounts" in {
    val account    = Account(UUID.randomUUID(), "varadha")
    val repository = STMAccountRepository()
    Await.result(repository.persist(account), 10 seconds)

    Await.result(repository.deposit(account.id, 10), 10 seconds).get.balance shouldBe 10
  }

  "deposit" should "handle gracefully for invalid accounts" in {
    val repository = STMAccountRepository()
    val uuid       = UUID.randomUUID()
    Await.ready(repository.deposit(uuid, 10), 10 seconds).value.get match {
      case Success(_)  => fail("Not supposed to succeed")
      case Failure(ex) => ex.getMessage shouldBe s"UUID: $uuid not found"
    }
  }

  "withdraw" should "withdraw money on valid accounts" in {
    val account    = Account(UUID.randomUUID(), "varadha")
    val repository = STMAccountRepository()
    Await.result(repository.persist(account), 10 seconds)
    Await.result(repository.deposit(account.id, 10), 10 seconds)
    Await.result(repository.withdraw(account.id, 10), 10 seconds).get.balance shouldBe 0
  }

  "withdraw" should "handle gracefully for invalid accounts" in {
    val repository = STMAccountRepository()
    val uuid       = UUID.randomUUID()
    Await.ready(repository.withdraw(uuid, 10), 10 seconds).value.get match {
      case Success(_)  => fail("Not supposed to succeed")
      case Failure(ex) => ex.getMessage shouldBe s"UUID: $uuid not found"
    }
  }

  "withdraw" should "handle gracefully for valid accounts with less balance" in {
    val account    = Account(UUID.randomUUID(), "varadha")
    val repository = STMAccountRepository()
    Await.result(repository.persist(account), 10 seconds)
    Await.ready(repository.withdraw(account.id, 10), 10 seconds).value.get match {
      case Success(_)  => fail("Not supposed to succeed")
      case Failure(ex) => ex.getMessage shouldBe s"Not enough money with UUID: ${account.id}"
    }
  }

  "transfer" should "transfer money from one account to another" in {
    val account1   = Account(UUID.randomUUID(), "varadha")
    val account2   = Account(UUID.randomUUID(), "varadha")
    val repository = STMAccountRepository()
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.persist(account2), 10 seconds)
    Await.result(repository.deposit(account1.id, 10), 10 seconds)
    Await.result(repository.transfer(account1.id, account2.id, 10), 10 seconds).status shouldBe TransactionSuccess
    Await.result(repository.find(account2.id), 10 seconds).get.balance shouldBe 10
  }

  "transfer" should "handle gracefully if for invalid to account" in {
    val repository = STMAccountRepository()
    val fromUUID   = UUID.randomUUID()
    val account1   = Account(fromUUID, "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    val toUUID = UUID.randomUUID()
    Await.ready(repository.transfer(fromUUID, toUUID, 10), 10 seconds).value.get match {
      case Success(_)  => fail("should not succeed")
      case Failure(ex) => ex.getMessage shouldBe s"UUID: $toUUID not found"
    }
  }

  "transfer" should "handle gracefully if for invalid from account" in {
    val repository = STMAccountRepository()
    val fromUUID   = UUID.randomUUID()
    val toUUID     = UUID.randomUUID()
    Await.ready(repository.transfer(fromUUID, toUUID, 10), 10 seconds).value.get match {
      case Success(_)  => fail("should not succeed")
      case Failure(ex) => ex.getMessage shouldBe s"UUID: $fromUUID not found"
    }
  }

  "transfer" should "handle gracefully if from and to accounts are same" in {
    val repository = STMAccountRepository()
    val fromUUID   = UUID.randomUUID()
    val account1   = Account(fromUUID, "varadha")
    Await.result(repository.persist(account1), 10 seconds)

    Await.ready(repository.transfer(fromUUID, fromUUID, 10), 10 seconds).value.get match {
      case Success(_)  => fail("should not succeed")
      case Failure(ex) => ex.getMessage shouldBe s"Cannot transfer between same accounts"
    }
  }

  "transfer" should "handle gracefully if amount is invalid" in {
    val repository = STMAccountRepository()
    val fromUUID   = UUID.randomUUID()
    val account1   = Account(fromUUID, "varadha")
    val account2   = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.persist(account2), 10 seconds)

    Await.ready(repository.transfer(fromUUID, account2.id, 0), 10 seconds).value.get match {
      case Success(_)  => fail("should not succeed")
      case Failure(ex) => ex.getMessage shouldBe s"Transaction amount cannot be zero or negative"
    }
  }

  "transfer" should "handle gracefully if enough amount is not available" in {
    val repository = STMAccountRepository()
    val fromUUID   = UUID.randomUUID()
    val account1   = Account(fromUUID, "varadha")
    val account2   = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.persist(account2), 10 seconds)

    Await.ready(repository.transfer(fromUUID, account2.id, 90), 10 seconds).value.get match {
      case Success(_)  => fail("should not succeed")
      case Failure(ex) => ex.getMessage shouldBe s"Not enough money with UUID: ${account1.id}"
    }
  }
}
