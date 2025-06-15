package ass03.actors;

import akka.actor.typed.ActorRef;
import ass03.model.Boid;
import ass03.model.BoidsModel;
import ass03.utils.SimulationParams;

import java.util.List;

/**
 * This class represents every type of commands exchanged by the actors.
 */
public abstract class Commands {
    private Commands() {}

    /**
     * This command is for the simulator actor to set the reference to the gui actor.
     */
    public static final class SetGuiActorRef extends Commands {
        public final ActorRef<Commands> guiActor;

        public SetGuiActorRef(final ActorRef<Commands> guiActor) {
            this.guiActor = guiActor;
        }
    }

    /**
     * This command is for the boid actors to calculate the velocities.
     */
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

    /**
     * This command is for the boid actors to calculate the positions.
     */
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

    /**
     * This command is for the simulator actor when the boid actor finishes to calculate the velocities.
     */
    public static final class VelocityCalculated extends Commands {
        public final List<Boid> boids;

        public VelocityCalculated(List<Boid> boids) {
            this.boids = boids;
        }
    }

    /**
     * This command is for the simulator actor when the boid actor finishes to calculate the positions.
     */
    public static final class PositionCalculated extends Commands {
        public final List<Boid> boids;

        public PositionCalculated(List<Boid> boids) {
            this.boids = boids;
        }
    }

    /**
     * This command is for the gui actor when the simulator actor got the updated boids.
     */
    public static final class PaintBoids extends Commands {
        public final List<Boid> boids;
        public final long initialTime;

        public PaintBoids(List<Boid> boids, long initialTime) {
            this.boids = boids;
            this.initialTime = initialTime;
        }
    }

    /**
     * This command is for the simulator actor when the gui finished to draw the boids.
     */
    public static final class GuiReady extends Commands {}

    /**
     * This command is for the simulator actor when the user modify the params of simulation. The params are:
     * <ul>
     *     <li>Alignment</li>
     *     <li>Cohesion</li>
     *     <li>Separation</li>
     * </ul>
     */
    public static final class SetSimulationParams extends Commands {
        public SimulationParams param;
        public double newValue;

        public SetSimulationParams(SimulationParams param, double newValue) {
            this.param = param;
            this.newValue = newValue;
        }
    }

    /**
     * This command is for the simulator actor to start the simulation.
     */
    public static final class StartSimulation extends Commands {
        public final int numOfBoids;

        public StartSimulation(int numOfBoids) {
            this.numOfBoids = numOfBoids;
        }
    }

    /**
     * This command is for the simulator actor to suspend the simulation.
     */
    public static final class SuspendSimulation extends Commands {}

    /**
     * This command is for the simulator actor to resume the simulation.
     */
    public static final class ResumeSimulation extends Commands {}

    /**
     * This command is for the simulator actor to stop the simulation.
     */
    public static final class StopSimulation extends Commands {}
}