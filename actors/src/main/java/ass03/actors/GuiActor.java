package ass03.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ass03.view.BoidsView;

import static ass03.utils.Constants.FRAMERATE;

public class GuiActor extends AbstractBehavior<Commands> {
    private final ActorRef<Commands> simulator;
    private final BoidsView boidsView;
    private int framerate;

    public GuiActor(ActorContext<Commands> context, ActorRef<Commands> simulator, BoidsView boidsView) {
        super(context);
        this.simulator = simulator;
        this.boidsView = boidsView;
        this.simulator.tell(new Commands.SetGuiActorRef(getContext().getSelf()));
    }

    public static Behavior<Commands> create(ActorRef<Commands> simulator, BoidsView boidsView) {
        return Behaviors.setup(context -> new GuiActor(context, simulator, boidsView));
    }

    @Override
    public Receive<Commands> createReceive() {
        return newReceiveBuilder()
                .onMessage(Commands.StartSimulation.class, this::onStartSimulation)
                .onMessage(Commands.PaintBoids.class, this::onPaintBoids)
                .build();
    }

    private Behavior<Commands> onStartSimulation(Commands.StartSimulation command) {
        System.out.println("Starting Simulation");
        this.simulator.tell(new Commands.StartSimulation(command.numOfBoids));
        return Behaviors.same();
    }

    private Behavior<Commands> onPaintBoids(Commands.PaintBoids command) {
        System.out.println("Start painting boids");
        System.out.println("Num of Boids: " + command.boids.size());
        this.boidsView.update(this.framerate, command.boids);
        var t1 = System.currentTimeMillis();
        var dtElapsed = t1 - command.initialTime;
        var frameratePeriod = 1000 / FRAMERATE;

        if (dtElapsed < frameratePeriod) {
            try {
                Thread.sleep(frameratePeriod - dtElapsed);
            } catch (InterruptedException e) {}
            this.framerate = FRAMERATE;
        } else
            this.framerate = (int) (1000 / dtElapsed);

        this.simulator.tell(new Commands.GuiReady());
        System.out.println("Completed painting boids");
        return Behaviors.same();
    }
}
