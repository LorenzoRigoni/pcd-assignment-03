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
                .onMessage(Commands.SetSimulationParams.class, this::onSetSimulationParams)
                .onMessage(Commands.SuspendSimulation.class, this::onSuspendSimulation)
                .onMessage(Commands.ResumeSimulation.class, this::onResumeSimulation)
                .onMessage(Commands.StopSimulation.class, this::onStopSimulation)
                .build();
    }

    private Behavior<Commands> onStartSimulation(Commands.StartSimulation command) {
        this.simulator.tell(new Commands.StartSimulation(command.numOfBoids));
        return Behaviors.same();
    }

    private Behavior<Commands> onPaintBoids(Commands.PaintBoids command) {
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
        return Behaviors.same();
    }

    private Behavior<Commands> onSetSimulationParams(Commands.SetSimulationParams command) {
        this.simulator.tell(command);
        return Behaviors.same();
    }

    private Behavior<Commands> onSuspendSimulation(Commands.SuspendSimulation command) {
        this.simulator.tell(command);
        return Behaviors.same();
    }

    private Behavior<Commands> onResumeSimulation(Commands.ResumeSimulation command) {
        this.simulator.tell(command);
        return Behaviors.same();
    }

    private Behavior<Commands> onStopSimulation(Commands.StopSimulation command) {
        this.simulator.tell(command);
        return Behaviors.same();
    }
}
