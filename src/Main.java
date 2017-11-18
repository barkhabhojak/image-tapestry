
import java.io.*;

public class Main {
  public static void main(String[] args) throws IOException {
  	//ImageCreation image = new ImageCreation("resources/USCWeek.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Hussein_Day1_007.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Michael_Day2_018.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Apple.rgb",45000);
  	//ImageCreation image = new ImageCreation("resources/Disney.rgb",17500);
 
  	int threshold = 17500;
	int width = 352;
	int height = 288;
	int nBytes = width*height*3;
	File file = new File("resources/Disney.rgb");
	double len = file.length();
	double nFrames = len/(width*height*3);
	int[] byteIndicies = new int[(int) nFrames+1];
	
	for (int b = 0; b < nFrames; ++b) {
		byteIndicies[b] = b * nBytes;
	}
	System.out.println("AVPlayer loading...");
	//ImageCreation imageCreation = new ImageCreation("resources/Disney.rgb",threshold);

	new AVPlayer("resources/Disney.rgb","resources/Disney.wav",byteIndicies,nFrames,threshold);
  }
}