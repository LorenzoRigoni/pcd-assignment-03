package ass03.view;

import akka.actor.typed.ActorRef;
import ass03.actors.Commands;

/**
 * This class wraps the reference to the gui actor. This wrapper has been created to separate GUI from actor logics.
 */
public class GuiActorWrapper {
    private ActorRef<Commands> guiActor;

    /**
     * Set the reference of gui actor.
     * @param guiActor the reference of gui actor.
     */
    public void setGuiActor(ActorRef<Commands> guiActor) {
        this.guiActor = guiActor;
    }

    /**
     * Get the reference of gui actor.
     * @return the reference of gui actor.
     */
    public ActorRef<Commands> getGuiActor() {
        return this.guiActor;
    }

    /**
     * Send a command to the gui actor.
     * @param command The command to send.
     */
    public void sendCommandToActor(SimulationCommands command) {
        switch (command) {
            case START_SIMULATION -> this.guiActor.tell(new Commands.StartSimulation(command.getNumOfBoids()));
            case STOP_SIMULATION -> this.guiActor.tell(new Commands.StopSimulation());
            case SUSPEND_SIMULATION -> this.guiActor.tell(new Commands.SuspendSimulation());
            case RESUME_SIMULATION -> this.guiActor.tell(new Commands.ResumeSimulation());
            case SET_PARAMS -> this.guiActor.tell(new Commands.SetSimulationParams(command.getParamType(), command.getParamValue()));
        }
    }
}
