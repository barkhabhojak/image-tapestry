import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.*;
import java.awt.Graphics2D;


public class ImageCreation {
	KeyFrameIdentification keyFrames;
	private ArrayList<Integer> sceneIndex;
	private ArrayList<Integer> lowerBoundryValues;
	private ArrayList<Integer> upperBoundryValues;
	private static int threshold;
	private boolean[] face;
	private static int originalWidth;
	private static int originalHeight;
	private static int newWidth;
	private static int newHeight;
	BufferedImage tapestry;
	
	public ImageCreation(String fileName, int threshold) throws IOException {
	    //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    keyFrames = new KeyFrameIdentification(fileName,threshold);
	    sceneIndex = new ArrayList<Integer>();
	    lowerBoundryValues = new ArrayList<Integer>();
	    upperBoundryValues = new ArrayList<Integer>();
	    sceneIndex = keyFrames.getSceneIndex();
	    this.threshold = threshold;
	    face = new boolean[sceneIndex.size()];
	    for (int i = 0; i < face.length; i++) {
	    	face[i] = false;
	    }
	    keyFrames.printIndexes();
	    this.originalWidth = 352;
	    this.originalHeight = 288;
	    this.newWidth = 200;
	    this.newHeight = 155;
	    //this.imageTapestryViaSeamCarving();
	    //this.runFaceDetection();
	    System.out.println("Running seam carving...");
	    this.runSeamCarving();
	    System.out.println("Stitching images together...");
	    this.finalOutputImageAfterSeam();
	}

	public int getNewHeight() {
		return this.newHeight;
	}

	public int getNewWidth() {
		return this.newWidth;
	}

	public ArrayList<Integer> getSceneIndex() {
		return this.sceneIndex;
	}

	public ArrayList<Integer> getLowerBoundryValues() {
		return this.lowerBoundryValues;
	}

	public ArrayList<Integer> getUpperBoundryValues() {
		return this.upperBoundryValues;
	}


	public void runSeamCarving() throws IOException {
	    int count = 0;
	    while (count < this.sceneIndex.size()) {
	    	String fileName = "keyframes/" + threshold + "_" + count + ".png";
	    	String outputFileName = "out" + count + ".png";
	    	SeamCarver seamCarver = new SeamCarver(fileName,outputFileName,this.originalWidth - this.newWidth, this.originalHeight - this.newHeight);
	    	count++;
	    }
	}

