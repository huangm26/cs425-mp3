package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {

	private static volatile Configuration INSTANCE = null;
	private static String CONFIG_PATH = "config.txt";

	public static int numProc = 0;
	public static String[] IP;
	public static int[] avgDelayTo;

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
		// IP = new String[numProc];
		avgDelayTo = new int[3];
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
					// Assign number of processes
					case "NumberProc":
						numProc = Integer.valueOf(val);
						IP = new String[numProc];
						break;

					// Assign delayTime
					case "Average_Delay_to_1":
						avgDelayTo[0] = Integer.valueOf(val);
						break;
					case "Average_Delay_to_2":
						avgDelayTo[1] = Integer.valueOf(val);
						break;
					case "Average_Delay_to_3":
						avgDelayTo[2] = Integer.valueOf(val);
						break;
					}
				}
				// Assign IP if the line has more than 3 items
				if (strArr.length > 3 && strArr[1].equals("IPs")) {
					for (int i = 0; i < numProc; i++) {
						IP[i] = strArr[2 + i];
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
