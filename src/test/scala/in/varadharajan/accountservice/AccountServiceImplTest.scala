package in.varadharajan.accountservice
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class AccountServiceImplTest extends FlatSpec with Matchers {
  "createAccount" should "orchestrate account creation" in {
    val repository = STMAccountRepository()
    val service    = AccountServiceImpl(repository)

    val account = Await.result(service.createAccount(APIRequest.Account("varadha")), 10 seconds)
    APIResponse.Account(Await.result(repository.find(UUID.fromString(account.id)), 10 seconds).get) shouldBe account
  }

  "findAccount" should "get valid accounts from repository" in {
    val repository = STMAccountRepository()
    val service    = AccountServiceImpl(repository)

    val account = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account), 10 seconds)
    Await.result(service.findAccount(account.id), 10 seconds).get shouldBe APIResponse.Account(account)
  }

  "allAccounts" should "return all account details" in {
    val repository = STMAccountRepository()
    val service    = AccountServiceImpl(repository)

    val account1 = Account(UUID.randomUUID(), "varadha")
    val account2 = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.persist(account2), 10 seconds)

    Await.result(service.allAccounts(), 10 seconds).toSet shouldBe Set(account1, account2).map(APIResponse.Account(_))
  }

  "transfer" should "orchestrate money transfer" in {
    val repository = STMAccountRepository()
    val service    = AccountServiceImpl(repository)

    val account1 = Account(UUID.randomUUID(), "varadha")
    val account2 = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.persist(account2), 10 seconds)
    Await.result(repository.deposit(account1.id, 10), 10 seconds)
    Await
      .result(service.transfer(APIRequest.TransferReq(account1.id.toString, account2.id.toString, 10)), 10 seconds)
      .status shouldBe "SUCCESS"
    Await.result(repository.find(account2.id), 10 seconds).get.balance shouldBe 10
    Await.result(repository.find(account1.id), 10 seconds).get.balance shouldBe 0
  }

  "deposit" should "orchestrate money deposit" in {
    val repository = STMAccountRepository()
    val service    = AccountServiceImpl(repository)
    val account1   = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(service.deposit(account1.id, 10), 10 seconds).get.balance shouldBe 10
  }

  "withdraw" should "orchestrate money withdrawal" in {
    val repository = STMAccountRepository()
    val service    = AccountServiceImpl(repository)
    val account1   = Account(UUID.randomUUID(), "varadha")
    Await.result(repository.persist(account1), 10 seconds)
    Await.result(repository.deposit(account1.id, 10), 10 seconds)
    Await.result(service.withdraw(account1.id, 10), 10 seconds).get.balance shouldBe 0
  }
}
