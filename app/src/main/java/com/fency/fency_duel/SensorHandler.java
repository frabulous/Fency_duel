package com.fency.fency_duel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorHandler extends FencyHandler implements SensorEventListener {

    private final long attackMinLength = 350000000; //nanoseconds
    private final float sogliaY = 5.0f;
    private final int sogliaRoll = 55, pitch_upbound = -60, pitch_midbound = -5, pitch_lowbound = 60;

    private SensorFusion sensorFusion;
    private SensorManager sensorManager;
    private Sensor sensorLinearAcceleration;
    private int state;
    private float yPrevious, yDelta, peak;
    private boolean start;
    private long startingAttackTime;
    private final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;

    public SensorHandler(FencyModeActivity context, Player player) {
        super(context, player);

        sensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorFusion = new SensorFusion();
        sensorFusion.setMode(SensorFusion.Mode.GYRO);
    }

    public void registerListeners() {
        // Register 3 sensors for SensorFusion
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SENSOR_DELAY);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SENSOR_DELAY);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SENSOR_DELAY);

        // Register sensorLinearAcc
        sensorManager.registerListener(this, sensorLinearAcceleration, SENSOR_DELAY);

        //
        start = true;
    }

    public void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int id = event.sensor.getType();
        //handle SensorFusion cases
        switch (id) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorFusion.setAccel(event.values);
                sensorFusion.calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                sensorFusion.gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorFusion.setMagnet(event.values);
                break;
        }

        if (id == Sensor.TYPE_LINEAR_ACCELERATION) {

            float yCurrent = event.values[1];
            double pitch = sensorFusion.getPitch();
            double roll = sensorFusion.getRoll();

            if (roll > sogliaRoll || roll < -sogliaRoll) {
                //("Invalid")
                player.changeState(R.integer.INVALID);
            } else if (pitch > pitch_upbound && pitch < pitch_midbound) {
                //("High Guard")
                player.changeState(R.integer.HIGH_STAND);
            } else if (pitch >= pitch_midbound && pitch < pitch_lowbound) {
                //("Low Guard")
                player.changeState(R.integer.LOW_STAND);
            } else {
                //("Invalid")
                player.changeState(R.integer.INVALID);
            }

            if(start){
                // Initialize last y
                yPrevious = yCurrent;
                start = false;
            }
            else {
                yDelta = yCurrent - yPrevious;

                switch (state) {
                    case 0:
                        if (yCurrent > sogliaY && yDelta > 0
                                && player.getState() != R.integer.INVALID) {
                            //save the starting time of the (possible) attack
                            startingAttackTime = event.timestamp;
                            //go to state 1
                            state = 1;
                        }
                        else if (yCurrent < -sogliaY && yDelta < 0){
                            //go to state -1
                            state = -1;
                        }
                        break;
                    case 1:
                        //waiting for positive spike
                        if (yDelta < 0) {
                            peak = yCurrent;
                            //go to state 2
                            state = 2;
                        }
                        break;
                    case 2:
                        //waiting for negative spike
                        if (yDelta > 0) {
                            float spike2 = yCurrent;
                            if (player.getState() != R.integer.INVALID &&
                                    (peak + spike2 < 0) && (event.timestamp- startingAttackTime > attackMinLength)) {
                                //AFFONDO!
                                if(player.getState()==R.integer.LOW_STAND)
                                    player.changeState(R.integer.LOW_ATTACK);
                                else if (player.getState()==R.integer.HIGH_STAND)
                                    player.changeState(R.integer.HIGH_ATTACK);
                            }
                            startingAttackTime = 0;
                            peak = 0;
                            //go to state 0
                            state = 0;
                        }
                        break;
                    case -1:
                        //waiting for negative spike
                        if(yDelta>0){
                            //go to state -2
                            state = -2;
                        }
                        break;
                    case -2:
                        //waiting for positive spike
                        if (yDelta<0){
                            //back to state 0 (idle)
                            state = 0;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
