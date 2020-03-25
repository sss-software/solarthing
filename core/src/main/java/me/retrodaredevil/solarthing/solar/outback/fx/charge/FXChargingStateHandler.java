package me.retrodaredevil.solarthing.solar.outback.fx.charge;

import me.retrodaredevil.solarthing.solar.outback.fx.ACMode;
import me.retrodaredevil.solarthing.solar.outback.fx.FXStatusPacket;
import me.retrodaredevil.solarthing.solar.outback.fx.OperationalMode;

public class FXChargingStateHandler {
	private final FXChargingSettings fxChargingSettings;

	private final ModeTimer rebulkTimer = new ModeTimer(1000 * 90);

	private final ModeTimer absorbTimer;
	private final ModeTimer floatTimer;
	private final ModeTimer equalizeTimer;

	private boolean atAbsorbSetpoint = false;
	private boolean atEqualizeSetpoint = false;
	private boolean atFloatSetpoint = false;

	private OperationalMode previousOperationalMode = null;

	public FXChargingStateHandler(FXChargingSettings fxChargingSettings) {
		this.fxChargingSettings = fxChargingSettings;
		absorbTimer = new ModeTimer(fxChargingSettings.getAbsorbTimeMillis());
		floatTimer = new ModeTimer(fxChargingSettings.getFloatTimeMillis());
		equalizeTimer = new ModeTimer(fxChargingSettings.getEqualizeTimeMillis());
	}


	public void update(long deltaTimeMillis, FXStatusPacket fx){
		update(deltaTimeMillis, fx.getOperationalMode(), fx.getBatteryVoltage());
	}
	public void update(long deltaTimeMillis, OperationalMode operationalMode, float batteryVoltage){
		OperationalMode previousOperationalMode = this.previousOperationalMode;
		this.previousOperationalMode = operationalMode;

		float refloatVoltage = fxChargingSettings.getRefloatVoltage();
		if(batteryVoltage <= refloatVoltage){
			floatTimer.resetTimer();
			atFloatSetpoint = false;
		}
		Float rebulkVoltage = fxChargingSettings.getRebulkVoltage();
		if(rebulkVoltage != null && batteryVoltage <= rebulkVoltage){
			rebulkTimer.countDown(deltaTimeMillis);
			if(rebulkTimer.isDone()) {
				absorbTimer.resetTimer();
				floatTimer.resetTimer();
				equalizeTimer.resetTimer();
				atAbsorbSetpoint = false;
				atEqualizeSetpoint = false;
				atFloatSetpoint = false;
			}
		} else {
			rebulkTimer.resetTimer();
		}
		if(operationalMode == OperationalMode.CHARGE){
			atEqualizeSetpoint = false;
			if(batteryVoltage >= fxChargingSettings.getAbsorbVoltage()){
				atAbsorbSetpoint = true;
			}
			if(atAbsorbSetpoint){
				absorbTimer.countDown(deltaTimeMillis);
			}
		} else if(operationalMode == OperationalMode.EQ){
			atAbsorbSetpoint = false;
			if(batteryVoltage >= fxChargingSettings.getEqualizeVoltage()){
				atEqualizeSetpoint = true;
			}
			if(atEqualizeSetpoint){
				equalizeTimer.countDown(deltaTimeMillis);
			}
		} else if(operationalMode == OperationalMode.SILENT){
			absorbTimer.resetTimer();
			equalizeTimer.resetTimer();
			atAbsorbSetpoint = false;
			atEqualizeSetpoint = false;
			atFloatSetpoint = false;
		} else if(operationalMode == OperationalMode.FLOAT){
			absorbTimer.resetTimer();
			equalizeTimer.resetTimer();
			atAbsorbSetpoint = false;
			atEqualizeSetpoint = false;
			if(previousOperationalMode == OperationalMode.SILENT){ // there's never an instance where a transition from silent to float should have the float timer counted down any amount
				floatTimer.resetTimer();
			}
			if(batteryVoltage >= fxChargingSettings.getFloatVoltage()){
				atFloatSetpoint = true;
			}
			if(atFloatSetpoint){
				floatTimer.countDown(deltaTimeMillis);
			}
		} else { // not charging
			atAbsorbSetpoint = false;
			atEqualizeSetpoint = false;
			atFloatSetpoint = false;
			if(rebulkVoltage == null){ // older FXs do not support continuing cycle through AC loss
				absorbTimer.resetTimer();
				floatTimer.resetTimer();
				equalizeTimer.resetTimer();
			}
		}
	}
	public FXChargingMode getMode(){
		OperationalMode operationalMode = this.previousOperationalMode;
		if(operationalMode == null){
			return null;
		} else if(operationalMode == OperationalMode.CHARGE){
			if(atAbsorbSetpoint){
				return FXChargingMode.ABSORB;
			}
			return FXChargingMode.BULK_TO_ABSORB;
		} else if(operationalMode == OperationalMode.EQ){
			if(atEqualizeSetpoint){
				return FXChargingMode.EQ;
			}
			return FXChargingMode.BULK_TO_EQ;
		} else if(operationalMode == OperationalMode.FLOAT){
			if(atFloatSetpoint){
				return FXChargingMode.FLOAT;
			}
			return FXChargingMode.REFLOAT;
		} else if(operationalMode == OperationalMode.SILENT){
			return FXChargingMode.SILENT;
		}
		return null;
	}
	public long getRemainingAbsorbTimeMillis(){
		return absorbTimer.getRemainingTimeMillis();
	}
	public long getRemainingFloatTimeMillis(){
		return floatTimer.getRemainingTimeMillis();
	}
	public long getRemainingEqualizeTimeMillis(){
		return equalizeTimer.getRemainingTimeMillis();
	}
}