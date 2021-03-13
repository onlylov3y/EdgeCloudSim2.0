package org.cloudbus.cloudsim.edge;

import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class CplexData_backup {
	
	public CplexData_backup() {}

	public static String newline = System.getProperty("line.separator");
	
	public String processInputData(List<Host> hostList, List<Vm> vmList) {
		/*********************************************
		 * OPL 12.6.0.0 Data
		 * Author: cv
		 * Creation Date: 10 2017 at 5:46:58
		 *********************************************/
		/* a host is defined with a dc_id and host_id */		
		String inputData =
		
		// {tier} T=...;
		"T={<1>, <2>, <3>};" + newline
		// {channel} C=...;
		+ "C={<<1>,<2>,1>, <<2>,<1>,2>, <<2>,<3>,3>, <<3>,<2>,4>};" + newline
		// {host} H=...;
		+ "H={<1>, <2>, <3>, <4>};" + newline
		// {link} L=...;
		+ "L={<<1>,<2>,1>, <<2>,<1>,2>, <<2>,<3>,3>, <<3>,<2>,4>, <<3>,<4>,7>, <<4>,<3>,8>};" + newline
		// S is a subset of T
		+ "S={<1>};" + newline
		// subset of H
		+ "HS={<4>};" + newline
		//set of unique resource offerd by a host e.g. CPU, MEM
		+ "R={CPU, MEMORY};" + newline
		//set of unique resources offered by a link e.g. bw gold, bw silver, ?ports?
		+ "Rhyphen={BW};" + newline
		//set of monitored metrics at hosts
		+ "M={ENERGYEFFICIENT};" + newline
		//set of monitored metrics at hosts		
		+ "Mhyphen={QoS};" + newline
		// host app is S
		+ "shat={<1>};" + newline
		// hosting device where host app is located in HS
		+ "hhat={<4>};" + newline
		// amount of of resource r demand by application task t
		+ "a= #[\"CPU\": [3,2,1],\"MEMORY\": [4,1,1]]#;" + newline
		// amount of bandwidth required by channel 
		+ "c= #[\"BW\": [10, 10, 5, 5 ]]#;" + newline
		// amount of resource r available at host h
		+ "beta= #[\"CPU\": [30,10,25,50], \"MEMORY\": [60,20,50,100]]#;" + newline
		// these maybe float as well measured value of metric k at host h
		+ "m=#[\"ENERGYEFFICIENT\": [1,1,1,1]]#;" + newline
		// acceptable upper bound to the value of  metric k for an application task t to be 
		// assigned to a host; **** captures Boolean as well
		+ "wu=#[\"ENERGYEFFICIENT\": [1,1,1]]#;" + newline
		// acceptable lower bound to the value of  metric k for an application task t to be assigned to a host
		+ "wl=#[\"ENERGYEFFICIENT\": [1,1,1]]#;" + newline
		// amount of bandwidth available at link 
		+ "b=#[\"BW\": [100,100, 500,500, 100,100]]#;" + newline
		// these maybe float as well measured value of metric k at at link <u,v>;		
		+ "mi= #[\"QoS\": [1,1, 1,1, 1,1]]#;" + newline
		// acceptable upper bound to the value of  metric k for a channel <s,d> to be routed through a link
		+ "zu=#[\"QoS\": [1, 1, 1, 1 ]]#;" + newline
		// acceptable lower bound to the value of  metric k for a channel <s,d> to be routed through a link
		+ "zl=#[\"QoS\": [1, 1, 1, 1 ]]#;"; 			
		return inputData;		
	}
}
