package org.firstinspires.ftc.teamcode.subsystems.impl;

import static org.firstinspires.ftc.teamcode.Utilities.CutPower;
import static org.firstinspires.ftc.teamcode.Utilities.RestorePower;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.outoftheboxrobotics.photoncore.hardware.servo.PhotonServo;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.Utilities;
import org.firstinspires.ftc.teamcode.subsystems.SystemEx;

public final class ClawSystem extends SystemEx
{
	private final PhotonServo servo;

	public ClawSystem(PhotonServo servo){ this.servo = servo; }

	@Override
	public void Init() { servo.setPosition(Constants.getClawIdle()); }

	@Override
	public void Disable() { CutPower(servo); }

	@Override
	public void Enable() { RestorePower(servo); }

	@Override
	public Action MoveToPositionWithDelay(double position, double delay, Utilities.DelayDirection delayDirection)
	{
		return new SequentialAction(
				new SleepAction(delayDirection == Utilities.DelayDirection.BEFORE ? delay : delayDirection == Utilities.DelayDirection.BOTH ? delay : 0),
				telemetryPacket -> {
					servo.setPosition(position);
					return false;
				},
				new SleepAction(delayDirection == Utilities.DelayDirection.AFTER ? delay : delayDirection == Utilities.DelayDirection.BOTH ? delay : 0)
		);
	}
}