	public void finalOutputImageAfterSeam() throws IOException {
		int count = 0;
		int tapX = 0;
		int tapY = 0;
		int width = 0;
		int maxSize = this.sceneIndex.size();
		System.out.println("maxSize "+maxSize);
		if ((maxSize-1)%2 == 0)
			width = ((maxSize-1)/2+1)*this.newWidth - ((maxSize-1)/2)*5;
		else
			width = ((maxSize-1)/2+1)*this.newWidth + this.newWidth/4 - ((maxSize-1)/2)*5;

		System.out.println("size "+ width +" x "+(this.newHeight*2-5));
	    tapestry = new BufferedImage(width, this.newHeight*2-5, BufferedImage.TYPE_INT_RGB);

		while (count < maxSize) {
			System.out.println("on count number: " + count);
			String fileName = "out" + count + ".png";
			BufferedImage img = ImageIO.read(new File(fileName));
			if (count%2 == 0) {
				tapY = 0;
				if (count == 0)
					tapX = 0;
				else
					tapX = (count/2)*this.newWidth - (count/2)*5;
				//System.out.println("tapX: " + tapX + " tapY: " + tapY);
				this.upperBoundryValues.add(tapX);
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				if (tapestry.getRGB(tapX,tapY) == -16777216) {
	    					tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				}
	    				else {
	    					int rgb = tapestry.getRGB(tapX,tapY);
	    					double r = (rgb >> 16) & 0x000000FF;
							double g = (rgb >> 8 ) & 0x000000FF;
							double b = rgb & 0x000000FF;
							int rgb1 = img.getRGB(x,y);
							double r1 = (rgb1 >> 16) & 0x000000FF;
							double g1 = (rgb1 >> 8 ) & 0x000000FF;
							double b1 = rgb1 & 0x000000FF;
							int avgr = (int) (r*0.65 + r1*0.35);
							int avgg = (int) (g*0.65 + g1*0.35);
							int avgb = (int) (b*0.65 + b1*0.35);
							int pix1 = 0xff000000 | ((avgr & 0xff) << 16) | ((avgg & 0xff) << 8) | (avgb & 0xff);
							tapestry.setRGB(tapX,tapY,pix1);
	    				}
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.newWidth - (count/2)*5;
	    			tapY++;
	    		}

			}
			else {
				tapY = this.newHeight - 5;
				if (count == 1) {
					tapX = this.newWidth/4;
				}
				else {
					tapX = (count/2)*this.newWidth + (this.newWidth/4) - (count/2)*5;
				}
				this.lowerBoundryValues.add(tapX);
				//System.out.println("tapX: " + tapX + " tapY: " + tapY);
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				if (tapestry.getRGB(tapX,tapY) == -16777216) {
	    					tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				}
	    				else {
	    					int rgb = tapestry.getRGB(tapX,tapY);
	    					double r = (rgb >> 16) & 0x000000FF;
							double g = (rgb >> 8 ) & 0x000000FF;
							double b = rgb & 0x000000FF;
							int rgb1 = img.getRGB(x,y);
							double r1 = (rgb1 >> 16) & 0x000000FF;
							double g1 = (rgb1 >> 8 ) & 0x000000FF;
							double b1 = rgb1 & 0x000000FF;
							int avgr = (int) (r*0.65 + r1*0.35);
							int avgg = (int) (g*0.65 + g1*0.35);
							int avgb = (int) (b*0.65 + b1*0.35);
							int pix1 = 0xff000000 | ((avgr & 0xff) << 16) | ((avgg & 0xff) << 8) | (avgb & 0xff);
							tapestry.setRGB(tapX,tapY,pix1);
	    				}
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.newWidth + (this.newWidth/4) - (count/2)*5;
	    			tapY++;
	    		}
			}
			count++;
		}
		BufferedImage tap = this.blurEdgesOfImage(tapestry);

		BufferedImage scaledImg = new BufferedImage(width/2,(this.newHeight*2-5)/2,BufferedImage.TYPE_INT_RGB);
		Graphics2D gImg = scaledImg.createGraphics();

		gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.drawImage(tap, 0, 0, width/2,(this.newHeight*2-5)/2, null);
  		gImg.dispose();

	    ImageIO.write(scaledImg,"png",new File("tapestry-seam.png"));
	    //SeamCarver seamCarver = new SeamCarver("tapestry-seam.png","tapestry-seam-post.png",50,50);
	}

	public BufferedImage blurEdgesOfImage(BufferedImage tapestry) {
		int width = tapestry.getWidth();
		int height = tapestry.getHeight();

		System.out.println("check 1");
		for (int y = 0; y < this.newHeight-5; y++) {
			for (int i = 0; i < width-5; i = i+this.newWidth-5) {
				int x = i;
				System.out.println("x " + x + " y " + y); 
				int rgb = tapestry.getRGB(x,y);
				if (y == 0 && x != 0) {
					for (int a = -20; a < 21; a++) {
						int pix = this.neighborFive(tapestry,x+a,y,true);
						tapestry.setRGB(x+a,y,pix);
					}
				}
				else if (x == 0) {

				}
				else {
					for (int a = -20; a < 21; a++) {
						int pix = this.neighborEight(tapestry,x+a,y);
						tapestry.setRGB(x+a,y,pix);
					}
				}
			}
		}
		System.out.println("check 2");

		for (int y = this.newHeight; y < height; y++) {
			for (int i = this.newWidth/4; i < width-5; i = i+this.newWidth-5) {
				int x = i;
				System.out.println("x " + x + " y " + y); 
				int rgb = tapestry.getRGB(x,y);
				if (y == height-1 && x != 0) {
					for (int a = -20; a < 21; a++) {
						int pix = this.neighborFive(tapestry,x+a,y,false);
						tapestry.setRGB(x+a,y,pix);
					}
				}
				else {
					for (int a = -20; a < 21; a++) {
						int pix = this.neighborEight(tapestry,x+a,y);
						tapestry.setRGB(x+a,y,pix);
					}
				}
			}
		}

		System.out.println("check 3");
		int yy = this.newHeight;
		for (int x = 1; x < width-1; x++) {
			for (int a = -10; a < 11; a++) {
				int pix = this.neighborEight(tapestry,x,yy+a);
				tapestry.setRGB(x,yy+a,pix);
			}
		}
		System.out.println("check 4");
		yy = this.newHeight - 5;
		for (int x = this.newWidth/4; x < width-1; x++) {
			for (int a = -10; a < 11; a++) {
				int pix = this.neighborEight(tapestry,x,yy+a);
				tapestry.setRGB(x,yy+a,pix);
			}
		}		


		return tapestry;
	}

	public int neighborFive(BufferedImage tapestry, int x, int y, boolean isUp) {
		int pix = 0;
		if (isUp) {
			int rgb1 = tapestry.getRGB(x-1,y);
			int r = (rgb1 >> 16) & 0x000000FF;
			int g = (rgb1 >> 8 ) & 0x000000FF;
			int b = rgb1 & 0x000000FF;
			int rgb2 = tapestry.getRGB(x+1,y);
			r += (rgb2 >> 16) & 0x000000FF;
			g += (rgb2 >> 8 ) & 0x000000FF;
			b += rgb2 & 0x000000FF;
			int rgb3 = tapestry.getRGB(x-1,y+1);
			r += (rgb3 >> 16) & 0x000000FF;
			g += (rgb3 >> 8 ) & 0x000000FF;
			b += rgb3 & 0x000000FF;
			int rgb4 = tapestry.getRGB(x,y+1);
			r += (rgb4 >> 16) & 0x000000FF;
			g += (rgb4 >> 8 ) & 0x000000FF;
			b += rgb4 & 0x000000FF;
			int rgb5 = tapestry.getRGB(x+1,y+1);
			r += (rgb5 >> 16) & 0x000000FF;
			g += (rgb5 >> 8 ) & 0x000000FF;
			b += rgb5 & 0x000000FF;
			r = r/5;
			g = g/5;
			b = b/5;
			pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
			return pix;
		}

		int rgb1 = tapestry.getRGB(x-1,y);
		int r = (rgb1 >> 16) & 0x000000FF;
		int g = (rgb1 >> 8 ) & 0x000000FF;
		int b = rgb1 & 0x000000FF;
		int rgb2 = tapestry.getRGB(x+1,y);
		r += (rgb2 >> 16) & 0x000000FF;
		g += (rgb2 >> 8 ) & 0x000000FF;
		b += rgb2 & 0x000000FF;
		int rgb3 = tapestry.getRGB(x-1,y-1);
		r += (rgb3 >> 16) & 0x000000FF;
		g += (rgb3 >> 8 ) & 0x000000FF;
		b += rgb3 & 0x000000FF;
		int rgb4 = tapestry.getRGB(x,y-1);
		r += (rgb4 >> 16) & 0x000000FF;
		g += (rgb4 >> 8 ) & 0x000000FF;
		b += rgb4 & 0x000000FF;
		int rgb5 = tapestry.getRGB(x+1,y-1);
		r += (rgb5 >> 16) & 0x000000FF;
		g += (rgb5 >> 8 ) & 0x000000FF;
		b += rgb5 & 0x000000FF;
		r = r/5;
		g = g/5;
		b = b/5;
		pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		return pix;
	}

	public int neighborEight(BufferedImage tapestry, int x, int y) {
		int rgb1 = tapestry.getRGB(x-1,y);
		int r = (rgb1 >> 16) & 0x000000FF;
		int g = (rgb1 >> 8 ) & 0x000000FF;
		int b = rgb1 & 0x000000FF;
		int rgb2 = tapestry.getRGB(x+1,y);
		r += (rgb2 >> 16) & 0x000000FF;
		g += (rgb2 >> 8 ) & 0x000000FF;
		b += rgb2 & 0x000000FF;
		int rgb3 = tapestry.getRGB(x-1,y+1);
		r += (rgb3 >> 16) & 0x000000FF;
		g += (rgb3 >> 8 ) & 0x000000FF;
		b += rgb3 & 0x000000FF;
		int rgb4 = tapestry.getRGB(x,y+1);
		r += (rgb4 >> 16) & 0x000000FF;
		g += (rgb4 >> 8 ) & 0x000000FF;
		b += rgb4 & 0x000000FF;
		int rgb5 = tapestry.getRGB(x+1,y+1);
		r += (rgb5 >> 16) & 0x000000FF;
		g += (rgb5 >> 8 ) & 0x000000FF;
		b += rgb5 & 0x000000FF;
		int rgb6 = tapestry.getRGB(x-1,y-1);
		r += (rgb6 >> 16) & 0x000000FF;
		g += (rgb6 >> 8 ) & 0x000000FF;
		b += rgb6 & 0x000000FF;
		int rgb7 = tapestry.getRGB(x,y-1);
		r += (rgb7 >> 16) & 0x000000FF;
		g += (rgb7 >> 8 ) & 0x000000FF;
		b += rgb7 & 0x000000FF;
		int rgb8 = tapestry.getRGB(x+1,y-1);
		r += (rgb8 >> 16) & 0x000000FF;
		g += (rgb8 >> 8 ) & 0x000000FF;
		b += rgb8 & 0x000000FF;
		r = r/8;
		g = g/8;
		b = b/8;
		int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		return pix;
	}


	public void imageTapestryViaSeamCarving() throws IOException {
		int count = 0;
		int tapX = 0;
		int tapY = 0;
		int width = 0;
		int maxSize = this.sceneIndex.size();

		if (maxSize%2 == 0) {
			width = ((maxSize/2)+1)*this.originalWidth;
		}
		else {
			width = ((maxSize+1)*this.originalWidth)/2;
		}

	    tapestry = new BufferedImage(width, this.originalHeight*2, BufferedImage.TYPE_INT_RGB);

		while (count < maxSize) {
			System.out.println("on keyframe number: " + count);
			String fileName = "keyframes/" + threshold + "_" + count + ".png";
			BufferedImage img = ImageIO.read(new File(fileName));
			if (count%2 == 0) {
				tapY = 0;
				if (count == 0)
					tapX = 0;
				else 
					tapX = (count/2)*this.originalWidth;
				System.out.println("tapx: " + tapX + " tapy: " + tapY);
	    		for (int y = 0; y < this.originalHeight; y++) {
	    			for (int x = 0; x < this.originalWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.originalWidth;
	    			tapY++;
	    		}

			}
			else {
				tapY = this.originalHeight;
				if (count == 1) {
					tapX = this.originalWidth/2;
				}
				else {
					tapX = (count/2)*this.originalWidth + (this.originalWidth/2);
				}
				System.out.println("tapx: " + tapX + " tapy: " + tapY);
	    		for (int y = 0; y < this.originalHeight; y++) {
	    			for (int x = 0; x < this.originalWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.originalWidth + (this.originalWidth/2);
	    			tapY++;
	    		}
			}
			count++;
		}

	    ImageIO.write(tapestry,"png",new File("tapestry.png"));
	    System.out.println("Running seam carving..");
	    //trial1
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",width/2,this.originalHeight/2);
	    //trial2
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(width/2),(int)this.originalHeight*5/3);
	    //trial4 - accurate
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(width/2),this.originalHeight);
	    //trial5 - accurate - columns done first then rows
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(int)(width*29/50),this.originalHeight*6/5);
	    //trial6 - accurate - rows done first then columns
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(int)(width*29/50),this.originalHeight*6/5);
	    //trial7 - accurate - rows first then columns
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(int)(width*17/24),this.originalHeight*7/5);
	    //trial8 - accurate - rows first then col
	    int trial = (int)((int)(width*29/50)+(int)(width*17/24))/2;
	    SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",trial,this.originalHeight*6/5);

	}

	public void finalOutputImageSideBySide() throws IOException {
	    int count = 0;
	    int offset = 15*(this.sceneIndex.size()-1);
	    int width = this.newWidth*this.sceneIndex.size() - offset;
	    tapestry = new BufferedImage(width, this.newHeight, BufferedImage.TYPE_INT_RGB);
    	int tapX = 0;

	    while (count < this.sceneIndex.size()) {
	    	if (count == 0) {
	    		//System.out.println("count 0");
		    	String outputFileName = "out" + count + ".png";
		    	BufferedImage bi = ImageIO.read(new File(outputFileName));
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				tapestry.setRGB(x,y,bi.getRGB(x,y));
	    			}
	    		}	    		    		
	    	}
	    	else {
		    	String outputFileName = "out" + count + ".png";
	    		BufferedImage im = ImageIO.read(new File(outputFileName));
	    		int imX = 0;
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = tapX; x < tapX + this.newWidth; x++) {
	    				if (x >= tapX && x < tapX + 15) {
	    					int val1 = tapestry.getRGB(x,y);
	    					int r1 = (val1 >> 16) & 0x000000FF;
	    					int g1 = (val1 >> 8 ) & 0x000000FF;
	    					int b1 = val1 & 0x000000FF;

	    					int val2 = im.getRGB(imX,y);
	    					int r2 = (val2 >> 16 ) & 0x000000FF;
	    					int g2 = (val2 >> 8 ) & 0x000000FF;
	    					int b2 = val2 & 0x000000FF;

	    					int avgR = (r1+r2)/2;
	    					int avgG = (g1+g2)/2;
	    					int avgB = (b1+b2)/2;
	    					int pix = 0xff000000 | ((avgR & 0xff) << 16) | ((avgG & 0xff) << 8) | (avgB & 0xff);
	    					tapestry.setRGB(x,y,pix);
	    				}
	    				else {
	    					tapestry.setRGB(x,y,im.getRGB(imX,y));
	    				}
	    				imX++;
	    			}
	    			imX = 0;
	    		}
	    	}

	        tapX += this.newWidth - 15;
	    	count++;
	    }

	  
	    ImageIO.write(tapestry,"png",new File("tapestry.png"));
	}


	public void runFaceDetection() {
	   System.out.println("\nRunning DetectFaceDemo");

	    CascadeClassifier haarUpperBody = new CascadeClassifier("resources/haarcascades/haarcascade_upperbody.xml");
	    CascadeClassifier haarLowerBody = new CascadeClassifier("resources/haarcascades/haarcascade_lowerbody.xml");
	    CascadeClassifier haarFullBody = new CascadeClassifier("resources/haarcascades/haarcascade_fullbody.xml");
	    CascadeClassifier haarProfileFace = new CascadeClassifier("resources/haarcascades/haarcascade_profileface.xml");
	    CascadeClassifier haarFrontalFaceAlt1 = new CascadeClassifier("resources/haarcascades/haarcascade_frontalface_alt_tree.xml");
	    CascadeClassifier haarFrontalFaceAlt2 = new CascadeClassifier("resources/haarcascades/haarcascade_frontalface_alt.xml");
	    CascadeClassifier haarFrontalFaceAlt3 = new CascadeClassifier("resources/haarcascades/haarcascade_frontalface_alt2.xml");

	    // CascadeClassifier lbpFrontalFace = new CascadeClassifier("resources/lbpcascades/lbpcascade_frontalface.xml");
	    // CascadeClassifier lbpProfileFace = new CascadeClassifier("resources/lbpcascades/lbpcascade_profileface.xml");
	    int count = 0;

	    while (count < this.sceneIndex.size()) {
	    	String fileName = "keyframes/" + threshold + "_" + count + ".png";
	    	Mat image = Imgcodecs.imread(fileName);
	    	String outputFileName = "out" + count + ".png";
	    	System.out.println(count);

		    MatOfRect fullBodyHaar = new MatOfRect();
		    haarFullBody.detectMultiScale(image, fullBodyHaar);


		    if (fullBodyHaar.toArray().length > 0) {
		    	System.out.println(String.format("Detected %s fullBodyHaar", fullBodyHaar.toArray().length));
				for (Rect rect : fullBodyHaar.toArray()) {
				    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
				}
				face[count] = true;
				System.out.println(String.format("Writing %s", outputFileName));
				Imgcodecs.imwrite(outputFileName, image);
		    }

		    else {
			    MatOfRect upperBodyHaar = new MatOfRect();
			    haarUpperBody.detectMultiScale(image, upperBodyHaar);
			    if (upperBodyHaar.toArray().length > 0) {
					System.out.println(String.format("Detected %s upper body haar", upperBodyHaar.toArray().length));
					for (Rect rect : upperBodyHaar.toArray()) {
					    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
					}	
					face[count] = true;
					System.out.println(String.format("Writing %s", outputFileName));
					Imgcodecs.imwrite(outputFileName, image);					    	
			    }

			    else {
				    MatOfRect lowerBodyHaar = new MatOfRect();
				    haarLowerBody.detectMultiScale(image, lowerBodyHaar);
				    if (lowerBodyHaar.toArray().length > 0) {
		    			System.out.println(String.format("Detected %s lower body haar", lowerBodyHaar.toArray().length));
		    			for (Rect rect : lowerBodyHaar.toArray()) {
						    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
						}
						face[count] = true;
						System.out.println(String.format("Writing %s", outputFileName));
						Imgcodecs.imwrite(outputFileName, image);	
			    	}

			    	else {

		    			MatOfRect frontalFaceHaar1 = new MatOfRect();
		    			haarFrontalFaceAlt1.detectMultiScale(image, frontalFaceHaar1);

		    			if (frontalFaceHaar1.toArray().length > 0) {
		    				System.out.println(String.format("Detected %s frontalFaceHaar1", frontalFaceHaar1.toArray().length));
			    			for (Rect rect : frontalFaceHaar1.toArray()) {
							    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
							}
							face[count] = true;
							System.out.println(String.format("Writing %s", outputFileName));
							Imgcodecs.imwrite(outputFileName, image);			
		    			}

		    			else {
			    			MatOfRect frontalFaceHaar2 = new MatOfRect();
			    			haarFrontalFaceAlt2.detectMultiScale(image, frontalFaceHaar2);

			    			if (frontalFaceHaar2.toArray().length > 0) {
			    				System.out.println(String.format("Detected %s frontalFaceHaar2", frontalFaceHaar2.toArray().length));
							    for (Rect rect : frontalFaceHaar2.toArray()) {
								    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
								}
								face[count] = true;
								System.out.println(String.format("Writing %s", outputFileName));
								Imgcodecs.imwrite(outputFileName, image);	
			    			}

			    			else {

					    		MatOfRect frontalFaceHaar3 = new MatOfRect();
				    			haarFrontalFaceAlt3.detectMultiScale(image, frontalFaceHaar3);

				    			if (frontalFaceHaar3.toArray().length > 0) {
				    				System.out.println(String.format("Detected %s frontalFaceHaar3", frontalFaceHaar3.toArray().length));
					    			for (Rect rect : frontalFaceHaar3.toArray()) {
									    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
									}
									face[count] = true;
									System.out.println(String.format("Writing %s", outputFileName));
									Imgcodecs.imwrite(outputFileName, image);			
				    			}

				    			else {
									MatOfRect profileFaceHaar = new MatOfRect();
								    haarProfileFace.detectMultiScale(image, profileFaceHaar);

								    if (profileFaceHaar.toArray().length > 0) {
								    	System.out.println(String.format("Detected %s profileFaceHaar", profileFaceHaar.toArray().length));
						    			for (Rect rect : profileFaceHaar.toArray()) {
										    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
										}
										face[count] = true;
										System.out.println(String.format("Writing %s", outputFileName));
										Imgcodecs.imwrite(outputFileName, image);
								    }
				    			}
			    			}
		    			}
			    	}		    	
			    }
		    }
	    	count++;
	    }


	    // MatOfRect frontalFaceLbp = new MatOfRect();
	    // lbpFrontalFace.detectMultiScale(image, frontalFaceLbp);
	    // System.out.println(String.format("Detected %s frontalFaceLbp", frontalFaceLbp.toArray().length));

	    // MatOfRect profileFaceLbp = new MatOfRect();
	    // lbpProfileFace.detectMultiScale(image, profileFaceLbp);
	    // System.out.println(String.format("Detected %s profileFaceLbp", profileFaceLbp.toArray().length));

	}
}