package teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static global.General.gph1;
import static global.General.gph2;
import static global.General.log;
import static global.General.voltageScale;
import static global.Modes.Drive.FAST;
import static global.Modes.OuttakeStatus.DRIVING;
import static global.Modes.OuttakeStatus.INTAKING;
import static global.Modes.OuttakeStatus.PLACING;
import static teleutil.button.Button.*;

@TeleOp(name = "TerraOp", group = "TeleOp")
public class NithinOp extends Tele {

    @Override
    public void initTele() {
        voltageScale = 1;

        gph1.link(LEFT_BUMPER, L_BUMPER);
        gph1.link(RIGHT_BUMPER, R_BUMPER);
        gph1.link(RIGHT_TRIGGER, L_TRIGGER);
        gph1.linkWithCancel(LEFT_TRIGGER, outtakeStatus.isMode(DRIVING), R_TRIGGER, CancelIntake);
//        gph1.link(LEFT_TRIGGER, R_TRIGGER);

//        gph1.link(DPAD_DOWN, () -> lift.adjustHolderTarget(-7));
        gph1.link(DPAD_RIGHT, RIGHT_DPAD);
        gph1.link(DPAD_LEFT, LEFT_DPAD);
        gph1.link(DPAD_UP,  UP_DPAD);


        gph1.linkWithCancel(X, outtakeStatus.isMode(PLACING), levelone, leveltwo);
        gph1.linkWithCancel(A, outtakeStatus.isMode(PLACING), levelthree, levelfour);
        gph1.linkWithCancel(B, outtakeStatus.isMode(PLACING), levelfive, levelsix);
        gph1.link(Y, levelseven);



        gph2.link(A, outtake::openClaw);
        gph2.link(B, outtake::closeClaw);
        gph2.link(X, intake::chubramani);

        gph2.link(LEFT_TRIGGER, X_BUTTON);

        gph2.link(DPAD_UP, HangReady);
        gph2.link(DPAD_DOWN, Hang );



        /**
         * Start code
         */
        outtake.moveStart();
        outtake.moveStartRotate();
        intake.moveInit();
        driveMode.set(FAST);
        outtakeStatus.set(DRIVING);
        lift.reset();

    }

    @Override
    public void startTele() {
        lift.reset();
//        Outtake.moveStart();


    }

    @Override
    public void loopTele() {

        drive.newMove(gph1.ry, gph1.rx, gph1.lx);
//        lift.move(gph2.ry);

//        log.show("", odometry.getEncX());

        /**
         * Gets Distance
         */

//        log.show("right distance (cm)", distanceSensorsNew.getCMDistanceRight());
//        log.show("left distance (cm)", distanceSensorsNew.getCMDistanceLeft());

        /**
         * Gets light of color sensor
         */
//        log.show("light 1", colorSensorsNew.getLight1());
//        log.show("light 2", colorSensorsNew.getLight2());


        /**
         * odo pose
         */
//        log.show("pose", odometry.getPose());

        /**
         * Outtake Status
         */
//        log.show("OuttakeStatus", outtakeStatus.get());

        /**
         * Heading
         */
//        log.show("heading", gyro.getHeading());


        /**
         * lift encoder positions
         */
        log.show("Right", lift.motorRight.getPosition());
        log.show("Left", lift.motorLeft.getPosition());


        /**
         * drive mode
         */
//        log.show("DriveMode", driveMode.get());

    }

}


