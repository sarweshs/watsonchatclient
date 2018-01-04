package com.ibm.texttospeech;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class TestSpeechToText {

	private static boolean stopped=true;
	public static void main(String[] args) {
		/*
		 * { "url": "https://stream.watsonplatform.net/speech-to-text/api", "username":
		 * "6eb3fb2f-4901-4f83-8bd6-bdea289c7220", "password": "tnX1Nhxvfjh8" }
		 */
		initForLiveMonitor();
	}

	private byte[] record() throws LineUnavailableException {
		// AudioFormat format = AudioUtil.getAudioFormat(audioConf);
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		// Checks if system supports the data line
		if (!AudioSystem.isLineSupported(info)) {
			return null;
		}

		TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
		microphone.open(format);
		microphone.start();

		System.out.println("Listening, tap enter to stop ...");

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int numBytesRead;
		byte[] data = new byte[microphone.getBufferSize() / 5];
		short[] shorts = new short[data.length / 2];
		long startSilence = 0;
		boolean pause = false;

		// Begin audio capture.
		microphone.start();

		// Here, stopped is a global boolean set by another thread.
		while (!stopped) {
			// Read the next chunk of data from the TargetDataLine.
			numBytesRead = microphone.read(data, 0, data.length);
			ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

			// Save this chunk of data.
			byteArrayOutputStream.write(data, 0, numBytesRead);

			double rms = 0;
			for (int i = 0; i < shorts.length; i++) {
				double normal = shorts[i] / 32768f;
				rms += normal * normal;
			}
			rms = Math.sqrt(rms / shorts.length);
			System.out.println("Listening, rms is " + rms);
			if (rms < 0.1) {
				long now = System.currentTimeMillis();
				if (now - startSilence > 5000 && pause)
					break;
				if (!pause) {
					startSilence = now;
					System.out.println("Listening, new silence at " + startSilence);
				}
				pause = true;
			} else
				pause = false;
		}

		return byteArrayOutputStream.toByteArray();
	}

	private static void initForLiveMonitor() {

		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		//AudioSystem audioSystem = new AudioS

		try {

			// Speaker
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
			sourceLine.open();

			// Microphone
			info = new DataLine.Info(TargetDataLine.class, format);
			TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
			targetLine.open();

			Thread monitorThread = new Thread() {
				@Override
				public void run() {
					int bufferLen = 1024 * 4; // experiment with buffer size here

					byte[] data = new byte[bufferLen];
					//sourceLine.open(bufferLen);
					//targetLine.open(bufferLen);
					targetLine.start();
					sourceLine.start();

					// byte[] data = new byte[targetLine.getBufferSize() / 5];

					int readBytes;

					while (true) {
						readBytes = targetLine.read(data, 0, data.length);
						sourceLine.write(data, 0, readBytes);
					}
				}
			};

			System.out.println("Start LIVE Monitor for 15 seconds");
			monitorThread.start();

			Thread.sleep(15000);
			targetLine.stop();
			targetLine.close();
			System.out.println("End LIVE Monitor");

		} catch (LineUnavailableException lue) {
			lue.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

}
