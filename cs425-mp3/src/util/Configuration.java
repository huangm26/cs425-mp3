package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {

	private static volatile Configuration INSTANCE = null;
	private static String CONFIG_PATH = "config.txt";


	public static int numProc = 0;
	public static String[] IP;
	public static int[] delayTime;
	public static int[] dropRate;

	public static Configuration getInstance() {
		if (INSTANCE == null) {
			synchronized (Configuration.class) {
				if (INSTANCE == null) {
					INSTANCE = new Configuration();
				}
			}
		}
		return INSTANCE;
	}

	private Configuration() {
		initialize();
		setVariables();
	}

	private void initialize() {
		IP = new String[6];
	}

	private void setVariables() {
		String currLine = "";
		BufferedReader br;
		String[] strArr = null;
		String val = "";
		try {
			br = new BufferedReader(new FileReader(CONFIG_PATH));
			while ((currLine = br.readLine()) != null) {
				strArr = currLine.split(" ");
				if (strArr.length == 3) {
					val = strArr[2];
					switch (strArr[1]) {
					
					case "NumberProc":
						numProc = Integer.valueOf(val);
						break;
					
					// Assign IP
					case "IP_P0":
						IP[0] = val;
						break;
					case "IP_P1":
						IP[1] = val;
						break;
					case "IP_P2":
						IP[2] = val;
						break;
					case "IP_P3":
						IP[3] = val;
						break;
					case "IP_P4":
						IP[4] = val;
						break;
					case "IP_P5":
						IP[5] = val;
						break;
						
					
						
					
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
