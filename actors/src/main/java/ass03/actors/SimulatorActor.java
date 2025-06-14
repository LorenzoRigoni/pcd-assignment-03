package ass03.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ass03.model.Boid;
import ass03.model.BoidsModel;
import static ass03.utils.Constants.*;

import java.util.ArrayList;
import java.util.List;

public class SimulatorActor extends AbstractBehavior<Commands> {
    private ActorRef<Commands> guiActor;
    private List<ActorRef<Commands>> boidActors;
    private List<Boid> collectedBoids;
    private List<List<Boid>> boidsLists;
    private long initialTime;
    private int velocityReplies = 0;
    private int positionReplies = 0;
    private int numOfBoids;
    private BoidsModel model;
    private boolean isSuspended;

    public SimulatorActor(ActorContext<Commands> context, ActorRef<Commands> guiActor) {
        super(context);
        this.guiActor = guiActor;
        this.boidActors = new ArrayList<>();
        this.collectedBoids = new ArrayList<>();
        this.boidsLists = new ArrayList<>();
        this.isSuspended = false;
    }

    public static Behavior<Commands> create(ActorRef<Commands> guiActor) {
        return Behaviors.setup(context -> new SimulatorActor(context, guiActor));
    }

    @Override
    public Receive<Commands> createReceive() {
        return newReceiveBuilder()
                .onMessage(Commands.SetGuiActorRef.class, this::onSetGuiActorRef)
                .onMessage(Commands.StartSimulation.class, this::onStartSimulation)
                .onMessage(Commands.VelocityComputed.class, this::onVelocityComputed)
                .onMessage(Commands.PositionComputed.class, this::onPositionComputed)
                .onMessage(Commands.GuiReady.class, this::onGuiReady)
                .onMessage(Commands.SetSimulationParams.class, this::onSetSimulationParams)
                .onMessage(Commands.SuspendSimulation.class, this::onSuspendSimulation)
                .onMessage(Commands.ResumeSimulation.class, this::onResumeSimulation)
                .build();
    }

    private Behavior<Commands> onSetGuiActorRef(Commands.SetGuiActorRef command) {
        this.guiActor = command.guiActor;
        return this;
    }

    private Behavior<Commands> onStartSimulation(Commands.StartSimulation command) {
        //System.out.println("Simulation started");
        this.numOfBoids = command.numOfBoids;
        this.model = new BoidsModel(SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED, PERCEPTION_RADIUS, AVOID_RADIUS);
        this.model.createBoids(this.numOfBoids);
        this.createBoidsLists();
        for (int i = 0; i < NUM_BOID_ACTORS; i++)
            this.boidActors.add(getContext().spawn(BoidActor.create(), "boid-actor-" + i));
        this.initialTime = System.currentTimeMillis();
        this.sendVelocityCommands();
        return this;
    }

    private Behavior<Commands> onGuiReady(Commands.GuiReady command) {
        if (!this.isSuspended) {
            this.restartCycle();
        }
        return Behaviors.same();
    }

    private void restartCycle() {
        this.initialTime = System.currentTimeMillis();
        this.sendVelocityCommands();
    }

    private void sendVelocityCommands() {
        this.velocityReplies = 0;
        this.collectedBoids.clear();
        for (int i = 0; i < NUM_BOID_ACTORS; i++)
            this.boidActors.get(i).tell(new Commands.CalculateVelocity(this.model, this.boidsLists.get(i), getContext().getSelf()));
    }

    private Behavior<Commands> onVelocityComputed(Commands.VelocityComputed command) {
        this.collectedBoids.addAll(command.boids);
        this.velocityReplies++;
        if (this.velocityReplies == NUM_BOID_ACTORS) {
            //System.out.println("Completed calc velocity");
            this.updateBoids();
            //System.out.println("Start calc position");
            this.sendPositionCommands();
        }
        return Behaviors.same();
    }

    private void sendPositionCommands() {
        this.positionReplies = 0;
        this.collectedBoids.clear();

        for (int i = 0; i < NUM_BOID_ACTORS; i++)
            this.boidActors.get(i).tell(new Commands.CalculatePosition(this.model, new ArrayList<>(this.boidsLists.get(i)), getContext().getSelf()));
    }

    private Behavior<Commands> onPositionComputed(Commands.PositionComputed command) {
        this.collectedBoids.addAll(command.boids);
        this.positionReplies++;
        if (this.positionReplies == NUM_BOID_ACTORS) {
            this.updateBoids();
            //System.out.println("Completed calc position");
            //System.out.println(this.guiActor);
            this.guiActor.tell(new Commands.PaintBoids(this.collectedBoids, this.initialTime));
        }
        return Behaviors.same();
    }

    private Behavior<Commands> onSetSimulationParams(Commands.SetSimulationParams command) {
        switch (command.param) {
            case ALIGNMENT:
                this.model.setAlignmentWeight(command.newValue);
                break;

            case COHESION:
                this.model.setCohesionWeight(command.newValue);
                break;

            case SEPARATION:
                this.model.setSeparationWeight(command.newValue);
                break;
        }
        return Behaviors.same();
    }

    private Behavior<Commands> onSuspendSimulation(Commands.SuspendSimulation command) {
        this.isSuspended = true;
        return Behaviors.same();
    }

    private Behavior<Commands> onResumeSimulation(Commands.ResumeSimulation command) {
        this.isSuspended = false;
        this.restartCycle();
        return Behaviors.same();
    }

    private void createBoidsLists() {
        final int boidsPerActor = this.numOfBoids / NUM_BOID_ACTORS;
        int start;
        int end;
        this.boidsLists.clear();

        for (int i = 0; i < NUM_BOID_ACTORS; i++) {
            start = i * boidsPerActor;
            end = (i == NUM_BOID_ACTORS - 1) ? this.numOfBoids : start + boidsPerActor;
            this.boidsLists.add(this.model.getBoids().subList(start, end));
        }
    }

    private void updateBoids() {
        this.model.setBoids(this.collectedBoids);
        this.createBoidsLists();
    }
}
