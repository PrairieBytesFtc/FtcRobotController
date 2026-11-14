package org.firstinspires.ftc.teamcode;

import static java.lang.Math.abs;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Disabled
public class PBAutoClass extends LinearOpMode {

   // public class myLinearOpMode extends LinearOpMode{}
    //myLinearOpMode.
    // Declare drive motors
    public DcMotorEx frontLeft = null;
    public DcMotorEx frontRight = null;
    public DcMotorEx backLeft = null;
    public DcMotorEx backRight = null;
    // Flywheel
    public DcMotorEx flywheel = null;
    // Drive encoder variables
    double frontLeftCm = 0;
    double frontRightCm = 0;
    double backLeftCm = 0;
    double backRightCm = 0;
    // Flywheel PIDF variables
    double curTargetVelocity = 0;
    static final double P = 170;
    static final double F = 12.99;
    final double lowVelocity = 1700;
    final double highVelocity = 2000;
    // Declare launching servo + related variables
    public Servo launcher = null;
    double launcherPos = 0.75;
    boolean flicking = false;
    boolean launching = false;
    // RBT + Ramp declaration
    public DcMotor RBT = null;
    public CRServo ramp11 = null;
    public CRServo ramp12 = null;
    double intakePower = 0;
    // LED declarations
    public LED LED0 = null;
    public LED LED1 = null;
    public LED LED2 = null;
    public LED LED3 = null;
    // Declare Elapsed Time variables
    public ElapsedTime runtime = new ElapsedTime();
    public ElapsedTime launchTime = new ElapsedTime();
    public ElapsedTime flickTimer = new ElapsedTime();
    // Cm to ticks calculations
    static final double COUNTS_PER_MOTOR_REV = 753.2;
    static final double WHEEL_DIAMETER_CM = 14;
    static final double COUNTS_PER_CM = COUNTS_PER_MOTOR_REV / (WHEEL_DIAMETER_CM * 3.1415);
    static final double MOTOR_POWER = 0.6;

    //***************************
    //
    //***************************
    //public void loop () {
    //}
    //*****************************
    //
    //*****************************

    //public void init() {
    //}

    //*****************************
    //
    //*****************************

    //public void init_loop() {
    //telemetry.addData("Status: ", "Initialized");
    //telemetry.update();
    //}

    //********************************
    //
    //********************************
    @Override
    public void runOpMode(){

    }

    public void runAuto(int autoMode){
        switch (autoMode) {
            case 0:
               // telemetry.addLine("Auto Mode 0");
               // telemetry.update();
                LED0.off();
                LED1.off();
                break;
            case 1:
                break;
            case 2:
                break;
        }


    }

    public void autoModeOne() {

        // Back up to fire
        frontLeftCm = -120;
        frontRightCm = -120;
        backLeftCm = -120;
        backRightCm = -120;
        encoderTargetSet();
        WaitForEncoderDriveToBeDone();

        // Launch preloads
        launching = true;
        launchTime.reset();
        launchSequence();


    }

    public void InitRobot(){


    }

    public void encoderTargetSet() {
        frontLeft.setTargetPosition((int) (frontLeftCm * COUNTS_PER_CM));
        frontRight.setTargetPosition((int) (frontRightCm * COUNTS_PER_CM));
        backLeft.setTargetPosition((int) (backLeftCm * COUNTS_PER_CM));
        backRight.setTargetPosition((int) (backRightCm * COUNTS_PER_CM));
    }

    public void WaitForEncoderDriveToBeDone() {
        int i = 0;
        telemetry.addLine("Running to position");
        telemetry.update();

        while (backLeft.isBusy() || frontLeft.isBusy() || frontRight.isBusy() || backRight.isBusy()) {
            i++;
        }
        // You can add a telemetry command here to see the current position
        telemetry.addLine("Done running to position");
        telemetry.update();
    }

    public void flickLauncher() { // Be sure to reset the launchTime variable when this is run
        if (flickTimer.milliseconds() > 250){
            launcherPos = 0.75;
            flicking = false;
        } else launcherPos = 0.55;
    }

    public void launchSequence() {
        while (launching) {
            if (launchTime.seconds() > 4.25) {
                curTargetVelocity = 0;
                intakePower = 0;
                launching = false;
            } else if (launchTime.seconds() > 4) {
                flicking = true;
                flickTimer.reset();
            } else if (launchTime.seconds() > 2.6) {
                intakePower = 0.67;
            } else if (launchTime.seconds() > 2.5) {
                flicking = true;
                flickTimer.reset();
            } else if (launchTime.seconds() > 0.85) {
                intakePower = 0;
            } else if (launchTime.seconds() > 0.75) {
                flicking = true;
                flickTimer.reset();
            } else if (launchTime.seconds() > 0) {
                curTargetVelocity = lowVelocity;
            }

            if (flicking) flickLauncher();

            // Assign calculated values to hardware
            // Intake
            RBT.setPower(intakePower);
            ramp11.setPower(intakePower);
            ramp12.setPower(-intakePower);
            // Flywheel
            flywheel.setVelocity(curTargetVelocity);
            // Launcher servo
            launcher.setPosition(launcherPos);

            double curVelocity = flywheel.getVelocity();
            double error = curTargetVelocity - curVelocity;

            if (abs(error) > 20) {
                LED0.off();
                LED1.on();
            } else {
                LED0.on();
                LED1.off();
            }

        }
    }

}
