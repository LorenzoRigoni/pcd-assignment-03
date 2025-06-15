package ass03.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ass03.model.Boid;

/**
 * This actor manages a sublist of boids. It performs two types of calculations:
 * <ul>
 *     <li>Calc the velocities of each boid;</li>
 *     <lI>Calc the position of each boid.</lI>
 * </ul>
 */
public class BoidActor extends AbstractBehavior<Commands> {

    private BoidActor(ActorContext<Commands> context) {
        super(context);
    }

    /**
     * Create a new boid actor.
     * @return A new boid actor
     */
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
        command.replyTo.tell(new Commands.VelocityCalculated(command.boids));
        return Behaviors.same();
    }

    private Behavior<Commands> onCalculatePosition(Commands.CalculatePosition command) {
        for (final Boid boid : command.boids)
            boid.updatePos(command.model);
        command.replyTo.tell(new Commands.PositionCalculated(command.boids));
        return Behaviors.same();
    }
}
