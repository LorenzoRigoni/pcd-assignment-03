package ass03.actors;

import akka.actor.typed.ActorRef;
import ass03.model.Boid;
import ass03.model.BoidsModel;

public abstract class Commands {
    private Commands() {}

    public static final class CalculateVelocity extends Commands {
        public final BoidsModel model;
        public final ActorRef<VelocityComputed> replyTo;

        public CalculateVelocity(BoidsModel model, ActorRef<VelocityComputed> replyTo) {
            this.model = model;
            this.replyTo = replyTo;
        }
    }

    public static final class CalculatePosition extends Commands {
        public final BoidsModel model;
        public final ActorRef<PositionComputed> replyTo;

        public CalculatePosition(BoidsModel model, ActorRef<PositionComputed> replyTo) {
            this.model = model;
            this.replyTo = replyTo;
        }
    }

    public static final class VelocityComputed extends Commands {}

    public static final class PositionComputed extends Commands {}

    public static final class StartCycle extends Commands {}

    public static final class VelocityDone extends Commands {}

    public static final class PositionDone extends Commands {}
}