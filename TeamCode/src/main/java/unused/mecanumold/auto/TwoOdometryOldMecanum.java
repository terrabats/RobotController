package unused.mecanumold.auto;

import geometry.position.Pose;
import geometry.position.Vector;
import robotparts.electronics.ElectronicType;
import robotparts.electronics.input.IEncoder;
import unused.tankold.TankOdometry;

import static robot.RobotFramework.odometryThread;
// Remove dependence on tank odometry
@Deprecated
public class TwoOdometryOldMecanum extends TankOdometry {

    private volatile IEncoder horizontalEncoder;
    private volatile IEncoder verticalEncoder;

    private volatile double startHorz = 0;
    private volatile double startVert = 0;


    private volatile double lastHorizontalEncoderPos;
    private volatile double lastVerticalEncoderPos;

    private volatile Vector positionOdometryCenter = new Vector(0,0);
    private volatile Vector positionRobotCenter = new Vector(0,0);
    private volatile Vector localOdometryCenterOffset;
    private volatile double heading = 0;

    public TwoOdometryOldMecanum() {
        super(0.0);
        localOdometryCenterOffset = new Vector(0.0, 12.6);
    }

    public synchronized void reset(){
        resetOnce();
        resetOnce();
    }

    public synchronized void resetOnce(){
        positionOdometryCenter = new Vector(0,0);
        positionRobotCenter = new Vector(0,0);
        localOdometryCenterOffset = new Vector(0.0, 12.6);
        horizontalEncoder.reset();
        verticalEncoder.reset();
        startHorz = getHorizontalEncoderPositionRaw();
        startVert = getVerticalEncoderPositionRaw();
        lastHorizontalEncoderPos = 0;
        lastVerticalEncoderPos = 0;
        heading = 0;
        gyro.reset();
    }


    @Override
    public void init() {
        horizontalEncoder = create("blEnc", ElectronicType.IENCODER_NORMAL);
        verticalEncoder = create("flEnc", ElectronicType.IENCODER_NORMAL);
        horizontalEncoder.reset();
        verticalEncoder.reset();
        lastHorizontalEncoderPos = horizontalEncoder.getPos();
        lastVerticalEncoderPos = verticalEncoder.getPos();
        odometryThread.setExecutionCode(this::update);
    }

    public double getHorizontalEncoderPosition(){
        return getHorizontalEncoderPositionRaw()-startHorz;
    }

    public double getVerticalEncoderPosition(){
        return getVerticalEncoderPositionRaw()-startVert;
    }


    public double getHorizontalEncoderPositionRaw(){
        return horizontalEncoder.getPos();
    }

    public double getVerticalEncoderPositionRaw(){
        return -verticalEncoder.getPos();
    }

    private double getLocalHorizontalDelta(){
        double delta = getHorizontalEncoderPosition() - lastHorizontalEncoderPos;
        lastHorizontalEncoderPos = getHorizontalEncoderPosition();
        return ticksToCm(delta);
    }

    private double getLocalVerticalDelta(){
        double delta = getVerticalEncoderPosition() - lastVerticalEncoderPos;
        lastVerticalEncoderPos = getVerticalEncoderPosition();
        return ticksToCm(delta);
    }

    public double ticksToCm(double ticks) {
        return ticks/8192 * 3.5 * Math.PI;
    }

    private void updateHeading(){
        heading = gyro.getHeading();
    }

    public void updatePosition(){
        verticalEncoder.updateNormal(); horizontalEncoder.updateNormal(); gyro.update();
        Vector localDelta = new Vector(getLocalHorizontalDelta(), getLocalVerticalDelta());
        Vector globalDelta = localDelta.getRotated(heading);
        positionOdometryCenter.add(globalDelta);
        Vector globalOdometryCenterOffset = localOdometryCenterOffset.getRotated(heading);
        positionRobotCenter = positionOdometryCenter.getAdded(globalOdometryCenterOffset.getSubtracted(localOdometryCenterOffset));
    }

    public void update(){
        updateHeading();
        updatePosition();
    }

    public double getCurX(){
        return positionRobotCenter.getX();
    }
    public double getCurY(){
        return positionRobotCenter.getY();
    }
    public double getHeading(){ return heading; }


    public Pose getCurrentPose(){ return new Pose(getCurX(), getCurY(), getHeading()); }

    @Override
    public double[] getPose(){
        return new double[]{getCurX(), getCurY(), getCurThetaRad()};
    }

}