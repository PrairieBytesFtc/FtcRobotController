package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;


@Autonomous(name="PB_auto_2ft_leave", group="Linear OpMode", preselectTeleOp = "PB_dev1_3")
public class PB_auto_2ft_leave extends LinearOpMode {


    // Declare OpMode members
    // Declare drive motors
    private DcMotor frontLeft = null;
    private DcMotor frontRight = null;
    private DcMotor backLeft = null;
    private DcMotor backRight = null;
    static final int DISTANCE_IN_TICKS = -1060;
    static final double COUNTS_PER_MOTOR_REV = 753.2;
    static final double WHEEL_DIAMETER_INCHES = 5.51;
    static final double COUNTS_PER_INCH = COUNTS_PER_MOTOR_REV / (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double MOTOR_POWER = 0.5;


    @Override
    public void runOpMode() {

        // Initialize motors
        frontLeft = hardwareMap.get(DcMotor.class, "leftfront");
        frontRight = hardwareMap.get(DcMotor.class, "rightfront");
        backLeft = hardwareMap.get(DcMotor.class, "leftrear");
        backRight = hardwareMap.get(DcMotor.class, "rightrear");

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Calculation variables
        double frontLeftInches = -24;
        double frontRightInches = -24;
        double backLeftInches = -24;
        double backRightInches = -24;

        int frontLeftTarget = (int) (frontLeftInches * COUNTS_PER_INCH);
        int frontRightTarget = (int) (frontRightInches * COUNTS_PER_INCH);
        int backLeftTarget = (int) (backLeftInches * COUNTS_PER_INCH);
        int backRightTarget = (int) (backRightInches * COUNTS_PER_INCH);

        frontLeft.setTargetPosition(frontLeftTarget);
        frontRight.setTargetPosition(frontRightTarget);
        backLeft.setTargetPosition(backLeftTarget);
        backRight.setTargetPosition(backRightTarget);

        // Let the operator know the bot is initialized
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for start
        waitForStart();

        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        frontLeft.setPower(MOTOR_POWER);
        frontRight.setPower(MOTOR_POWER);
        backLeft.setPower(MOTOR_POWER);
        backRight.setPower(MOTOR_POWER);

        while(opModeIsActive()) {

            // Telemetry for current pos of drive motors
            telemetry.addData("Encoder Pos Front Left/Right", " %7d :%7d", frontLeft.getCurrentPosition(), frontRight.getCurrentPosition());
            telemetry.addData("Encoder Pos Back Left/Right", " %7d :%7d", backLeft.getCurrentPosition(), backRight.getCurrentPosition());
            telemetry.update();
        }
    }
}
