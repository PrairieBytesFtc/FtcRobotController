/* Copyright (c) 2021 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

@TeleOp(name="PB_dev2_5_debug", group="Linear OpMode")
//@Disabled
public class PB_dev2_5_debug extends LinearOpMode { // This is the main teleop, but WITH telemetry. Other teleop doesn't have telemetry, in order to (in theory) improve code loop speed.

    // Flicks launcher servo up and down
    private void flickLauncher() { // Be sure to reset the launchTime variable when this is run
        if (flickTimer.milliseconds() > 300){
            launcherPos = 0.75;
            flicking = false;
        } else launcherPos = 0.55;
    }
    // Launching controls
    private void autoLaunchControls() {
        if (gamepad2.right_bumper || gamepad2.rightBumperWasPressed()) {
            if (curTargetVelocity == 0) {
                curTargetVelocity = lowVelocity;
                wait.reset();
                spunUp = false;
            } else if (artifactReady) {
                if (spunUp) {
                    if (wait.seconds() > 0.6) {
                        //intakePower = 0.67;
                        flicking = true;
                        flickTimer.reset();
                        wait.reset();
                    }
                } else {
                    if (wait.seconds() > 0.85) {
                        intakePower = 0.67;
                        flicking = true;
                        flickTimer.reset();
                        wait.reset();
                        spunUp = true;
                    }
                }
            }
        }
        if (gamepad2.leftBumperWasPressed()) {
            curTargetVelocity = 0;
            intakePower = 0;
        }
    }

    // Init webcam/aprilTag processor
    private void aprilTagInit() {

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(false)
                .setDrawTagOutline(false)
                .setDrawAxes(false)
                .setDrawCubeProjection(false)
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)
                .setNumThreads(1)

                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(this.hardwareMap.get(WebcamName.class, "webcam"));
        builder.setCameraResolution(new Size(640, 480));
        builder.enableLiveView(false); // reduces latency

        builder.addProcessor(aprilTagProcessor);

        visionPortal = builder.build();

        setManualExposure(8,100);

    }

    // Used by aprilTagInit
    private void setManualExposure(int exposureMS, int gain) {
        if (visionPortal == null || visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            telemetry.addLine("Camera Waiting...");
            // Waits until camera is ready
            while ((visionPortal != null && visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING)) {
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
            telemetry.addLine("Camera Ready");
        }

        // Set camera controls
        if (visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING) {
            try {
                ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
                GainControl gainControl = visionPortal.getCameraControl(GainControl.class);

                // Set exposure mode to manual
                if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                    exposureControl.setMode(ExposureControl.Mode.Manual);
                    Thread.sleep(50); // gives the camera time to switch modes
                }

                // Set exposure and gain
                exposureControl.setExposure((long)exposureMS, TimeUnit.MILLISECONDS);
                Thread.sleep(20);
                gainControl.setGain(gain);
                Thread.sleep(20);

            } catch (Exception e) {
                // Handles exceptions
                telemetry.addData("Camera Control Error", e.getMessage());
                telemetry.update();
            }
        }
    }

    // Main AprilTag Code
   private void aprilTagInfo() {
       List<AprilTagDetection> currentDetections = aprilTagProcessor.getDetections();
       // Creates a list of detected tag IDs and assigns a integer variable to the detected tag ID
       idsFound = new StringBuilder();
       for (AprilTagDetection detection : currentDetections) {
           idsFound.append(detection.id);
           idsFound.append(" ");
           currentID = detection.id;
       }

       if (!(" ".contentEquals(idsFound) || idsFound.length() == 0)) {
           tagDetected = true;

           // Assigns AprilTag readings to double variables, bearing is the only value that is currently used
           for (AprilTagDetection detection : currentDetections) {
//               tagValueX = Double.parseDouble(df2.format(detection.ftcPose.x));
//               tagValueY = Double.parseDouble(df2.format(detection.ftcPose.y));
//               tagValueZ = Double.parseDouble(df2.format(detection.ftcPose.z));
//               tagValuePitch = Double.parseDouble(df2.format(detection.ftcPose.pitch));
//               tagValueRoll = Double.parseDouble(df2.format(detection.ftcPose.roll));
//               tagValueYaw = Double.parseDouble(df2.format(detection.ftcPose.yaw));
//               tagValueRange = Double.parseDouble(df2.format(detection.ftcPose.range));
               tagValueBearing = Double.parseDouble(df2.format(detection.ftcPose.bearing));
//               tagValueElevation = Double.parseDouble(df2.format(detection.ftcPose.elevation));
           }

       } else {
           tagDetected = false;
           currentID = 0;
       }
   }

   private void aprilTagAlign() {
       if (gamepad1.right_trigger > 0) {
           if (currentID == 20) { // Blue goal tag
               tagError = -(goalX - tagValueBearing);

               if (Math.abs(tagError) < angleTolerance) {
                   yaw = 0;
                   strlockstatus = "In Lock";
               } else {
                   double pTerm = tagError * kP;

                   curTime = getRuntime();
                   dT = curTime - lastTime;
                   double dTerm = ((tagError - lastTagError) / dT) * kD;

                   yaw = Range.clip(pTerm + dTerm, -1, 1);

                   lastTagError = tagError;
                   lastTime = curTime;

                   strlockstatus = "Correcting";
               }
               LED2.off();
               LED3.on();
           } else if (currentID == 24) { // Red goal tag
               tagError = -(goalX - tagValueBearing);

               if (Math.abs(tagError) < angleTolerance) {
                   yaw = 0;
                   strlockstatus = "In Lock";
               } else {
                   double pTerm = tagError * kP;

                   curTime = getRuntime();
                   dT = curTime - lastTime;
                   double dTerm = ((tagError - lastTagError) / dT) * kD;

                   yaw = Range.clip(pTerm + dTerm, -1, 1);

                   lastTagError = tagError;
                   lastTime = curTime;

                   strlockstatus = "Correcting";
               }
               LED2.off();
               LED3.on();
           } else { // Invalid tag/no tag
               curTime = getRuntime();
               lastTagError = 0;
               dT = curTime - lastTime;
               lastTime = curTime;
               strlockstatus = "No/Wrong Tag";
               LED2.on();
               LED3.off();
           }
       } else {
           curTime = getRuntime();
           lastTagError = 0;
           dT = curTime - lastTime;
           lastTime = curTime;
           // AprilTag LED indicator and status variable
           strlockstatus = "Off";
           LED2.off();
           LED3.off();
       }
   }

    // Declare Elapsed Time variables
    private ElapsedTime runtime = new ElapsedTime();
    private ElapsedTime wait = new ElapsedTime();
    private ElapsedTime flickTimer = new ElapsedTime();
    // Declare OpMode members for each of the 4 motors.
    private DcMotor frontLeftDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    // Declare drive input variables
    double axial = 0;
    double lateral = 0;
    double yaw = 0;
    // RBT + Ramp declaration
    private DcMotor RBT = null;
    private CRServo ramp11 = null;
    private CRServo ramp12 = null;
    // Flywheel motor declaration
    private DcMotorEx flywheel = null;
    // Flywheel PIDF variables
    double curTargetVelocity = 0;
    final double P = 170;
    final double F = 12.99;
    final double lowVelocity = 1700;
    final double highVelocity = 2000;
    // Launcher servo declaration
    private Servo launcher = null;

    // Color sensor
    private ColorSensor color = null;
    private ColorSensor intake_color = null;
    private ColorSensor middle_color = null;

    // Range sensor declaration, currently unused
    //private Rev2mDistanceSensor rangesensor = null;

    // Declarations for AprilTag code, currently unused
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;
    // AprilTag info strings + integers
    private StringBuilder idsFound;
    int currentID;
    double tagValueX = 0;
    double tagValueY = 0;
    double tagValueZ = 0;
    double tagValuePitch = 0;
    double tagValueRoll = 0;
    double tagValueYaw = 0;
    double tagValueRange = 0;
    double tagValueBearing = 0;
    double tagValueElevation = 0;
    private DecimalFormat df2 = new DecimalFormat("0.00");
    boolean tagDetected = false;
    boolean lockToTag = false;
    // Also april tag variables; used for auto-align
    double kP = 0.035;
    double tagError = 0;
    double lastTagError = 0;
    double goalX = 0; // used for offsetting tag alignment center
    double angleTolerance = 0.4;
    double kD = 0.002;
    double dT = 0;
    double curTime = 0;
    double lastTime = 0;

    // Status string initiations
    private String strintakestatus = "Off";
    private String strlockstatus = "Off";
    // LED declarations
    private LED LED0 = null;
    private LED LED1 = null;
    private LED LED2 = null;
    private LED LED3 = null;
    private LED LED4 = null;
    private LED LED5 = null;
    private LED LED6 = null;
    private LED LED7 = null;

    // Misc
    boolean intakeWasAutoOff = false;
    boolean artifactInIntake = false;
    boolean artifactReady = false;
    boolean artifactInMiddle = false;
    boolean flicking = false;
    double intakePower = 0;
    double launcherPos = 0.75;
    boolean spunUp = false;




    @Override
    public void runOpMode() {

        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        // Drive motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "leftfront");
        backLeftDrive = hardwareMap.get(DcMotor.class, "leftrear");
        frontRightDrive = hardwareMap.get(DcMotor.class, "rightfront");
        backRightDrive = hardwareMap.get(DcMotor.class, "rightrear");
        // Intake motor and servos
        // Distance sensor, currently unused
        //rangesensor = hardwareMap.get(Rev2mDistanceSensor.class, "distance0");


        // Set motor directions and behaviors
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);

        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        // Controls drive speed
        double drivemultiplier = 0.6;

        // Wait for the game to start (driver presses START)
        telemetry.addLine("Initialization complete");
        telemetry.update();

        waitForStart();

        // Give hardware initial positions and powers
        // Intake

        // Drive motors
        // The drive power variables are local, so the motors' powers are simply set to zero
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
        // Launcher servo
        launcher.setPosition(launcherPos);

        // Runtime elapsed time variable not necessary?
        runtime.reset();
        // Replaced with this?
        resetRuntime();
        curTime = getRuntime();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            double max;
            //boolean x = gamepad1.xWasPressed();

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            axial = -gamepad1.left_stick_y;
            lateral = gamepad1.left_stick_x;
            yaw = gamepad1.right_stick_x;

            aprilTagInfo();

            // Heading lock
            aprilTagAlign();

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
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
                frontLeftPower  /= max;
                frontRightPower /= max;
                backLeftPower   /= max;
                backRightPower  /= max;
            }


            autoLaunchControls();

            // Flicks launcher servo manually
            if (gamepad2.dpadUpWasPressed()) {
                flicking = true;
                flickTimer.reset();
            }

            // Manual flywheel control
            if (gamepad2.xWasPressed()) {
                curTargetVelocity = lowVelocity;
            }
            if (gamepad2.bWasPressed()) {
                curTargetVelocity = 0;
            }
            if (gamepad2.yWasPressed()) {
                curTargetVelocity = highVelocity;
            }

            // Intake control code
            if (gamepad2.bWasPressed()) {
                intakePower = 0;
                strintakestatus = "Off";
            }
            if (gamepad2.aWasPressed()) {
                intakePower = 1;
                strintakestatus = "In";
            }
            if (gamepad2.right_trigger > 0 && gamepad2.left_trigger > 0) {
                intakePower = -0.5;
                strintakestatus = "Out";
            }




            // Run timed programs
            if (flicking) flickLauncher();

            // Assign calculated values to hardware
            // Intake

            // Drive motors
            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(backRightPower);
            // Flywheel
            flywheel.setVelocity(curTargetVelocity);
            // Launcher servo
            launcher.setPosition(launcherPos);

            double curVelocity = flywheel.getVelocity();
            double error = curTargetVelocity - curVelocity;

            // LED indicators
            // Flywheel error LED indicator

                }
            }

            // Telemetry
            // Elapsed time since program started
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            // Final wheel power values
            telemetry.addData("Front left/Right", "%4.2f, %4.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", backLeftPower, backRightPower);
            // Flywheel data
            telemetry.addData("Flywheel Target Velocity", curTargetVelocity);
            telemetry.addData("Current Velocity", "%.2f", curVelocity);
            telemetry.addData("Flywheel Error", "%.2f", error);
            // Rear color sensor
            telemetry.addData("Rear color alpha", color.alpha());
            // Middle color sensor
            telemetry.addData("Middle Color alpha", middle_color.alpha());
            // Intake color sensor
            telemetry.addData("Intake Color alpha", intake_color.alpha());
            // April Tag telemetry
            telemetry.addData("Detected AprilTag ID", currentID);
            telemetry.addData("Bearing", tagValueBearing);
            telemetry.addData("Auto-align Error", tagError);
            telemetry.addData("Code Loop Time (s)", dT);
            // Statuses and misc info
            telemetry.addData("Intake Status", strintakestatus);
            telemetry.addData("Heading Lock Status", strlockstatus);
            // Call telemetry update
            telemetry.update();
            //sleep(20); This (might) improve performance if uncommented
        }
    }

}
