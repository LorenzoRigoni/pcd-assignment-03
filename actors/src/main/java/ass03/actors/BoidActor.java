package ass03.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ass03.model.Boid;

/**
 * This actor represents a sublist of boids. He receives two type of commands:
 * <ul>
 *     <li>Calc the velocities of each boid;</li>
 *     <lI>Calc the position of each boid.</lI>
 * </ul>
 */
public class BoidActor extends AbstractBehavior<Commands> {

    public BoidActor(ActorContext<Commands> context) {
        super(context);
    }

    public static Behavior<Commands> create() {
        return Behaviors.setup(BoidActor::new);
    }

    @Override
    public Receive<Commands> createReceive() {
        return newReceiveBuilder()
                .onMessage(Commands.CalculateVelocity.class, this::onCalculateVelocity)
                .onMessage(Commands.CalculatePosition.class, this::onCalculatePosition)
                .build();
    }

    private Behavior<Commands> onCalculateVelocity(Commands.CalculateVelocity command) {
        for (final Boid boid : command.boids)
            boid.updateVelocity(command.model);
        final Commands.VelocityComputed res = new Commands.VelocityComputed(command.boids);
        command.replyTo.tell(res);
        return Behaviors.same();
    }

    private Behavior<Commands> onCalculatePosition(Commands.CalculatePosition command) {
        for (final Boid boid : command.boids)
            boid.updatePos(command.model);
        final Commands.PositionComputed res = new Commands.PositionComputed(command.boids);
        command.replyTo.tell(res);
        return Behaviors.same();
    }
}
