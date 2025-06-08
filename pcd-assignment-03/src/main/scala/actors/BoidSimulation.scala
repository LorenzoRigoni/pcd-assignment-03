package actors

import actors.model.BoidsModel
import actors.system.ManagerActor
import actors.utils.Constants.*
import akka.actor.typed.ActorSystem

@main def startSimulation(): Unit =
  val model = BoidsModel(List(),
    Separation_weight, Alignment_weight, Cohesion_weight,
    Environment_width, Environment_height,
    Max_speed, Perception_radius, Avoid_radius)
  val boidsModel = model.createBoids(1000)
  val system = ActorSystem(ManagerActor(boidsModel), "BoidsSimulation")