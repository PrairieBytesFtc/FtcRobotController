package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.*;

@Autonomous(name="radarEncoderDrive")
public class radarEncoderDrive extends LinearOpMode {

    // Drive motor declaration
    private DcMotor frontRight = null;
    private DcMotor frontLeft = null;
    private DcMotor backRight = null;
    private DcMotor backLeft = null;
    // Variable declaration
    int distanceInTicks = 200;

    @Override
    public void runOpMode() {

        // Drive motor hardware map
        frontRight = hardwareMap.get(DcMotor.class,"rightfront");
        frontLeft = hardwareMap.get(DcMotor.class,"leftfront");
        backRight = hardwareMap.get(DcMotor.class,"rightrear" );
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

        resetEncoders();

        frontRight.setTargetPosition(distanceInTicks);
        frontLeft.setTargetPosition(distanceInTicks);
        backRight.setTargetPosition(distanceInTicks);
        backLeft.setTargetPosition(distanceInTicks);

        telemetry.addLine("Initialized");
        telemetry.update();

        waitForStart();

        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        frontRight.setPower(1);
        frontLeft.setPower(1);
        backRight.setPower(1);
        backLeft.setPower(1);

        while (opModeIsActive()) {


            telemetry.addData("front right target pos", frontRight.getTargetPosition());
            telemetry.addData("front right current pos", frontRight.getCurrentPosition());
            telemetry.addData("front left target pos", frontLeft.getTargetPosition());
            telemetry.addData("front left current pos", frontLeft.getCurrentPosition());
            telemetry.update();

        }

    }

    /**
     * This function resets the drive motor encoders
     */
    public void resetEncoders() {
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }

}
