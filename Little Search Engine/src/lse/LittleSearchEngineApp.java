package lse;

import java.io.*;
import java.util.*;

public class LittleSearchEngineApp {
	public static void main(String[] args) 
			throws FileNotFoundException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the master list of files in which you would like to search.");
		String docFile = sc.nextLine();
		LittleSearchEngine engine = new LittleSearchEngine();
		engine.makeIndex(docFile, "noisewords.txt");
		System.out.println("Enter the first keyword you would like to search for.");
		String kw1 = sc.nextLine();
		System.out.println("Enter the second keyword you would like to search for.");
		String kw2 = sc.nextLine();
		ArrayList<String> top5 = engine.top5search(kw1, kw2);
		sc.close();
		for(int i = 0; i < top5.size(); i++) {
			System.out.println((i+1) + ". " + top5.get(i));
		}
	}
}
