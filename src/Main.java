
import java.io.*;

public class Main {
  public static void main(String[] args) throws IOException {
  	/**
		To run for Hussein_Day1_007
		1) set method to "background"
		2) change AVPlayer to the following:
		new AVPlayer("resources/Hussein_Day1_007.rgb","resources/Hussein_Day1_007.wav",byteIndicies,nFrames,22000,12500,8000,method);
		22000: 10 scenes
		12500: 19 scenes
		8000: 23 scenes

		To run for Apple
		1) set method to "people"
		2) change AVPlayer to the following:
		new AVPlayer("resources/Apple.rgb","resources/Apple.wav",byteIndicies,nFrames,70000,20000,1000,method);
		70000: 10 scenes
		20000: 18 scenes
		1000: 22 scenes

		To run for USCWeek
		1) set method to "people"
		2) change AVPlayer to the following:
		new AVPlayer("resources/USCWeek.rgb","resources/USCWeek.wav",byteIndicies,nFrames,65000,12000,5000,method);
		65000: 10 scenes
		12000: 17 scenes
		5000: 23 scenes


		To run for Disney
		1) set method to "people"
		2) change AVPlayer to the following:
		new AVPlayer("resources/Disney.rgb","resources/Disney.wav",byteIndicies,nFrames,37000,5000,1000,method);
		37000: 10 scenes
		5000: 17 scenes
		1000: 24 scenes

		To run for Michael_Day2_018
		1) set method to "background"
		2) change AVPlayer to the following:
		new AVPlayer("resources/Michael_Day2_018.rgb","resources/Michael_Day2_018.wav",byteIndicies,nFrames,22000,12500,3000,method);
		22000: 9 scenes
		12500: 18 scenes
		3000: 23 scenes
		
		To run for USCVillage
		1) set method to "people"
		2) change AVPlayer to the following:
		new AVPlayer("resources/USCVillage.rgb","resources/USCVillage.wav",byteIndicies,nFrames,20000,10000,2000,method);
		20000: 10 scenes
		10000: 17 scenes
		2000: 24 scenes
  	*/
	int flag = 1;

	if (flag == 0) {
		String name = "resources/Disney.rgb";
		File file = new File(name);
	  	String method = "people";
		int width = 352;
		int height = 288;
		int nBytes = width*height*3;
		double len = file.length();
		double nFrames = len/(width*height*3);
		int[] byteIndicies = new int[(int) nFrames+1];
		
		for (int b = 0; b < nFrames; ++b) {
			byteIndicies[b] = b * nBytes;
		}
		new AVPlayer("resources/Disney.rgb","resources/Disney.wav",byteIndicies,nFrames,37000,5000,1000,method);
	}

	else {
		String name = "resources/USCVillage.rgb";
		File file = new File(name);
		//method can be background or people depending on the kind of person. People will run foreground analysis
	  	String method = "people";
		int width = 352;
		int height = 288;
		int nBytes = width*height*3;
		double len = file.length();
		double nFrames = len/(width*height*3);
		int[] byteIndicies = new int[(int) nFrames+1];
		
		for (int b = 0; b < nFrames; ++b) {
			byteIndicies[b] = b * nBytes;
		}

		new AVPlayer("resources/USCVillage.rgb","resources/USCVillage.wav",byteIndicies,nFrames,20000,10000,2000,method);
	}
  
  }
}