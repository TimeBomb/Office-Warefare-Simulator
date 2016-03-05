import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props, Actor}
import akka.util.Timeout

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import akka.pattern.{ask}

case class HireSomeone(name: String)

case class CrackTheWhip()

case class Hired(name: String)

case class GetEmployees()

case class RecruitSomeone()

case class RecruitSomeoneAsked()

case class Instigate()

case class ShootAt(name: String)

case class ShotAt(name: String)

class InstigatorActor(Christian: ActorRef) extends Actor {
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  def instigate() {
    val employeesFuture = Christian.ask(GetEmployees)
    var employees = Await.result(employeesFuture, timeout.duration).asInstanceOf[ListBuffer[String]]
    if (employees.length > 1) {
      employees = scala.util.Random.shuffle(employees)
      val attackerName = employees(0)
      val victimName = employees(1)
      val attacker = Await.result(context.actorSelection(s"/user/Christian/$attackerName").resolveOne(), timeout.duration)
      attacker ! ShootAt(victimName)
    }
  }

  def receive = {
    case Instigate => {
      instigate()
    }
  }
}

class HRActor(Christian: ActorRef) extends Actor {
  var candidateNames = scala.util.Random.shuffle(ListBuffer("Geoff", "Will", "Ben", "Kate", "Jim", "Neil", "Elaine", "Jason", "Andres", "Ravi", "Jose", "Romel", "Nate", "Rolf", "Denica"))
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  val rand = new scala.util.Random

  def receive = {
    case RecruitSomeone => {
      var candidateName = ""
      if (candidateNames.length > 0) {
        candidateName = candidateNames(0)
      } else if (candidateNames.length > 1) {
        candidateName = candidateNames(rand.nextInt(candidateNames.length - 1))
      }
      if (candidateName.length > 0) {
        Christian ! HireSomeone(candidateName)
      }
    }
    case Hired(name) => {
      candidateNames = candidateNames.filter(_ != name)
    }
  }
}

class OverlordActor(gameSpeedMultiplier: Double) extends Actor {
  var employees = ListBuffer[String]()
  val rand = new scala.util.Random

  def receive = {
    case HireSomeone(myName) =>
      if (!employees.contains(myName)) {
        context.actorOf(Props(new SoldierActor), myName)
        employees += myName
        println(s"[${self.path.name}] Everyone, please give a big Tradeshift welcome to $myName!")
        sender() ! Hired(myName)
      }

    case GetEmployees =>
      sender ! employees
  }
}

class SoldierActor() extends Actor {
  var dartsShot = 0
  var dartsHitWith = 0
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  val rand = new scala.util.Random

  def receive = {
    case ShotAt(attackerName) =>
      dartsHitWith += 1
      if (rand.nextInt(3) == 1) {
        println(s"[${self.path.name}] Hah, where'd you even learn to shoot, $attackerName?")
      }

    case ShootAt(victimName) =>
      println(s"[${self.path.name}] Take that, $victimName!")
      dartsShot += 1

      val victim = Await.result(context.actorSelection(s"/user/Christian/$victimName").resolveOne(), timeout.duration)
      victim ! ShotAt(self.path.name)
  }
}