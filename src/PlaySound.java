
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PlaySound implements LineListener {
//	private static final int SECONDS_IN_HOUR = 60 * 60;
//	private static final int SECONDS_IN_MINUTE = 60;
	int frames;

	/**
	 * this flag indicates whether the playback completes or not.
	 */
	private boolean playCompleted;

	/**
	 * this flag indicates whether the playback is stopped or not.
	 */
	private boolean isStopped;

	private boolean isPaused;

	private Clip audioClip;
	private boolean fastForward = false;
	private AudioFormat format = null;
	String audioFilePath;

	/**
	 * Load audio file before playing back
	 * 
	 * @param audioFilePath
	 *            Path of the audio file.
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws LineUnavailableException
	 */
	public void load(String audioFilePath,int currFrame)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {

		this.audioFilePath = audioFilePath;
		format = null;
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
		format = audioStream.getFormat();
		audioStream.skip(currFrame*4410);
		DataLine.Info info = new DataLine.Info(Clip.class, format);

		audioClip = null;
		audioClip = (Clip) AudioSystem.getLine(info);

		audioClip.addLineListener(this);
		audioClip.open(audioStream);
	}

//	public long getClipSecondLength() {
//		return audioClip.getMicrosecondLength() / 1000000;
//	}

//	public String getClipLengthString() {
//		String length = "";
//		long hour = 0;
//		long minute = 0;
//		long seconds = audioClip.getMicrosecondLength() / 1000000;
//
//		System.out.println(seconds);
//
//		if (seconds >= SECONDS_IN_HOUR) {
//			hour = seconds / SECONDS_IN_HOUR;
//			length = String.format("%02d:", hour);
//		} else {
//			length += "00:";
//		}
//
//		minute = seconds - hour * SECONDS_IN_HOUR;
//		if (minute >= SECONDS_IN_MINUTE) {
//			minute = minute / SECONDS_IN_MINUTE;
//			length += String.format("%02d:", minute);
//
//		} else {
//			minute = 0;
//			length += "00:";
//		}
//
//		long second = seconds - hour * SECONDS_IN_HOUR - minute * SECONDS_IN_MINUTE;
//
//		length += String.format("%02d", second);
//
//		return length;
//	}

	/**
	 * Play a given audio file.
	 * 
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws LineUnavailableException
	 */
	void play() throws IOException {

		audioClip.start();

		playCompleted = false;
		isStopped = false;

		while (!playCompleted) {
			// wait for the playback completes
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {				 
				if(fastForward){
					audioClip.stop();
					audioClip.setFramePosition(0);
					this.frames = frames*4410;
					audioClip.setFramePosition(frames);
					fastForward = false;
//					audioClip.start();
				}
				if (isStopped) {
					audioClip.stop();
					break;
				}
				if (isPaused) {
					audioClip.stop();
				}
				else {
					audioClip.start();
				}
			}
		}

		audioClip.close();

	}

	/**
	 * Stop playing back.
	 */
	public void stop() {
		isStopped = true;
	}

	public void pause() {
		isPaused = true;
	}

	public void resume() {
		isPaused = false;
	}
	
	public void jump(int frames){
		fastForward = true;
	}

	/**
	 * Listens to the audio line events to know when the playback completes.
	 */
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();
		if (type == LineEvent.Type.STOP) {
			if (isStopped || !isPaused) {
				playCompleted = true;
			}
		}
	}

//	public Clip getAudioClip() {
//		return audioClip;
//	}

	public long getPosition() {
		return audioClip.getFramePosition();
	}
	
	public float getSampleRate() {
		return format.getFrameRate();
	}

	
}