package ass03.actors;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class SimulatorActor extends AbstractBehavior<Commands> {
    public SimulatorActor(ActorContext<Commands> context) {
        super(context);
    }

    @Override
    public Receive<Commands> createReceive() {
        return null;
    }
}
