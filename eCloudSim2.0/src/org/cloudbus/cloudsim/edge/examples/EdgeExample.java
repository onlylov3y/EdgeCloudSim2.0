package org.cloudbus.cloudsim.edge.examples;

import java.io.File;
import java.util.Calendar;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.edge.util.CustomLog;

public class EdgeExample {

	public static void run(String simulationType, String algorithm, int experimentNumber) {

		System.out.println("STARTING EDGEEXAMPLE " + algorithm+" Nr: " + experimentNumber);

		String measurementPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "measurements";		
		String topologyPath = measurementPath + System.getProperty("file.separator") + "topology";
		String simulationTypePath = topologyPath + System.getProperty("file.separator") + simulationType;
		String rootResultFolderPath = measurementPath + System.getProperty("file.separator") + "results";
		String resultFolderPath = rootResultFolderPath + System.getProperty("file.separator") + simulationType
													   + System.getProperty("file.separator") + algorithm
													   + System.getProperty("file.separator") + experimentNumber;
		File directory = new File(resultFolderPath);
	    if (! directory.exists()){
	        directory.mkdirs();}
	    
		try {

			Properties props = new Properties();
//			props.setProperty("FilePath", resultFolderPath + System.getProperty("file.separator") + "results_baseline.txt");
			props.setProperty("ServerFilePath", resultFolderPath + System.getProperty("file.separator") + "server_utilization.txt");
			props.setProperty("ResponseFilePath", resultFolderPath + System.getProperty("file.separator") + "response_time.txt");
			props.setProperty("VmRequestFilePath", resultFolderPath + System.getProperty("file.separator") + "vm_request.txt");
			props.setProperty("serviceChainFilePath", resultFolderPath + System.getProperty("file.separator") + "service_chain.txt");
			props.setProperty("serviceStreamingFilePath", resultFolderPath + System.getProperty("file.separator") + "service_streaming.txt");
			// props.setProperty("LogRealTimeClock", "true");
//			props.setProperty("LogCloudSimClock", "true");
			// props.setProperty("LogReadableSimClock", "true");
			props.setProperty("LogFormat", "getMessage");

			CustomLog.configLogger(props);

//			CustomLog.printf("%s\t%s\t%s\t%s\t%s", "SimTime", "Entity", "Time", "Real_Time", "Data");
			CustomLog.printResponse("%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Service_ID", "Type", "Time", "Data", "Video_Legnth");
			CustomLog.printServiceStreaming("%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Service_ID", "Video_ID", "Time", "Data", "Video_Legnth");
			CustomLog.printVmRequest("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Host_ID", "Host_Name", "VM_ID", "DC_ID",
					"Owner_ID", "Reason", "Status");
			CustomLog.printServer("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "SimTime",  "Host_ID", "Host_Name", "DC_ID", "RAM", "CPU", "MIPS", "BW",
					"Storage", "Num Of VMs");
			CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", "SimTime", "Service_ID", "Type", "Chain", "Hop_Count");

			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology.buildNetworkTopology(
					simulationTypePath
					// briteFolderPathSecond
					+ System.getProperty("file.separator") + "topology_baseline.brite");
			System.out.println(simulationTypePath + System.getProperty("file.separator") + "topology_baseline.brite");
			
			BaseDatacenter
					//.createNetworkWorkingFirst();
					// .createNetworkWorkingSecond();
					//.createNetworkWorkingThird();
					.createNetworkWorking();

			// Ends after 1 day : 24*60*60*1000 ms
//			CloudSim.terminateSimulation(86400000);
			// Ends after 30 min : 30*60*1000 ms
			CloudSim.terminateSimulation(1800000);
			CloudSim.startSimulation();

			System.out.println("EDGEEXAMPLE " + algorithm+" Nr: " + experimentNumber + " FINISHED!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		run("testbed", "orchestration", 2);
		
//        for (int i = 1; i <= 10; i++) {
//            run("orchestration", i);
//        }
//        for (int i = 1; i <= 10; i++) {
//            run("default", i);
//        }		
		
//      run("testbed", "default", 2);
//      run("testbed", "default", 3);
//      run("testbed", "default", 4);
//      run("testbed", "default", 5);
//      run("testbed", "default", 6);
//      run("testbed", "default", 7);
//      run("testbed", "default", 8);
//      run("testbed", "default", 9);
//      run("testbed", "default", 10);
      
      
      
//      for (int i = 1; i <= 10; i++) {
//          run("real", "orchestration", i);
//          run("testbed", "orchestration", i);
//      }
//
//      for (int i = 1; i <= 10; i++) {
//          run("real", "default", i);
//          run("testbed", "default", i);
//      }		

	}
}
