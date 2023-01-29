package automodules;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

import automodules.stage.Main;
import automodules.stage.Stage;
import autoutil.reactors.MecanumJunctionReactor2;
import autoutil.reactors.Reactor;
import autoutil.vision.JunctionScannerAll;
import elements.Field;
import elements.Robot;
import geometry.framework.Point;
import geometry.position.Pose;
import global.Modes;
import math.polynomial.Linear;
import robot.RobotUser;
import robotparts.RobotPart;
import robotparts.electronics.output.OLed;
import teleop.TerraOp;
import teleutil.TeleTrack;
import teleutil.independent.Independent;
import teleutil.independent.Machine;
import util.codeseg.CodeSeg;
import util.condition.OutputList;
import util.template.Iterator;
import util.template.Precision;

import static global.General.bot;
import static global.General.log;
import static global.Modes.*;
import static global.Modes.Height.GROUND;
import static global.Modes.Height.HIGH;
import static global.Modes.Height.LOW;
import static global.Modes.Height.MIDDLE;
import static global.Modes.OuttakeStatus.DRIVING;
import static global.Modes.OuttakeStatus.PLACING;


public interface AutoModuleUser extends RobotUser{


    CodeSeg stack = () -> {bot.addAutoModule(AutoModuleUser.ForwardStackTele(lift.stackedMode)); lift.stackedMode++;};
    TeleTrack kappaBefore = new TeleTrack(
            new TeleTrack.Step(heightMode.setTo(LOW)), // LOW
            new TeleTrack.Step(heightMode.setTo(LOW)), // TERMINAL
            new TeleTrack.Step(heightMode.setTo(LOW)), // LOW
            new TeleTrack.Step(heightMode.setTo(LOW)).add(stack), // LOW
            new TeleTrack.Step(heightMode.setTo(HIGH)).add(stack), // HIGH
            new TeleTrack.Step(heightMode.setTo(GROUND)).add(stack), // GROUND
            new TeleTrack.Step(heightMode.setTo(GROUND)), // TERMINAL
            new TeleTrack.Step(heightMode.setTo(GROUND)), // GROUND
            new TeleTrack.Step(heightMode.setTo(LOW)) // LOW + CAP
    );

    static void enableKappa(){ kappaBefore.enable(); }
    static void disableKappa(){ kappaBefore.disable(); }

    /**
     * Tele
     */


    /**
     * Forward
     */


    AutoModule ForwardTeleHigh = new AutoModule(
            outtakeStatus.ChangeMode(DRIVING),
            outtake.stageEnd(0.1),
            outtake.stageOpen(0.0),
            lift.resetCutoff(),
            lift.stageLift(0.9,0).attach(outtake.stageStartAfter(0.1)),
            kappaBefore.next()
    );

    AutoModule ForwardTeleMiddle = new AutoModule(
            outtakeStatus.ChangeMode(DRIVING),
            outtake.stageEnd(0.1),
            outtake.stageOpen(0.0),
            drive.moveTime(1.0, 0.0, 0.0, 0.22),
            outtake.stageStart(0.0),
            lift.resetCutoff(),
            lift.stageLift(1.0,0),
            kappaBefore.next()
    );

    AutoModule ForwardTeleLow = new AutoModule(
            outtakeStatus.ChangeMode(DRIVING),
            outtake.stageEnd(0.1),
            outtake.stageOpen(0.0),
            drive.moveTime(1.0, 0.0, 0.0, 0.25),
            outtake.stageStart(0.0),
            lift.resetCutoff(),
            lift.stageLift(1.0,0),
            kappaBefore.next()
    );

    AutoModule ForwardTeleGround = new AutoModule(
            outtakeStatus.ChangeMode(DRIVING),
            outtake.stageStart(0.1),
            lift.resetCutoff(),
            lift.stageLift(1.0,0),
            outtake.stageOpen(0.0),
            kappaBefore.next()
    );

    /**
     * Backward
     */

    AutoModule BackwardPlaceHighTele = new AutoModule(
            lift.changeHigh(false),
            outtake.stageReadyEnd(0.0),
            lift.stageLift(1.0, heightMode.getValue(HIGH)+2)
    );

    AutoModule BackwardGrabHighTele = new AutoModule(
            heightMode.ChangeMode(HIGH),
            outtakeStatus.ChangeMode(PLACING),
            lift.changeHigh(true),
            outtake.stageClose(0.22),
            outtake.stageMiddle(0.0),
            lift.stageLift(1.0, heightMode.getValue(MIDDLE)+2)
    );

    OutputList BackwardHighTele = new OutputList(outtakeStatus::get).addOption(DRIVING, BackwardGrabHighTele).addOption(PLACING, ForwardTeleHigh);

    AutoModule BackwardGrabMiddleTele = new AutoModule(
            outtakeStatus.ChangeMode(PLACING),
            heightMode.ChangeMode(MIDDLE),
            outtake.stageClose(0.22),
            lift.stageLift(1.0, heightMode.getValue(MIDDLE)+2).attach(outtake.stageReadyEndAfter(0.1))
    );

