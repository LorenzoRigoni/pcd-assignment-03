package ass03.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import ass03.model.BoidsModel;

public class BoidActor extends AbstractBehavior<Commands> {

    public BoidActor(ActorContext<Commands> context) {
        super(context);
    }

    @Override
    public Receive<Commands> createReceive() {
        return null;
    }
}
