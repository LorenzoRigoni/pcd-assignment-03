package ass03;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import ass03.actors.Commands;
import ass03.actors.GuiActor;
import ass03.actors.SimulatorActor;
import ass03.view.BoidsView;
import ass03.view.GuiAdapter;

import static ass03.utils.Constants.SCREEN_WIDTH;
import static ass03.utils.Constants.SCREEN_HEIGHT;

public class BoidsSimulation {
    public static void main(String[] args) {
        final GuiAdapter guiAdapter = new GuiAdapter();

        final BoidsView view = new BoidsView(SCREEN_WIDTH, SCREEN_HEIGHT, guiAdapter);

        ActorSystem<Commands> system = ActorSystem.create(Behaviors.setup(context -> {
            ActorRef<Commands> simulatorActor = context.spawn(
                    SimulatorActor.create(guiAdapter.getGuiActor()),
                    "SimulatorActor"
            );

            ActorRef<Commands> guiActorRef = context.spawn(
                    GuiActor.create(simulatorActor, view),
                    "GuiActor"
            );

            guiAdapter.setGuiActor(guiActorRef);

            return Behaviors.ignore();
        }), "boids-system");
    }
}
