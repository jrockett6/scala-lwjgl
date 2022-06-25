package wafflepop

import cats.effect.{IO, IOApp}
import org.lwjgl.*


object Main extends IOApp.Simple {
  def run: cats.effect.IO[Unit] = 
    IO.println(s"Hello LWJGL ${Version.getVersion}")

}