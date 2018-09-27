package com.marmeto.global;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

public class ErrorHandling {

	
	
	public void handleError(Context context){
		
		
	//	AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	   
	    
	    //int originalVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
	  //  manager.setStreamVolume(AudioManager.STREAM_MUSIC, manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
	    
		try {
		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    Ringtone r = RingtoneManager.getRingtone(context, notification);
		    r.play();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		long[] twice = { 0, 200, 700, 200 };
		 Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		 // Vibrate for 500 milliseconds
	 
		 v.vibrate(twice, -1);
	}
}
