package autoutil;



import com.qualcomm.robotcore.hardware.PIDCoefficients;

import org.firstinspires.ftc.teamcode.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import automodules.AutoModule;
import automodules.stage.Main;
import automodules.stage.Stage;
import auton.Auto;
import autoutil.generators.AutoModuleGenerator;
import autoutil.generators.BreakpointGenerator;
import autoutil.generators.Generator;
import autoutil.generators.PauseGenerator;
import autoutil.reactors.Reactor;
import autoutil.vision.CaseScanner;
import autoutil.vision.JunctionScanner;
import autoutil.vision.Scanner;
import elements.Case;
import elements.FieldPlacement;
import elements.FieldSide;
import geometry.framework.CoordinatePlane;
import geometry.position.Pose;
import robotparts.RobotPart;
import util.ExceptionCatcher;
import util.Timer;
import util.codeseg.CodeSeg;
import util.codeseg.ParameterCodeSeg;
import util.codeseg.ReturnCodeSeg;
import util.condition.Decision;
import util.condition.DecisionList;
import util.condition.Expectation;
import util.condition.Magnitude;
import util.condition.OutputList;
import util.template.Iterator;

import static global.General.fault;
import static global.General.fieldPlacement;
import static global.General.fieldSide;
import static global.General.log;

public abstract class AutoFramework extends Auto implements AutoUser {
    protected AutoConfig config;

    public void setConfig(AutoConfig config){ this.config = config; }

    protected Pose startPose = new Pose();

    protected CoordinatePlane autoPlane = new CoordinatePlane();

    protected ArrayList<Pose> poses = new ArrayList<>();
    protected ArrayList<AutoSegment.Type> segmentTypes = new ArrayList<>();
    protected ArrayList<AutoSegment<?, ?>> segments = new ArrayList<>();
    protected ArrayList<Double> pauses = new ArrayList<>();
    protected ArrayList<AutoModule> autoModules = new ArrayList<>();
    protected ArrayList<Double> movementScales = new ArrayList<>();
    protected ArrayList<Double> accuracyScales = new ArrayList<>();
    protected ArrayList<AutoSegment<?,?>> customSegments = new ArrayList<>();
    protected ArrayList<CodeSeg> breakpoints = new ArrayList<>();

    protected boolean scanning = false;
    protected boolean haltCameraAfterInit = true;
    protected CaseScanner caseScanner;
    protected Scanner scannerAfterInit;
    protected Case caseDetected = Case.FIRST;

    private int segmentIndex = 1;
    private int pauseIndex, autoModuleIndex, customSegmentIndex, breakpointIndex = 0;

    protected Timer timer = new Timer();


    {
        poses.add(new Pose()); movementScales.addAll(Collections.nCopies(100,1.0)); accuracyScales.addAll(Collections.nCopies(100,1.0));
    }

    public void preProcess(){}

    public abstract void define();

    public void postProcess(){}

    public final void setup(){
        preProcess();
        if(isFlipped()){ flipCases(); }
        define();
        autoPlane.addAll(poses);
        postProcess();
        if(isFlipped()){ flip(); }
//        autoPlane.rotate(-90);
        autoPlane.setStart(startPose);
//        autoPlane.toPoses(p -> p.rotateOrientation(90));
        System.out.println(autoPlane.getPoses());
        System.out.println(startPose);
    }

    public static boolean isFlipped(){ return fieldSide.equals(FieldSide.RED) ^ fieldPlacement.equals(FieldPlacement.UPPER); }
    public void flip(){ autoPlane.reflectX(); autoPlane.reflectPoses(); }
    public void flipCases(){ if(caseDetected.equals(Case.FIRST)){ caseDetected = Case.THIRD; }else if(caseDetected.equals(Case.THIRD)){ caseDetected = Case.FIRST; }}

