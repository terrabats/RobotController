package unittests.tele.framework.movement;

import automodules.AutoModule;
import automodules.stage.Exit;
import automodules.stage.Main;
import automodules.stage.Stage;
import geometry.framework.Point;
import geometry.position.Pose;
import geometry.position.Vector;
import robotparts.RobotPart;
import teleutil.button.Button;
import teleutil.button.OnTurnOffEventHandler;
import teleutil.button.OnTurnOnEventHandler;
import unittests.tele.TeleUnitTest;

import static global.General.bot;
import static global.General.gph1;
import static global.General.log;

public class TwoOdometryTest extends TeleUnitTest {

    private boolean customMove = false;

    @Override
    protected void start() {
        gph1.link(Button.RIGHT_BUMPER, moveHeading(180)); // TODO FIX PROBLEM WITH ODOMETRY IN AUTOMODULES?
        // TOD 5 FIX PROBLEM WITH ODOMETRY IN AUTOMODULES?
        gph1.link(Button.LEFT_BUMPER, moveHeading(0));
        gph1.link(Button.RIGHT_TRIGGER, moveHeading(-180));
        gph1.link(Button.LEFT_TRIGGER, testModule());
        gph1.link(Button.B, () -> bot.cancelAutoModules());
        gph1.link(Button.Y, OnTurnOnEventHandler.class, () -> customMove = true);
        gph1.link(Button.Y, OnTurnOffEventHandler.class, () -> {
            odometry.reset(); customMove = false;});
    }

    @Override
    protected void loop() {
        Pose power = movePower(new Pose(new Point(), 0));
        if(customMove) {
            drive.move(gph1.ry / 2.0, gph1.rx / 2.0, gph1.lx / 2.0);
        }else {
            drive.move(power.getY() + gph1.ry / 2.0, power.getX() + gph1.rx / 2.0, gph1.lx);
        }
        log.show("Odometry Pose", odometry);
    }

    private AutoModule testModule(){
        return new AutoModule(new Stage(
                drive.usePart(),
                new Main(() -> {
                    log.show("Yes sir");
                    drive.move(gph1.ry / 2.0, gph1.rx / 2.0, gph1.lx);
                }),
                RobotPart.exitTime(2),
                drive.returnPart()
        ));
    }

    private AutoModule moveHeading(double target){
        return new AutoModule(new Stage(
                drive.usePart(),
                new Main(() -> {
                    Pose power = movePower(new Pose(new Point(), target));
                    drive.move(power.getY(), power.getX(), power.getAngle());
                }),
                new Exit(() -> Math.abs(odometry.getHeading()-target) < 2),
                drive.returnPart()
        ));
    }

    private Pose movePower(Pose target){
        Pose error = target.getAdded(odometry.getPose().getInverted());
        double xPow = getPower(error.getX(), 0.05, 0.02);
        double yPow = getPower(error.getY(), 0.05, 0.02);
        double hPow = getPower(-error.getAngle(), 0.008, 0.06); //0.008, 0.06
        Vector powerVector = new Vector(xPow, yPow).getRotated(-odometry.getHeading()).getScaled(1);

        return new Pose(powerVector, hPow);
    }

    private double getPower(double error, double k, double rp){
        return error*k + Math.signum(error)*rp;
    }
}