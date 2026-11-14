package org.firstinspires.ftc.teamcode; // Must be in the teamcode package

import static java.lang.Math.abs;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;


public class PB_robot_class1_4 {


    // Declare variables
    private LinearOpMode opMode;
    // Drive motors
    public DcMotorEx frontLeft = null;
    public DcMotorEx backLeft = null;
    public DcMotorEx frontRight = null;
    public DcMotorEx backRight = null;
    // Intake
    public DcMotor RBT = null;
    public CRServo ramp11 = null;
    public CRServo ramp12 = null;
    double intakePower = 0;
    double intakeServo = 0;
    // Launching mechanism
    public DcMotorEx flywheel = null;
    double curTargetVelocity = 0;
    static final double P = 170;
    static final double F = 12.99;
    final double lowVelocity = 1200;
    final double highVelocity = 1950;
    double error = 0;
    public Servo launcher = null;
    double launcherPos = 0.75;
    boolean flicking = false;
    boolean launching = false;
    int launchState = 0;
    // Vision processing variables, not used for anything currently
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    // LED variables
    private LED LED0 = null;
    private LED LED1 = null;
    private LED LED2 = null;
    private LED LED3 = null;
    // org.firstinspires.ftc.teamcode.IMU
    private IMU imu = null;
    // Elapsed time variables
    private ElapsedTime runtime = new ElapsedTime();
    private ElapsedTime wait = new ElapsedTime();
    private ElapsedTime flickTimer = new ElapsedTime();
    // Color sensor and related variables
    private ColorSensor color = null;
    private ColorSensor intake_color = null;
    boolean artifactReady = false;
    // Encoder ticks to CM calculation variables
    static final double COUNTS_PER_MOTOR_REV = 753.2;
    static final double WHEEL_DIAMETER_CM = 10.4;
    static final double COUNTS_PER_CM = COUNTS_PER_MOTOR_REV / (WHEEL_DIAMETER_CM * 3.1415);
    static final double DEFAULT_MOTOR_POWER = 0.6;
    double MOTOR_POWER = DEFAULT_MOTOR_POWER;
    double frontLeftCm = 0;
    double frontRightCm = 0;
    double backLeftCm = 0;
    double backRightCm = 0;
    int frontLeftTarget = (int) (frontLeftCm * COUNTS_PER_CM);
    int frontRightTarget = (int) (frontRightCm * COUNTS_PER_CM);
    int backLeftTarget = (int) (backLeftCm * COUNTS_PER_CM);
    int backRightTarget = (int) (backRightCm * COUNTS_PER_CM);
    // Misc
    int state = 0;
    boolean complete = false;


    // Constructor that accepts the hardwareMap
    // and the OpMode that is creating this class
    public PB_robot_class1_4(HardwareMap hwMap, LinearOpMode callingOpMode) {

        this.opMode = callingOpMode;

        // Can also add methods for movement, etc.
        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        // Drive motors
        frontLeft = hwMap.get(DcMotorEx.class, "leftfront");
        backLeft = hwMap.get(DcMotorEx.class, "leftrear");
        frontRight = hwMap.get(DcMotorEx.class, "rightfront");
        backRight = hwMap.get(DcMotorEx.class, "rightrear");
        // Intake motor and servos
        RBT = hwMap.get(DcMotor.class, "RBT");
        ramp11 = hwMap.get(CRServo.class, "ramp11");
        ramp12 = hwMap.get(CRServo.class, "ramp12");
        // Flywheel motor
        flywheel = hwMap.get(DcMotorEx.class, "flywheel");
        // Launcher servo
        launcher = hwMap.get(Servo.class, "swoop_servo");
        color = hwMap.get(ColorSensor.class, "color_sensor");
        intake_color = hwMap.get(ColorSensor.class, "intake_color");
        // LEDs
        LED0 = hwMap.get(LED.class, "Led0");
        LED1 = hwMap.get(LED.class, "Led1");
        LED2 = hwMap.get(LED.class, "Led2");
        LED3 = hwMap.get(LED.class, "Led3");

        // Distance sensor, currently unused
        //rangesensor = hardwareMap.get(Rev2mDistanceSensor.class, "distance0");

        // Webcam and april tag processor
        WebcamName webcamName = hwMap.get(WebcamName.class, "webcam");
        aprilTagProcessor = AprilTagProcessor.easyCreateWithDefaults();
        visionPortal = VisionPortal.easyCreateWithDefaults(webcamName, aprilTagProcessor);
        LED0 = hwMap.get(LED.class, "Led0");
        LED1 = hwMap.get(LED.class, "Led1");
        LED2 = hwMap.get(LED.class, "Led2");
        LED3 = hwMap.get(LED.class, "Led3");
        imu = hwMap.get(IMU.class, "imu");

    }

