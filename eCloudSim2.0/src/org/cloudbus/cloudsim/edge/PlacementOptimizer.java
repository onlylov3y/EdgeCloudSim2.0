package org.cloudbus.cloudsim.edge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;


/**
 * PlacementOptimizer class contains methods to check current available
 * resources (CPU, RAM, BW) before every new incoming service chain. Provide
 * input parameters for CPLEX optimizer and get back optimized placements for
 * service chains.
 * 
 * @author Lam Dinh-Xuan
 */
public class PlacementOptimizer {
	
	private static List<Host> freeHostList = new ArrayList<Host>();
	private static List<String> concurrentService = new ArrayList<String>();
	
	public PlacementOptimizer() {}

	/**
	 * @param hostList
	 *            the free Host list
	 */
	public void setFreeHostList(List<Host> hostList) {	
		for (Host host : hostList) {
			PlacementOptimizer.freeHostList.add(host);
		}
	}
	
	public List<Host> getFreeHostList() {
		return PlacementOptimizer.freeHostList;
	}
	
	public void clearFreeHostList() {
		PlacementOptimizer.freeHostList.clear();
	}
	
	public List<String> getConcurrentServices() {
		return PlacementOptimizer.concurrentService;
	}
	
	public void addConcurrentService(String serviceId) {
		PlacementOptimizer.concurrentService.add(serviceId);
	}
	
	public void removeConcurrentService(String serviceId) {
		PlacementOptimizer.concurrentService.remove(PlacementOptimizer.concurrentService.indexOf(serviceId));
	}
	
	public void serverUsageToConcurrentServices(Object[] avr) {
		// Object[]{host_milano1,host_milano2,host_genova,host_sanova,host_torino,userVm,vm1,vm2,vm3}
		int[] host_milano1 = (int[]) avr[0];	// Object[0]
		int[] host_milano2 = (int[]) avr[1];	// Object[1]
		int[] host_genova = (int[]) avr[2];		// Object[2]
		int[] host_sanova = (int[]) avr[3];		// Object[3]
		int[] host_torino = (int[]) avr[4];		// Object[4]
		
		List<String> ccserv = getConcurrentServices();
		
//		System.out.println("=================" + TextUtil.toString(CloudSim.clock()) + "==========================");
//		System.out.println("Milano 1  Milano 2  Genova  Sanova  Torino Concurrent Services");
//		System.out.println(host_milano1[0] + "  " 
//						 + host_milano2[0] + "  "
//						 + host_genova[0] + "  "
//						 + host_sanova[0] + "  "
//						 + host_torino[0] + "  "
//						 + ccserv.size());
		String ccsId = "-";
		if (ccserv.size() > 0) {
			ccsId = "#" + ccserv.get(ccserv.size()-1);
		}
		CustomLog.printServerToCCS("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", TextUtil.toString(CloudSim.clock()), ccsId, 
												host_milano1[0], host_milano1[1],
												host_milano2[0], host_milano2[1],
												host_genova[0], host_genova[1],
												host_sanova[0], host_sanova[1],
												host_torino[0], host_torino[1],
												ccserv.size());		
	}
	
