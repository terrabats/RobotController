package global;

import robotparts.hardware.Drive;
import robotparts.hardware.Lift;
import util.template.Mode;


public interface Modes {
    /**
     * Interface to hold modes (different types of movement/control)
     */


    /**
     * List of mode types
     */

    enum GamepadMode implements Mode.ModeType { NORMAL, AUTOMATED }

    enum OuttakeStatus implements Mode.ModeType { DRIVING, PLACING, INTAKING, PLACING2}
    Mode outtakeStatus = new Mode(OuttakeStatus.class);


    enum Height implements Mode.ModeType {HIGH, MIDDLE, LOW, GROUND}
    Mode heightMode = new Mode(Height.class)
            .set(Height.HIGH, 50)
            .set(Height.MIDDLE, 35)
            .set(Height.LOW, 15)
            .set(Height.GROUND, 0);


    // TOD5 MULTI DIMENSIONAL MODE
    enum Drive implements Mode.ModeType {FAST, MEDIUM, SLOW, SUPERSLOW}
    Mode driveMode = new Mode(Drive.class);

}
//