    public void addDecision(DecisionList decisionList){ decisionList.check(); }
    public void addAutomodule(DecisionList decisionList){ addAutoModule(new AutoModule(new Stage(new Main(decisionList::check), RobotPart.exitAlways()))); }
    public void customSide(CodeSeg one, CodeSeg two){ addDecision(new DecisionList(() -> fieldSide).addOption(FieldSide.BLUE, one).addOption(FieldSide.RED, two)); }
    public void customFlipped(CodeSeg one, CodeSeg two){ if(!isFlipped()){ one.run();}else{two.run();}}
    public void customPlacement(CodeSeg one, CodeSeg two){ addDecision(new DecisionList(() -> fieldPlacement).addOption(FieldPlacement.LOWER, one).addOption(FieldPlacement.UPPER, two)); }
    public void customSidePlacement(CodeSeg one, CodeSeg two, CodeSeg three, CodeSeg four){customSide(() -> customPlacement(one, two), () -> customPlacement(three, four));}
    public void customCase(CodeSeg first, CodeSeg second, CodeSeg third){ addDecision(new DecisionList(() -> caseDetected).addOption(Case.FIRST, first).addOption(Case.SECOND, second).addOption(Case.THIRD, third)); }
    public void customNumber(int num, ParameterCodeSeg<Integer> one){ for (int i = 0; i < num; i++) { one.run(i); } }

    public void setScannerAfterInit(Scanner scanner){
        haltCameraAfterInit = false;
        scannerAfterInit = scanner;
    }

    public void scan(boolean view){
        scanning = true;
        caseScanner = new CaseScanner();
        camera.setScanner(caseScanner);
        camera.start(view);
    }

    @Override
    public final void initAuto() {
        initialize();
        setup();
        createSegments();
        if(scanning){
            while (!isStarted() && !isStopRequested()){ caseDetected = caseScanner.getCase(); caseScanner.log(); log.showTelemetry(); }
            if(haltCameraAfterInit) { camera.halt(); }else{ camera.setScanner(scannerAfterInit); }
        }
    }

    public abstract void initialize();

    @Override
    public void runAuto() {
        odometry.setCurrentPose(startPose);
        pause(0.05);
        timer.reset();
        Iterator.forAll(segments, segment -> segment.run(this));
    }


    private void addSegmentType(double time){ pauses.add(time); segmentTypes.add(AutoSegment.Type.PAUSE); addLastPose();  }
    private void addSegmentType(AutoSegment.Type type, AutoModule autoModule){ autoModules.add(autoModule); segmentTypes.add(type); addLastPose();  }
    private void addSegmentType(AutoSegment.Type type){ segmentTypes.add(type); }

    public void addPause(double time){ addSegmentType(time); }
    public void addSetpoint(double x, double y, double h){ addSegmentType(AutoSegment.Type.SETPOINT); poses.add(new Pose(x,y,h)); }
    public void addWaypoint(double x, double y, double h){ addSegmentType(AutoSegment.Type.WAYPOINT); poses.add(new Pose(x,y,h)); }
    public void addAutoModule(AutoModule autoModule){ addSegmentType(AutoSegment.Type.AUTOMODULE, autoModule); }
    public void addConcurrentAutoModule(AutoModule autoModule){ addSegmentType(AutoSegment.Type.CONCURRENT_AUTOMODULE, autoModule);}
    public void addCancelAutoModules(){ addSegmentType(AutoSegment.Type.CANCEL_AUTOMODULE); addLastPose(); }
    public void addCustomSegment(AutoSegment<?,?> segment, double x, double y, double h){ customSegments.add(segment); segmentTypes.add(AutoSegment.Type.CUSTOM); poses.add(new Pose(x, y, h)); }

    private void addStationarySegment(ReturnCodeSeg<Generator> generator){ addSegment(config.getSetpointSegment().getReactorReference(), generator); }

    public void addScale(double scale){ movementScales.set(poses.size()-1, scale); }
    public void addAccuracy(double scale){ accuracyScales.set(poses.size()-1, scale); }

    public void addScaledSetpoint(double scale, double x, double y, double h){ addScale(scale); addSetpoint(x, y, h);}
    public void addScaledWaypoint(double scale, double x, double y, double h){ addScale(scale); addWaypoint(x, y, h);}

    public void addAccuracySetpoint(double acc, double x, double y, double h){ addAccuracy(acc); addSetpoint(x, y, h);}
    public void addAccuracyScaledSetpoint(double acc, double scale, double x, double y, double h){ addAccuracy(acc); addScaledSetpoint(scale, x, y, h);}

    public void addCustomCode(CodeSeg seg){ addSegmentType(AutoSegment.Type.BREAKPOINT);  breakpoints.add(seg); addLastPose(); }
    public void addSynchronisedDecision(DecisionList decisionList){ addCustomCode(decisionList::check); }
    public void addBreakpoint(ReturnCodeSeg<Boolean> exit){ addCustomCode(() -> {
        if(exit.run()){
            Iterator.forAll(segments, AutoSegment::skip);
        }
    });}
    public void addBreakpointReturn(){ addCustomCode(() -> Iterator.forAll(segments, AutoSegment::reset));}

