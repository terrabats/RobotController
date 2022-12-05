package robotparts.sensors.odometry;

import geometry.framework.Point;
import geometry.position.Pose;
import geometry.position.Vector;
import math.linearalgebra.Matrix2D;
import math.linearalgebra.Matrix3D;
import math.linearalgebra.Vector3D;
import robotparts.electronics.ElectronicType;
import robotparts.electronics.input.IEncoder;
import util.template.Precision;

import static java.lang.Math.*;

public class ThreeOdometry extends TwoOdometry {
    private IEncoder enc3;
    public final double width = 22.8507;
    public double angleLeft = toRadians(0);
    public double angleRight = toRadians(0); //2
    public final double scaleCenter = 1.004;
    public int mode = 0;
    public final Point odometryCenter = new Point();
    private final Vector leftOdometryCenterToRobotCenter = new Vector(11.5, 13.0);

    @Override
    protected void createEncoders() {
        super.createEncoders();
        enc3 = create("brEnc", ElectronicType.IENCODER_NORMAL);
        addEncoders(enc3);
        enc3.invert();
    }

    @Override
    protected void update() {

        double dyl = 0, dyr = 0, dx = 0, dh = 0;

        if(mode == 0) {
            dyl = (enc1.getDeltaPosition() - dx*sin(angleLeft));
        }else if(mode == 1){
            dyl = enc2.getDeltaPosition()*scaleCenter;
        }else if(mode == 2){
            dyl = (enc3.getDeltaPosition() - dx*sin(angleRight));
        }
//
//        dx = scaleCenter*enc2.getDeltaPosition();
//        dyl = (enc1.getDeltaPosition() - dx*sin(angleLeft));
//        dyr = (enc3.getDeltaPosition() - dx*sin(angleRight));
//        dh = (dyr-dyl)/width;

        Vector localDelta = new Vector(dx, dyl);
//        setHeading(gyro.getHeading());
//        updateCurrentHeading(dh);
        odometryCenter.translate(toGlobalFrame(localDelta));
        Vector globalOdometryCenterToRobotCenter = toGlobalFrame(leftOdometryCenterToRobotCenter).getSubtracted(leftOdometryCenterToRobotCenter);
        setCurrentPose(odometryCenter.getAdded(globalOdometryCenterToRobotCenter.getPoint()));
    }

    @Override
    protected void resetObjects() { odometryCenter.set(new Point()); }
}
