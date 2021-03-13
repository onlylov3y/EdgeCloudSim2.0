package org.cloudbus.cloudsim.edge.examples;

import java.io.File;
import java.util.Calendar;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.edge.util.CustomLog;

public class MeasurementScript {

	public static void run(String simulationType, String algorithm, String replication ) {
		System.out.println("Starting measurement with simulation type: " + simulationType + " - Algorithm: " + algorithm + " - Replication #: " + replication);
		String measurementPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "measurements";
		String topologyPath = measurementPath + System.getProperty("file.separator") + "topology";
		String topoTypePath = topologyPath + System.getProperty("file.separator") + simulationType;
		String rootResultFolderPath = measurementPath + System.getProperty("file.separator") + "results";
		String resultFolderPath = rootResultFolderPath + System.getProperty("file.separator") + algorithm
													   + System.getProperty("file.separator") + replication;
		
//		System.out.println("ThangLog - resultFolderPath" + resultFolderPath);
//		System.out.println("ThangLog - rootResultFolderPath" + rootResultFolderPath);
//		System.out.println("ThangLog - topoTypePath" + topoTypePath);
//		System.out.println("ThangLog - topologyPath" + topologyPath);
		
		File directory = new File(resultFolderPath);
	    if (! directory.exists()){
	        directory.mkdirs();}
		try {

			Properties props = new Properties();
			props.setProperty("FilePath", resultFolderPath + System.getProperty("file.separator") + "results_baseline.txt");
			props.setProperty("ServerFilePath", resultFolderPath + System.getProperty("file.separator") + "server_utilization.txt");
			props.setProperty("ResponseFilePath", resultFolderPath + System.getProperty("file.separator") + "response_time.txt");
			props.setProperty("VmRequestFilePath", resultFolderPath + System.getProperty("file.separator") + "vm_request.txt");
			props.setProperty("serviceChainFilePath", resultFolderPath + System.getProperty("file.separator") + "service_chain.txt");
			props.setProperty("serviceStreamingFilePath", resultFolderPath + System.getProperty("file.separator") + "service_streaming.txt");
			props.setProperty("ServerUsageToCCSFilePath", resultFolderPath + System.getProperty("file.separator") + "server_concurrent_services.txt");
			
			// props.setProperty("LogRealTimeClock", "true");
//			props.setProperty("LogCloudSimClock", "true");
			// props.setProperty("LogReadableSimClock", "true");
			props.setProperty("LogFormat", "getMessage");

			CustomLog.configLogger(props);

			CustomLog.printf("%s\t%s\t%s\t%s\t%s", "SimTime", "Entity", "Time", "Real_Time", "Data");
			CustomLog.printResponse("%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Service_ID", "Type", "Time", "Data", "Video_Legnth");
			CustomLog.printServiceStreaming("%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Service_ID", "Video_ID", "Time", "Data", "Video_Legnth");
			CustomLog.printVmRequest("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Host_ID", "Host_Name", "VM_ID", "DC_ID",
					"Owner_ID", "Reason", "Status");
			CustomLog.printServer("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "SimTime",  "Host_ID", "Host_Name", "DC_ID", "RAM", "CPU", "MIPS", "BW",
					"Storage", "Num Of VMs");
			CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", "SimTime", "Service_ID", "Type", "Chain", "Hop_Count");
			CustomLog.printServerToCCS("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "SimTime", "Current Service_ID", 
																			"Milano1-CPU", "Milano1-RAM",
																			"Milano2-CPU", "Milano2-RAM",
																			"Genova-CPU", "Genova-RAM",
																			"Sanova-CPU", "Sanova-RAM",
																			"Torino-CPU", "Torino-RAM",
																			"Concurrent Services");
			

			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology.buildNetworkTopology(topoTypePath + System.getProperty("file.separator") + "topology_baseline.brite");
					
			BaseDatacenter.createNetworkWorking();

			// Ends after 1 day : 24*60*60*1000 = 86400000 ms
			// Ends after 2h : 2*60*60*1000 ms = 7200000
			// Ends after 2 min : 2*60*1000 ms = 120000
			CloudSim.terminateSimulation(180000);
			CloudSim.startSimulation();
						
		}catch (Exception e){
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}		
	}
	public static void main(String[] args) {
		// require type of simulation: testbed or real
		// require type of algorithm: default, orchestration or optimization
//		run("real", "default", args[0]);
		run("real", "default", "first_run_default_algorithm");
	}
}
