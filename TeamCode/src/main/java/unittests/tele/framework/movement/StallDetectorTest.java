package unittests.tele.framework.movement;

import debugging.StallDetector;
import robotparts.RobotPart;
import robotparts.electronics.continuous.CMotor;
import robotparts.electronics.positional.PMotor;
import robotparts.hardware.Carousel;
import robotparts.hardware.Lift;
import unittests.tele.TeleUnitTest;
import unused.mecanumold.MecanumLift;

import static global.General.bot;
import static global.General.gamepad1;
import static global.General.gph1;
import static global.General.log;

public class StallDetectorTest extends TeleUnitTest {

    // TODO 4 TEST
    // Test CMotors too


    private final MecanumLift part = mecanumLift;
    private final PMotor motor = part.motorUp;
    private final StallDetector detector = motor.getStallDetector();

    @Override
    public void init() {
        detector.setCustomThresholds(10, 3);
    }

    @Override
    protected void loop() {
        part.move(gph1.ry);
        log.show("Speed (deg/s)", detector.getMotorSpeed());
        log.show("Stable Speed (deg/s)", Math.toDegrees(motor.getMotorEncoder().getAngularVelocityStable()));
        log.show("Current (amps)", detector.getMotorCurrent());
        log.show("Stalling", detector.isStalling());
    }
}