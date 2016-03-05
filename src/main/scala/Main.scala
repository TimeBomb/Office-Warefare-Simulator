import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration}

object Main extends App {
  println("Good morning everyone, time for another productive day...")
  val system = ActorSystem("jsonb")
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  val rand = new scala.util.Random
  val turns = 100
  val gameSpeedMultiplier = 1.0
  val hireSpeed = Duration((4000 / gameSpeedMultiplier).toInt, TimeUnit.MILLISECONDS)
  val turnSpeed = Duration((1500 / gameSpeedMultiplier).toInt, TimeUnit.MILLISECONDS)

  val Christian = system.actorOf(Props(new OverlordActor(gameSpeedMultiplier)), "Christian")
  val HR = system.actorOf(Props(new HRActor(Christian)), "Toni")
  val Instigator = system.actorOf(Props(new InstigatorActor(Christian)), "jsonB")

  val zero = Duration(0, TimeUnit.MILLISECONDS)
  HR.tell(RecruitSomeone, null)
  HR.tell(RecruitSomeone, null)
  HR.tell(RecruitSomeone, null)
  system.scheduler.schedule(zero, hireSpeed, HR, RecruitSomeone)
  system.scheduler.schedule(zero, turnSpeed, Instigator, Instigate)
}