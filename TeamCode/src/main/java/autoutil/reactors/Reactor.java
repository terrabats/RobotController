package autoutil.reactors;

import automodules.stage.Exit;
import automodules.stage.Initial;
import automodules.stage.Main;
import automodules.stage.Stage;
import automodules.stage.Stop;
import autoutil.controllers.control1D.Controller1D;
import autoutil.controllers.control2D.Controller2D;
import autoutil.generators.PoseGen;
import geometry.position.Pose;
import robot.RobotUser;
import robotparts.RobotPart;

public abstract class Reactor implements RobotUser {

    protected Controller2D movementController;
    protected Controller1D headingController;

    protected static boolean forceExit = false;

    public abstract void init();
    public abstract Pose getPose();
    public abstract void setTarget(Pose target);
    public abstract void nextTarget();
    public abstract boolean isAtTarget();
    public abstract void moveToTarget(PoseGen generator);

    public void firstTarget(){}

    public static Stage forceExit(){ return new Stage(new Main(() -> forceExit = true), RobotPart.exitAlways()); }

    public void scale(double scale) {
        movementController.scale(scale);
        headingController.scale(scale);
    }

    public void scaleAccuracy(double scale){
        movementController.scaleAccuracy(scale);
        headingController.scaleAccuracy(scale);
    }

    public void setTime(double time){
        movementController.setTime(time);
    }

    public final Initial initialTarget(){ return new Initial(this::firstTarget);}
    public final Main mainTarget(PoseGen generator){return new Main(() -> moveToTarget(generator)); }
    public final Exit exitTarget(){ return new Exit(() -> isAtTarget() || forceExit); }
    public final Stop stopTarget(){ return new Stop(() -> {nextTarget(); forceExit = false; }); }


    protected void setControllers(Controller2D movementController, Controller1D headingController){
        this.movementController = movementController;
        this.headingController = headingController;
    }

}