import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.io.*;

public class ImageTapestry {
	
	private ArrayList<Integer> sceneIndex;
	private ArrayList<Integer> colorHistogramDifferences;
	private static int originalHeight;
	private static int originalWidth;
	private static long totalFrames;
	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;

	public ImageTapestry(String fileName) throws InterruptedException{
		sceneIndex = new ArrayList<Integer>();
		colorHistogramDifferences = new ArrayList<Integer>();
		originalWidth = 352;
		originalHeight = 288;
		this.sceneIdentificationUsingColorHistogram(fileName);
		//this.sceneIdentificationUsingSAD(fileName);
	}

	public void sceneIdentificationUsingSAD(String fileName) throws InterruptedException {
		img = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);

			long totalFileSize = file.length();
			this.totalFrames = totalFileSize/(3*originalWidth*originalHeight);

			long len = originalWidth*originalHeight*3;
			int offset = 0;
			int count = 0;
			byte[] bytes = new byte[(int)totalFileSize];
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			frame = new JFrame();
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);

			JLabel lbText1 = new JLabel("Video: ");
			lbText1.setHorizontalAlignment(SwingConstants.LEFT);
			JLabel lbText2 = new JLabel("Audio: ");
			lbText2.setHorizontalAlignment(SwingConstants.LEFT);		

			while (count < totalFrames-2) {
				int ind1 = count*originalHeight*originalWidth*3;
				int ind2 = (count+2)*originalHeight*originalWidth*3;
				int sad = 0;
				int numOfBlack = 0;

				for(int y = 0; y < originalHeight; y++){

					for(int x = 0; x < originalWidth; x++){
						
						int r1 = bytes[ind1] & 0xff;
						int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
						int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 

						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						if (r1 == 0 && g1 == 0 && b1 == 0) {
							numOfBlack++;
						}

						int r2 = bytes[ind2] & 0xff;
						int g2 = bytes[ind2+originalHeight*originalWidth] & 0xff;
						int b2 = bytes[ind2+originalHeight*originalWidth*2] & 0xff; 

						int pix2 = 0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff);

				  		sad += pix1 - pix2;
						ind1++;
						ind2++;
					}
				}


				//To decide threshold use the following values: 2139999999, 2119999999, 2123899999 disney when comparing frames direct
				//if there are scenes with black scene cuts use || numOfBlack > 32000

				if (Math.abs(sad) > 2139999999) {
					if (this.sceneIndex.size() != 0) {
						int prevIndex = this.sceneIndex.get(this.sceneIndex.size()-1);
						int currIndex = count*originalWidth*originalHeight*3;
						double prevTime = (double)prevIndex/((double)originalHeight*(double)originalHeight*(double)3*(double)20*(double)60);
						double currTime = (double)currIndex/((double)originalHeight*(double)originalHeight*(double)3*(double)20*(double)60);
						if (currTime > prevTime + 0.2) {
							this.sceneIndex.add(currIndex);
							this.displayFrame(count, bytes);
						}
					}
					else {
						this.sceneIndex.add(count*originalWidth*originalHeight*3);
						this.displayFrame(count, bytes);
					}
				}

				count++;
			}
			this.decideKeyFrames();
			this.printIndexes();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sceneIdentificationUsingColorHistogram(String fileName) throws InterruptedException {
		img = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);

			long totalFileSize = file.length();
			this.totalFrames = totalFileSize/(3*originalWidth*originalHeight);

			long len = originalWidth*originalHeight*3;
			int offset = 0;
			int count = 0;
			byte[] bytes = new byte[(int)totalFileSize];
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			frame = new JFrame();
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);

			JLabel lbText1 = new JLabel("Video: ");
			lbText1.setHorizontalAlignment(SwingConstants.LEFT);
			JLabel lbText2 = new JLabel("Audio: ");
			lbText2.setHorizontalAlignment(SwingConstants.LEFT);	

			while (count < totalFrames-2) {
				int ind1 = count*originalHeight*originalWidth*3;
				int ind2 = (count+2)*originalHeight*originalWidth*3;
				int[][][] color1 = new int[4][4][4];
				int[][][] color2 = new int[4][4][4];

				for(int y = 0; y < originalHeight; y++){

					for(int x = 0; x < originalWidth; x++){
						
						int r1 = bytes[ind1] & 0xff;
						int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
						int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 
						color1[r1 / 64][g1 / 64][b1 / 64]++;

						int r2 = bytes[ind2] & 0xff;
						int g2 = bytes[ind2+originalHeight*originalWidth] & 0xff;
						int b2 = bytes[ind2+originalHeight*originalWidth*2] & 0xff; 
						color2[r2 / 64][g2 / 64][b2 / 64]++;

						ind1++;
						ind2++;
					}
				}

				int sad = 0;
				for (int a = 0; a < 4; a++) {
					for (int b = 0; b < 4; b++) {
						for (int c = 0; c < 4; c++) {
								sad += Math.abs(color1[a][b][c] - color2[a][b][c]);
						}
					}
				}
				this.colorHistogramDifferences.add(Math.abs(sad));

				//decide threshold values

				if (Math.abs(sad) > 89000) {
					if (this.sceneIndex.size() != 0) {
						int prevIndex = this.sceneIndex.get(this.sceneIndex.size()-1);
						int currIndex = count*originalWidth*originalHeight*3;
						double prevTime = (double)prevIndex/((double)originalHeight*(double)originalHeight*(double)3*(double)20*(double)60);
						double currTime = (double)currIndex/((double)originalHeight*(double)originalHeight*(double)3*(double)20*(double)60);
						if (currTime > prevTime + 0.2) {
							this.sceneIndex.add(currIndex);
							this.displayFrame(count, bytes);
						}
					}
					else {
						this.sceneIndex.add(count*originalWidth*originalHeight*3);
						this.displayFrame(count, bytes);
					}
				}

				count++;
			}
			this.decideKeyFrames();
			this.printIndexes();
			//this.setKeyFrames(bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void decideKeyFrames() {
		double mean = 0;
		int max = 0;
		for (int i = 0; i < this.colorHistogramDifferences.size(); i++) {
			if (this.colorHistogramDifferences.get(i) > max) {
				max = this.colorHistogramDifferences.get(i);
			}
			mean += (double)this.colorHistogramDifferences.get(i);
		}
		mean = mean/((double)totalFrames-1);
	    double temp = 0;

	    for (int i = 0; i < this.colorHistogramDifferences.size(); i++)
	    {
	        int val = this.colorHistogramDifferences.get(i);
	        double squrDiffToMean = Math.pow(val - mean, 2);
	        temp += squrDiffToMean;
	    }

	    double meanOfDiffs = (double) temp / (double) (this.colorHistogramDifferences.size());
	    double stdeviation = Math.sqrt(meanOfDiffs);
	    System.out.println("mean is " + mean + " sdt " + stdeviation);
	    System.out.println(mean-stdeviation);
	    System.out.println(mean+stdeviation);
	    int count = 0;
		for (int i = 0; i < this.colorHistogramDifferences.size(); i++) {
			if (this.colorHistogramDifferences.get(i) > mean+stdeviation) {
				count++;
			}
		}
		System.out.println(count);
	}

	// public void setKeyFrames(byte[] bytes) throws InterruptedException {
	// 	int a = 0;
	// 	for (int i = 0; i < this.sceneIndex.size(); i++) {
	// 		a = this.sceneIndex.get(i);
	// 		a = a + 20*originalWidth*originalHeight*3;
	// 		if (a < bytes.length)
	// 			this.keyframeIndex.add(a);
	// 		else 
	// 			this.keyframeIndex.add(this.sceneIndex.get(i));
	// 		this.displayFrameWithIndex(a,bytes);
	// 	}
	// 	this.printKeyFrames();
	// }

	// public void printKeyFrames() throws InterruptedException{
	// 	System.out.println("final number of keyframes " + this.sceneIndex.size());
	// 	System.out.println(this.keyframeIndex);
	// }	

	public void printIndexes() throws InterruptedException{
		System.out.println("final number of scenes " + this.sceneIndex.size());
		System.out.println(this.sceneIndex);
	}

	public void displayFrameWithIndex(int ind1, byte[] bytes) {
		for(int y = 0; y < originalHeight; y++){

			for(int x = 0; x < originalWidth; x++){
				
				int r1 = bytes[ind1] & 0xff;
				int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
				int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 

		        int grayLevel1 = (r1 + g1 + b1) / 3;
		        int gray1 = (grayLevel1 << 16) + (grayLevel1 << 8) + grayLevel1;
				int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
				
		        img.setRGB(x, y, pix1);
				ind1++;
			}
		}

		JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (img)));
	    
	    JFrame frame = new JFrame("Display images");
	    
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}	

	public void displayFrame(int count, byte[] bytes) {
		int ind1 = count*originalHeight*originalWidth*3;

		for(int y = 0; y < originalHeight; y++){

			for(int x = 0; x < originalWidth; x++){
				
				int r1 = bytes[ind1] & 0xff;
				int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
				int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 

		        int grayLevel1 = (r1 + g1 + b1) / 3;
		        int gray1 = (grayLevel1 << 16) + (grayLevel1 << 8) + grayLevel1;
				int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
				
		        img.setRGB(x, y, pix1);
				ind1++;
			}
		}

		JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (img)));
	    
	    JFrame frame = new JFrame("Display images");
	    
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}

	public void showScenes(byte[] bytes) throws InterruptedException {
		int count = 0;
		int count2 = 0;
		int ind1 = 0, ind2 = 0;
		while (count < this.sceneIndex.size()-1) {
			if (count == 0) {
				ind1 = 0;
				ind2 = this.sceneIndex.get(count);
			}
			else {
				ind1 = this.sceneIndex.get(count);
				ind2 = this.sceneIndex.get(count+1);
			}

			while (ind1 < ind2) {
				ind1 = count2*originalHeight*originalWidth*3;
				count2++;
				for(int y = 0; y < originalHeight; y++){

					for(int x = 0; x < originalWidth; x++){
						
						int r1 = bytes[ind1] & 0xff;
						int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
						int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						
				        img.setRGB(x, y, pix1);
						ind1++;
					}
				}
				JPanel  panel = new JPanel ();

			    panel.add (new JLabel (new ImageIcon (img)));
			    
			    JFrame frame = new JFrame("Display images");
			    
			    frame.getContentPane().add (panel);
			    frame.pack();
			    frame.setVisible(true);
			    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				
			}
		
			count++;
			TimeUnit.SECONDS.sleep(5);
		}
	}



}