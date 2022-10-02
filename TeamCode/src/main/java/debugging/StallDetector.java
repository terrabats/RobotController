package debugging;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import robotparts.electronics.input.IEncoder;
import util.template.Precision;
import autoutil.profilers.Profiler;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class StallDetector implements Precision {

    private final IEncoder encoder;
    private double minSpeed;
    private double maxCurrent;

    /**
     * @param minSpeedThresh (deg/s) ~10
     * @param maxCurrentThresh (amps) ~8
     */
    public StallDetector(IEncoder e, double minSpeedThresh, double maxCurrentThresh){
        encoder = e;
        minSpeed = minSpeedThresh;
        maxCurrent = maxCurrentThresh;
        resetPrecisionTimers();
    }

    public void setCustomThresholds(double minSpeedThresh, double maxCurrentThresh){
        minSpeed = minSpeedThresh;
        maxCurrent = maxCurrentThresh;
    }

    public double getMotorSpeed(){ return abs(encoder.getAngularVelocity()); }

    public double getMotorCurrent(){
        return abs(encoder.getCurrent());
    }

    public boolean isMotorVelocityLow(){
        return getMotorSpeed() < minSpeed;
    }

    public boolean isMotorCurrentHigh(){
        return getMotorCurrent() > maxCurrent;
    }

    public boolean isStalling() {
        return outputTrueForTime(isMotorVelocityLow()&&isMotorCurrentHigh(), 1);
    }
}
