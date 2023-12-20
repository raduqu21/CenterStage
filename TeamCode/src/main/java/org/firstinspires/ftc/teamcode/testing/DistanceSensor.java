package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "DistanceSensorTest", group = "Testing")
public class DistanceSensor extends LinearOpMode
{
	Rev2mDistanceSensor distanceSensor;

	@Override
	public void runOpMode() throws InterruptedException
	{
		distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

		waitForStart();

		while (!isStopRequested())
			Run();
	}

	private void Run()
	{
		telemetry.addData("Distance", distanceSensor.getDistance(DistanceUnit.CM));
		telemetry.update();
	}
}