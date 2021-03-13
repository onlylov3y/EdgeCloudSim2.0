package org.cloudbus.cloudsim.edge.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.EdgeDatacenterBroker;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.PlacementOptimizer;
import org.cloudbus.cloudsim.edge.ServiceTyp;
import org.cloudbus.cloudsim.edge.lists.ServiceList;
import org.cloudbus.cloudsim.edge.random.ExponentialRNS;
import org.cloudbus.cloudsim.edge.service.EdgeDbService;
import org.cloudbus.cloudsim.edge.service.EdgeStreamingService;
import org.cloudbus.cloudsim.edge.service.EdgeWebService;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.vm.T2Nano;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyCpu;
import org.cloudbus.cloudsim.network.datacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class BaseDatacenter {

	/**
	 * simulates 1 day = 24 * 60 * 60 * 1000 msec = 86400000 msec
	 */
	private static double simulationTime = 86400000;

	private BaseDatacenter() {

	}

	/**
	 * Creates a datacenter for an User only, it has only one host and can take
	 * only one T2NANO {@link T2Nano} }.
	 * 
	 * @param name
	 *            the name
	 * @param dcNum
	 *            the number of available data centers in this simulation
	 * 
	 * @return the NetworkDatacenter
	 */
	public static NetworkDatacenter createUserNetworkDatacenter(String name, int numHost, int ram, int hostCpuNum) {

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		// int mips = 18870;
		int mips = NetworkConstants.DEFAULT_CPU_MIPS;
		// int ram = 8048;
		long storage = 1000000;
		// bandwidth
		int bw = 50000;
		// system architecture
		String arch = "x86";
		// operating system
		String os = "Linux";
		String vmm = "Xen";
		// time zone this resource located
		double time_zone = 10.0;
		// the cost of using processing in this resource
		double cost = 3.0;
		// the cost of using memory in this resource
		double costPerMem = 0.05;
		// the cost of using storage in this resource
		double costPerStorage = 0.001;
		// the cost of using bw in this resource
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		for (int i = 0; i < numHost; i++) {
			// creates an host.
			hostList.add(createHost(mips, ram, storage, bw, hostCpuNum, ""));
		}

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// create the NetworkDatacenter object.
		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(name, characteristics, new VmAllocationPolicyCpu(hostList), storageList,
					0);
			datacenter.setUserDC(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// Create Internal Datacenter network
		createInternalDcNetwork(datacenter);
		return datacenter;
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the DC name
	 * @param numHost
	 *            number of host in this DC
	 * @param ram
	 *            Amount of RAM per host
	 * @param hostCpuNum
	 *            Amount of CPU per host
	 * @return NetworkDatacenter the NetworkDatacenter
	 */
	public static NetworkDatacenter createNetworkDatacenter(String name, int numHost, int ram, int hostCpuNum,
			String... hostNames) {

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		// int mips = 18870;
		int mips = NetworkConstants.DEFAULT_CPU_MIPS;
		// int ram = 8048;
		long storage = 1000000;
		// bandwidth
		int bw = 50000;
		// system architecture
		String arch = "x86";
		// operating system
		String os = "Linux";
		String vmm = "Xen";
		// time zone this resource located
		double time_zone = 10.0;
		// the cost of using processing in this resource
		double cost = 3.0;
		// the cost of using memory in this resource
		double costPerMem = 0.05;
		// the cost of using storage in this resource
		double costPerStorage = 0.001;
		// the cost of using bw in this resource
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		for (int i = 0; i < numHost; i++) {
			// creates an host.
			EdgeHost host;
			try {
				host = createHost(mips, ram, storage, bw, hostCpuNum, hostNames[i]);
			} catch (Exception e) {
				host = createHost(mips, ram, storage, bw, hostCpuNum, "");
			}
			hostList.add(host);

			CustomLog.printServer("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + host.getId(), ((EdgeHost) host).getName(), name,
					host.getRamProvisioner().getAvailableRam(), host.getPeList().size(), host.getAvailableMips(),
					host.getBwProvisioner().getAvailableBw(), storage, 0);
		}

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// create the NetworkDatacenter object.
		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(name, characteristics, new VmAllocationPolicyCpu(hostList), storageList,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// Create Internal Datacenter network
		createInternalDcNetwork(datacenter);	
		return datacenter;
	}

	/**
	 * Create an EdgeHost.
	 * 
	 * @param mips
	 *            the mips
	 * @param ram
	 *            the amount of RAM
	 * @param storage
	 *            the amount of Storage
	 * @param bw
	 *            the bandwidth
	 * @param numOfPes
	 *            the number of processing units (CPUs)
	 * @return
	 */
	public static EdgeHost createHost(int mips, int ram, long storage, int bw, int numOfPes, String hostName) {
		// List of Processing elements (CPU)
		List<Pe> peList = new ArrayList<Pe>();
		for (int i = 0; i < numOfPes; i++) {
			peList.add(new Pe(new PeProvisionerSimple(mips)));
		}
		return new EdgeHost(new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerSpaceShared(peList), hostName);
	}


	/**
	 * inter-connect data centers.
	 * 
	 * @param dcs
	 *            list of data centers
	 * @throws Exception
	 */
	public static void createNetworkWorking() throws Exception {
		ArrayList<NetworkDatacenter> udcs = new ArrayList<>();
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		ArrayList<AggregateSwitch> aggSwitch = new ArrayList<>();
		ArrayList<EdgeDatacenterBroker> brokers = new ArrayList<>();

		NetworkDatacenter udc;
		for (int i = 0; i < 4; i++) {
			udc = createUserNetworkDatacenter("User_DC" + i, 1, 1000 * 8048, 1000 * 4);
			udcs.add(udc);
		}

		dcs.add(createNetworkDatacenter("DC_Milano", 2, 64 * 1024, 24, "server-004", "server-005"));
		dcs.add(createNetworkDatacenter("DC_Genova", 1, 20 * 1024, 24, "server-002"));
		// DC with 1 host (16 GB, 16 CPUs)
		dcs.add(createNetworkDatacenter("DC_Savona", 1, 16 * 1024, 16, "server-001"));
		dcs.add(createNetworkDatacenter("DC_Torino", 1, 48 * 1024, 24, "server-003"));

		aggSwitch.add(new AggregateSwitch("MI-a", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #0
		aggSwitch.add(new AggregateSwitch("Milano", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #1
		aggSwitch.add(new AggregateSwitch("GE-a", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #2
		aggSwitch.add(new AggregateSwitch("Genova", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #3
		aggSwitch.add(new AggregateSwitch("SV-a", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #4
		aggSwitch.add(new AggregateSwitch("Savona", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #5
		aggSwitch.add(new AggregateSwitch("TO-a", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #6
		aggSwitch.add(new AggregateSwitch("Torino", NetworkConstants.Agg_LEVEL, dcs.get(0))); // #7

		aggSwitch.get(0).uplinkswitches.add(aggSwitch.get(1));

		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(0));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(3));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(7));

		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(3));

		aggSwitch.get(3).uplinkswitches.add(aggSwitch.get(1));
		aggSwitch.get(3).uplinkswitches.add(aggSwitch.get(2));
		aggSwitch.get(3).uplinkswitches.add(aggSwitch.get(5));
		aggSwitch.get(3).uplinkswitches.add(aggSwitch.get(7));

		aggSwitch.get(4).uplinkswitches.add(aggSwitch.get(5));

		aggSwitch.get(5).uplinkswitches.add(aggSwitch.get(3));
		aggSwitch.get(5).uplinkswitches.add(aggSwitch.get(4));
		aggSwitch.get(5).uplinkswitches.add(aggSwitch.get(7));

		aggSwitch.get(6).uplinkswitches.add(aggSwitch.get(7));

		aggSwitch.get(7).uplinkswitches.add(aggSwitch.get(1));
		aggSwitch.get(7).uplinkswitches.add(aggSwitch.get(3));
		aggSwitch.get(7).uplinkswitches.add(aggSwitch.get(5));
		aggSwitch.get(7).uplinkswitches.add(aggSwitch.get(6));

		// Generate Web Service start times, one per 5 min = 5 *60 *1000 ms =
		// 300000 ms
		List<Double> webServiceStarts = getServiceStartTime(1.0 / 300000);
		// Generate DB Service start times,one per 5 min = 5 *60 *1000 ms =
		// 300000 ms
		List<Double> dbServiceStarts = getServiceStartTime(1.0 / 300000);
		// Generate Streaming Service start times, one per 5 min = 5 *60 *1000 ms =
		// 300000 ms
		List<Double> streamingServiceStarts = getServiceStartTime(1.0 / 300000);

		// One user per service!
		int numUserWeb = webServiceStarts.size();
		int numUserDb = dbServiceStarts.size();
		int numUserStreaming = streamingServiceStarts.size();
		EdgeDatacenterBroker broker;
		List<Double> serviceStart = new ArrayList<>();

		System.out.println(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter # USERS: "
				+ (numUserWeb + numUserDb + numUserStreaming));
		System.out.println(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter: " + numUserWeb + " WEB + "
				+ numUserDb + " DB + " + numUserStreaming + " STR");

		for (int i = 0; i < numUserWeb; i++) {
			broker = new EdgeDatacenterBroker("Broker_WEB" + i);
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter # Broker: " + broker.getName());
			Log.printLine();
			serviceStart.clear();
			serviceStart.add(webServiceStarts.get(i));
			// Add DB Services, lifetime distr. : 15 min = 15 * 60 * 1000 ms =
			// 900000 ms
			addServices(broker, serviceStart, ServiceTyp.WEB, 1.0 / 900000);
			// add requests for Web Services, request distr. : 1 min = 60 *
			// 1000 ms = 60000 ms
			addRequests(broker, serviceStart, ServiceTyp.WEB, 1.0 / 60000);
			broker.sortRequestIdList();
			brokers.add(broker);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[ERROR]: BaseDatacenter # Broker: " + broker.getName());
			Log.printLine();
			Log.printLine();
			Log.printLine();
		}
		for (int i = 0; i < numUserDb; i++) {
			broker = new EdgeDatacenterBroker("Broker_DB" + i);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter #	Broker: " + broker.getName());
			Log.printLine();
			serviceStart.clear();
			serviceStart.add(dbServiceStarts.get(i));
			// Add DB Services, lifetime distr. : 1h20min = 80 * 60 * 1000 ms =
			// 4800000 ms
			addServices(broker, serviceStart, ServiceTyp.DB, 1.0 / 4800000);
			// add requests for DB Services, request distr. : 10 min = 10 * 60 *
			// 1000 ms = 600000 ms
			addRequests(broker, serviceStart, ServiceTyp.DB, 1.0 / 600000);
			broker.sortRequestIdList();
			brokers.add(broker);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[ERROR]: BaseDatacenter # Broker: " + broker.getName());
			Log.printLine();
			Log.printLine();
			Log.printLine();
		}
		for (int i = 0; i < numUserStreaming; i++) {
			broker = new EdgeDatacenterBroker("Broker_STREAMING" + i);
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter # Broker: " + broker.getName());
			Log.printLine();
			serviceStart.clear();
			serviceStart.add(streamingServiceStarts.get(i));
			// Add STR Services, lifetime distr. : 20min = 20 * 60 * 1000 ms = 1200000 ms
			addServices(broker, serviceStart, ServiceTyp.STREAMING, 1.0 / 1200000);
			// add requests for STR Services, request distr. : min = 5 * 60 * 1000 ms = 300000 ms
			addStreamingRequest(broker, serviceStart, ServiceTyp.STREAMING, 1.0 / 300000, (1.0 / 30));
			broker.sortRequestIdList();
			brokers.add(broker);
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: BaseDatacenter #		 Broker: "
					+ broker.getName());
			Log.printLine();
			Log.printLine();
			Log.printLine();
		}

		// maps CloudSim entities to BRITE entities
		NetworkTopology.mapNode(udcs.get(0).getId(), 0);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(0)), 1);

		NetworkTopology.mapNode(aggSwitch.get(0).getId(), 2);

		NetworkTopology.mapNode(aggSwitch.get(1).getId(), 3);

		NetworkTopology.mapNode(dcs.get(0).getId(), 4);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(0)), 5);

		NetworkTopology.mapNode(aggSwitch.get(3).getId(), 6);

		NetworkTopology.mapNode(dcs.get(1).getId(), 7);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(1)), 8);

		NetworkTopology.mapNode(aggSwitch.get(2).getId(), 9);

		NetworkTopology.mapNode(udcs.get(1).getId(), 10);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(1)), 11);

		NetworkTopology.mapNode(udcs.get(2).getId(), 12);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(2)), 13);

		NetworkTopology.mapNode(aggSwitch.get(4).getId(), 14);

		NetworkTopology.mapNode(aggSwitch.get(5).getId(), 15);

		NetworkTopology.mapNode(dcs.get(2).getId(), 16);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(2)), 17);

		NetworkTopology.mapNode(aggSwitch.get(7).getId(), 18);

		NetworkTopology.mapNode(dcs.get(3).getId(), 19);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(3)), 20);

		NetworkTopology.mapNode(aggSwitch.get(6).getId(), 21);

		NetworkTopology.mapNode(udcs.get(3).getId(), 22);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(3)), 23);

		PlacementOptimizer po = new PlacementOptimizer();
		po.initiatedResources(udcs, dcs);			
	}

	/**
	 * generate a list of service start times.
	 * 
	 * @param lambda
	 *            distribution coefficient
	 * 
	 * @return list of service start times
	 */
	public static List<Double> getServiceStartTime(double lambda) {
		List<Double> serviceStarts = new ArrayList<>();
		ExponentialRNS interServiceStarttimeDist = new ExponentialRNS(lambda);
		for (double nextServiceStart = 0; nextServiceStart < simulationTime;) {
			double next = interServiceStarttimeDist.next();
			nextServiceStart += next;
			serviceStarts.add(nextServiceStart);
		}
		return serviceStarts;
	}

	/**
	 * @param broker
	 *            the Broker the services will be added to.
	 * @param serviceStarts
	 *            the services start times.
	 * @param serviceType
	 *            the type of the service, 0 => DB, 1 => Web
	 * @param lambda
	 *            distribution coefficient
	 */
	public static void addServices(EdgeDatacenterBroker broker, List<Double> serviceStarts, ServiceTyp serviceType,
			double lambda) {
		ExponentialRNS serviceLifetimeDist = new ExponentialRNS(lambda);
		Service service;
		// Add number of Web Service X based on the service start times.
		for (int i = 0; i < serviceStarts.size(); i++) {
			double next = serviceLifetimeDist.next();
			// This sum can be bigger than the simulation time!!!
			double startPlusLifetime = serviceStarts.get(i) + next;
			// choose Service type.
			switch (serviceType) {
			case DB:
				service = new EdgeDbService("EDS" + i, startPlusLifetime);
				break;
			case WEB:
				service = new EdgeWebService("EWS" + i, startPlusLifetime);
				break;
			case STREAMING:
				service = new EdgeStreamingService("ESS" + i, startPlusLifetime);
				break;

			default:
				service = null;
				break;
			}

			broker.addService(service);

			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter SERVICE: " + service.getName()
					+ ": #" + service.getReadableId());
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter START OF NEXT SERVICE: "
					+ serviceStarts.get(i));
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter LIFETIME OF SERVICE: " + next);
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter REAL LIFETIME OF SERVICE: "
					+ startPlusLifetime);
			Log.printLine();
		}
	}

	/**
	 * @param broker
	 *            the Broker the services will be added to.
	 * @param serviceStarts
	 *            the services start times.
	 * @param serviceType
	 *            the type of the service, 0 => DB, 1 => Web
	 * @param lambda
	 *            distribution coefficient
	 */
	public static void addRequests(EdgeDatacenterBroker broker, List<Double> serviceStarts, ServiceTyp serviceType,
			double lambda) {
		// Request Type List

		List<Service> serviceList = broker.getServiceList();
		List<Service> serviceTypeList;

		// List of Services of the given type

		switch (serviceType) {
		case DB:
			serviceTypeList = ServiceList.getDbServices(serviceList);
			break;
		case WEB:
			serviceTypeList = ServiceList.getWebServices(serviceList);
			break;
		default:
			serviceTypeList = new ArrayList<>();
			break;
		}

		Object[] data = new Object[3];
		int requestId;
		Service service;
		RequestId rId;
		double serviceStart;
		Message message;
		double next;
		for (int i = 0; i < serviceTypeList.size(); i++) {
			ExponentialRNS interRequestDist = new ExponentialRNS(lambda);
			requestId = 0;
			service = serviceTypeList.get(i);
			serviceStart = serviceStarts.get(i);

			// necessary to know to create the VM for this Service
			broker.addServiceFirstRequestTime(service.getId(), serviceStart);
			// simulates 30 min = 30 * 60 * 1000 msec = 1800000 msec
			for (double requestStart = serviceStart; requestStart <= simulationTime;) {
				// randomly choose Request type.
				message = Message.ZERO;
				rId = new RequestId(0, serviceType);
				data[0] = rId;
				data[1] = service.getId();
				data[2] = message;
				broker.addRequestId(service.getId(), rId);
				broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, requestStart);

				next = interRequestDist.next();
				requestStart += next;
				requestId++;
			}
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter # of Request for Service "
					+ service.getName() + " = " + requestId);
		}
	}

	public static void addStreamingRequest(EdgeDatacenterBroker broker, List<Double> serviceStarts,
			ServiceTyp serviceType, double lambda, double lambdaStreaming) {
		// Request Type List

		List<Service> serviceList = broker.getServiceList();
		List<Service> serviceTypeList;

		// List of Services of the given type

		serviceTypeList = ServiceList.getStreamingServices(serviceList);

		Object[] data = new Object[3];
		int requestId;
		Service service;

		RequestId rId, subRId = null;
		double serviceStart;
		Message message;
		double next;
		for (int i = 0; i < serviceTypeList.size(); i++) {

			ExponentialRNS interRequestDist = new ExponentialRNS(lambda);
			ExponentialRNS interVideoLengthDist = new ExponentialRNS(lambdaStreaming);
			requestId = 0;
			service = serviceTypeList.get(i);
			serviceStart = serviceStarts.get(i);

			// necessary to know to create the VM for this Service
			broker.addServiceFirstRequestTime(service.getId(), serviceStart);
			// simulates 30 min = 30 * 60 * 1000 msec = 1800000 msec
			for (double requestStart = serviceStart; requestStart <= simulationTime;) {
				int requestCount = 0;
				// randomly choose Request type.
				message = Message.ZERO;
				rId = new RequestId(0, ServiceTyp.STREAMING);
				double videoLength = interVideoLengthDist.next();
				Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter Service #"
						+ service.getName() + " video length = " + videoLength + "s");
				// every chunk is 2s of the video.
				int numOfChunks = (int) videoLength / 2;
				for (int j = 0; j < numOfChunks; j++) {
					subRId = new RequestId(rId.getNatural(), j, ServiceTyp.STREAMING);
					data[0] = subRId;
					data[1] = service.getId();
					data[2] = message;
					broker.addRequestId(service.getId(), subRId);
					requestCount++;
					broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, requestStart);
				}
				Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter # of Request for Service "
						+ service.getName() + " = " + requestCount);
				broker.addVideoLastRequest(subRId);

				next = interRequestDist.next();
				requestStart += next;
				requestId++;
			}
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: BaseDatacenter # of Request for Service "
					+ service.getName() + " = " + requestId);
		}
	}

	/**
	 * @return a list of all request types.
	 */
	public static List<Message> getMessageList() {
		List<Message> messageList = new ArrayList<>();
		messageList.add(Message.ZERO);
		messageList.add(Message.ONE);
		messageList.add(Message.TEN);
		messageList.add(Message.HUNDRED);
		messageList.add(Message.THOUSAND);
		return messageList;
	}

	public static int getDcFirstEdgeSwitch(NetworkDatacenter dc) {
		int key = -1;
		for (int k : dc.Switchlist.keySet()) {
			key = k;
			break;
		}
		return key;
	}

	/**
	 * define the internal network of a data center.
	 * 
	 * @param dc
	 *            the data center
	 */
	@SuppressWarnings("unchecked")
	public static void createInternalDcNetwork(NetworkDatacenter dc) {

		// Edge Switch
		EdgeSwitch edgeswitch = new EdgeSwitch(dc.getName() + "_Edge", NetworkConstants.EDGE_LEVEL, dc);

		dc.Switchlist.put(edgeswitch.getId(), edgeswitch);

		for (Host hs : dc.getHostList()) {
			EdgeHost hs1 = (EdgeHost) hs;
			// hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
			hs1.bandwidth = edgeswitch.downlinkbandwidth;
			edgeswitch.hostlist.put(hs.getId(), hs1);
			dc.HostToSwitchid.put(hs.getId(), edgeswitch.getId());
			hs1.sw = edgeswitch;
			// list of hosts connected to this switch for processing
			List<EdgeHost> hostList = (List<EdgeHost>) (List<?>) hs1.sw.fintimelistHost.get(0D);
			if (hostList == null) {
				hostList = new ArrayList<EdgeHost>();
				hs1.sw.fintimelistHost.put(0D, (List<NetworkHost>) (List<?>) hostList);
			}
			hostList.add(hs1);
		}

	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		System.out.println();
		System.out.println(indent + indent + indent + indent + indent + "========== OUTPUT ==========");
		System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				System.out.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}
}
