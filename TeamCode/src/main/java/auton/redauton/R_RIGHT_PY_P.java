package auton.redauton;

import static global.General.bot;
import static global.General.log;
import static global.Modes.Height.GROUND;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import automodules.AutoModule;
import auton.Auto;
import autoutil.AutoFramework;
import elements.TeamProp;
import geometry.position.Pose;
import robotparts.RobotPart;

@Autonomous(name = "R. Right PY&P ", group = "auto", preselectTeleOp = "TerraOp")
public class R_RIGHT_PY_P extends AutoFramework {
    @Override
    public void initialize() {
        this.setConfig(NonstopConfig);
        bot.saveLocationOnField();
        outtake.moveStart();
        outtake.moveLock();
        outtake.closeClaw();
        intake.moveInit();
        propCaseDetected = TeamProp.THIRD;
//        AutoFramework auto = this;
//        auto.scan(true, "red", "right");

    }
    AutoModule autoMove = new AutoModule(
            drive.driveSmart(-.2,0,0)

    );
    AutoModule ExtakeandLift = new AutoModule(
            outtake.stageClose(.1),
            intake.moveTime(.25,.2).attach(lift.stageLift(1, 10)),
            lift.stageLift(1, 18).attach(outtake.stageThruPivot(.1)),
            outtake.stageEnd(.3).attach(outtake.stageTransferPivot(.3)),
            outtake.stageEndPivot(.2).attach(outtake.stageStackRotate(.2))

    );

    AutoModule Drop = new AutoModule(
            lift.stageLift(1, 7),
            lift.stageLift(1, 18).attach(outtake.stageThruPivot(.1)),
            outtake.stageEnd(.3).attach(outtake.stageTransferPivot(.3)),
            outtake.stageEndPivot(.2).attach(outtake.stageStackRotate(.2))
    );

    AutoModule Reset = new AutoModule(
            outtake.stageOpen(.1),
            outtake.stageTransferPivot(.2).attach(outtake.stageStart(.2)),
            outtake.stageStartRotate(.05),

            lift.stageLift(1, heightMode.getValue(GROUND)).attach(outtake.stageThruPivot(.2)),
            outtake.stageStartPivot(.1)

    );
    @Override
    public void define() {
        customCase(() -> {
            addWaypoint(0,-40,0);
            addWaypoint(-34,-71,90);

            addTimedSetpoint(1.0,1,.8,-34,-71,90);
            addAutoModule(ExtakeandLift);
            addTimedSetpoint(1.0,1,.9,-70,-85,90);

            addTimedSetpoint(1.0,1,1.2,-78,-87.5,90);
            addCustomCode(
                    () -> {

                        whileNotExit(() -> distanceSensorsNew.getCMDistanceRight() < 21.5 && distanceSensorsNew.getCMDistanceLeft() < 21.5, () -> {

                            addTimedSetpoint(1,1,1, -(odometry.getX() + 5), -87.5,90);




                        });







                    });
            addAutoModule(Reset);
            addWaypoint(-60,-3,0);
            addTimedSetpoint(1.0,1,1.5,-105,-7,0);



        }, () -> {
            addWaypoint(0,-40,0);
            addWaypoint(-35,-90,90);
            addTimedSetpoint(1.0,1,1,-18,-93,90);

            addAutoModule(ExtakeandLift);
            addTimedSetpoint(1.0,1,1.2,-76.5,-64.5,90);
            addCustomCode(
                    () -> {

                        whileNotExit(() -> distanceSensorsNew.getCMDistanceRight() < 21.5 && distanceSensorsNew.getCMDistanceLeft() < 21.5, () -> {

                            addTimedSetpoint(1,1,1, -(odometry.getX() + 5), -64.5,90);

                        });


                    });
            addAutoModule(Reset);
            addWaypoint(-60,-7,0);
            addTimedSetpoint(1.0,1,1.3,-105,-7,0);


        }, () -> {

            addWaypoint(0,-40,0);
            addWaypoint(10,-65,90);

            addTimedSetpoint(1.0,1,1,15,-70,90);
            addAutoModule(ExtakeandLift);
            addTimedSetpoint(1.0,1,.85,-65,-48,90);
            addTimedSetpoint(1.0,1,.5,-75,-48,90);


            addCustomCode(
                    () -> {

                        whileNotExit(() -> distanceSensorsNew.getCMDistanceRight() < 21.5 && distanceSensorsNew.getCMDistanceLeft() < 21.5, () -> {

                            addTimedSetpoint(1,1,1, -(odometry.getX() + 5), -48,90);

                        });


                    });
            addPause(.1);
            addAutoModule(Reset);
            addWaypoint(-60,-7,0);
            addTimedSetpoint(1.0,1,1.5,-105,-7,0);
        });
    }
    @Override
    public void postProcess() {
        autoPlane.reflectY();
        autoPlane.reflectX();
    }


}
