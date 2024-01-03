package org.firstinspires.ftc.teamcode.testing;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.outoftheboxrobotics.photoncore.Photon;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities;

/*
 * Move 1 meter on the Y axis
 */

@Photon
@Autonomous(name = "Auto Test Y", group = "Testing")
public class AutoTestY extends LinearOpMode
{
	private MecanumDrive mecanumDrive;

	@Override
	public void runOpMode()
	{
		Init();
		waitForStart();
		Run();
	}

	private void Init()
	{
		mecanumDrive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0), Utilities.GetCurrentRobotType(hardwareMap, telemetry, gamepad1, gamepad2));
	}

	private void Run()
	{
		Actions.runBlocking(mecanumDrive.actionBuilder(new Pose2d(0, 0, 0))
				.setTangent(Math.PI / 2)
				.lineToY(39.37)
				.build());
	}
}
