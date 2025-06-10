package ass03.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import ass03.model.Boid;
import ass03.model.BoidsModel;
import static ass03.utils.Constants.NUM_BOID_ACTORS;

import java.util.ArrayList;
import java.util.List;

public class SimulatorActor extends AbstractBehavior<Commands> {
    private List<ActorRef<Commands>> boidActors;
    private List<Boid> collectedBoids;
    private List<List<Boid>> boidsLists;
    private int velocityReplies = 0;
    private int positionReplies = 0;
    private int numOfBoids;
    private BoidsModel model;

    public SimulatorActor(ActorContext<Commands> context) {
        super(context);
        this.boidActors = new ArrayList<>();
        this.collectedBoids = new ArrayList<>();
        this.boidsLists = new ArrayList<>();
    }

    @Override
    public Receive<Commands> createReceive() {
        return newReceiveBuilder()
                .onMessage(Commands.StartSimulation.class, this::onStartSimulation)
                .build();
    }

    private Behavior<Commands> onStartSimulation(Commands.StartSimulation command) {
        this.numOfBoids = command.numOfBoids;
        this.model = command.model;
        this.model.createBoids(this.numOfBoids);
        this.createBoidsLists();
        for (int i = 0; i < NUM_BOID_ACTORS; i++)
            this.boidActors.add(getContext().spawn(new BoidActor(getContext()), "boid-actor-" + i));
        this.sendVelocityCommands();
        return Behaviors.receive(Commands.class)
                .onMessage(Commands.VelocityComputed.class, this::onVelocityComputed)
                .onMessage(Commands.PositionComputed.class, this::onPositionComputed)
                .build();
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
            this.updateBoids();
            this.sendPositionCommands();
        }
        return Behaviors.same();
    }

    private void sendPositionCommands() {
        this.positionReplies = 0;
        this.collectedBoids.clear();

        for (int i = 0; i < NUM_BOID_ACTORS; i++)
            this.boidActors.get(i).tell(new Commands.CalculatePosition(this.model, this.boidsLists.get(i), getContext().getSelf()));
    }

    private Behavior<Commands> onPositionComputed(Commands.PositionComputed command) {
        this.collectedBoids.addAll(command.boids);
        this.positionReplies++;
        if (this.positionReplies == NUM_BOID_ACTORS) {
            this.updateBoids();
            //TODO: send message to GUIActor
        }
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