	/**
	 * Return list of hosts and datacenter that have available resources	 * 
	 * @param hostList
	 * 		- List of hosts that are unused
	 * @return 
	 * @throws IOException 
	 */	
	public Object[] availableResources(List<Host> hostList, List<Vm> vmList){
		int[] host_milano1 = new int[2];	// Object[0]
		int[] host_milano2 = new int[2];	// Object[1]
		int[] host_genova = new int[2];		// Object[2]
		int[] host_sanova = new int[2];		// Object[3]
		int[] host_torino = new int[2];		// Object[4]
		for (Host host : hostList) {			
//			System.out.println(host.getDatacenter().getName()
//							+ ";" + host.getDatacenter().getId()
//							+ ";" + host.getId() 
//							+ ";" + PeList.getNumberOfFreePes(host.getPeList())
//							+ ";" + host.getRamProvisioner().getAvailableRam()
//							+ ";" + host.getBwProvisioner().getAvailableBw());
			
			switch (host.getId()) {
				case 9: 	host_milano1[0] = PeList.getNumberOfFreePes(host.getPeList());
							host_milano1[1] = host.getRamProvisioner().getAvailableRam();
							break;
				case 11: 	host_milano2[0] = PeList.getNumberOfFreePes(host.getPeList());
							host_milano2[1] = host.getRamProvisioner().getAvailableRam();
							break;

				case 13: 	host_genova[0] = PeList.getNumberOfFreePes(host.getPeList());
							host_genova[1] = host.getRamProvisioner().getAvailableRam();
							break;

				case 15: 	host_sanova[0] = PeList.getNumberOfFreePes(host.getPeList());
							host_sanova[1] = host.getRamProvisioner().getAvailableRam();
							break;

				case 17: 	host_torino[0] = PeList.getNumberOfFreePes(host.getPeList());
							host_torino[1] = host.getRamProvisioner().getAvailableRam();
							break;				
			}
		}
		
		int[] userVm = new int[5];	// Object[5]		
		userVm[0] = vmList.get(0).getHost().getDatacenter().getId();
		userVm[1] = vmList.get(0).getHost().getId();
		userVm[2] = vmList.get(0).getNumberOfPes();
		userVm[3] = vmList.get(0).getRam();
		userVm[4] = vmList.get(0).getId();
		
		int[] vm1 = new int[3];		// Object[6]
		vm1[0] = vmList.get(1).getNumberOfPes();
		vm1[1] = vmList.get(1).getCurrentRequestedRam();
		vm1[2] = vmList.get(1).getId();	
		
		int[] vm2 = new int[3];		// Object[7]
		vm2[0] = vmList.get(2).getNumberOfPes();
		vm2[1] = vmList.get(2).getCurrentRequestedRam();
		vm2[2] = vmList.get(2).getId();

		int[] vm3 = new int[3];		// Object[8]
		vm3[0] = vmList.get(3).getNumberOfPes();
		vm3[1] = vmList.get(3).getCurrentRequestedRam();
		vm3[2] = vmList.get(3).getId();
		
		return new Object[]{host_milano1,host_milano2,host_genova,host_sanova,host_torino,userVm,vm1,vm2,vm3};
		
//		// Print out user VM and VMs that are waiting to be placed
//		System.out.println("@vm_id;dc_id;host_id;cpu;ram;bw");
//		// UserVM that has been created in datacenter
//		System.out.println(vmList.get(0).getId() + ";" + vmList.get(0).getHost().getId() +  ";" 
//				+ vmList.get(0).getHost().getDatacenter().getId() +  ";"  
//				+ vmList.get(0).getNumberOfPes() + ";" + vmList.get(0).getRam() + ";" + vmList.get(0).getBw());	
//		
//		// Service chain VMs that are requesting to be created in datacenter
//		System.out.println(vmList.get(1).getId() + ";-;-;" + vmList.get(1).getNumberOfPes() + ";" 
//				+ vmList.get(1).getCurrentRequestedRam() + ";" + vmList.get(1).getCurrentRequestedBw());
//		System.out.println(vmList.get(2).getId() + ";-;-;" + vmList.get(2).getNumberOfPes() + ";" 
//			+ vmList.get(2).getCurrentRequestedRam() + ";" + vmList.get(2).getCurrentRequestedBw());
//		System.out.println(vmList.get(3).getId() + ";-;-;" + vmList.get(3).getNumberOfPes() + ";" 
//			+ vmList.get(3).getCurrentRequestedRam() + ";" + vmList.get(3).getCurrentRequestedBw());
		
	}
	
