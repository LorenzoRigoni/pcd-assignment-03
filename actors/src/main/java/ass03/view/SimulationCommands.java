package ass03.view;

import ass03.utils.SimulationParams;

/**
 * This enum represents the possible commands to send for manage the simulation.
 */
public enum SimulationCommands {
    START_SIMULATION,
    STOP_SIMULATION,
    SUSPEND_SIMULATION,
    RESUME_SIMULATION,
    SET_PARAMS;

    private int numOfBoids;
    private SimulationParams paramType;
    private double paramValue;

    public static SimulationCommands startSimulation(int numOfBoids) {
        SimulationCommands cmd = START_SIMULATION;
        cmd.numOfBoids = numOfBoids;
        return cmd;
    }

    public static SimulationCommands setParams(SimulationParams paramType, double newValue) {
        SimulationCommands cmd = SET_PARAMS;
        cmd.paramType = paramType;
        cmd.paramValue = newValue;
        return cmd;
    }

    public int getNumOfBoids() {
        return numOfBoids;
    }

    public SimulationParams getParamType() {
        return paramType;
    }

    public double getParamValue() {
        return paramValue;
    }
}
