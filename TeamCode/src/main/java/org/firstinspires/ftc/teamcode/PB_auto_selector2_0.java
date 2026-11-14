/* Copyright (c) 2017 FIRST. All rights reserved.
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

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/*
 * This OpMode illustrates the concept of driving a path based on encoder counts.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: RobotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forward, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backward for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This method assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@Autonomous(name="PB_auto_selector2_0", group="Linear OpMode", preselectTeleOp="PB_dev2_1")
//@Disabled
public class PB_auto_selector2_0 extends LinearOpMode {

    private int runMode = 0; // The mode to start the PbRobotClass in


    @Override
    public void runOpMode() {
        String strModeDesc = ""; // Show description of selected run mode

        // Loop repeatedly during the INIT phase to accept controller input
        while (!isStarted() && !isStopRequested()) {

            // Check for D-pad input on gamepad1
            if (gamepad1.dpadLeftWasPressed()) {
                    runMode--;
                    if(runMode < 1) runMode = 7;

                } else if (gamepad1.dpadRightWasPressed()) {
                    runMode++;
                    if(runMode > 7) runMode = 1;
            }


            // Provide telemetry feedback to the Driver Station
            telemetry.addData( "Waiting for start, current run mode: ", runMode);
            switch(runMode) {
                case 1:
                    strModeDesc = "Start from goal, Red side, Six artifact.";
                    break;
                case 2:
                    strModeDesc = "Start from goal, Blue side, Six artifact";
                    break;
                case 3:
                    strModeDesc = "Start from small triangle, Either side, Preload only";
                    break;
                case 4:
                    strModeDesc = "Start from small triangle, Red side, Six artifact";
                    break;
                case 5:
                    strModeDesc = "Start from small triangle, Blue side, Six artifact";
                    break;
                case 6:
                    strModeDesc = "AutoTest1";
                    break;

                default:
                    strModeDesc = "No OpMode or Invalid OpMode selected.";

            }
            telemetry.addData("Auto mode selected: ", runMode);
            telemetry.addData( "Mode Desc: ", strModeDesc);
            telemetry.update();
            idle(); // Important to yield the processor
        }

        // Wait for the game to start (driver presses START)
        telemetry.addData("Auto mode selected: ", runMode);
        telemetry.addData( "Mode Desc: ", strModeDesc);

        telemetry.update();
        //> Create robot class and run it with the selected mode
        PB_robot_class1_4 PbRobot = new PB_robot_class1_4(hardwareMap, this); // Instantiate with the hardwareMap

        // Initiate robot
        PbRobot.init();

        // Wait for the game to start (driver presses START)
        waitForStart();

        PbRobot.RunAutoMode(runMode);

        telemetry.addLine("Auto Mode Finished");
        telemetry.update();
        sleep(1000);  // pause to display final telemetry message.
    }

}
