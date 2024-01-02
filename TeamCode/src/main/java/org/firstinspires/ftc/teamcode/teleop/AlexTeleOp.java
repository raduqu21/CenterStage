package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.Constants.TOLERANCE;
import static org.firstinspires.ftc.teamcode.Constants.getSuspenderSuspend;
import static org.firstinspires.ftc.teamcode.Utilities.CutPower;
import static org.firstinspires.ftc.teamcode.Utilities.GetCurrentRobotType;
import static org.firstinspires.ftc.teamcode.Utilities.IsDebugging;
import static org.firstinspires.ftc.teamcode.Utilities.RestorePower;
import static org.firstinspires.ftc.teamcode.Utilities.setTimeout;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.outoftheboxrobotics.photoncore.Photon;
import com.outoftheboxrobotics.photoncore.hardware.servo.PhotonServo;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.outoftheboxrobotics.photoncore.hardware.motor.PhotonDcMotor;


import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.BaseOpMode;
import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.InputSystem;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities;

@Photon
@TeleOp(name = "Alex TeleOp", group = "TeleOp")
public final class AlexTeleOp extends BaseOpMode
{
	// Wheel motors
	private MecanumDrive mecanumDrive;

	// Intake Motor
	private PhotonDcMotor intakeMotor;

	// Lift Motors
	private PhotonDcMotor liftMotor1, liftMotor2;

	// Tumbler Motor
	private PhotonDcMotor tumblerMotor;

	// Servos
	private PhotonServo clawServo, rotatorServo, lockerServo, planeLevelServo, planeShooterServo;

	// Distance Sensor
	private Rev2mDistanceSensor distanceSensor;

	// Input System
	private InputSystem wheelInput, armInput;

	private boolean INVERTED = false;
	private boolean DEBUG = false;

	private Utilities.RobotType currentRobot = Utilities.RobotType.ROBOT_1;

	private final static class Bindings
	{
		private final static class Wheel
		{
			private static final InputSystem.Key TURBO_KEY = new InputSystem.Key("right_bumper");
			private static final InputSystem.Key SUPPRESS_KEY = new InputSystem.Key("left_bumper");
			private static final InputSystem.Axis DRIVE_AXIS_X = new InputSystem.Axis("left_stick_x");
			private static final InputSystem.Axis DRIVE_AXIS_Y = new InputSystem.Axis("left_stick_y");
			private static final InputSystem.Axis ROTATE_AXIS_L = new InputSystem.Axis("left_trigger");
			private static final InputSystem.Axis ROTATE_AXIS_R = new InputSystem.Axis("right_trigger");
			private static final InputSystem.Key INTAKE_KEY = new InputSystem.Key("a");
			private static final InputSystem.Key INTAKE_REVERSE_KEY = new InputSystem.Key("b");
			private static final InputSystem.Key ALIGN_KEY = new InputSystem.Key("x");
		}

		private final static class Arm
		{
			private static final InputSystem.BindingCombo PLANE_COMBO = new InputSystem.BindingCombo("_plane", new InputSystem.Key("left_bumper"), new InputSystem.Key("right_bumper"));
			private static final InputSystem.Key SUSPENDER_KEY = new InputSystem.Key("x");
			private static final InputSystem.Key SUSPENDER_CANCEL_KEY = new InputSystem.Key("y");
			private static final InputSystem.Key ARM_KEY = new InputSystem.Key("a");
			private static final InputSystem.Key ARM_CONFIRM_KEY = new InputSystem.Key("b");
			private static final InputSystem.Key LEVEL_1_KEY = new InputSystem.Key("dpad_down");
			private static final InputSystem.Key LEVEL_2_KEY = new InputSystem.Key("dpad_up");
			private static final InputSystem.Key LEVEL_3_KEY = new InputSystem.Key("dpad_left");
			private static final InputSystem.Key LEVEL_4_KEY = new InputSystem.Key("dpad_right");
			private static final InputSystem.Key RESET_MODE_KEY = new InputSystem.Key("y");
			private static final InputSystem.Axis RESET_AXIS = new InputSystem.Axis("left_stick_y");
			private static final InputSystem.Key RESET_CONFIRM = new InputSystem.Key("b");
			private static final InputSystem.Key RESET_STACK = new InputSystem.Key("start");
			private static final InputSystem.Key TOGGLE_PICKUP_KEY = new InputSystem.Key("back");
		}
	}

