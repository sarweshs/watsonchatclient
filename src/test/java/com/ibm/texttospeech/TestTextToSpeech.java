package com.ibm.texttospeech;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

public class TestTextToSpeech {

	public static void main(String[] args) {
		System.setProperty("http.proxyHost", "seproxy.hm.com");
	    System.setProperty("http.proxyPort", "8080");
	    System.setProperty("https.proxyHost", "seproxy.hm.com");
	    System.setProperty("https.proxyPort", "8080");
	    System.setProperty("http.nonProxyHosts","*.hm.com");
		TextToSpeech service = new TextToSpeech();
		service.setUsernameAndPassword("743ba06e-a167-4290-8118-c703bcc1e317", "XENuPgo6TE2R");

		try {
			String text = "Hello world";
			InputStream stream = service.synthesize(text, Voice.EN_ALLISON,
					com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat.WAV).execute();
			InputStream in = WaveUtils.reWriteWaveHeader(stream);
			/*
			 * OutputStream out = new FileOutputStream("hello_world.wav"); byte[] buffer =
			 * new byte[1024]; int length; while ((length = in.read(buffer)) > 0) {
			 * out.write(buffer, 0, length); } out.close();
			 */
			/*AudioInputStream audioIn = AudioSystem.getAudioInputStream(in);
			// Get a sound clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			clip.start();
			
			audioIn.close();*/
			playSound(in);
			in.close();
			stream.close();
			//while(!audioIn.)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Taken from http://www3.ntu.edu.sg/home/ehchua/programming/java/J8c_PlayingSound.html
	
	 public static void playSound(InputStream soundFile) {
	      SourceDataLine soundLine = null;
	      int BUFFER_SIZE = 64*1024;  // 64 KB
	   
	      // Set up an audio input stream piped from the sound file.
	      try {
	         //File soundFile = new File("gameover.wav");
	         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
	         AudioFormat audioFormat = audioInputStream.getFormat();
	         DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	         soundLine = (SourceDataLine) AudioSystem.getLine(info);
	         soundLine.open(audioFormat);
	         soundLine.start();
	         int nBytesRead = 0;
	         byte[] sampledData = new byte[BUFFER_SIZE];
	         while (nBytesRead != -1) {
	            nBytesRead = audioInputStream.read(sampledData, 0, sampledData.length);
	            if (nBytesRead >= 0) {
	               // Writes audio data to the mixer via this source data line.
	               soundLine.write(sampledData, 0, nBytesRead);
	            }
	         }
	      } catch (UnsupportedAudioFileException ex) {
	         ex.printStackTrace();
	      } catch (IOException ex) {
	         ex.printStackTrace();
	      } catch (LineUnavailableException ex) {
	         ex.printStackTrace();
	      } finally {
	         soundLine.drain();
	         soundLine.close();
	      }
	   }

}
