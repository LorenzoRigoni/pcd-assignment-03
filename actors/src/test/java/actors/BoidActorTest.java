package actors;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import ass03.actors.BoidActor;
import ass03.actors.Commands;
import ass03.model.Boid;
import ass03.model.BoidsModel;
import ass03.model.P2d;
import ass03.model.V2d;
import org.junit.jupiter.api.*;

import javax.xml.datatype.Duration;
import java.util.Arrays;
import java.util.List;

import static ass03.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class BoidActorTest {
    private static ActorTestKit testKit;
    private BoidsModel model;

    @BeforeEach
    public void setup() {
        testKit = ActorTestKit.create();
        this.model = new BoidsModel(SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED, PERCEPTION_RADIUS, AVOID_RADIUS);
    }

    @AfterEach
    public void teardown() {
        testKit.shutdownTestKit();
    }

    @Test
    public void testCalcVelocity() {
        final TestProbe<Commands> simulatorProbe = testKit.createTestProbe();

        final V2d initialVel = new V2d(1, 0);

        final Boid boid = new Boid(new P2d(0, 0), initialVel);

        final ActorRef<Commands> boidActor = testKit.spawn(
                BoidActor.create(), "test-boid-actor"
        );

        this.model.setBoids(Arrays.asList(
                new Boid(new P2d(15, 0), new V2d(0, 1)),
                new Boid(new P2d(30, 30), new V2d(-1, -1))
        ));

        boidActor.tell(new Commands.CalculateVelocity(this.model, List.of(boid), simulatorProbe.getRef()));

        final Commands.VelocityCalculated result = simulatorProbe.expectMessageClass(Commands.VelocityCalculated.class);

        assertAll(
                () -> assertNotNull(result.boids),
                () -> assertFalse(result.boids.isEmpty()),
                () -> assertNotEquals(initialVel, result.boids.getFirst().getVel())
        );
    }

    @Test
    public void testCalcPosition() {
        final TestProbe<Commands> simulatorProbe = testKit.createTestProbe();

        final P2d initialPos = new P2d(0, 0);

        final Boid boid = new Boid(initialPos, new V2d(1, 0));

        final ActorRef<Commands> boidActor = testKit.spawn(
                BoidActor.create(), "test-boid-actor"
        );

        this.model.setBoids(Arrays.asList(
                new Boid(new P2d(15, 0), new V2d(0, 1)),
                new Boid(new P2d(30, 30), new V2d(-1, -1))
        ));

        boidActor.tell(new Commands.CalculatePosition(this.model, List.of(boid), simulatorProbe.getRef()));

        final Commands.PositionCalculated result = simulatorProbe.expectMessageClass(Commands.PositionCalculated.class);

        assertAll(
                () -> assertNotNull(result.boids),
                () -> assertFalse(result.boids.isEmpty()),
                () -> assertNotEquals(initialPos, result.boids.getFirst().getPos())
        );
    }
}
