package org.firstinspires.ftc.teamcode.subsystems.impl;

import static org.firstinspires.ftc.teamcode.Utilities.CutPower;
import static org.firstinspires.ftc.teamcode.Utilities.RestorePower;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.Utilities;
import org.firstinspires.ftc.teamcode.subsystems.SystemEx;

public final class IntakeSystem extends SystemEx
{
	private final DcMotorEx motor;
	private final Servo servo;

	public IntakeSystem(DcMotorEx motor, Servo servo) {
		this.motor = motor;
		this.servo = servo;
	}

	@Override
	public void Init() { motor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER); }

	@Override
	public void Disable() { CutPower(motor); }

	@Override
	public void Enable() { RestorePower(motor); }

	@Override
	public Action MoveToPositionWithDelay(double position, double delay, Utilities.DelayDirection delayDirection)
	{
		throw new UnsupportedOperationException();
	}

	public Action RunIntakeFor(double time)
	{
		return new SequentialAction(
				telemetryPacket -> {
					motor.setPower(Constants.getIntakeMaxPower());
					return false;
				},
				new SleepAction(time),
				telemetryPacket -> {
					motor.setPower(0);
					return false;
				}
		);
	}

	public Action RunIntakeWithAntennaFor(double time)
	{
		return new SequentialAction(
				telemetryPacket -> {
					motor.setPower(Constants.getIntakeMaxPower());
					servo.setPosition(Constants.getAntennaGrab());
					return false;
				},
				new SleepAction(time),
				telemetryPacket -> {
					motor.setPower(0);
					servo.setPosition(Constants.getAntennaIdle());
					return false;
				}
		);
	}
}