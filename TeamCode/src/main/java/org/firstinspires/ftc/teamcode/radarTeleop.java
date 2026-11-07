package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "Radar Teleop")
public class radarTeleop extends LinearOpMode {

    // Drive motor declaration
    private DcMotor frontRight = null;
    private DcMotor frontLeft = null;
    private DcMotor backRight = null;
    private DcMotor backLeft = null;
    // Flywheel declaration
    private DcMotorEx flywheel = null;
    // Launch servo
    private Servo launchServo = null;
    //Intake
    private DcMotor RBT = null;
    private CRServo ramp11 = null;
    private CRServo ramp12 = null;
    double IntakePower = 0;
    private String strintakestatus = "Off";
    double curTargetVelocity = 0;
   // final double lowVelocity = 1650;
   //final double highVelocity = 2300;
    final double lowVelocity = 600;
    final double highVelocity = 1200;

    boolean flicking = false;
    double launcherPos = 0.75;

    //private ElapsedTime wait = new ElapsedTime();
    private final ElapsedTime flickTimer = new ElapsedTime();
    // Flicks launcher servo up and down
    private void flickLauncher() { // Be sure to reset the launchTime variable when this is run
        if (flickTimer.milliseconds() > 300){
            launcherPos = 0.75;
            flicking = false;
        } else launcherPos = 0.55;
    }


    @Override
        public void runOpMode() {

        // Drive motor hardware map
        frontRight = hardwareMap.get(DcMotor.class, "rightfront");
        frontLeft = hardwareMap.get(DcMotor.class, "leftfront");
        backRight = hardwareMap.get(DcMotor.class, "rightrear");
        backLeft = hardwareMap.get(DcMotor.class, "leftrear");

        // Drive motor configuration
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Flywheel
        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //servo
        launchServo = hardwareMap.get(Servo.class, "swoop_servo");

        double drivemultiplier = 0.6;

        telemetry.addLine("Initialized");
        telemetry.update();

        waitForStart();


        launchServo.setPosition(0.75);


        //RBT
        RBT = hardwareMap.get(DcMotor.class, "RBT");
        ramp11 = hardwareMap.get(CRServo.class, "ramp11");
        ramp12 = hardwareMap.get(CRServo.class, "ramp12");
        RBT.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RBT.setPower(IntakePower);
        ramp11.setPower(IntakePower);
        ramp12.setPower(-IntakePower);



        while (opModeIsActive()) {
            double max;

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            double axial = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
           // double frontLeftPower = axial + lateral + yaw;
           // double frontRightPower = axial - lateral - yaw;
            //double backLeftPower = axial - lateral + yaw;
           // double backRightPower = axial + lateral - yaw;
            // Set up a variable for each drive wheel to save the power level for telemetry.
            double frontLeftPower  = drivemultiplier * (axial + lateral + yaw);
            double frontRightPower = drivemultiplier * (axial - lateral - yaw);
            double backLeftPower   = drivemultiplier * (axial - lateral + yaw);
            double backRightPower  = drivemultiplier * (axial + lateral - yaw);

            // Normalize the values so no wheel power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(backRightPower));

            if (max > 1.0) {
                frontLeftPower /= max;
                frontRightPower /= max;
                backLeftPower /= max;
                backRightPower /= max;
            }

            frontRight.setPower(frontRightPower);
            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            backRight.setPower(backRightPower);


            if (gamepad2.aWasPressed()){
                curTargetVelocity = lowVelocity;
            }
            if (gamepad2.bWasPressed()){
                curTargetVelocity = 0;
            }
            if (gamepad2.xWasPressed()){
                curTargetVelocity = highVelocity;
            }
           if (gamepad2.leftBumperWasPressed()) {
                curTargetVelocity = curTargetVelocity+50;
            }
            if (gamepad2.rightBumperWasPressed()){
                curTargetVelocity = curTargetVelocity-50;
           }

            if (gamepad2.dpadRightWasPressed()) {
                flywheel.setVelocity(1500);
            }
            if (gamepad2.dpadLeftWasPressed()) {
                flywheel.setVelocity(0);
            }

            if (gamepad2.dpadUpWasPressed()) {
                flicking = true;
                flickTimer.reset();
            }
            if (gamepad2.dpadDownWasPressed()) {
                launcherPos = 0.75;
            }
            if (gamepad2.rightTriggerWasPressed()) {
                IntakePower = 0;
                strintakestatus = "off";
            }
            if (gamepad2.leftTriggerWasPressed()) {
                IntakePower = 0.67;
                strintakestatus = "in";
            }
            if (gamepad2.right_trigger > 0 && gamepad2.left_trigger > 0) {
                IntakePower = -0.5;
                strintakestatus = "out";
            }
            // Turbo Mode™
            if (gamepad1.left_bumper) {
                drivemultiplier = 1;
            } else if (gamepad1.right_bumper) {
                drivemultiplier = 0.15;
            } else {
                drivemultiplier = 0.6;
            }
            // Run timed programs
            if (flicking) flickLauncher();

            RBT.setPower(IntakePower);
            ramp11.setPower(IntakePower);
            ramp12.setPower(-IntakePower);

            launchServo.setPosition(launcherPos);

            flywheel.setVelocity(curTargetVelocity);

            telemetry.addData("Intake Status", strintakestatus);
            telemetry.addLine("Running");
            telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
            telemetry.update();
        }
    }
}