    private void createSegments(){
        Iterator.forAll(segmentTypes, type -> {
            switch (type){
                case PAUSE:
                    final double time = getCurrentPause(); addStationarySegment(() -> new PauseGenerator(time)); break;
                case SETPOINT:
                    addSegment(config.getSetpointSegment()); break;
                case WAYPOINT:
                    addSegment(config.getWaypointSegment()); break;
                case AUTOMODULE:
                    final AutoModule autoModule = getCurrentAutoModule(); addStationarySegment(() -> new AutoModuleGenerator(autoModule, false)); break;
                case CONCURRENT_AUTOMODULE:
                    final AutoModule autoModule2 = getCurrentAutoModule(); addStationarySegment(() -> new AutoModuleGenerator(autoModule2, true));  break;
                case CANCEL_AUTOMODULE:
                    addStationarySegment(() -> new AutoModuleGenerator(true)); break;
                case CUSTOM:
                    addSegment(getCurrentCustomSegment()); break;
                case BREAKPOINT:
                    final CodeSeg code = getCurrentBreakpoint(); addStationarySegment(() -> new BreakpointGenerator(code)); break;
            }
            segmentIndex++;
        });
    }

    private AutoModule getCurrentAutoModule(){ AutoModule autoModule = autoModules.get(autoModuleIndex); autoModuleIndex++; return autoModule; }
    private double getCurrentPause(){ double time = pauses.get(pauseIndex); pauseIndex++; return time; }
    private AutoSegment<?,?> getCurrentCustomSegment(){AutoSegment<?,?> segment = customSegments.get(customSegmentIndex); customSegmentIndex++; return segment; }
    private CodeSeg getCurrentBreakpoint(){ CodeSeg breakpoint = breakpoints.get(breakpointIndex); breakpointIndex++; return breakpoint; }

    private void addSegment(AutoSegment<?, ?> segment){ addSegment(segment.getReactorReference(), segment.getGeneratorReference()); }
    private <R extends Reactor, G extends Generator> void addSegment(@NonNull ReturnCodeSeg<R> reactor, @NonNull ReturnCodeSeg<G> generator){
        fault.check("Auto Config Not Set", Expectation.EXPECTED, Magnitude.MODERATE, config == null, false);
        AutoSegment<?,?> autoSegment = new AutoSegment<>(reactor, generator);
        final Pose lastPose = poses.get(segmentIndex-1); final Pose currentPose = poses.get(segmentIndex);
        autoSegment.setGeneratorFunction(gen -> gen.addSegment(lastPose, currentPose));
        final double scale = movementScales.get(segmentIndex-1);
        final double accuracy = accuracyScales.get(segmentIndex-1);
        autoSegment.setReactorFunction(rea -> {rea.scale(scale); rea.scaleAccuracy(accuracy);});
        segments.add(autoSegment);
    }

    public void addLastPose(){ poses.add(poses.get(poses.size()-1).getCopy()); }

    public ArrayList<Pose> getPoses(){ return poses; }
    public ArrayList<AutoSegment.Type> getSegmentTypes(){ return segmentTypes; }
    public CoordinatePlane getAutoPlane(){ return autoPlane; }

    @Override
    public void stopAuto() {
        if(scanning && !haltCameraAfterInit){ camera.halt(); }
    }

    public void reset(){
        config = null;
        autoPlane = new CoordinatePlane();
        poses = new ArrayList<>();
        segmentTypes = new ArrayList<>();
        segments = new ArrayList<>();
        pauses = new ArrayList<>();
        autoModules = new ArrayList<>();
        movementScales = new ArrayList<>();
        accuracyScales = new ArrayList<>();
        customSegments = new ArrayList<>();
        breakpoints = new ArrayList<>();
        scanning = false;
        haltCameraAfterInit = true;
        caseScanner = null;
        scannerAfterInit = null;
        caseDetected = Case.FIRST;
        segmentIndex = 1;
        pauseIndex = 0;
        autoModuleIndex = 0;
        customSegmentIndex = 0;
        breakpointIndex = 0;
        startPose = new Pose();
        poses.add(new Pose()); movementScales.addAll(Collections.nCopies(100,1.0)); accuracyScales.addAll(Collections.nCopies(100,1.0));
    }
}
