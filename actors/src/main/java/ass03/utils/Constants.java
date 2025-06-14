package ass03.utils;

/**
 * This class contains the constants used in the project.
 */
public class Constants {
    public final static double SEPARATION_WEIGHT = 1.0;

    public final static double ALIGNMENT_WEIGHT = 1.0;
    public final static double COHESION_WEIGHT = 1.0;

    public final static int ENVIRONMENT_WIDTH = 1000;
    public final static int ENVIRONMENT_HEIGHT = 1000;
    public static final double MAX_SPEED = 4.0;
    public static final double PERCEPTION_RADIUS = 50.0;
    public static final double AVOID_RADIUS = 20.0;

    public final static int SCREEN_WIDTH = 1000;
    public final static int SCREEN_HEIGHT = 800;

    public final static int NUM_BOID_ACTORS = Runtime.getRuntime().availableProcessors() + 1;

    public final static int FRAMERATE = 25;
}
