package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.PBAutoClass.COUNTS_PER_CM;
import static org.firstinspires.ftc.teamcode.PBAutoClass.F;
import static org.firstinspires.ftc.teamcode.PBAutoClass.P;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.*;



@Autonomous(name="PB_auto_mode_one", group="Autonomous")
@Disabled
public class PB_auto_mode_one extends LinearOpMode {

    PBAutoClass  RobotAuto;
    public PB_auto_mode_one(){
        RobotAuto = new PBAutoClass();
    }


    public void runOpMode(){

        // Initialize
        RobotAuto.InitRobot();
///
        // Initialize drive motors
        RobotAuto.frontLeft = hardwareMap.get(DcMotorEx.class, "leftfront");
        RobotAuto.frontRight = hardwareMap.get(DcMotorEx.class, "rightfront");
        RobotAuto.backLeft = hardwareMap.get(DcMotorEx.class, "leftrear");
        RobotAuto.backRight = hardwareMap.get(DcMotorEx.class, "rightrear");
        // Intake motor and servos
        RobotAuto.RBT = hardwareMap.get(DcMotor.class, "RBT");
        // Flywheel motor
        RobotAuto.flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        // Launcher servo
        RobotAuto.launcher = hardwareMap.get(Servo.class, "swoop_servo");
        // LEDs
       // LED0 = hardwareMap.get(LED.class, "Led0");
        //LED1 = hardwareMap.get(LED.class, "Led1");
       // LED2 = hardwareMap.get(LED.class, "Led2");
       // LED3 = hardwareMap.get(LED.class, "Led3");
        // set motor directions and behaviors
        RobotAuto.frontLeft.setDirection(DcMotor.Direction.REVERSE);
        RobotAuto.frontRight.setDirection(DcMotor.Direction.FORWARD);
        RobotAuto.backLeft.setDirection(DcMotor.Direction.REVERSE);
        RobotAuto.backRight.setDirection(DcMotor.Direction.FORWARD);

        RobotAuto.frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RobotAuto.frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RobotAuto.backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        RobotAuto.backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        RobotAuto.flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        RobotAuto.flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(P, 0, 0, F);
        RobotAuto.flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
        // Calculation variables
        double frontLeftInches = 20;
        double frontRightInches = 20;
        double backLeftInches = 20;
        double backRightInches = 20;

        int frontLeftTarget = (int) (frontLeftInches * COUNTS_PER_CM);
        int frontRightTarget = (int) (frontRightInches * COUNTS_PER_CM);
        int backLeftTarget = (int) (backLeftInches * COUNTS_PER_CM);
        int backRightTarget = (int) (backRightInches * COUNTS_PER_CM);

        // Set drive motor encoder target positions
        RobotAuto.frontLeft.setTargetPosition(frontLeftTarget);
        RobotAuto.frontRight.setTargetPosition(frontRightTarget);
        RobotAuto.backLeft.setTargetPosition(backLeftTarget);
        RobotAuto.backRight.setTargetPosition(backRightTarget);


        // Let the operator know the bot is initialized
        telemetry.addLine("Initialization complete");
        telemetry.update();

        ///
        waitForStart();
    //while (opModeIsactive()) {
    //    RobotAuto.runAuto(0);
    //}

    }

}