    OutputList BackwardMiddleTele = new OutputList(outtakeStatus::get).addOption(DRIVING, BackwardGrabMiddleTele).addOption(PLACING, ForwardTeleMiddle);

    AutoModule BackwardGrabLowTele = new AutoModule(
            outtakeStatus.ChangeMode(PLACING),
            heightMode.ChangeMode(LOW),
            outtake.stageClose(0.22),
            lift.changeCutoff(0.0),
            outtake.stageReadyEndAfter(0.0),
            lift.stageLift(1.0, heightMode.getValue(LOW)+2)
    );

    OutputList BackwardLowTele = new OutputList(outtakeStatus::get).addOption(DRIVING, BackwardGrabLowTele).addOption(PLACING, ForwardTeleLow);


    AutoModule BackwardGrabGroundTele = new AutoModule(
            outtakeStatus.ChangeMode(PLACING),
            heightMode.ChangeMode(GROUND),
            outtake.stageClose(0.22),
            lift.changeCutoff(0.0),
            lift.stageLift(1.0, heightMode.getValue(GROUND)+2)
    );

    OutputList BackwardGroundTele = new OutputList(outtakeStatus::get).addOption(DRIVING, BackwardGrabGroundTele).addOption(PLACING, ForwardTeleGround);

    AutoModule ForwardTeleBottom = new AutoModule(
            outtakeStatus.ChangeMode(DRIVING),
            lift.changeHigh(false),
            outtake.stageStart(0.0),
            outtake.stageOpen(0.0),
            lift.resetCutoff(),
            lift.stageLift(1.0,0)
    );


    AutoModule ResetLift = new AutoModule(lift.moveTime(-0.3, 0.5),  lift.resetLift() );
//    AutoModule RetractOdometry = new AutoModule(drive.stageRetract());
//    AutoModule EngageOdometry = new AutoModule(drive.stageEngage());
    AutoModule UprightCone = new AutoModule(lift.stageLift(1.0, 15));
    AutoModule TakeOffCone = new AutoModule(outtake.stageClose(0.0), lift.stageLift(1.0, heightMode.getValue(HIGH)+3.5).attach(outtake.stageReadyStartAfter(0.5)),RobotPart.pause(0.3),outtake.stageFlip(0.0));

    static AutoModule ForwardStackTele(int i){return new AutoModule(
            lift.changeCutoff(2),
            outtake.stageOpen(0.0),
            outtake.stageStart(0.0),
            lift.stageLift(1.0,  i == 0 ? 14.5 : Math.max(15.0 - (i*15.0/5.0), 0))
    );}

    /**
     * Misc
     */
    Independent MoveToZero = new Independent() { @Override public void define() {addSetpoint(0.0, 0.01, 0.0); }};
    Independent MoveToCycleStart = new Independent() {
        @Override
        public void define() {

            // TODO FIX
            // EVERYTHING WORKS, USE DIFFERENT JUNCTION?
            // DONT USE JUNCTION SCANNER???


//            addCustomCode(JunctionScannerAll::resume, 0.05);
//            addCustomCode(() -> {
//                Pose pose = JunctionScannerAll.getPose();
////                whileActive(() -> {
////                    log.show("JunctionScannerPose", JunctionScannerAll.getPose());
////                });
//                Point point = new Point(-pose.getX(), odometry.getY());
//                odometry.setCurrentPoint(point); odometry.setCurrentPoint(point);
//            });
//            addSegment(0.7, 0.3, mecanumNonstopSetPoint, 0, 0.01, 0);




            addCustomCode(JunctionScannerAll::resume, 0.05);
            addCustomCode(ResetOdometryForCycle, 0.05);
            addWaypoint(1.0, -10, -14, 0);
            addSegment(0.7, 0.7, mecanumNonstopSetPoint, 0, 0.01, 0);
            addCustomCode(() -> {
                Pose pose = new Pose();
                while (pose.getLength() < 10) {
                    ArrayList<Pose> poses = new ArrayList<>();
                    for (int i = 0; i < 100; i++) {
                        poses.add(JunctionScannerAll.getPose());
                    }
                    pose = Pose.forAllAverage(poses);
//                    log.show("JunctionScannerPose", pose);
//                    log.show("CurrentPose", JunctionScannerAll.getPose());
                }
//                pose.add(new Pose(0, -23, 0));
                Point point = new Point(pose.getX(), odometry.getY());
                odometry.setCurrentPoint(point); odometry.setCurrentPoint(point);
//                odometry.setCurrentPose(pose);
//                odometry.setCurrentPose(pose);
//                whileActive(() -> {
//                    log.show("JunctionScannerPose", pose);
//                });

            });
            addSegment(0.7, 0.7, mecanumNonstopSetPoint, 0, 0.01, 0);
            addCustomCode(JunctionScannerAll::pause, 0.05);
            addCustomCode(() -> drive.slow = true);
            addAutoModule(ForwardTeleBottom);
        }
    };


