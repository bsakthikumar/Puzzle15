package in.iandroid.puzzle15;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		/*
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Intent intent = new Intent(Splash.this, MainActivity.class);
		startActivity(intent);
		*/
		
		new CountDownTimer(5000, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				return;
			}
			
			@Override
			public void onFinish() {
				Intent intent = new Intent(Splash.this, MainActivity.class);
				finish();
				startActivity(intent);
			}
		}.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

}