	@Override
	protected void OnInitialize()
	{
		currentRobot = GetCurrentRobotType(hardwareMap, telemetry, gamepad1, gamepad2);
		mecanumDrive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0), currentRobot);
		Constants.Init(currentRobot);
		DEBUG = IsDebugging(hardwareMap);
		if(currentRobot == Utilities.RobotType.ROBOT_2) INVERTED = !INVERTED;

		intakeMotor = hardwareMap.get(PhotonDcMotor.class, "intake");
		intakeMotor.setMode(PhotonDcMotor.RunMode.RUN_WITHOUT_ENCODER);

		liftMotor1 = hardwareMap.get(PhotonDcMotor.class, "lift1");
		liftMotor2 = hardwareMap.get(PhotonDcMotor.class, "lift2");
		liftMotor1.setMode(PhotonDcMotor.RunMode.STOP_AND_RESET_ENCODER);
		liftMotor2.setMode(PhotonDcMotor.RunMode.STOP_AND_RESET_ENCODER);
		liftMotor1.setMode(PhotonDcMotor.RunMode.RUN_USING_ENCODER);
		liftMotor2.setMode(PhotonDcMotor.RunMode.RUN_USING_ENCODER);
		liftMotor1.setDirection(PhotonDcMotor.Direction.REVERSE);
		liftMotor1.setZeroPowerBehavior(PhotonDcMotor.ZeroPowerBehavior.BRAKE);
		liftMotor2.setZeroPowerBehavior(PhotonDcMotor.ZeroPowerBehavior.BRAKE);
		liftMotor1.setTargetPosition(Constants.getSuspenderIdle());
		liftMotor2.setTargetPosition(Constants.getSuspenderIdle());
		liftMotor1.setMode(PhotonDcMotor.RunMode.RUN_TO_POSITION);
		liftMotor2.setMode(PhotonDcMotor.RunMode.RUN_TO_POSITION);

		tumblerMotor = hardwareMap.get(PhotonDcMotor.class, "tumbler");
		tumblerMotor.setMode(PhotonDcMotor.RunMode.STOP_AND_RESET_ENCODER);
		tumblerMotor.setZeroPowerBehavior(PhotonDcMotor.ZeroPowerBehavior.BRAKE);
		if(currentRobot == Utilities.RobotType.ROBOT_1) tumblerMotor.setDirection(PhotonDcMotor.Direction.REVERSE);
		tumblerMotor.setMode(PhotonDcMotor.RunMode.RUN_USING_ENCODER);
		tumblerMotor.setTargetPosition(Constants.getTumblerIdle());
		tumblerMotor.setMode(PhotonDcMotor.RunMode.RUN_TO_POSITION);

		rotatorServo = hardwareMap.get(PhotonServo.class, "rotator");
		clawServo = hardwareMap.get(PhotonServo.class, "claw");
		lockerServo = hardwareMap.get(PhotonServo.class, "locker");
		planeShooterServo = hardwareMap.get(PhotonServo.class, "shooter");
		planeLevelServo = hardwareMap.get(PhotonServo.class, "leveler");

		planeShooterServo.setPosition(Constants.getPlaneShooterIdle());
		lockerServo.setPosition(Constants.getLockerIdle());
		clawServo.setPosition(Constants.getClawIdle());
		rotatorServo.setPosition(Constants.getRotatorIdle());

		distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

		wheelInput = new InputSystem(gamepad1);
		armInput = new InputSystem(gamepad2);

		telemetry.setMsTransmissionInterval(50);
	}

	@Override
	protected void OnRun()
	{
		Telemetry();
		UpdateMotorPowers();
		Suspender();
		if (!robotSuspended)
		{
			AlignToBackdrop();
			Wheels();
		}
		if (suspending) return;
		ManualReset();
		Leveler();
		Pickup();
		Arm();
		Plane();
		if(armInput.wasPressedThisFrame(Bindings.Arm.RESET_STACK)) stackLevel = 5;
	}


	// ================ Wheels ================
	private void Wheels()
	{
		boolean turbo = wheelInput.isPressed(Bindings.Wheel.TURBO_KEY);
		boolean suppress = wheelInput.isPressed(Bindings.Wheel.SUPPRESS_KEY);
		int modifier = INVERTED ? -1 : 1;
		double angle = (wheelInput.getValue(Bindings.Wheel.ROTATE_AXIS_L) - wheelInput.getValue(Bindings.Wheel.ROTATE_AXIS_R)) * (turbo ? 0.1 : suppress ? 0.03 : 0.08);
		Vector2d wheelVel = new Vector2d(
				wheelInput.getValue(Bindings.Wheel.DRIVE_AXIS_Y) * modifier * (currentRobot == Utilities.RobotType.ROBOT_2 ? -1 : 1),
				wheelInput.getValue(Bindings.Wheel.DRIVE_AXIS_X) * modifier * (currentRobot == Utilities.RobotType.ROBOT_2 ? -1 : 1) * frontBackMovementEnable
		).times(turbo ? 1.0 : suppress ? 0.3 : 0.8);
		mecanumDrive.setDrivePowers(new PoseVelocity2d(wheelVel, angle));
	}

	private int frontBackMovementEnable = 1;
	private void AlignToBackdrop()
	{
		if (wheelInput.isPressed(Bindings.Wheel.ALIGN_KEY))
		{
			frontBackMovementEnable = 0;
			mecanumDrive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, distanceSensor.getDistance(DistanceUnit.CM) - Constants.getBackdropDistance()),
															-mecanumDrive.pose.heading.real));
		} else frontBackMovementEnable = 1;
	}

	private Utilities.PickupMode pickupMode = Utilities.PickupMode.INTAKE;

	private void Pickup()
	{
		if (armInput.wasPressedThisFrame(Bindings.Arm.TOGGLE_PICKUP_KEY)) {
			pickupMode = pickupMode == Utilities.PickupMode.INTAKE ? Utilities.PickupMode.STACK : Utilities.PickupMode.INTAKE;
			if (pickupMode == Utilities.PickupMode.STACK) intakeMotor.setMotorDisable();
			else intakeMotor.setMotorEnable();
			gamepad2.rumble(500);
		}
		if (pickupMode == Utilities.PickupMode.INTAKE)
			intakeMotor.setPower(wheelInput.isPressed(Bindings.Wheel.INTAKE_KEY) ? Constants.getIntakeMaxPower() : wheelInput.isPressed(Bindings.Wheel.INTAKE_REVERSE_KEY) ? -Constants.getIntakeMaxPower() : 0);
	}

	private void UpdateMotorPowers()
	{
		if(robotSuspended || suspending)
			liftMotor1.setPower(Constants.getLiftSuspendPower());
		else if (Math.abs(liftMotor1.getCurrentPosition() - liftMotor1.getTargetPosition()) > TOLERANCE)
			liftMotor1.setPower(Constants.getLiftNormalPower());
		else
			liftMotor1.setPower(0.05);

		if(robotSuspended || suspending)
			liftMotor2.setPower(Constants.getLiftSuspendPower());
		else if (Math.abs(liftMotor2.getCurrentPosition() - liftMotor2.getTargetPosition()) > TOLERANCE)
			liftMotor2.setPower(Constants.getLiftNormalPower());
		else
			liftMotor2.setPower(0.05);

		if(inResetMode) return;
		if (Math.abs(tumblerMotor.getCurrentPosition() - tumblerMotor.getTargetPosition()) > TOLERANCE / 2)
			tumblerMotor.setPower(1);
		else
			tumblerMotor.setPower(0.05);
	}

	// ================ Arm ================

	private short liftLevel = 1;

	private void Leveler()
	{
		int initialLevel = liftLevel;
		if (armInput.wasPressedThisFrame(Bindings.Arm.LEVEL_1_KEY)) liftLevel = 1;
		else if (armInput.wasPressedThisFrame(Bindings.Arm.LEVEL_2_KEY)) liftLevel = 2;
		else if (armInput.wasPressedThisFrame(Bindings.Arm.LEVEL_3_KEY)) liftLevel = 3;
		else if (armInput.wasPressedThisFrame(Bindings.Arm.LEVEL_4_KEY)) liftLevel = 4;
		if(initialLevel != liftLevel && armState == Utilities.State.BUSY) {
			liftMotor1.setTargetPosition(LiftLevelToValue(liftLevel));
			liftMotor2.setTargetPosition(LiftLevelToValue(liftLevel));
		}
	}

	private boolean inResetMode = false;
	private void ManualReset()
	{
		if(armInput.wasPressedThisFrame(Bindings.Arm.RESET_MODE_KEY)) inResetMode = !inResetMode;
		if(!inResetMode) return;
		if(armInput.wasPressedThisFrame(Bindings.Arm.RESET_CONFIRM)) {
			tumblerMotor.setMode(PhotonDcMotor.RunMode.STOP_AND_RESET_ENCODER);
			tumblerMotor.setZeroPowerBehavior(PhotonDcMotor.ZeroPowerBehavior.BRAKE);
			if(currentRobot == Utilities.RobotType.ROBOT_1) tumblerMotor.setDirection(PhotonDcMotor.Direction.REVERSE);
			tumblerMotor.setMode(PhotonDcMotor.RunMode.RUN_USING_ENCODER);
			tumblerMotor.setTargetPosition(Constants.getTumblerIdle());
			tumblerMotor.setMode(PhotonDcMotor.RunMode.RUN_TO_POSITION);
			inResetMode = false;
		} else {
			tumblerMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
			tumblerMotor.setPower(armInput.getValue(Bindings.Arm.RESET_AXIS));
		}
	}

	private Utilities.State armState = Utilities.State.IDLE;
	private volatile boolean armBusy = false;
	private boolean atLevel = false;
	private boolean pixelInStorage = false;
	private boolean waitingSecondInstruction = false;
	private short stackLevel = 5;

	private void Arm()
	{
		if (armBusy) return;
		if (armInput.wasPressedThisFrame(Bindings.Arm.ARM_CONFIRM_KEY) && waitingSecondInstruction && !pixelInStorage) {
			armBusy = true;
			rotatorServo.setPosition(Constants.getRotatorIdle());
			setTimeout(() -> {
				tumblerMotor.setTargetPosition(Constants.getTumblerLoad());
				setTimeout(() -> {
					clawServo.setPosition(Constants.getClawIdle());
					setTimeout(() -> {
						tumblerMotor.setTargetPosition(Constants.getTumblerIdle());
						armBusy = false;
						waitingSecondInstruction = false;
						pixelInStorage = true;
					}, 500);
				}, 500);
			}, 500);
		} else if (armInput.wasPressedThisFrame(Bindings.Arm.ARM_KEY)) {
			if (armState == Utilities.State.BUSY) { // Drop Pixel and return to Idle
				armBusy = true;
				clawServo.setPosition(Constants.getClawIdle());
				setTimeout(() -> {
					tumblerMotor.setTargetPosition(Constants.getTumblerIdle());
					setTimeout(() -> {
						liftMotor1.setTargetPosition(Constants.getLiftPickup());
						liftMotor2.setTargetPosition(Constants.getLiftPickup());
						rotatorServo.setPosition(Constants.getRotatorIdle());
						armState = Utilities.State.IDLE;
						armBusy = false;
					}, 500);
				}, 750);
			} else { // Pickup Pixel and go to Backdrop
				if (pickupMode == Utilities.PickupMode.INTAKE) {
					armBusy = true;
					tumblerMotor.setTargetPosition(Constants.getTumblerLoad());
					setTimeout(() -> {
						clawServo.setPosition(Constants.getClawBusy());
						setTimeout(() -> {
							liftMotor1.setTargetPosition(LiftLevelToValue(liftLevel));
							liftMotor2.setTargetPosition(LiftLevelToValue(liftLevel));
							tumblerMotor.setTargetPosition(Constants.getTumblerBackdrop());
							setTimeout(() -> {
								rotatorServo.setPosition(Constants.getRotatorBusy());
								armBusy = false;
								armState = Utilities.State.BUSY;
							}, 200 * liftLevel);
						}, 500);
					}, 500);
				} else if (stackLevel > 0) {
					if (!atLevel && !waitingSecondInstruction) {
						armBusy = true;
						rotatorServo.setPosition(Constants.getRotatorBusy());
						setTimeout(() -> {
							tumblerMotor.setTargetPosition(Constants.getTumblerStackPoses()[stackLevel - 1] - 100);
							atLevel = true;
							armBusy = false;
						}, 100);
					} else if (atLevel && !waitingSecondInstruction) {
						armBusy = true;
						tumblerMotor.setTargetPosition(Constants.getTumblerStackPoses()[stackLevel - 1]);
						setTimeout(() -> {
							clawServo.setPosition(Constants.getClawBusy());
							setTimeout(() -> {
								tumblerMotor.setTargetPosition(Constants.getTumblerBackdrop());
								armBusy = false;
								atLevel = false;
								waitingSecondInstruction = true;
								stackLevel--;
							}, 500);
						}, 300);
					} else if (!atLevel) {
						armBusy = true;
						liftMotor1.setTargetPosition(LiftLevelToValue(liftLevel));
						liftMotor2.setTargetPosition(LiftLevelToValue(liftLevel));
						armBusy = false;
						waitingSecondInstruction = false;
						armState = Utilities.State.BUSY;
					}
				}
			}
		}
	}

	private volatile boolean planeLaunched = false;

	private void Plane()
	{
		if (armInput.wasPressedThisFrame(Bindings.Arm.PLANE_COMBO) && !planeLaunched)
		{
			planeLaunched = true;
			planeLevelServo.setPosition(Constants.getPlaneLevelerBusy());
			setTimeout(() -> {
				planeShooterServo.setPosition(Constants.getPlaneShooterBusy());
				setTimeout(() -> {
					planeLevelServo.setPosition(Constants.getPlaneLevelerIdle());
					planeShooterServo.setPosition(Constants.getPlaneShooterIdle());
				}, 200);
			}, 300);
		}
	}

	private boolean robotSuspended = false;
	private boolean suspending = false;

	private void Suspender()
	{
		// Locker
		if (currentRobot == Utilities.RobotType.ROBOT_2) {
			if (robotSuspended && liftMotor1.getCurrentPosition() <= TOLERANCE && liftMotor2.getCurrentPosition() <= TOLERANCE)
				setTimeout(() -> lockerServo.setPosition(Constants.getLockerBusy()), 500);
		}

		// Lift
		if (armInput.wasPressedThisFrame(Bindings.Arm.SUSPENDER_CANCEL_KEY) && suspending && !robotSuspended) {
			suspending = false;
			liftMotor2.setTargetPosition(Constants.getSuspenderIdle());
			liftMotor1.setTargetPosition(Constants.getSuspenderIdle());
			RestorePower(tumblerMotor, intakeMotor); // Restore power in case the suspension was interrupted
		} else if (armInput.wasPressedThisFrame(Bindings.Arm.SUSPENDER_KEY)) {
			if (!suspending) {
				CutPower(tumblerMotor, intakeMotor); // Cut power to the wheels as it's not needed
				suspending = true;
				liftMotor2.setTargetPosition(getSuspenderSuspend());
				liftMotor1.setTargetPosition(Constants.getSuspenderSuspend());
			} else if (liftMotor1.getCurrentPosition() >= Constants.getSuspenderSuspend() - TOLERANCE && liftMotor2.getCurrentPosition() >= Constants.getSuspenderSuspend() - TOLERANCE) {
				if(currentRobot == Utilities.RobotType.ROBOT_1) {
					liftMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
					liftMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
					liftMotor2.setTargetPosition(Constants.getSuspenderSuspend() - 800);
					liftMotor1.setTargetPosition(Constants.getSuspenderSuspend() - 800);
				} else {
					liftMotor2.setTargetPosition(Constants.getSuspenderLock());
					liftMotor1.setTargetPosition(Constants.getSuspenderLock());
				}
				CutPower(mecanumDrive.leftBack, mecanumDrive.leftFront, mecanumDrive.rightBack, mecanumDrive.rightFront); // Cut power to the wheels as it's not needed
				robotSuspended = true;
			}
		}
	}

	private void Telemetry()
	{
		// Warn the user if the robot is out of bounds
		if (stackLevel == 0 && pickupMode == Utilities.PickupMode.STACK) {
			telemetry.clearAll();
			telemetry.addLine("[WARN] Robot is out of bounds. Please change pickup mode or reset by pressing start!");
			telemetry.update();
			return;
		}

		// Warn the user when in manual override mode
		if(inResetMode)
		{
			telemetry.clearAll();
			telemetry.addLine("[WARN] Robot is in manual override mode. To exit press Y, to save changes press B");
			telemetry.update();
			return;
		}

		// Warn the user if the robot is in suspension mode
		if (suspending) {
			telemetry.clearAll();
			if (!robotSuspended)
				telemetry.addLine("[WARN] Currently in suspension mode. Press Y to cancel.");
			telemetry.addData("[INFO] Robot Suspended:", robotSuspended ? "Yes" : "No");
			telemetry.update();
			return;
		}

		telemetry.addData("[INFO] Pickup Mode", pickupMode.toString());
		telemetry.addData("[INFO] Arm State", armState.toString());
		telemetry.addData("[INFO] Lift Level", liftLevel);
		telemetry.addData("[INFO] Plane Launched", planeLaunched ? "Yes" : "No");

		if (DEBUG) {
			telemetry.addLine();
			telemetry.addData("[DEBUG] Lift 1", liftMotor1.getCurrentPosition());
			telemetry.addData("[DEBUG] Lift 2", liftMotor2.getCurrentPosition());
			telemetry.addData("[DEBUG] Lift 1 Target", liftMotor1.getTargetPosition());
			telemetry.addData("[DEBUG] Lift 2 Target", liftMotor2.getTargetPosition());
			telemetry.addData("[DEBUG] Tumbler", tumblerMotor.getCurrentPosition());
			telemetry.addData("[DEBUG] Tumbler Target", tumblerMotor.getTargetPosition());
			telemetry.addData("[DEBUG] Rotator", rotatorServo.getPosition());
			telemetry.addData("[DEBUG] Claw", clawServo.getPosition());
			telemetry.addData("[DEBUG] Locker", lockerServo.getPosition());
			telemetry.addData("[DEBUG] Plane Level", planeLevelServo.getPosition());
			telemetry.addData("[DEBUG] Plane Release", planeShooterServo.getPosition());
			telemetry.addData("[DEBUG] Arm Busy", armBusy);
			telemetry.addData("[DEBUG] Arm Waiting", waitingSecondInstruction);
			telemetry.addData("[DEBUG] Arm At Level", atLevel);
			telemetry.addData("[DEBUG] Arm Out Of Bounds", stackLevel == 0);
			telemetry.addData("[DEBUG] Arm Has 2 Stack Pixels", pixelInStorage);
			telemetry.addData("[DEBUG] Stack Level", stackLevel);
		}

		telemetry.update();
	}

	private int LiftLevelToValue(int level)
	{
		return liftLevel == 1 ? Constants.getLiftLevel1() : liftLevel == 2 ? Constants.getLiftLevel2() : liftLevel == 3 ? Constants.getLiftLevel3() : Constants.getLiftLevel4();
	}
}