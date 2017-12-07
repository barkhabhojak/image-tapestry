import java.util.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.BufferedImage;


public class IndexMap {

	private int threshold1, threshold2, threshold3;
	private ArrayList<Integer> index1, index2, index3;
	private Hashtable<Integer,ArrayList<ArrayList<Integer>>> indexTable;
	private BufferedImage indexImage1,indexImage2,indexImage3;

	public IndexMap(int threshold1, int threshold2, int threshold3, ArrayList<Integer> index1, ArrayList<Integer> index2, ArrayList<Integer> index3) throws IOException{
		this.indexTable = new Hashtable<Integer,ArrayList<ArrayList<Integer>>>();
		this.threshold1 = threshold1;
		this.threshold2 = threshold2;
		this.threshold3 = threshold3;
		this.index1 = index1;
		this.index2 = index2;
		this.index3 = index3;
		String file1 = "index_" + threshold1 + "_out_scaled.png";
		String file2 = "index_" + threshold2 + "_out_scaled.png";
		String file3 = "index_" + threshold3 + "_out_scaled.png";
		this.indexImage1 = ImageIO.read(new File(file1));
		this.indexImage2 = ImageIO.read(new File(file2));
		this.indexImage3 = ImageIO.read(new File(file3));
		this.run();
	}

	public Hashtable<Integer,ArrayList<ArrayList<Integer>>> getIndexTable() {
		return this.indexTable;
	}


	public void run() {
		System.out.println("======================= creating hashtable =======================");
		int width  = indexImage3.getWidth();
		int height = indexImage3.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = indexImage3.getRGB(x,y);
				int frameNum = (rgb >> 16) & 0x000000FF;
				int keyFrameIndex = index3.get(frameNum);
				int currFrame = keyFrameIndex/(352*288*3);

				if (!indexTable.containsKey(currFrame)) {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(x);
//					temp.add(y);
//					ArrayList<Integer> temp1 = new ArrayList<Integer>();
//					temp1.add(x);
//					temp1.add(y);
					ArrayList<ArrayList<Integer>> addVal = new ArrayList<ArrayList<Integer>>();
					addVal.add(temp);
					addVal.add(temp);
					addVal.add(temp);
//					System.out.println(addVal.get(0) + "   ,   " + addVal.get(1) + "   ,   " + addVal.get(2));
					indexTable.put(currFrame,addVal);
				}

			}
		}
//		System.out.println("Running on second image..");
		width = indexImage2.getWidth();
		height = indexImage2.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = indexImage2.getRGB(x,y);
				int frameNum = (rgb >> 16) & 0x000000FF;
				int keyFrameIndex = index2.get(frameNum);
				int currFrame = keyFrameIndex/(352*288*3);

				if (!indexTable.containsKey(currFrame)) {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(x);
//					temp.add(y);
//					ArrayList<Integer> temp1 = new ArrayList<Integer>();
//					temp1.add(x);
//					temp1.add(y);
					ArrayList<ArrayList<Integer>> addVal = new ArrayList<ArrayList<Integer>>();
					addVal.add(temp);
					addVal.add(temp);
					addVal.add(temp);
					indexTable.put(currFrame,addVal);
				}
				else {
					ArrayList<ArrayList<Integer>> tempVal = new ArrayList<ArrayList<Integer>>();
					tempVal = indexTable.get(currFrame);
//					if (tempVal.get(1).get(0) == -1 && tempVal.get(1).get(1) == -1) {
						tempVal.get(1).set(0,x);
//						tempVal.get(1).set(1,y);
//					}
				}

			}
		}
//		System.out.println("Running on image one..");
		width = indexImage1.getWidth();
		height = indexImage1.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = indexImage1.getRGB(x,y);
				int frameNum = (rgb >> 16) & 0x000000FF;
				int keyFrameIndex = index1.get(frameNum);
				int currFrame = keyFrameIndex/(352*288*3);

				if (!indexTable.containsKey(currFrame)) {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(x);
//					temp.add(y);
//					ArrayList<Integer> temp1 = new ArrayList<Integer>();
//					temp1.add(x);
//					temp1.add(y);
					ArrayList<ArrayList<Integer>> addVal = new ArrayList<ArrayList<Integer>>();
					addVal.add(temp);
					addVal.add(temp);
					addVal.add(temp);
					indexTable.put(currFrame,addVal);
				}
				else {
					ArrayList<ArrayList<Integer>> tempVal = new ArrayList<ArrayList<Integer>>();
					tempVal = indexTable.get(currFrame);
//					if (tempVal.get(0).get(0) == -1 && tempVal.get(0).get(1) == -1) {
						tempVal.get(0).set(0,x);
//						tempVal.get(0).set(0,y);
//					}
				}
			}
		}
	}

	public void printHashTable() {
	    Enumeration names = indexTable.keys();
	  
	    while(names.hasMoreElements()) {
	        Integer str = (Integer) names.nextElement();
	        System.out.println("key: " + str + " value: " + this.indexTable.get(str));
	    }
	}
}