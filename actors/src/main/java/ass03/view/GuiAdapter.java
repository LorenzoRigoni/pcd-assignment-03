package ass03.view;

import akka.actor.typed.ActorRef;
import ass03.actors.Commands;

public class GuiAdapter {
    private ActorRef<Commands> guiActor;

    public void setGuiActor(ActorRef<Commands> guiActor) {
        this.guiActor = guiActor;
    }

    public ActorRef<Commands> getGuiActor() {
        return this.guiActor;
    }
}