    // Initialize the class var's
    // Initialization method
    public void init() {

        // Set motor directions and behaviors
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RBT.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(P, 0, 0, F);
        flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);

        // init the org.firstinspires.ftc.teamcode.IMU
        // Initialize org.firstinspires.ftc.teamcode.IMU with orthogonal mounting (adjust logo and USB directions based on your robot's mounting)
        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
                )
        );
        imu.initialize(parameters);

        // Reset yaw to set current heading as 0
        imu.resetYaw();

        // Initiate LEDs
        LED0.off();
        LED1.off();
        LED2.off();
        LED3.off();

    }

    // Run the inputted Auto routine
    public void RunAutoMode(int runMode) {
        switch (runMode) {
            case 1:
                autoNearRedSix();
                break;
            case 2:
                autoNearBlueSix();
                break;
            case 3:
                autoFarPreload();
                break;
            case 4:
                autoFarRedSix();
                break;
            case 5:
                autoFarBlueSix();
                break;
            case 6:
                autoTest1();
                break;

        }
    }

    // Auto mode functions
    public void autoNearRedSix() {
        beforeMainAuto();
        while (this.opMode.opModeIsActive() && !complete) {
            switch (state) {
                case 0: // Move to firing position, turn on flywheel
                    curTargetVelocity = lowVelocity;
                    flywheel.setVelocity(curTargetVelocity);
                //    MOTOR_POWER = 0.4; Not needed? Was to slow down motors so that they didn't overshoot so much
                    setTargetPosition(-115,-115,-115,-115);
                    state = 1;
                    break;
                case 1: // Move to firing position
                    waitForEncoders();
                    break;
                case 2: // Launch preloads
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(lowVelocity);
                    break;
                case 3: // Turn right
                //    MOTOR_POWER = DEFAULT_MOTOR_POWER; Not needed? see above
                    setTargetPosition(23,-23,23,-23);
                    state = 4;
                    break;
                case 4: // Turn right
                    waitForEncoders();
                    break;
                case 5: // Strafe right
                    setTargetPosition(32,-32,-32,32);
                    state = 6;
                    break;
                case 6: // Strafe right
                    waitForEncoders();
                    break;
                case 7: // Drive forward and collect artifacts
                    MOTOR_POWER = 0.2;
                    setTargetPosition(85,85,85,85);
                    // Intake
                    intakePower = 1;
                    RBT.setPower(intakePower);
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state = 8;
                    break;
                case 8: // Drive forward an collect artifacts
                    waitForEncoders();
                    break;
                case 9: // Back up; turn off intake
                    MOTOR_POWER = DEFAULT_MOTOR_POWER;
                    setTargetPosition(-85,-85,-85,-85);
                    // RBT off
                    intakePower = 0;
                    RBT.setPower(intakePower);
                    state = 10;
                    break;
                case 10: // Back up
                    waitForEncoders();
                    break;
                case 11: // Strafe left
                    setTargetPosition(-50,50,50,-50);
                    // Helicopter wheels off
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state = 12;
                    break;
                case 12: // Strafe left
                    waitForEncoders();
                    break;
                case 13: // Turn left
                    setTargetPosition(-22,22,-22,22);
                    state = 14;
                    break;
                case 14: // Turn left
                    waitForEncoders();
                    break;
                case 15: // Launch artifacts
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(lowVelocity);
                    break;
                case 16:
                    complete = true;
                    break;
            }

            this.opMode.telemetry.addData("Current State", state);
            this.opMode.telemetry.addData("Runtime", runtime.seconds());
            this.opMode.telemetry.update();

        }

    }

    public void autoTest1() {
        beforeMainAuto();
        while (this.opMode.opModeIsActive() && !complete) {
            switch (state) {
                case 0: // Move to firing position, turn on flywheel
                    curTargetVelocity = lowVelocity;
                    //flywheel.setVelocity(curTargetVelocity);
                    //    MOTOR_POWER = 0.4; Not needed? Was to slow down motors so that they didn't overshoot so much
                    setTargetPosition(20,20,20,20);
                    state = 1;
                    break;
                case 1: // Move to firing position
                    waitForEncoders();
                    break;
                case 2: //turn on flywheel
                    launchState = 1;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(lowVelocity);

                    state = 3;
                    break;
                case 3:
                    complete = true;
                    break;
                case 4: // turn left
                    setTargetPosition(20,-20,20,-20);
                    state = 5;
                    break;
                case 5: //turn left
                    waitForEncoders();
                    break;
                case 6: //


            }

           //this.opMode.telemetry.addData("Current State", state);
           // this.opMode.telemetry.addData("Runtime", runtime.seconds());
           // this.opMode.telemetry.update();

        }

    }


    public void autoNearBlueSix() {
        beforeMainAuto();
        while (this.opMode.opModeIsActive() && !complete) {
            switch (state) {
                case 0: // Move to firing position, turn on flywheel
                    curTargetVelocity = lowVelocity;
                    flywheel.setVelocity(curTargetVelocity);
                    setTargetPosition(-105, -105, -105, -105);
                    state = 1;
                    break;
                case 1: // Move to firing position
                    waitForEncoders();
                    break;
                case 2: // Launch preloads
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(lowVelocity);
                    break;
                case 3: // Turn right
                    setTargetPosition(-23,23,-23,23);
                    state = 4;
                    break;
                case 4: // Turn right
                    waitForEncoders();
                    break;
                case 5: // Strafe right
                    setTargetPosition(-47,47,47,-47);
                    state = 6;
                    break;
                case 6: // Strafe right
                    waitForEncoders();
                    break;
                case 7: // Drive forward an collect artifacts
                    MOTOR_POWER = 0.2;
                    setTargetPosition(75,75,75,75);
                    // Intake on
                    intakePower = 1;
                    RBT.setPower(intakePower);
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state = 8;
                    break;
                case 8: // Drive forward an collect artifacts
                    waitForEncoders();
                    break;
                case 9: // Back up; turn off intake
                    MOTOR_POWER = DEFAULT_MOTOR_POWER;
                    setTargetPosition(-75,-75,-75,-75);
                    // RBT off
                    intakePower = 0;
                    RBT.setPower(intakePower);
                    state = 10;
                    break;
                case 10: // Back up
                    waitForEncoders();
                    break;
                case 11: // Strafe left
                    setTargetPosition(65,-65,-65,65);
                    // Helicopter wheels off
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state = 12;
                    break;
                case 12: // Strafe left
                    waitForEncoders();
                    break;
                case 13: // Turn left
                    setTargetPosition(20, -20, 20, -20);
                    state = 14;
                    break;
                case 14: // Turn left
                    waitForEncoders();
                    break;
                case 15: // Launch artifacts
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(lowVelocity);
                    break;
                case 16:
                    complete = true;
                    break;
            }

            this.opMode.telemetry.addData("Current State", state);
            this.opMode.telemetry.addData("Runtime", runtime.seconds());
            this.opMode.telemetry.update();

        }
    }

    public void autoFarPreload() {
        beforeMainAuto();
        while(this.opMode.opModeIsActive() && !complete) {

            if (runtime.seconds() > 4.25) {
                curTargetVelocity = 0;
                intakePower = 0;
                setTargetPosition(50,50,50,50);
                waitForEncoders();
                complete = true;
            } else if (runtime.seconds() > 4) {
                flicking = true;
                flickTimer.reset();
            } else if (runtime.seconds() > 2.6) {
                intakePower = 0.67;
            } else if (runtime.seconds() > 2.5) {
                flicking = true;
                flickTimer.reset();
            } else if (runtime.seconds() > 0.85) {
                intakePower = 0.67;
            } else if (runtime.seconds() > 0.75) {
                flicking = true;
                flickTimer.reset();
            } else if (runtime.seconds() > 0) {
                curTargetVelocity = highVelocity;
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

            if (abs(error) > 10) {
                LED0.off();
                LED1.on();
            } else {
                LED0.on();
                LED1.off();
            }

            // Telemetry
            // Elapsed time since program started
            this.opMode.telemetry.addData("Runtime", runtime.seconds());
            // Current drive motor encoder positions
            this.opMode.telemetry.addData("Encoder Pos Front Left/Right", " %7d :%7d", frontLeft.getCurrentPosition(), frontRight.getCurrentPosition());
            this.opMode.telemetry.addData("Encoder Pos Back Left/Right", " %7d :%7d", backLeft.getCurrentPosition(), backRight.getCurrentPosition());
            // Flywheel data
            this.opMode.telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
            this.opMode.telemetry.addData("Current Velocity", "%.2f", curVelocity);
            this.opMode.telemetry.addData("Error", "%.2f", error);
            // Call telemetry update
            this.opMode.telemetry.update();
        }
    }

    public void autoFarRedSix() {
        beforeMainAuto();
        while (this.opMode.opModeIsActive() && !complete) {
            switch (state) {
                case 0:
                    curTargetVelocity = highVelocity;
                    flywheel.setVelocity(curTargetVelocity);
                    setTargetPosition(7,5,7,5);
                    state = 2;
                    break;
                case 2:
                    waitForEncoders();
                    break;
                case 3:
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(highVelocity);
                    break;
                case 4:
                    setTargetPosition(63,63,63,63);
                    state++;
                    break;
                case 5:
                    waitForEncoders();
                    break;
                case 6:
                    setTargetPosition(45,-45,45,-45);
                    state++;
                    break;
                case 7:
                    waitForEncoders();
                    break;
                case 8:
                    MOTOR_POWER = 0.2;
                    setTargetPosition(50,50,50,50);
                    // Intake on
                    intakePower = 1;
                    RBT.setPower(intakePower);
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state++;
                    break;
                case 9:
                    waitForEncoders();
                    break;
                case 10:
                    MOTOR_POWER = DEFAULT_MOTOR_POWER;
                    setTargetPosition(-50,-50,-50,-50);
                    // RBT off
                    intakePower = 0;
                    RBT.setPower(intakePower);
                    state++;
                    break;
                case 11:
                    waitForEncoders();
                    break;
                case 12:
                    setTargetPosition(-44,44,-44,44);
                    // Helicopter wheels off
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state++;
                    break;
                case 13:
                    waitForEncoders();
                    break;
                case 14:
                    setTargetPosition(-65,-65,-65,-65);
                    state++;
                    break;
                case 15:
                    waitForEncoders();
                    break;
                case 16:
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(highVelocity);
                    break;
                case 17:
                    setTargetPosition(30,30,30,30);
                    state++;
                    break;
                case 18:
                    waitForEncoders();
                    break;
                case 19:
                    complete = true;
                    break;
            }

            this.opMode.telemetry.addData("Current State", state);
            this.opMode.telemetry.addData("Runtime", runtime.seconds());
            this.opMode.telemetry.update();

        }
    }

    public void autoFarBlueSix() {
        beforeMainAuto();
        while (this.opMode.opModeIsActive() && !complete) {
            switch (state) {
                case 0:
                    curTargetVelocity = highVelocity;
                    flywheel.setVelocity(curTargetVelocity);
                    setTargetPosition(5,7,5,7);
                    state = 2;
                    break;
                case 2:
                    waitForEncoders();
                    break;
                case 3:
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(highVelocity);
                    break;
                case 4:
                    setTargetPosition(63,63,63,63);
                    state++;
                    break;
                case 5:
                    waitForEncoders();
                    break;
                case 6:
                    setTargetPosition(-44,44,-44,44);
                    state++;
                    break;
                case 7:
                    waitForEncoders();
                    break;
                case 8:
                    MOTOR_POWER = 0.2;
                    setTargetPosition(50,50,50,50);
                    // Intake
                    intakePower = 1;
                    RBT.setPower(intakePower);
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state++;
                    break;
                case 9:
                    waitForEncoders();
                    break;
                case 10:
                    MOTOR_POWER = DEFAULT_MOTOR_POWER;
                    setTargetPosition(-50,-50,-50,-50);
                    // Intake
                    intakePower = 0;
                    RBT.setPower(intakePower);
                    ramp11.setPower(intakePower);
                    ramp12.setPower(-intakePower);
                    state++;
                    break;
                case 11:
                    waitForEncoders();
                    break;
                case 12:
                    setTargetPosition(44,-44,44,-44);
                    state++;
                    break;
                case 13:
                    waitForEncoders();
                    break;
                case 14:
                    setTargetPosition(-65,-64,-65,-64);
                    state++;
                    break;
                case 15:
                    waitForEncoders();
                    break;
                case 16:
                    launchState = 0;
                    launching = true;
                    while (launching && this.opMode.opModeIsActive()) launchSequence(highVelocity);
                    break;
                case 17:
                    setTargetPosition(30,30,30,30);
                    state++;
                    break;
                case 18:
                    waitForEncoders();
                    break;
                case 19:
                    complete = true;
                    break;
            }

            this.opMode.telemetry.addData("Current State", state);
            this.opMode.telemetry.addData("Runtime", runtime.seconds());
            this.opMode.telemetry.update();

        }
    }

    // Functions to reduce need for repetitive code
    private void resetEncoders() {
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    private void setTargetPosition(double frontleftCM, double frontrightCM, double backleftCM, double backrightCM) {
        // Calculation variables
        frontLeftCm = frontleftCM;
        frontRightCm = frontrightCM;
        backLeftCm = backleftCM;
        backRightCm = backrightCM;
        frontLeftTarget = (int) (frontLeftCm * COUNTS_PER_CM);
        frontRightTarget = (int) (frontRightCm * COUNTS_PER_CM);
        backLeftTarget = (int) (backLeftCm * COUNTS_PER_CM);
        backRightTarget = (int) (backRightCm * COUNTS_PER_CM);
        // Set drive motor encoder target positions
        frontLeft.setTargetPosition(frontLeftTarget);
        frontRight.setTargetPosition(frontRightTarget);
        backLeft.setTargetPosition(backLeftTarget);
        backRight.setTargetPosition(backRightTarget);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setPower(MOTOR_POWER);
        frontRight.setPower(MOTOR_POWER);
        backLeft.setPower(MOTOR_POWER);
        backRight.setPower(MOTOR_POWER);
    }

    private void waitForEncoders() {
        while (this.opMode.opModeIsActive() && (frontLeft.isBusy() || frontRight.isBusy() || backRight.isBusy() || backLeft.isBusy())) {
            this.opMode.telemetry.addData("Current State", state);
            this.opMode.telemetry.update();
        }
        state++;
        resetEncoders();
    }

    /*
    private void farLaunchSequence() {
        switch (launchState) {
            case 0:
                curTargetVelocity = highVelocity;
                launchState++;
                break;
            case 1:
                this.opMode.sleep(1500);
                launchState++;
                break;
            case 2:
                flicking = true;
                flickTimer.reset();
                intakeServo = 1;
                launchState++;
                break;
            case 3:
                if (!flicking) launchState++;
                break;
            case 4:
                this.opMode.sleep(1000);
                launchState++;
                break;
            case 5:
                flicking = true;
                flickTimer.reset();
                intakeServo = 0;
                intakePower = 0.67;
                launchState++;
                break;
            case 6:
                if (!flicking) launchState++;
                break;
            case 7:
                this.opMode.sleep(1000);
                launchState++;
                break;
            case 8:
                flicking = true;
                flickTimer.reset();
                launchState++;
                break;
            case 9:
                if (!flicking) launchState++;
                break;
            case 10:
                this.opMode.sleep(250);
                launchState++;
                break;
            case 11:
                curTargetVelocity = 0;
                intakePower = 0;
                launching = false;
                launchState++;
                launcherPos = 0.75;
                state++;
                break;
        }

        if (flicking) flickLauncher();

        // Assign calculated values to hardware
        // Intake
        RBT.setPower(intakePower);
        ramp11.setPower(intakePower);
        ramp12.setPower(-intakePower);
        if (!(intakeServo == 0)) {
            ramp11.setPower(intakeServo);
            ramp12.setPower(-intakeServo);
        }
        // Flywheel
        flywheel.setVelocity(curTargetVelocity);
        // Launcher servo
        launcher.setPosition(launcherPos);

        double curVelocity = flywheel.getVelocity();
        double error = curTargetVelocity - curVelocity;

        if (abs(error) > 10) {
            LED0.off();
            LED1.on();
        } else {
            LED0.on();
            LED1.off();
        }

        // Telemetry
        // Current state
        this.opMode.telemetry.addData("Current State", state);
        // Elapsed time since program started
        this.opMode.telemetry.addData("Runtime", runtime.seconds());
        // Current drive motor encoder positions
        this.opMode.telemetry.addData("Encoder Pos Front Left/Right", " %7d :%7d", frontLeft.getCurrentPosition(), frontRight.getCurrentPosition());
        this.opMode.telemetry.addData("Encoder Pos Back Left/Right", " %7d :%7d", backLeft.getCurrentPosition(), backRight.getCurrentPosition());
        // Flywheel data
        this.opMode.telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
        this.opMode.telemetry.addData("Current Velocity", "%.2f", curVelocity);
        this.opMode.telemetry.addData("Error", "%.2f", error);
        // Call telemetry update
        this.opMode.telemetry.update();

    }

    private void nearLaunchSequence() {
        switch (launchState) {
            case 0:
                curTargetVelocity = lowVelocity;
                launchState++;
                break;
            case 1:
                this.opMode.sleep(1000);
                launchState++;
                break;
            case 2:
                flicking = true;
                flickTimer.reset();
                intakeServo = 1;
                launchState++;
                break;
            case 3:
                if (!flicking) launchState++;
                break;
            case 4:
                this.opMode.sleep(1000);
                launchState++;
                break;
            case 5:
                flicking = true;
                flickTimer.reset();
                intakeServo = 0;
                intakePower = 0.67;
                launchState++;
                break;
            case 6:
                if (!flicking) launchState++;
                break;
            case 7:
                this.opMode.sleep(1000);
                launchState++;
                break;
            case 8:
                flicking = true;
                flickTimer.reset();
                launchState++;
                break;
            case 9:
                if (!flicking) launchState++;
                break;
            case 10:
                this.opMode.sleep(250);
                launchState++;
                break;
            case 11:
                curTargetVelocity = 0;
                intakePower = 0;
                launching = false;
                launchState++;
                launcherPos = 0.75;
                state++;
                break;
        }

        if (flicking) flickLauncher();

        // Assign calculated values to hardware
        // Intake
        RBT.setPower(intakePower);
        ramp11.setPower(intakePower);
        ramp12.setPower(-intakePower);
        if (!(intakeServo == 0)) {
            ramp11.setPower(intakeServo);
            ramp12.setPower(-intakeServo);
        }
        // Flywheel
        flywheel.setVelocity(curTargetVelocity);
        // Launcher servo
        launcher.setPosition(launcherPos);

        double curVelocity = flywheel.getVelocity();
        double error = curTargetVelocity - curVelocity;

        if (abs(error) > 10) {
            LED0.off();
            LED1.on();
        } else {
            LED0.on();
            LED1.off();
        }

        // Telemetry
        // Current state
        this.opMode.telemetry.addData("Current State", state);
        // Elapsed time since program started
        this.opMode.telemetry.addData("Runtime", runtime.seconds());
        // Current drive motor encoder positions
        this.opMode.telemetry.addData("Encoder Pos Front Left/Right", " %7d :%7d", frontLeft.getCurrentPosition(), frontRight.getCurrentPosition());
        this.opMode.telemetry.addData("Encoder Pos Back Left/Right", " %7d :%7d", backLeft.getCurrentPosition(), backRight.getCurrentPosition());
        // Flywheel data
        this.opMode.telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
        this.opMode.telemetry.addData("Current Velocity", "%.2f", curVelocity);
        this.opMode.telemetry.addData("Error", "%.2f", error);
        // Call telemetry update
        this.opMode.telemetry.update();

    }
     */

    // Global launch sequence.
    private void launchSequence(double velocity) {
       // updateRearColorSensor();
        double curVelocity = flywheel.getVelocity();

        switch (launchState) {
            case 1:
                curTargetVelocity = velocity;
                launchState = 2;
                break;
            case 2:
               // this.opMode.telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
                //this.opMode.telemetry.addData("Current Velocity", "%.2f", curVelocity);
               // this.opMode.telemetry.addData("Error", "%.2f", error);
                // Call telemetry update
              //  this.opMode.telemetry.update();
                if (Math.abs(error) < 100) {
                    launchState = 3;
                }
                break;
            case 3:
               // if (artifactReady) {
                    flicking = true;
                    flickTimer.reset();
                    launchState = 4;
              //  }
                break;
            case 4:
                if (!flicking) {
                    launchState = 5;
                    wait.reset();
                    //if (intake_color.alpha() > 70) intakePower = 0.67;
                }
                break;
            case 5:
                if (wait.seconds() > 0.75) launchState = 6;
                break;
            case 6:
               // if (artifactReady) launchState = 2;
                //else if (wait.seconds() > 3)
                    launchState = 7;
                break;
           // case 7:
               // curTargetVelocity = 0;
               // intakePower = 0;
                //launchState = 0;
               // break;
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

        //double curVelocity = flywheel.getVelocity();
        error = curTargetVelocity - curVelocity;

        if (abs(error) > 10) {
            LED0.off();
            LED1.on();
        } else {
            LED0.on();
            LED1.off();
        }

        // Telemetry
        // Current state
        this.opMode.telemetry.addData("Current Launch State", launchState);
        // Elapsed time since program started
        this.opMode.telemetry.addData("Runtime", runtime.seconds());
        // Current drive motor encoder positions
        this.opMode.telemetry.addData("Encoder Pos Front Left/Right", " %7d :%7d", frontLeft.getCurrentPosition(), frontRight.getCurrentPosition());
        this.opMode.telemetry.addData("Encoder Pos Back Left/Right", " %7d :%7d", backLeft.getCurrentPosition(), backRight.getCurrentPosition());
        // Flywheel data
        this.opMode.telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
        this.opMode.telemetry.addData("Current Velocity", "%.2f", curVelocity);
        this.opMode.telemetry.addData("Error", "%.2f", error);
        // Call telemetry update
        this.opMode.telemetry.update();

    }

    private void updateRearColorSensor() {artifactReady = color.alpha() > 80;}


    private void flickLauncher() { // Be sure to reset the launchTime variable when this is run
        if (flickTimer.milliseconds() > 250){
            launcherPos = 0.75;
            flicking = false;
        } else launcherPos = 0.55;
    }

    public void beforeMainAuto() { // Sets initial hardware positions and resets runtime. Must be run immediately before main auto script
        // Give hardware initial positions and powers
        // Intake
        RBT.setPower(intakePower);
        ramp11.setPower(intakePower);
        ramp12.setPower(-intakePower);
        // Flywheel
        flywheel.setVelocity(curTargetVelocity);
        // Launcher servo
        launcher.setPosition(launcherPos);

        resetEncoders();

        frontLeft.setTargetPositionTolerance(10);
        frontRight.setTargetPositionTolerance(10);
        backLeft.setTargetPositionTolerance(10);
        backRight.setTargetPositionTolerance(10);

        runtime.reset();
    }

} // end class
