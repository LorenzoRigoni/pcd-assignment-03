package ass03.actors;

import akka.actor.typed.ActorRef;
import ass03.model.Boid;
import ass03.model.BoidsModel;

import java.util.List;

public abstract class Commands {
    private Commands() {}

    public static final class SetGuiActorRef extends Commands {
        public final ActorRef<Commands> guiActor;

        public SetGuiActorRef(final ActorRef<Commands> guiActor) {
            this.guiActor = guiActor;
        }
    }

    public static final class CalculateVelocity extends Commands {
        public final BoidsModel model;
        public final List<Boid> boids;
        public final ActorRef<Commands> replyTo;

        public CalculateVelocity(BoidsModel model, List<Boid> boids, ActorRef<Commands> replyTo) {
            this.model = model;
            this.boids = boids;
            this.replyTo = replyTo;
        }
    }

    public static final class CalculatePosition extends Commands {
        public final BoidsModel model;
        public final List<Boid> boids;
        public final ActorRef<Commands> replyTo;

        public CalculatePosition(BoidsModel model, List<Boid> boids, ActorRef<Commands> replyTo) {
            this.model = model;
            this.boids = boids;
            this.replyTo = replyTo;
        }
    }

    public static final class VelocityComputed extends Commands {
        public final List<Boid> boids;

        public VelocityComputed(List<Boid> boids) {
            this.boids = boids;
        }
    }

    public static final class PositionComputed extends Commands {
        public final List<Boid> boids;

        public PositionComputed(List<Boid> boids) {
            this.boids = boids;
        }
    }

    public static final class PaintBoids extends Commands {
        public final List<Boid> boids;
        public final long initialTime;

        public PaintBoids(List<Boid> boids, long initialTime) {
            this.boids = boids;
            this.initialTime = initialTime;
        }
    }

    public static final class GuiReady extends Commands {}

    public static final class StartSimulation extends Commands {
        public final int numOfBoids;

        public StartSimulation(int numOfBoids) {
            this.numOfBoids = numOfBoids;
        }
    }
}