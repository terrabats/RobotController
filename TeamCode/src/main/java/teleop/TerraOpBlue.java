package teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static global.General.gph1;
import static global.General.voltageScale;
import static global.Modes.TeleStatus.BLUEA;
import static teleutil.button.Button.B;
import static global.General.*;

import autoutil.vision.SampleScanner;
import teleutil.button.Button;

@TeleOp(name = "TerraOpBlue", group = "TeleOp")
public class TerraOpBlue extends Tele {

    @Override
    public void initTele() {

        voltageScale = 1;

        Tele auto = this;
        auto.scan(true);

        intake.scanner = (SampleScanner) Scanner;

        teleStatus.set(BLUEA);

    }

    @Override
    public void startTele() {
        /**
         * Start code
         */


    }

    @Override
    public void loopTele() {


        intake.updatePipeline();
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
//        log.show("Right", lift.motorRight.getPosition());


        /**
         * extendo encoder positions
         */
//        log.show("left", extendo.motorLeft.getPosition());

        /**
         * drive mode
         */
//        log.show("DriveMode", driveMode.get());

        /**
         * outtake status
         */
//                log.show("outske", outtakeStatus.get());
        /**
         * heights
         */
//        log.show("current height", current.getValue(currentHeight));

    }

}