    /**
     * Cycle
     */
    Point cyclePoint = new Point(Field.halfWidth-19, -62);
    CodeSeg ResetOdometryForCycle =  () -> {
        distanceSensors.ready();
        Point point = new Point(distanceSensors.getRightDistance() - cyclePoint.getX(),-distanceSensors.getFrontDistance() - cyclePoint.getY());
        odometry.setCurrentPoint(point); odometry.setCurrentPoint(point);
        odometry.setHeading(0); gyro.reset();
    };

    static AutoModule BackwardCycle(Height height, double offset) {return new AutoModule(
            outtake.stageClose(0.2),
            outtake.stageReadyEnd(0.1),
            lift.stageLift(1.0, heightMode.getValue(height)+offset)
    );}

    AutoModule ForwardCycle = new AutoModule(
            outtake.stageEnd(0.1),
            outtake.stageOpen(0.0),
            lift.resetCutoff(),
            lift.stageLift(1.0,0).attach(outtake.stageStartAfter(0.1))
    );

    AutoModule StageStart = new AutoModule(outtake.stageStart(0.0));
    AutoModule ReadyStart = new AutoModule(outtake.stageReadyStart(0.0));

    AutoModule HoldMiddle = new AutoModule(outtake.stageClose(0.18), outtake.stageMiddle(0.0));

    static Independent Cycle2(int i) { return new Independent() {
            @Override
            public void define() {
            if(i+1 == 1){
                addCustomCode(() -> drive.slow = false);
                addAutoModule(leds.autoModuleColor(OLed.LEDColor.OFF));
                addWaypoint(0.7,  0, 17, 0);
                addWaypoint(0.4,  0, 21, 0);
            }
            if(i+1 != 11){
                if(i+1 == 10){ addAutoModule(leds.autoModuleColor(OLed.LEDColor.ORANGE)); }

                addConcurrentAutoModuleWithCancel(BackwardCycle(HIGH, 4), 0.2);
                addWaypoint(i == 0 ? 0.5 : 0.6, 0, -15, 0);
                addSegment(0.2, 0.5, mecanumNonstopSetPoint, 0, -23, 0);
                addConcurrentAutoModuleWithCancel(ForwardCycle);
                addSegment(1.0, 0.25, mecanumNonstopWayPoint, 0, 25, 0);

            } else{
                addAutoModule(leds.autoModuleColor(OLed.LEDColor.GREEN));
                addConcurrentAutoModuleWithCancel(HoldMiddle, 0.2);
                addSegment(0.6, 0.5, mecanumNonstopSetPoint, 0, 0, 0);
                addPause(0.05);
//                addAutoModule(RetractOdometry);
                addAutoModule(leds.autoModuleColor(OLed.LEDColor.OFF));
            }
    }};}

    Machine MachineCycle = new Machine()
            .addInstruction(odometry::reset, 0.1)
            .addIndependent(11, AutoModuleUser::Cycle2)
    ;








    /**
     * Cycle Medium
     */
    Independent CycleExtra = new Independent() {
        @Override
        public void define() {
//            startPose = new Pose(Robot.halfLength + 59, Field.width/2.0, 90);
            addWaypoint(0.7,0,-10,0);
            addWaypoint(1.0,-16.0, -14.0, -21.0);
            addConcurrentAutoModule(BackwardCycle(MIDDLE, 2));
            addWaypoint(1.0, -31.0, 1.0, -25.0);
//
//            addSegment(0.8, 0.7, mecanumNonstopSetPoint,  -25.0, 37.0, -21.0);
//            addConcurrentAutoModuleWithCancel(BackwardCycle(MIDDLE), 0.2);
            addSegment(1.1, 0.7, mecanumNonstopSetPoint, -47.0, -14.0, -25.0);
            addConcurrentAutoModuleWithCancel(ForwardCycle, 0.3);


//            addSegment(0.8, 0.7, mecanumNonstopSetPoint,  -25.0, 37.0, -21.0);
//            addConcurrentAutoModuleWithCancel(BackwardCycle(LOW), 0.2);
//            addSegment(0.8, 0.7, mecanumNonstopSetPoint, -34.0, 37.0, -57.0);
//            addConcurrentAutoModuleWithCancel(ForwardCycle, 0.3);
//            addWaypoint(0.5, -43.0, 43.0, -57.0 );
//            addSegment(0.8, 0.7, mecanumNonstopSetPoint, -37.0, 47.5, -57.0);
//            addConcurrentAutoModuleWithCancel(BackwardCycle(GROUND), 0.2);
//            addWaypoint(0.5, -64.0, 55.0, -90.0);
//            addWaypoint(0.5, -136.0, 58.0, -90.0);
//            addConcurrentAutoModuleWithCancel(ForwardCycle, 0.3);
        }
    };

    Machine MachineCycleExtra = new Machine()
            .addInstruction(odometry::reset, 0.1)
            .addIndependent(CycleExtra)
    ;


}
