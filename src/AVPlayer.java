import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class AVPlayer implements MouseListener, MouseMotionListener {

	// variables to read rgb file
	int ind = 0;
	int offset = 0;
	int numRead = 0;
	// variables to create UI
	JFrame frame;
	JPanel panel, sliderPanel, buttonPanel, tapestry;
	public static JSlider slider;
	int width = 352;
	int height = 288;
	BufferedImage img;
	// variable that holds the start of each frame index in video
	public int[] byteIndicies;
	// number of frames per second
	public int fps = 20;
	// total number of frames in the video file
	double Frames;
	public static File soundFile, file;
	public static InputStream is, sis;
	// length of audio and video file
	public static long len, slen;
	// if doing fast forward like clicking on time slider
	public static boolean fastForward = false;
	public static byte[] bytes, sbytes;
	// to keep track of video state = 0 -> playing, state=1 -> pause, state=2 ->
	// stop
	public static int state;
	// to keep track of playing frame number
	public static int startFrame, currFrame = 0;
	// threads for audio and video
	public Thread soundThread, videoThread, zoomImageThread;
	// to keep track if audio is already playing
	public static boolean isAlreadyPlaying = false;
	// to keep track if audio
	public boolean isPause = false;
	// to store audio and video file name
	public String audioFileName;
	public String videoFileName;
	private String nameOfTapestry;
	public MyButton playButton, stopButton, pauseButton;
	String method;
	PlaySound playSound = new PlaySound();
	boolean enabled = false;
	boolean pbutton_enabled = false;
	boolean sbutton_enabled = false;
	boolean playbutton_enabled = true;

	// variables needed for zoom
	private ArrayList<Integer> sceneIndex, zoom1SceneIndex, zoom2SceneIndex;
	private BufferedImage indexImage, zoom1IndexImage, zoom2IndexImage;
	int threshold_zoom1, threshold_zoom2;
	int zoom_count = 0;
	int start_x;
	int end_x;

	int original_tapestry_size_width,original_tapestry_size_height;

	public AVPlayer(String video, String audio, int[] byteIndicies, double f, int threshold, int threshold2, int threshold3, String method)
			throws IOException {

		// initialization for variables
		this.byteIndicies = byteIndicies;
		this.Frames = f;
		this.audioFileName = audio;
		this.videoFileName = video;

		ImageCreation imageCreation = new ImageCreation(video, threshold, method);
		this.sceneIndex = imageCreation.getSceneIndex();
		System.out.println("======================= tapestry1 done =======================");
		// change this threshold based on video
		this.threshold_zoom1 = threshold2;
		this.threshold_zoom2 = threshold3;
		this.method = method;

		// creating zoom images and getting scene index
		 ImageCreation imageCreation1 = new ImageCreation(video,this.threshold_zoom1, method);
		 this.zoom1SceneIndex = imageCreation1.getSceneIndex();
		System.out.println("======================= tapestry2 done =======================");
		
		 ImageCreation imageCreation2 = new ImageCreation(video,this.threshold_zoom2, method);
		 this.zoom2SceneIndex = imageCreation2.getSceneIndex();
		System.out.println("======================= tapestry3 done =======================");

		this.nameOfTapestry = imageCreation.getName();

		IndexMap indexMap = new IndexMap(threshold, threshold_zoom1, threshold_zoom2, sceneIndex, zoom1SceneIndex,
				zoom2SceneIndex);

		String t = "index_" + threshold + "_out_scaled.png";
		indexImage = ImageIO.read(new File(t));

		// creating zoom index images
		String t1 = "index_" + threshold_zoom1 + "_out_scaled.png";
		zoom1IndexImage = ImageIO.read(new File(t1));
		String t2 = "index_" + threshold_zoom2 + "_out_scaled.png";
		zoom2IndexImage = ImageIO.read(new File(t2));

		// setting slider, stop, pause, play buttons - UI build
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();

		// load video file
		System.out.println("======================= adding all components to player =======================");
		try {
			file = new File(video);
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		len = file.length();
		bytes = new byte[(int) len];

		// get start image of the video and display
		img = refreshFrame(0);
		showImage(frame.getContentPane());

		// adding slider after video screen
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		sliderPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing
		slider = new JSlider();
		slider.setValue(0);
		slider.setMinimum(0);
		slider.setMaximum(6000);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setAutoscrolls(true);
		slider.setMaximumSize(new Dimension(width, 50));
		slider.setEnabled(enabled);
		startFrame = currFrame;

		sliderPanel.add(slider);
		slider.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if(enabled){
				JSlider slider = (JSlider) e.getSource();
				fastForward = true;
				currFrame = (int) ((e.getX() * 1.85 * 9) + startFrame);
				slider.setValue(currFrame);
				isAlreadyPlaying = false;

				videoThread.interrupt();
				soundThread.interrupt();
				playSound.stop();
				try {
					videoThread.sleep(10);
					soundThread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				soundThread = null;
				videoThread = null;
				playSound.jump(currFrame);

				playback();
				}

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

		});

		// adding button after slider
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		playButton = new MyButton("Play");
		buttonPanel.add(playButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
		pauseButton = new MyButton("Pause");
		buttonPanel.add(pauseButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
		stopButton = new MyButton("Stop");
		buttonPanel.add(stopButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		sliderPanel.add(buttonPanel);
		pauseButton.setEnabled(false);
		stopButton.setEnabled(false);

		// need to add tapestry panel
		ImageIcon tap = new ImageIcon(this.nameOfTapestry + "_" + method + "_" + threshold + ".png");
		original_tapestry_size_width = tap.getIconWidth();
		original_tapestry_size_height = tap.getIconHeight();
		JLabel label = new JLabel("", tap, JLabel.CENTER);
		tapestry = new JPanel(new BorderLayout());
		tapestry.add(label, BorderLayout.CENTER);
		tapestry.setEnabled(enabled);

		// to add scrolling
		tapestry.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				// System.out.println(e.getWheelRotation());
				// System.out.println(e.getX());
				// System.out.println(e.getY());
				if(enabled){
				ImageIcon tap = null;
				if (e.getWheelRotation() < 0) {
					// zoom in - increase size

					if (zoom_count < 0) {
						zoom_count = 0;
					}
					zoom_count++;
					// fetch key frame number and get x, y of that frame in new
					// tapestry then crop image to that frame
					Hashtable<Integer, ArrayList<ArrayList<Integer>>> xy = indexMap.getIndexTable();
					tapestry.removeAll();
					BufferedImage i = null;
					if (zoom_count == 1) {
						int pix = indexImage.getRGB(e.getX(), e.getY());
						int frameNum = (pix >> 16) & 0x000000FF;
						int keyFrameIndex = sceneIndex.get(frameNum);
						int frame = keyFrameIndex / (352 * 288 * 3);

						// find x,y of new tapestry
						System.out.println("x point in new tapestry: " + xy.get(frame).get(1).get(0));

						try {
							i = ImageIO.read(new File(nameOfTapestry + "_" + method + "_" + threshold_zoom1 + ".png"));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						int zoom_image_width = i.getWidth();
						int middle = zoom_image_width - original_tapestry_size_width;

						int T = 5;

						if (xy.get(frame).get(1).get(0) - T < 0) {
							start_x = 0;
							end_x = original_tapestry_size_width;
						} else if ((xy.get(frame).get(1).get(0) - T + original_tapestry_size_width) > zoom_image_width) {
							start_x = zoom_image_width - original_tapestry_size_width;
							end_x = zoom_image_width;
						} else {
							start_x = xy.get(frame).get(1).get(0) - T;
							end_x = xy.get(frame).get(1).get(0) - T + original_tapestry_size_width;
						}
						System.out.println("x_start point : " + start_x);
						System.out.println("x_endt point : " + end_x);

						i = i.getSubimage(start_x, 0, (end_x - start_x), original_tapestry_size_height);
						tap = new ImageIcon(i);
						zoom_count=1;

					} else if (zoom_count >= 2) {

						int pix = zoom1IndexImage.getRGB(e.getX(), e.getY());
						int frameNum = (pix >> 16) & 0x000000FF;
						int keyFrameIndex = zoom1SceneIndex.get(frameNum);
						int frame = keyFrameIndex / (352 * 288 * 3);

						// find x,y of new tapestry
						System.out.println("x point in new tapestry: " + xy.get(frame).get(2).get(0));

						try {
							i = ImageIO.read(new File(nameOfTapestry + "_" + method + "_" + threshold_zoom2 + ".png"));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						int zoom_image_width = i.getWidth();
						int middle = zoom_image_width - original_tapestry_size_width;

						int T = 20;

						if (xy.get(frame).get(2).get(0) - T < 0) {
							start_x = 0;
							end_x = original_tapestry_size_width;
						} else if ((xy.get(frame).get(2).get(0) - T + original_tapestry_size_width) > zoom_image_width) {
							start_x = zoom_image_width - original_tapestry_size_width;
							end_x = zoom_image_width;
						} else {
							start_x = xy.get(frame).get(2).get(0) - T;
							end_x = xy.get(frame).get(2).get(0) - T + original_tapestry_size_width;
						}
						System.out.println("x_start point : " + start_x);
						System.out.println("x_endt point : " + end_x);

						i = i.getSubimage(start_x, 0, (end_x - start_x), original_tapestry_size_height);
						tap = new ImageIcon(i);
						zoom_count=2;

					}

					JLabel label = new JLabel("", tap, JLabel.CENTER);

					tapestry.add(label, BorderLayout.CENTER);

					tapestry.revalidate();
					tapestry.repaint();

				} else {
					// zoom out - decrease size
					if (zoom_count > 2) {
						zoom_count = 2;
					}
					Hashtable<Integer, ArrayList<ArrayList<Integer>>> xy = indexMap.getIndexTable();
					tapestry.removeAll();
					if (zoom_count >= 2) {
						int pix = zoom2IndexImage.getRGB(e.getX(), e.getY());
						int frameNum = (pix >> 16) & 0x000000FF;
						int keyFrameIndex = zoom2SceneIndex.get(frameNum);
						int frame = keyFrameIndex / (352 * 288 * 3);

						// find x,y of new tapestry
						System.out.println("x point in new tapestry: " + xy.get(frame).get(1).get(0));
						BufferedImage i = null;
						try {
							i = ImageIO.read(new File(nameOfTapestry + "_" + method + "_" + threshold_zoom1 + ".png"));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						int zoom_image_width = i.getWidth();
						int middle = zoom_image_width - original_tapestry_size_width;

						int T = 20;

						if (xy.get(frame).get(1).get(0) - T < 0) {
							start_x = 0;
							end_x = original_tapestry_size_width;
						} else if ((xy.get(frame).get(1).get(0) - T + original_tapestry_size_width) > zoom_image_width) {
							start_x = zoom_image_width - original_tapestry_size_width;
							end_x = zoom_image_width;
						} else {
							start_x = xy.get(frame).get(1).get(0) - T;
							end_x = xy.get(frame).get(1).get(0) - T + original_tapestry_size_width;
						}
						System.out.println("x_start point : " + start_x);
						System.out.println("x_endt point : " + end_x);

						i = i.getSubimage(start_x, 0, (end_x - start_x), original_tapestry_size_height);
						tap = new ImageIcon(i);
						zoom_count = 1;

					} else if (zoom_count <= 1) {
						tap = new ImageIcon(nameOfTapestry + "_" + method + "_" + threshold + ".png");
						zoom_count = 0;
					}
					JLabel label = new JLabel("", tap, JLabel.CENTER);

					tapestry.add(label, BorderLayout.CENTER);

					tapestry.revalidate();
					tapestry.repaint();
				}

			}
			}

		});

		tapestry.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if(enabled){
				int pix, frameNum, keyFrameIndex;
				System.out.println("x: " + e.getX() + " y: " + e.getY());
				fastForward = true;
				if (zoom_count == 0) {
					pix = indexImage.getRGB(e.getX(), e.getY());
					frameNum = (pix >> 16) & 0x000000FF;
					keyFrameIndex = sceneIndex.get(frameNum);
					currFrame = keyFrameIndex / (352 * 288 * 3);
				} else if (zoom_count == 1) {
					pix = zoom1IndexImage.getRGB(e.getX() + start_x, e.getY());
					frameNum = (pix >> 16) & 0x000000FF;
					keyFrameIndex = zoom1SceneIndex.get(frameNum);
					currFrame = keyFrameIndex / (352 * 288 * 3);
				} else if (zoom_count == 2) {
					pix = zoom2IndexImage.getRGB(e.getX() + start_x, e.getY());
					frameNum = (pix >> 16) & 0x000000FF;
					keyFrameIndex = zoom2SceneIndex.get(frameNum);
					currFrame = keyFrameIndex / (352 * 288 * 3);
				}

				if (currFrame > 40) {
					currFrame -= 40;
				}
				isAlreadyPlaying = false;

				videoThread.interrupt();
				soundThread.interrupt();
				playSound.pause();
				try {
					videoThread.sleep(10);
					soundThread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				soundThread = null;
				videoThread = null;
				playSound.jump(currFrame);

				playback();

			}
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		sliderPanel.add(tapestry);

		System.out.println("======================= loading video player =======================");
		// adding whole panel to frame
		frame.getContentPane().add(sliderPanel, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	// button class for displaying button and taking click on button
	class MyButton extends JButton {
		MyButton(String label) {
			setFont(new Font("Helvetica", Font.BOLD, 10));
			setText(label);
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					buttonPressed(getText());
				}
			});
		}
	}

	// playing video and audio based on the state or button press
	public void buttonPressed(String name) {
		if (name.equals("Play") && isAlreadyPlaying == false && playbutton_enabled == true) { // Play
			playButton.setEnabled(false);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			playbutton_enabled = false;
			enabled = true;
			pbutton_enabled = true;
			sbutton_enabled = true;
			slider.setEnabled(enabled);
			tapestry.setEnabled(enabled);
			isAlreadyPlaying = true;
			state = 0;
			playback();
		} else if (name.equals("Play") && state == 1 && playbutton_enabled == true ) { // resume
			playButton.setEnabled(false);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			pbutton_enabled = true;
			sbutton_enabled = true;
			playbutton_enabled = false;
			enabled = true;
			slider.setEnabled(enabled);
			tapestry.setEnabled(enabled);
			isAlreadyPlaying = true;
			state = 0;
			isPause = false;
			playSound.resume();
			videoThread.interrupt();
			soundThread.interrupt();
		} else if (name.equals("Pause") && pbutton_enabled == true) { // Pause
			playButton.setEnabled(true);
			pauseButton.setEnabled(false);
			stopButton.setEnabled(true);
			pbutton_enabled = false;
			playbutton_enabled = true;
			sbutton_enabled = true;
			enabled = false;
			slider.setEnabled(enabled);
			tapestry.setEnabled(enabled);
			state = 1;
			isPause = true;
			videoThread.interrupt();
			soundThread.interrupt();
			playSound.pause();
		} else if (name.equals("Stop") && sbutton_enabled == true) { // Stop
			playButton.setEnabled(true);
			pauseButton.setEnabled(false);
			stopButton.setEnabled(false);
			pbutton_enabled = false;
			playbutton_enabled = true;
			sbutton_enabled = false;
			enabled = false;
			slider.setEnabled(enabled);
			tapestry.setEnabled(enabled);
			state = 2;
			currFrame = 0;
			isAlreadyPlaying = false;
			soundThread.interrupt();
			videoThread.interrupt();
			playSound.stop();
		}
	}

	// start the video and audio when play button is clicked
	public void playback() {
		try {
			playSound.load(audioFileName, currFrame);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		fastForward = false;
		soundThread = null;
		soundThread = new Thread(new sound());
		soundThread.start();
		videoThread = null;
		videoThread = new Thread(new video());
		videoThread.start();
	}

	// get new frame of video based on frame number
	public BufferedImage refreshFrame(int frame_no) {

		ind = byteIndicies[frame_no];

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		try {

			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					byte r = bytes[ind];
					byte g = bytes[ind + height * width];
					byte b = bytes[ind + height * width * 2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x, y, pix);
					ind++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	// to show the start frame of the video
	public void showImage(Container pane) {
		// show the initial panel and start of video
		currFrame = 0;
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		pane.add(panel, BorderLayout.CENTER);

	}

	// repaint the video after every frame
	public void videoOriginal(BufferedImage img) {
		// update image
		currFrame++;
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		slider.setValue(currFrame);
	}

	// start playing sound thread
	public class sound implements Runnable {
		public void run() {
			try {
				playSound.play();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// start playing video thread
	public class video implements Runnable {
		public void run() {
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			double spf = playSound.getSampleRate() / fps;
			int j = 0;
			while ((j < Math.round(playSound.getPosition()) / spf) && fastForward == false) {
				img = refreshFrame(currFrame);
				videoOriginal(img);
				j++;
			}
			while (j > Math.round(playSound.getPosition() / spf) && fastForward == false) {
				// Do Nothing : this to slow down the video number of frames per
				// sec
			}

			for (int i = j; i < Frames && fastForward == false; i++) {
				if (currFrame >= Frames) {
					buttonPressed("Stop");
					break;
				}
				else if(state==2||state==1){
					break;
				}
				// Video ahead of audio, wait for audio to catch up
				else {
					while (i > Math.round(playSound.getPosition() / spf) && fastForward == false) {
						// Do Nothing
						if(state==2)
							break;
					}

					while (i < Math.round(playSound.getPosition() / spf) && fastForward == false) {
						img = refreshFrame(currFrame);
						videoOriginal(img);
						i++;
					}

					img = refreshFrame(currFrame);
					videoOriginal(img);
					System.gc();
				}
			}
		}
	}
}