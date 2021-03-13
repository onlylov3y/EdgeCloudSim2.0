package org.cloudbus.cloudsim.edge;

public class CplexData {
	
	public CplexData() {}

	/*********************************************
	 * OPL 12.6.0.0 Data for Objective 1 - Minimum delay between DC and user
	 * Author: Constantinos Vassilakis,
	 * 		   Lam Dinh-Xuan
	 * Creation Date: 18, Dec 2017
	 * @hostList 
	 * 			is the list of all host in the topology, dc_id is extracted from host properties
	 * @vmList
	 * 			including UserVM and service VMs, in which UserVM is already know where to place
	 * @return inputData
	 * 			this will be sent to CPLEX solver class to get the location of service VMs (dc_id, host_id) 
	 *********************************************/	
	public String processDataMinDelay(Object[] ob) {
		int[] host_milano1 = (int[]) ob[0];
		int[] host_milano2 = (int[]) ob[1];
		int[] host_genova = (int[]) ob[2];
		int[] host_sanova = (int[]) ob[3];
		int[] host_torino = (int[]) ob[4];
		int[] userVm = (int[]) ob[5];
		int[] vm1 = (int[]) ob[6];
		int[] vm2 = (int[]) ob[7];
		int[] vm3 = (int[]) ob[8];
		
		// (R) for read only (U) for update on each request placement
		// (R) Hosts - a host is defined with a dc_id and host_id
		StringBuilder md = new StringBuilder("H={<2,1>,<4,3>,<6,5>,<8,7>,<10,9>,<10,11>,<12,13>,<14,15>,<16,17>};");
		md.append(System.lineSeparator());
		// (R) Links
		md.append("L={<<2,1>,<10,9>,1>,<<4,3>,<12,13>,2>,<<6,5>,<14,15>,3>,");	
		md.append("<<8,7>,<16,17>,4>,<<10,9>,<12,13>,5>,<<10,9>,<14,15>,6>,");	
		md.append("<<10,9>,<16,17>,7>,<<12,13>,<14,15>,8>,");	
		md.append("<<12,13>,<16,17>,9>,<<14,15>,<16,17>,10>,");	
		md.append("<<10,9>,<2,1>,11>,<<12,13>,<4,3>,12>,");	
		md.append("<<14,15>,<6,5>,13>,<<16,17>,<8,7>,14>,");
		md.append("<<12,13>,<10,9>,15>,<<14,15>,<10,9>,16>,");
		md.append("<<16,17>,<10,9>,17>,<<14,15>,<12,13>,18>,");
		md.append("<<16,17>,<12,13>,19>,<<16,17>,<14,15>,20>,");
		md.append("<<10,9>,<10,11>,21>,<<10,11>,<10,9>,22>};");	md.append(System.lineSeparator());
		// links 1 to 10 specify one direction, 11 to 20 the inverse direction of the bidirectional link, 
		// 21 and 22 the two directions of a link between the two hosts in DC 10
		// (R) User_Hosts
		md.append("UH={<2,1>,<4,3>,<6,5>,<8,7>};");	md.append(System.lineSeparator());
		// (U) User host used to host userVM
		md.append("hhat={<"+userVm[0]+","+userVm[1]+">};");	md.append(System.lineSeparator());
		md.append("HS={<"+userVm[0]+","+userVm[1]+">};");	md.append(System.lineSeparator());
		// (R) Resources in Hosts
		md.append("R={CPU,MEM};");	md.append(System.lineSeparator());
		// (R) Resources in Links
		md.append("Rhyphen={BW};");	md.append(System.lineSeparator());
		// (R) Host Metrics -> not used here
		md.append("M={ENERGYEFFICIENT};");	md.append(System.lineSeparator());
		// (R) Link metrics
		md.append("Mhyphen={Delay};");	md.append(System.lineSeparator());
		// (U) Host Available resources
		md.append("beta=#[");	md.append(System.lineSeparator());
		md.append("\"CPU\":[4000,4000,4000,4000,"+host_milano1[0]+","+host_milano2[0]+","
												 +host_genova[0]+","+host_sanova[0]+","+host_torino[0]+"],");	
		md.append("\"MEM\":[8048000,8048000,8048000,8048000,"+host_milano1[1]+","+host_milano2[1]+","
												 +host_genova[1]+","+host_sanova[1]+","+host_torino[1]+"]]#;"); 
		md.append(System.lineSeparator());
		// (R) Initial resource capacities
		md.append("ibeta=#[\"CPU\":[4000,4000,4000,4000,24,24,24,16,24],");	
		md.append("\"MEM\":[8048000,8048000,8048000,8048000,65536,65536,20480,16384,49152]]#;");
		md.append(System.lineSeparator());
		// (R) Host Metrics values -> not used here
		md.append("m=#[\"ENERGYEFFICIENT\":[1,1,1,1,1,1,1,1,1]]#;"); md.append(System.lineSeparator());
		// (U) Link Available resources 
		md.append("b=#[\"BW\":[125000,125000,125000,125000,1250000,1250000,1250000,"); 
		md.append("1250000,1250000,1250000,125000,125000,125000,125000,1250000,"); 
		md.append("1250000,1250000,1250000,1250000,1250000,125000000,125000000]]#;"); md.append(System.lineSeparator());
		// (U) Link metrics values for Real scenario
//		2;10;25.0
//		4;12;25.0
//		6;14;25.0
//		8;16;25.0
//		10;12;70.0
//		10;14;120.0
//		10;16;70.0
//		12;14;70.0
//		12;16;70.0
//		14;16;70.0		
		md.append("mi=#[\"Delay\":[25,25,25,25,70,120,70,70,70,70,"); 
		md.append("25,25,25,25,70,120,70,70,70,70,0,0]]#;");	md.append(System.lineSeparator());
		// (U) Service chain nodes (tiers)
		md.append("T={<"+vm1[2]+">,<"+vm2[2]+">,<"+vm3[2]+">,<"+userVm[4]+">};");	md.append(System.lineSeparator());
		// (U) Service chain channels (links) between tiers
//		md.append("C={<<"+vm1[2]+">,<"+vm2[2]+">,1>,<<"+vm2[2]+">,<"+vm1[2]+">,2>,<<"+vm1[2]+">,<"+vm3[2]+">,3>,<<"+vm3[2]+">,<"+vm1[2]+">,4>,");	
//		md.append("<<"+vm2[2]+">,<"+vm3[2]+">,5>,<<"+vm3[2]+">,<"+vm2[2]+">,6>,<<"+userVm[4]+">,<"+vm2[2]+">,7>,<<"+vm2[2]+">,<"+userVm[4]+">,8>};");
		md.append("C={<<"+vm1[2]+">,<"+vm2[2]+">,1>,<<"+vm2[2]+">,<"+vm1[2]+">,2>,");	
		md.append("<<"+vm2[2]+">,<"+vm3[2]+">,3>,<<"+vm3[2]+">,<"+vm2[2]+">,4>,<<"+userVm[4]+">,<"+vm1[2]+">,5>,<<"+vm1[2]+">,<"+userVm[4]+">,6>};");				
		md.append(System.lineSeparator());
		// (U) User app (tier)
		md.append("shat={<"+userVm[4]+">};"); md.append(System.lineSeparator());
		md.append("S={<"+userVm[4]+">};"); md.append(System.lineSeparator());
		// (U) tier resources demand
		md.append("a=#[\"CPU\": ["+vm1[0]+","+vm2[0]+","+vm3[0]+","+userVm[2]+"],"); 
		md.append("\"MEM\": ["+vm1[1]+","+vm2[1]+","+vm3[1]+","+userVm[3]+"]]#;");	md.append(System.lineSeparator());
		// (U) channel resources demand
		//md.append("c=#[\"BW\":[13,13,53,53,78,78,18,18]]#;");	md.append(System.lineSeparator());
		md.append("c=#[\"BW\":[10,10,10,10,10,10]]#;");	md.append(System.lineSeparator());
		// *** CAUTION -> JUST MAKE SURE THAT THE NUMBER OF ENTRIES EQUALS TIERS FOR wu,wl AND CHANNEL FOR zl,zu
		// Requested range for a host metric ->no effect -> disabled
		md.append("wl=#[\"ENERGYEFFICIENT\":[1,1,1,1]]#;");	md.append(System.lineSeparator());
		md.append("wu=#[\"ENERGYEFFICIENT\":[1,1,1,1]]#;");	md.append(System.lineSeparator());
		// Requested range for a link metric -> no effect -> disabled
		//md.append("zl=#[\"Delay\":[0,0,0,0,0,0,0,0]]#;");	md.append(System.lineSeparator());
		md.append("zl=#[\"Delay\":[0,0,0,0,0,0]]#;");	md.append(System.lineSeparator());
		//md.append("zu=#[\"Delay\":[10000,10000,10000,10000,10000,10000,10000,10000]]#;");
		md.append("zu=#[\"Delay\":[10000,10000,10000,10000,10000,10000]]#;");
	
		return md.toString();		
	}

