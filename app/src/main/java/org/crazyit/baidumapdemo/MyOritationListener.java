package org.crazyit.baidumapdemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by xxuxe on 2017/12/22.
 */
public class MyOritationListener implements SensorEventListener {
    private Context mContext;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    public void setmOnOrientationListener(OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }

    private OnOrientationListener mOnOrientationListener;
    float lastX=0f;
    public MyOritationListener(Context context){
      this.mContext=context;
    }
    //开始监听
    public void start(){
        mSensorManager= (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager!=null){
          mSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)  ;
        }
        if(mSensor!=null){
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);
        }
    }
    public void stop(){
        mSensorManager.unregisterListener(this);

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
         if(sensorEvent.sensor.getType()==Sensor.TYPE_ORIENTATION){
             float x=sensorEvent.values[SensorManager.DATA_X];
             if(Math.abs(x-lastX)>1.0){
               mOnOrientationListener.onOrientationChanged(x);
             }
             lastX=x;
         }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

   public  interface OnOrientationListener{
       void onOrientationChanged(float x);
   }
}
