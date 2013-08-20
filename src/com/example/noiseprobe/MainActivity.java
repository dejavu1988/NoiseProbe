package com.example.noiseprobe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import com.musicg.wave.Wave;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;

public class MainActivity extends Activity {

	private Button start_btn;
	private EditText tag_txt;
	private TextView result;
	private String tag, time, root, wavName, wavPath;
	private int rmsPwrMax;
	private ExtAudioRecorder extAudioRecorder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tag_txt = (EditText) findViewById(R.id.tag);
		result = (TextView) findViewById(R.id.result);
		start_btn = (Button) findViewById(R.id.start);
		start_btn.setOnClickListener(new StartListener());
	}

	class StartListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			new AudioTask().execute();
		}
		
	}
	
	private class AudioTask extends AsyncTask<Void, Void, Void> {
		@Override
		public void onPreExecute(){
			start_btn.setText("Recording");
			start_btn.setEnabled(false);
			result.setText("...");
			
			Time now = new Time();
	    	now.setToNow(); 
	    	time = now.format2445();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			FileHelper fh = new FileHelper();
			root = fh.getDir();
			wavName = "pcm16_"+time+".wav";
			wavPath = root + "/" + wavName;
			
			extAudioRecorder = ExtAudioRecorder.getInstanse(false);			
			extAudioRecorder.setOutputFile(wavPath);
			
			try {
				//recorder.prepare();
				extAudioRecorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			extAudioRecorder.start();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			extAudioRecorder.stop();
			extAudioRecorder.reset();
			extAudioRecorder.release();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Wave wave = new Wave(wavPath);
			Log.i("wav", wave.toString());
	    	short[] amps = wave.getSampleAmplitudes();
	    	//List<Float> normalized_data = getNormalizedTimebasedSignal(amps);
	    	//Log.i("wav", "Normalized: "+ normalized_data.toString());
	    	rmsPwrMax = getRMSPwrMax(amps,2048);
			
			

		    /*byte[] input = wave.getBytes();
		    final int decompressedLength = input.length;
		    Log.i("wav", "Decompressed: "+ decompressedLength);
		    
		    // Compress the bytes
			byte[] output = new byte[decompressedLength];
			Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
			compresser.setInput(input); 
			compresser.finish();
			int compressedDataLength = compresser.deflate(output);  		    
		    rmsPwrMax = compressedDataLength;*/
			
			return null;
			
		}

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	
	    	result.setText(String.valueOf(rmsPwrMax));
	    	tag = tag_txt.getText().toString();
	    	try {
				saveToCSV(root, time, tag, String.valueOf(rmsPwrMax) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	start_btn.setText("Start");
	    	start_btn.setEnabled(true);
	    	tag_txt.setText("");
	    	
	    }
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*private List<Float> getNormalizedTimebasedSignal(short[] amps){
		int n = amps.length;
		List<Short> ampList = Arrays.asList(ArrayUtils.toObject(amps));
		List<Float> normalized_data = new ArrayList<Float>();
		int counter = 0;
		long sum_pwr = 0;
		for(short i: ampList){
			sum_pwr += (long)i * i;	
		}
		float tmp_pwr = (float) Math.sqrt((double)sum_pwr);
		Log.i("wav", "Sum of Energy: "+tmp_pwr);
		for(short j: ampList){
			normalized_data.add((float)j/tmp_pwr);
		}
		return normalized_data;
	}*/
	
	private int getRMSPwrMax(short[] amps, int numOfSample){
		int window = numOfSample;
		int n = amps.length;
		int n_slide_window = n - window + 1;
		List<Short> ampList = Arrays.asList(ArrayUtils.toObject(amps));
		List<Long> pwrList = new ArrayList<Long>();
		List<Float> rms_data = new ArrayList<Float>();
		int counter = 0;
		long sum_pwr = 0;
		for(short i: ampList){
			long pwr = (long)i * i;
			pwrList.add(pwr);
			if(counter < window){
				sum_pwr += pwr;
				counter += 1;
			}			
		}
		rms_data.add((float) Math.sqrt((double)sum_pwr/window));
		for(int i = 1; i < n_slide_window; i++){
			sum_pwr = sum_pwr - pwrList.get(i-1) + pwrList.get(i+window-1);
			rms_data.add((float) Math.sqrt((double)sum_pwr/window));
		}
		return Math.round(Collections.max(rms_data));
	}
	
	private boolean saveToCSV(String root, String time, String tag, String pwr) throws IOException{
		
		String backupDBPath = "record.csv";
		File sd = new File(root);
		
		final String entryString = time + "#" + tag + "#" + pwr + "\n";
        File file = new File(sd, backupDBPath);
    	FileWriter filewriter = null;
    	filewriter = new FileWriter(file, true); 
        BufferedWriter out = new BufferedWriter(filewriter);
        out.write(entryString);
        out.close();
		return false;
		
	}

}
