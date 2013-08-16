package com.example.noiseprobe;

import java.io.File;
import android.os.Environment;

public class FileHelper {
	

	private String root;
	
	public FileHelper(){
		
		this.root = "";
	}
	
	public String getDir(){
		root = Environment.getExternalStorageDirectory().getPath() + "/NoiseProbe";
		File f = new File(root);
		if(!f.exists()){
			buildDir();
		}
		
		return root;
	}
	
	public boolean buildDir(){
		
    	File dir = new File(root);
    	boolean success = dir.mkdir();
		return success;
	}
	
	
	
}