	/*********************************************
	 * OPL 12.6.0.0 Data for Objective 2 - Maximum resource utilization
	 * Author: Constantinos Vassilakis,
	 * 		   Lam Dinh-Xuan
	 * Creation Date: 18, Dec 2017
	 * @hostList 
	 * 			is the list of all host in the topology, dc_id is extracted from host properties
	 * @vmList
	 * 			including UserVM and service VMs, in which UserVM is already know where to place
	 * @return inputData
	 * 			this will be sent to CPLEX solver class to get the location of service VMs (dc_id, host_id) 
	 *********************************************/
	// (R) for read only (U) for update on each request placement
	// (R) Hosts - a host is defined with a dc_id and host_id
	public String processDataMaxResources(Object[] ob) {
		int[] host_milano1 = (int[]) ob[0];
		int[] host_milano2 = (int[]) ob[1];
		int[] host_genova = (int[]) ob[2];
		int[] host_sanova = (int[]) ob[3];
		int[] host_torino = (int[]) ob[4];
		int[] userVm = (int[]) ob[5];
		int[] vm1 = (int[]) ob[6];
		int[] vm2 = (int[]) ob[7];
		int[] vm3 = (int[]) ob[8];		
		StringBuilder mr = new StringBuilder("H={<2,1>,<4,3>,<6,5>,<8,7>,<10,9>,<10,11>,<12,13>,<14,15>,<16,17>};"); 
		mr.append(System.lineSeparator());
		// (R) Links
		mr.append("L={<<2,1>,<10,9>,1>,<<4,3>,<12,13>,2>,<<6,5>,<14,15>,3>,");	
		mr.append("<<8,7>,<16,17>,4>,<<10,9>,<12,13>,5>,<<10,9>,<14,15>,6>,");	
		mr.append("<<10,9>,<16,17>,7>,<<12,13>,<14,15>,8>,");	
		mr.append("<<12,13>,<16,17>,9>,<<14,15>,<16,17>,10>,");	
		mr.append("<<10,9>,<2,1>,11>,<<12,13>,<4,3>,12>,");	
		mr.append("<<14,15>,<6,5>,13>,<<16,17>,<8,7>,14>,");	
		mr.append("<<12,13>,<10,9>,15>,<<14,15>,<10,9>,16>,");	
		mr.append("<<16,17>,<10,9>,17>,<<14,15>,<12,13>,18>,");	
		mr.append("<<16,17>,<12,13>,19>,<<16,17>,<14,15>,20>,"); 
		mr.append("<<10,9>,<10,11>,21>,<<10,11>,<10,9>,22>};");	mr.append(System.lineSeparator());
		// links 1 to 10 specify one direction, 11 to 20 the inverse direction of the bidirectional link, 
		// 21 and 22 the twi directions of a link between the two hosts in DC 10
		// (R) User_Hosts
		mr.append("UH={<2,1>,<4,3>,<6,5>,<8,7>};"); mr.append(System.lineSeparator());
		// (U) User host used to host userVM
		mr.append("hhat={<"+userVm[0]+","+userVm[1]+">};");	mr.append(System.lineSeparator());
		mr.append("HS={<"+userVm[0]+","+userVm[1]+">};");	mr.append(System.lineSeparator());
		// (R) Resources in Hosts
		mr.append("R={CPU,MEM};"); mr.append(System.lineSeparator());
		// (R) Resources in Links
		mr.append("Rhyphen={BW};"); mr.append(System.lineSeparator());
		// (R) Host Metrics -> not used here
		mr.append("M={ENERGYEFFICIENT};"); mr.append(System.lineSeparator());
		// (R) Link metrics
		mr.append("Mhyphen={Delay};"); mr.append(System.lineSeparator());
		// (U) Host Available resources
		mr.append("\"CPU\":[4000,4000,4000,4000,"+host_milano1[0]+","+host_milano2[0]+","
				 +host_genova[0]+","+host_sanova[0]+","+host_torino[0]+"],");	
		mr.append("\"MEM\":[8048000,8048000,8048000,8048000,"+host_milano1[1]+","+host_milano2[1]+","
				 +host_genova[1]+","+host_sanova[1]+","+host_torino[1]+"]]#;"); 
		mr.append(System.lineSeparator());
		// (R) Initial resource capacities
		mr.append("ibeta=#[\"CPU\":[4000,4000,4000,4000,24,24,24,16,24],"); 
		mr.append("\"MEM\":[8048000,8048000,8048000,8048000,65536,65536,20480,16384,49152]]#;"); 
		mr.append(System.lineSeparator());
		// (R) Host Metrics values -> not used here
		mr.append("m=#[\"ENERGYEFFICIENT\":[1,1,1,1,1,1,1,1,1]]#;"); mr.append(System.lineSeparator());
		// (U) Link Available resources 
		mr.append("b=#[\"BW\":[125000,125000,125000,125000,1250000,1250000,1250000,1250000,1250000,1250000,"); 
		mr.append("125000,125000,125000,125000,1250000,1250000,1250000,1250000,1250000,1250000,125000000,125000000]]#;"); 
		mr.append(System.lineSeparator());
		// (U) Link metrics values
		mr.append("mi=#[\"Delay\":[25,25,25,25,70,120,70,70,70,70,"); 
		mr.append("25,25,25,25,70,120,70,70,70,70,0,0]]#;");	mr.append(System.lineSeparator());
		// (U) Service chain nodes (tiers)
		mr.append("T={<"+vm1[2]+">,<"+vm2[2]+">,<"+vm3[2]+">,<"+userVm[4]+">};"); mr.append(System.lineSeparator());
		// (U) Service chain channels (links) between tiers
		mr.append("C={<<"+vm1[2]+">,<"+vm2[2]+">,1>,<<"+vm2[2]+">,<"+vm1[2]+">,2>,");	
		mr.append("<<"+vm2[2]+">,<"+vm3[2]+">,3>,<<"+vm3[2]+">,<"+vm2[2]+">,4>,<<"+userVm[4]+">,<"+vm1[2]+">,5>,<<"+vm1[2]+">,<"+userVm[4]+">,6>};");				
		// (U) User app (tier)
		mr.append("shat={<"+userVm[4]+">};"); mr.append(System.lineSeparator());
		mr.append("S={<"+userVm[4]+">};"); mr.append(System.lineSeparator());
		// (U) tier resources demand
		mr.append("a=#[\"CPU\": ["+vm1[0]+","+vm2[0]+","+vm3[0]+","+userVm[2]+"],"); 
		mr.append("\"MEM\": ["+vm1[1]+","+vm2[1]+","+vm3[1]+","+userVm[3]+"]]#;");	mr.append(System.lineSeparator());
		// (U) channel resources demand
		//mr.append("c=#[\"BW\":[13,13,53,53,78,78,18,18]]#;"); mr.append(System.lineSeparator());
		mr.append("c=#[\"BW\":[10,10,10,10,10,10]]#;"); mr.append(System.lineSeparator());
		// *** CAUTION -> JUST MAKE SURE THAT THE NUMBER OF ENTRIES EQUALS TIERS FOR wu,wl AND CHANNEL FOR zl,zu
		// Requested range for a host metric ->no effect -> disabled
		mr.append("wl=#[\"ENERGYEFFICIENT\":[1,1,1,1]]#;"); mr.append(System.lineSeparator());
		mr.append("wu=#[\"ENERGYEFFICIENT\":[1,1,1,1]]#;"); mr.append(System.lineSeparator());
		// Requested range for a link metric -> no effect -> disabled
		//mr.append("zl=#[\"Delay\":[0,0,0,0,0,0,0,0]]#;"); mr.append(System.lineSeparator());
		mr.append("zl=#[\"Delay\":[0,0,0,0,0,0]]#;"); mr.append(System.lineSeparator());
		//mr.append("zu=#[\"Delay\":[10000,10000,10000,10000,10000,10000,10000,10000]]#;"); mr.append(System.lineSeparator());
		mr.append("zu=#[\"Delay\":[10000,10000,10000,10000,10000,10000]]#;");
		
		return mr.toString();
				
	}	
	
}