	/**
	 * Return list of initiated hosts, datacenters and links between them
	 * @param userDcList
	 * 		- List of user datacenter
	 * @param dcList
	 * 		- List of datacenter for service chains 
	 */		
	public void initiatedResources(List<NetworkDatacenter> userDcList, List<NetworkDatacenter> dcList) {
		// Initiated resources in UsedDC
		System.out.println("@dc_name;dc_id;host_id;cpu;ram;bandwidth");	
		for (int i = 0; i < userDcList.size(); i++) {
			List<Host> hostList = userDcList.get(i).getHostList();
			for (Host host : hostList) {
				System.out.println(host.getDatacenter().getName()
								+ ";" + host.getDatacenter().getId()
								+ ";" + host.getId() 
								+ ";" + PeList.getNumberOfFreePes(host.getPeList())
								+ ";" + host.getRamProvisioner().getAvailableRam()
								+ ";" + host.getBwProvisioner().getAvailableBw());											
			}			
		}
		// Initiated resources in DCs		
		for (int i = 0; i < dcList.size(); i++) {
			List<Host> hostList = dcList.get(i).getHostList();
			for (Host host : hostList) {
				System.out.println(host.getDatacenter().getName()
								+ ";" + host.getDatacenter().getId()
								+ ";" + host.getId() 
								+ ";" + PeList.getNumberOfFreePes(host.getPeList())
								+ ";" + host.getRamProvisioner().getAvailableRam()
								+ ";" + host.getBwProvisioner().getAvailableBw());											
			}			
		}			
		// Links between UserDC and its corresponding datacenter
		System.out.println("@dcs_link_id;from;to;delay;bw");		
		for (int i = 0; i < 4; i++) {
			System.out.println(i+1 + ";" + userDcList.get(i).getId() + ";" + dcList.get(i).getId() + ";"	
					+ NetworkTopology.getDelay(userDcList.get(i).getId(), dcList.get(i).getId()) + ";125000");
		}
		// Links between datacenters
		int[][] pairDc = {{0,1}, {0,2}, {0,3}, {1,2}, {1,3}, {2,3}};
		for (int i = 0; i < pairDc.length; i++) {
			System.out.println(i+5 + ";" + dcList.get(pairDc[i][0]).getId() + ";" + dcList.get(pairDc[i][1]).getId() + ";"	
					+ NetworkTopology.getDelay(dcList.get(pairDc[i][0]).getId(), dcList.get(pairDc[i][1]).getId()) + ";1250000");			
		}
	}	

	public void delayBetweenHosts(List<EdgeHost> hostList) {
		// Links between hosts
		int[][] pairHost = {{0,1}, {0,2}, {0,3}, {0,4}, {1,2}, {1,3}, {1,4}, {2,3}, {2,4}, {3,4}};
		for (int i = 0; i < pairHost.length; i++) {
			System.out.println(i + ";" + hostList.get(pairHost[i][0]).getId() + ";" + hostList.get(pairHost[i][1]).getId() + ";"	
					+ NetworkTopology.getDelay(hostList.get(pairHost[i][0]).getId(), hostList.get(pairHost[i][1]).getId()) + ";1250000");			
		}
		

	}	

	/**
	 * Return list of datacenters available for service chain VMs. For testing only
	 * @return 
	 * 		- List of datacenter for service chains
	 * @author Lam Dinh-Xuan 
	 */		
	
	public List<Integer> findAvailableDatacenter(List<Vm> vmList, List<Host> hostList) {
		List<Host> freeHost = new ArrayList<Host>();

		for (Host host : hostList) {
			// To eliminate information from User datacenter
			String dc = host.getDatacenter().getName();
			boolean isFound = dc.indexOf("User_DC") !=-1? true: false;					
			if (!isFound) {				
				freeHost.add(host);
			}
		}
		
		double delay = Double.MAX_VALUE;
		int dcSelected = 0;
		List<Integer> dcIdFree = new ArrayList<>();
		int userDcId = vmList.get(0).getHost().getDatacenter().getId();
		
		// Calculate total required resources
		int requireCpu = vmList.get(1).getNumberOfPes() + vmList.get(2).getNumberOfPes() + vmList.get(3).getNumberOfPes();
		int requireRam = vmList.get(1).getRam() + vmList.get(2).getRam() + vmList.get(3).getRam();					
		long requireBw = vmList.get(1).getBw() + vmList.get(2).getBw() + vmList.get(3).getBw();		
		
		// loop until we get the smallest delay between datacenter and userDC
		for (Host host : freeHost) {
			double tmpDelay = NetworkTopology.getDelay(userDcId, host.getDatacenter().getId());
		
			// get all available datacenters
			if (PeList.getNumberOfFreePes(host.getPeList()) >= requireCpu
				&& host.getRamProvisioner().getAvailableRam() >= requireRam
				&& host.getBwProvisioner().getAvailableBw() >= requireBw) {
				dcIdFree.add(host.getDatacenter().getId());				
				// get the best one
				if (tmpDelay < delay) {
					delay = tmpDelay;
					dcSelected = host.getDatacenter().getId();
				}
			}
		}
		
		System.out.println("=====Available Datacenters: " + dcIdFree);
		System.out.println("=====Best Datacenter: " + dcSelected);
		return dcIdFree;
	}
}
