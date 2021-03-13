package org.cloudbus.cloudsim.edge.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

//import org.apache.commons.math3.geometry.spherical.twod.Edge;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.EdgeDatacenterBroker;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.PlacementOptimizer;
import org.cloudbus.cloudsim.edge.PresetEvent;
import org.cloudbus.cloudsim.edge.ProblemSolver;
import org.cloudbus.cloudsim.edge.ServiceTyp;
import org.cloudbus.cloudsim.edge.util.AlgorithmType;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.RequestId;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.edge.vm.VMStatus;
import org.cloudbus.cloudsim.edge.vm.EdgeVm;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

/**
 * @author Brice Kamneng Kwam
 */
public abstract class Service extends SimEntity {

	/**
	 * The User or Broker ID. It is advisable that broker set this ID with its
	 * own ID, so that CloudResource returns to it after the execution.
	 **/
	private int userId;

	/**
	 * Events that will be executed after the broker has started. The are
	 * usually set before the simulation start.
	 */
	private List<PresetEvent> presetEvents = new ArrayList<>();

	/** If this broker has started receiving and responding to events. */
	private boolean started = false;

	private Map<Integer, Message> cloudletIdToMessage = new HashMap<Integer, Message>();

	/**
	 * How long we should keep this broker alive. If negative - the broker is
	 * killed when no more cloudlets are left.
	 */
	private final double lifeLength;

	private boolean cloudletGenerated;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/** Number of VM destructions requested. */
	private int vmDestructsRequested = 0;

	/** Number of VM destructions acknowledged. */
	private int vmDestructsAcks = 0;

	/**
	 * The first Cloudlet of this Service. The one that communicates with the
	 * Broker
	 */
	private NetworkCloudlet firstCloudlet = null;
	/**
	 * The second Cloudlet of this Service. The one that communicates with the
	 * Broker
	 */
	private NetworkCloudlet secondCloudlet = null;
	/**
	 * The third Cloudlet of this Service. The one that communicates with the
	 * Broker
	 */
	private NetworkCloudlet thirdCloudlet = null;
	/**
	 * The Vm assigned to the first Cloudlet of this Service.
	 */
	private Vm firstVm = null;
	/**
	 * The Vm assigned to the first Cloudlet of this Service.
	 */
	private int firstVmId = -1;
	/**
	 * The Vm assigned to the second Cloudlet of this Service.
	 */
	private Vm secondVm = null;
	/**
	 * The Vm assigned to the third Cloudlet of this Service.
	 */
	private Vm thirdVm = null;
	/**
	 * The User Vm corresponding
	 */
	private Vm userVm = null;
	/**
	 * The CPLEX Solutions for optimization placement
	 */
	
	private Map<Integer, Integer> solution = null;
	
	/**
	 * The cloudlet of the Broker.
	 */
	private int brokerCloudletId = -1;
	/**
	 * The Vm of the Broker.
	 */
	private int brokerVmId = -1;

	/**
	 * Whether or not a request is being processed
	 */
	private boolean processingRequest;

	private Map<Integer, Integer> vmCreationAttempts;

	/**
	 * mapping between services and their Cloudlets (to help identify Services
	 * with their Cloudlets Id)
	 */
	private static Map<Integer, Service> cloudletIdToService = new HashMap<>();

	private ServiceTyp serviceTyp = null;

//	private AlgorithmType algorithmType = AlgorithmType.DEFAULT;
	private AlgorithmType algorithmType = AlgorithmType.ORCHESTRATION;
//	private AlgorithmType algorithmType = AlgorithmType.OPTIMIZATION;
	/**
	 * For orchestration, last sent Vm
	 */
	private Vm vmBeingCreated = null;

	/**
	 * the request being processed at the moment.
	 */
	private RequestId requestBeingProcessed = null;
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 *            - the name of the service.
	 * @param lifeLength
	 *            - for how long we need to keep this broker alive. If -1, then
	 *            the broker is kept alive/running untill all cloudlets
	 *            complete.
	 * @throws Exception
	 *             - from the superclass.
	 */
	public Service(String name, final double lifeLength) {
		super(name);
		this.lifeLength = lifeLength;
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		cloudletsSubmitted = 0;
		cloudletGenerated = false;

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());

		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		setVmCreationAttempts(new HashMap<Integer, Integer>());

	}

	/**
	 * This method is used to send to the broker the list with virtual machines
	 * that must be created.
	 * 
	 * @param list
	 *            the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * Process the return of a request for the characteristics of a
	 * PowerDatacenter.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);
		
		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());		
			submitVmList();			
		}
	}

	public abstract void submitVmList();

	/**
	 * Process a request for the available resource of a PowerDatacenter.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceConsumptionRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
				+ ": Cloud Resource List received with " + getDatacenterIdsList().size() + " resource(s)");
		
		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTagsExt.AVAILABLE_RESOURCE, getId());
		}
	}	
	
	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
				+ ": Cloud Resource List received with " + getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId); // a map with key and value
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId)); // a list of created VMs

			EdgeVm vm = (EdgeVm) VmList.getById(getVmsCreatedList(), vmId);

			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM #" + vmId
					+ " created in Datacenter #" + datacenterId + ", Host #" + vm.getHost().getReadableId());
		} else {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Service #" + getReadableId()
					+ ": Creation of VM #" + vmId + " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			// all the requested VMs have been created
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getReadableId()
					+ ": all the requested VMs have been created");
			
			// Add concurrent service request to list
			PlacementOptimizer po = new PlacementOptimizer();
			po.addConcurrentService(getReadableId());

			// print service chain
			System.out.println("[SERVICE CHAIN]: #" + getReadableId() + " " + getServiceTyp().getName()
					+ " #" + firstVm.getReadableId()
					+ " <=> " + "#" + secondVm.getReadableId() 
					+ " <=> " + "#" + thirdVm.getReadableId());
			
			printChain();

		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				int dcId = getNextDcIdWithShortestDelay();
				if (dcId != -1) {
					createVmsInDatacenter(dcId);
					return;
				}

				CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + getReadableId(), getServiceTyp().getName(),
						"blocked", -1);
				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Service #" + getReadableId()
							+ " some VMs were created... But not all. Aborting");
				} else { // no vms created.
					Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
							+ ": none of the required VMs could be created. Aborting");
				}
				// Abort.
				finishExecution();
			}
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreateOchestration(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];
				
		if (result == CloudSimTags.TRUE) {
			// if the first or next VM can be created in datacenter (there are still available resources)
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			
			EdgeVm vm = (EdgeVm) VmList.getById(getVmsCreatedList(), vmId);
			
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM #" + vmId
					+ " created in Datacenter #" + datacenterId + ", Host #" + vm.getHost().getReadableId());
			EdgeVm nextVm = (EdgeVm) getNextVmInChain(vmId);
			// if there remain VMs have not been created
			if (nextVm != null) {
				createVmsInDatacenterOchestration(datacenterId, nextVm);
			}
		} else {
			// find another closest Datacenters
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Service #" + getReadableId()
					+ ": Creation of VM #" + vmId + " failed in Datacenter #" + datacenterId);
			int nextDcId = getNextDcIdWithShortestDelayFrom(datacenterId);
			if (nextDcId != -1) {
				Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Service #" + getReadableId()
						+ ": will now try to create VM #" + vmId + " in Datacenter #" + nextDcId);
				createVmsInDatacenterOchestration(getNextDcIdWithShortestDelayFrom(datacenterId), getVmBeingCreated());
			} else {
				CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "Service #" + getReadableId(), getServiceTyp().getName(),
						"blocked", -1);
				// all datacenters have been already queried
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": Aborting!");
				finishExecution();
			}
		}

		incrementVmsAcks();

		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			// all the requested VMs have been created
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getReadableId()
					+ ": all the requested VMs have been created");
			// Add concurrent service request to list
			PlacementOptimizer po = new PlacementOptimizer();
			po.addConcurrentService(getReadableId());
			
			// print service chain
			System.out.println("[SERVICE CHAIN]: #" + getReadableId() + " " + getServiceTyp().getName()
					+ " #" + firstVm.getReadableId()
					+ " <=> " + "#" + secondVm.getReadableId() 
					+ " <=> " + "#" + thirdVm.getReadableId());
			
			printChain();
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreateOptimization(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId); // a map with key and value
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId)); // a list of created VMs

			EdgeVm vm = (EdgeVm) VmList.getById(getVmsCreatedList(), vmId);

			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM #" + vmId
					+ " created in Datacenter #" + datacenterId + ", Host #" + vm.getHost().getReadableId());
			
			EdgeVm nextVm = (EdgeVm) getNextVmInChain(vmId);
			// if there remain VMs have not been created
			if (nextVm != null) {
				createVmsInDatacenterOptimization(getCplexSolution(), nextVm);
			}
		} else {
			// Log.printLine --- System.out.println
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Service #" + getReadableId()
			+ ": Creation of first VM #" + vmId + " failed due to none available resources");			
			CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + getReadableId(), getServiceTyp().getName(),
					"blocked", -1);			
			finishExecution();
		}
		
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			// all the requested VMs have been created
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getReadableId()
					+ ": all the requested VMs have been created");
			
			// Add concurrent service request to list
			PlacementOptimizer po = new PlacementOptimizer();
			po.addConcurrentService(getReadableId());
			
			// print service chain
			// Log.printLine
			EdgeVm userVm = (EdgeVm) getUserVm();
			System.out.println("[SERVICE CHAIN]: #" + getReadableId() + " " + getServiceTyp().getName()
					+ " #" + userVm.getReadableId()
					+ " <=> " + "#" + firstVm.getReadableId()
					+ " <=> " + "#" + secondVm.getReadableId() 
					+ " <=> " + "#" + thirdVm.getReadableId());			
			printChain();
		} 
	}	
//	protected void processVmCreateOptimization(SimEvent ev) {
//		int[] data = (int[]) ev.getData();
//		int datacenterId = data[0];
//		int vmId = data[1];
//		int result = data[2];
//
//		if (result == CloudSimTags.TRUE) {
//			getVmsToDatacentersMap().put(vmId, datacenterId); // a map with key and value
//			getVmsCreatedList().add(VmList.getById(getVmList(), vmId)); // a list of created VMs
//
//			EdgeVm vm = (EdgeVm) VmList.getById(getVmsCreatedList(), vmId);
//
//			System.out.println(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM #" + vmId
//					+ " created in Datacenter #" + datacenterId + ", Host #" + vm.getHost().getReadableId());
//		} else {
//			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Service #" + getReadableId()
//					+ ": Creation of VM #" + vmId + " failed in Datacenter #" + datacenterId);
//		}
//
//		incrementVmsAcks();
//
//		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
//			// all the requested VMs have been created
//			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getReadableId()
//					+ ": all the requested VMs have been created");
//			// print service chain
//			System.out.println("[SERVICE CHAIN]: #" + getReadableId() + " " + getServiceTyp().getName()
//					+ " #" + firstVm.getReadableId()
//					+ " <=> " + "#" + secondVm.getReadableId() 
//					+ " <=> " + "#" + thirdVm.getReadableId());
//			
//			printChain();
//
//		} else {
//			// all the acks received, but some VMs were not created
//			if (getVmsRequested() == getVmsAcks()) {
//				// find id of the next datacenter that has not been tried
//				int dcId = getNextDcIdWithShortestDelay();
//				if (dcId != -1) {
//					createVmsInDatacenterOptimization(dcId);
//					return;
//				}
//
//				CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + getReadableId(), getServiceTyp().getName(),
//						"blocked", -1);
//				// all datacenters already queried
//				if (getVmsCreatedList().size() > 0) { // if some vm were created
//					Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Service #" + getReadableId()
//							+ " some VMs were created... But not all. Aborting");
//				} else { // no vms created.
//					Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
//							+ ": none of the required VMs could be created. Aborting");
//				}
//				// Abort.
//				finishExecution();
//			}
//		}
//	}	
	
	/**
	 * get the next Vm in the chain or -1 if this Id is the last or is unknown.
	 * 
	 * @param actualVmId
	 *            the actual Vm Id
	 * @return return the next Vm Id in the chain or -1 otherwise.
	 * does it mean there is only maximum three VMs?
	 */
	public Vm getNextVmInChain(int actualVmId) {
		if (actualVmId == getFirstVm().getId()) {
			return getSecondVm();
		} else if (actualVmId == getSecondVm().getId()) {
			return getThirdVm();
		} else { // this might be the third vm or an un known ID.
			return null;
		}
	}

	/**
	 * get the Vm in the chain or -1 if this Id is the last or is unknown.
	 * 
	 * @param actualVmId
	 *            the actual Vm Id
	 * @return return the next Vm Id in the chain or -1 otherwise.
	 * does it mean there is only maximum three VMs?
	 */
	public Vm getVmInChain(int actualVmId) {
		if (actualVmId == getFirstVm().getId()) {
			return getFirstVm();
		} else if (actualVmId == getSecondVm().getId()) {
			return getSecondVm();
		} else if (actualVmId == getThirdVm().getId()) {
			return getThirdVm();			
		} else { // this might be the third vm or an un known ID.
			return null;
		}
	}	
	
	/**
	 * print this Service chain of VMs
	 */
	public void printChain() {
		Vm userVm = getUserVm();
		Vm firstVm = getVmsCreatedList().get(0);
		Vm secondVm = getVmsCreatedList().get(1);
		Vm thirdVm = getVmsCreatedList().get(2);
		
		int hopCount = 0;
		hopCount += NetworkTopology.countHops(userVm.getHost().getDatacenter().getId(),
				firstVm.getHost().getDatacenter().getId());
		
		if (firstVm.getHost().getId() != secondVm.getHost().getId()) {			
			// Count from Datacenter
			hopCount += NetworkTopology.countHops(firstVm.getHost().getDatacenter().getId(),
					secondVm.getHost().getDatacenter().getId());
		}
		if (secondVm.getHost().getId() != thirdVm.getHost().getId()) {
			hopCount += NetworkTopology.countHops(secondVm.getHost().getDatacenter().getId(),
					thirdVm.getHost().getDatacenter().getId());
		}

		CustomLog.printServiceChain("%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + getReadableId(), getServiceTyp().getName(),
						"#" + userVm.getReadableId() + " <=> " +
						"#" + firstVm.getReadableId() + " <=> " + 
						"#" + secondVm.getReadableId() + " <=> " + 
						"#" + thirdVm.getReadableId(), hopCount);
		
//		System.out.println("[CREATED SERVICE CHAIN ]: #" + getReadableId() + " " + getServiceTyp().getName()
//				+ " #" + userVm.getReadableId()
//				+ " <=> " + "#" + firstVm.getReadableId()
//				+ " <=> " + "#" + secondVm.getReadableId() 
//				+ " <=> " + "#" + thirdVm.getReadableId());					
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId
	 *            Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		// String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) { 
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
						+ ": Trying to Create VM #" + vm.getId() + " in Datacenter #" + datacenterId);				
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				// Statistics
				if (getVmCreationAttempts().containsKey(vm.getId())) {
					getVmCreationAttempts().put(vm.getId(), getVmCreationAttempts().get(vm.getId()) + 1);
				} else {
					getVmCreationAttempts().put(vm.getId(), 1);
				}
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId
	 *            Id of the chosen Datacenter
	 * @param vmId
	 *            Id of the the Vm to create
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenterOchestration(int datacenterId, Vm vm) {
		// is called by processOtherEvent - CloudSimTags.SERVICE_SUBMIT_VMS_NOW
		// send first VM only to this datacenter before
		int requestedVms = getVmsRequested();
		if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
					+ ": Trying to Create VM #" + vm.getId() + " in Datacenter #" + datacenterId);
			
			// Send first VM to Datacenter (in Datacenter.java class) that will host service chain VMs
			// only Datacenters for service chains. Asking datacenters if there are still available resources			
			sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
			requestedVms++;

			// Statistics
			addVmCreationAttempt(vm.getId());
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);

		setVmBeingCreated(vm);
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * @param vm1_id 
	 * @param avr 
	 * 
	 * @param datacenterId
	 *            Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenterOptimization(Map<Integer, Integer> solution, Vm vm) {
		if (solution != null) {
			// for each vm, send request to NetworkDatacenter to create VM, 3 times in total
			// for optimization, it can be assigned to one or more pre-defined datacenterId
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				// Log.printLine; System.out.println						
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
						+ ": Trying to Create VM #" + vm.getId() + " in Datacenter #" + solution.get(vm.getId()));				
				// Send at once to NetworkDatacenter class, 3 times for 3 VMs
				sendNow(solution.get(vm.getId()), CloudSimTags.VM_CREATE_ACK, vm);
			}
		} else {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.FALSE;
			sendNow(getId(), CloudSimTags.VM_CREATE_ACK, data);
		}
	}
	
//	protected void createVmsInDatacenterOptimization(Map<Integer, Integer> solution, List<Vm> serviceVmList) {
//		if (solution != null) {
//			// for each vm, send request to NetworkDatacenter to create VM, 3 times in total
//			// for optimization, it can be assigned to one or more pre-defined datacenterId			
//			for (Vm vm : serviceVmList) {
//					if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
//						// Log.printLine; System.out.println						
//						Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
//								+ ": Trying to Create VM #" + vm.getId() + " in Datacenter #" + solution.get(vm.getId()));
//						
//						// Send at once to NetworkDatacenter class, 3 times for 3 VMs
//						sendNow(solution.get(vm.getId()), CloudSimTags.VM_CREATE_ACK, vm);
//						
////						 Statistics
//						if (getVmCreationAttempts().containsKey(vm.getId())) {
//							getVmCreationAttempts().put(vm.getId(), getVmCreationAttempts().get(vm.getId()) + 1);
//						} else {
//							getVmCreationAttempts().put(vm.getId(), 1);
//						}
//					}					
//				getDatacenterRequestedIdsList().add(solution.get(vm.getId()));
//			}						
//		} else {
//			int[] data = new int[3];
//			data[0] = getId();
//			data[1] = serviceVmList.get(0).getId();
//			data[2] = CloudSimTags.FALSE;
//			sendNow(getId(), CloudSimTags.VM_CREATE_ACK, data);
//		}
//	}	
	/**
	 * Statistics: count this attempt to create the given VM Id
	 * 
	 * @param vmId
	 *            the VM Id
	 */
	public void addVmCreationAttempt(int vmId) {
		if (getVmCreationAttempts().containsKey(vmId)) {
			getVmCreationAttempts().put(vmId, getVmCreationAttempts().get(vmId) + 1);
		} else {
			getVmCreationAttempts().put(vmId, 1);
		}
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			if (vm.getHost() == null || vm.getHost().getDatacenter() == null) {
				Log.print("VM " + vm.getReadableId()
						+ " has not been assigned in a valid way and can not be terminated.");
				continue;
			}

			// Update the cloudlets before we send the kill event
			vm.getHost().updateVmsProcessing(CloudSim.clock());

			Log.printLine(CloudSim.clock() + ": Service #" + getReadableId() + ": Trying to Destroy VM #"
					+ vm.getReadableId() + " in DC #" + getVmsToDatacentersMap().get(vm.getReadableId()));

			// Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #"
			// + getId() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY_ACK, vm);
			incrementVmsDetructsRequested();
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM CREATION STATS");
		getVmCreationAttempts().forEach((k, v) -> Log.printLine(
				TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM #" + k + ": " + v));

		Log.printLine(
				TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": AUTO DESTRUCTION started");
		// Notify Broker
		sendNow(getUserId(), CloudSimTagsExt.SERVICE_DESTROYED_ITSELF);

		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	private void finilizeVM(final Vm vm) {
		if (vm instanceof EdgeVm) {
			EdgeVm vmEX = ((EdgeVm) vm);
			if (vmEX.getStatus() != VMStatus.TERMINATED) {
				vmEX.setStatus(VMStatus.TERMINATED);
			}
		}
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param vmList
	 *            the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param vmsCreatedList
	 *            the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested
	 *            the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks
	 *            the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	protected void incrementVmsDetructsRequested() {
		this.vmDestructsRequested++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed
	 *            the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList
	 *            the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList
	 *            the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList
	 *            the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

	/**
	 * Sets the number of requested VM destructions.
	 * 
	 * @param vmDestructsRequested
	 *            - the number of requested VM destructions. A valid positive
	 *            integer or 0.
	 */
	public void setVmDestructsRequested(int vmDestructsRequested) {
		this.vmDestructsRequested = vmDestructsRequested;
	}

	/**
	 * Returns the number of requested VM destructions.
	 * 
	 * @return the number of requested VM destructions.
	 */
	public int getVmDestructsRequested() {
		return vmDestructsRequested;
	}

	/**
	 * Returns the number of acknowledged VM destructions.
	 * 
	 * @return the number of acknowledged VM destructions.
	 */
	public int getVmDestructsAcks() {
		return vmDestructsAcks;
	}

	/**
	 * Sets the number of acknowledged VM destructions.
	 * 
	 * @param vmDestructsAcks
	 *            - acknowledged VM destructions. A valid positive integer or 0.
	 */
	public void setVmDestructsAcks(int vmDestructsAcks) {
		this.vmDestructsAcks = vmDestructsAcks;
	}

	/**
	 * Increments the counter of VM destruction acknowledgments.
	 */
	protected void incrementVmDesctructsAcks() {
		vmDestructsAcks++;
	}

	/**
	 * Submits the list of vms after a given delay
	 * 
	 * @param list
	 * @param delay
	 */
	public void createVmsAfter(List<? extends Vm> vms, double delay) {
		if (started) {
			send(getId(), delay, CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW, vms);
		} else {
			presetEvent(getId(), CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW, vms, delay);
		}
	}

	/**
	 * Destroys the VMs after a specified time period. Used mostly for testing
	 * purposes.
	 * 
	 * @param vms
	 *            - the list of vms to terminate.
	 * @param delay
	 *            - the period to wait for.
	 */
	public void destroyVMsAfter(final List<? extends Vm> vms, double delay) {
		if (started) {
			send(getId(), delay, CloudSimTagsExt.SERVICE_DESTROY_VMS_NOW, vms);
		} else {
			presetEvent(getId(), CloudSimTagsExt.SERVICE_DESTROY_VMS_NOW, vms, delay);
		}
	}

	private void processVMDestroy(SimEvent ev) {
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
				+ ": PROCESSING VM DESTROYED ACK");
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			Vm vm = VmList.getById(getVmsCreatedList(), vmId);

			// One more ack. to consider
			incrementVmDesctructsAcks();

			// Remove the vm from the created list
			getVmsCreatedList().remove(vm);
			finilizeVM(vm);

			// Kill all cloudlets associated with this VM

			for (Cloudlet cloudlet : getCloudletSubmittedList()) {
				if (!cloudlet.isFinished() && vmId == cloudlet.getVmId()) {
					Log.printLine(
							TextUtil.toString(CloudSim.clock()) + ": Service #" + "TRYING TO TERMINATE CLOUDLET #"
									+ cloudlet.getReadableId() + " ASSOCIATED WITH VM #" + cloudlet.getVmId());
					try {
						// Vm is always null at this point
						// vm.getCloudletScheduler().cloudletCancel(cloudlet.getCloudletId());
						cloudlet.setCloudletStatus(Cloudlet.FAILED_RESOURCE_UNAVAILABLE);
					} catch (Exception e) {
						CustomLog.logError(Level.SEVERE, e.getMessage(), e);
						Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
								+ ": CLOUDLET TERMINATION DID NOT WORK!!!");
						Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
								+ ": Level: " + Level.SEVERE + " - Exception Message: " + e.getMessage()
								+ " - Exception Type: " + e.toString());
					}

					sendNow(cloudlet.getUserId(), CloudSimTags.CLOUDLET_RETURN, cloudlet);
				}
			}

			// Use the standard log for consistency ....
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": VM #" + vmId
					+ " has been destroyed in Datacenter #" + datacenterId);
		} else {
			// Use the standard log for consistency ....
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
					+ ": Desctuction of VM #" + vmId + " failed in Datacenter #" + datacenterId);
		}

	}

	/**
	 * Destroys/terminates the vms.
	 * 
	 * @param vms
	 *            - the vms to terminate. Must not be null.
	 */
	public void destroyVMList(final List<? extends Vm> vms) {
		if (getVmDestructsAcks() != getVmsDestroyed()) {
			throw new IllegalStateException("#" + getVmsDestroyed() + " have been marked for termination, but only #"
					+ getVmDestructsAcks() + " acknowlegdements have been received.");
		}

		int requestedVmTerminations = 0;
		for (final Vm vm : vms) {
			if (vm.getHost() == null || vm.getHost().getDatacenter() == null) {
				Log.print("VM " + vm.getReadableId()
						+ " has not been assigned in a valid way and can not be terminated.");
				continue;
			}

			// Update the cloudlets before we send the kill event
			vm.getHost().updateVmsProcessing(CloudSim.clock());

			int datacenterId = vm.getHost().getDatacenter().getId();

			// Tell the data center to destroy it
			sendNow(datacenterId, CloudSimTags.VM_DESTROY_ACK, vm);
			requestedVmTerminations++;
		}

		setVmsDestroyed(requestedVmTerminations);
		setVmDestructsAcks(0);
	}

	/**
	 * get the next datacenter with the shortest delay to this broker. meaning
	 * the next best Dc after those that have already been requested.
	 * 
	 * @pre Network enable
	 * @post none
	 * @return the next datacenter with the shortest delay
	 */
	public int getNextDcIdWithShortestDelay() {
		int datacenterId = -1;
		double delay = Double.MAX_VALUE;

		Log.printLine(
				TextUtil.toString(CloudSim.clock()) + "[INFO]: Service #: " + getReadableId() + " Issue with UserDC");
		int userDcId = ((EdgeDatacenterBroker) CloudSim.getEntity(getUserId())).getUserDC().getId();
		// loop until we get the smallest delay between datacenter and userDC
		for (Integer dcId : datacenterIdsList) {
			if (!((NetworkDatacenter) CloudSim.getEntity(dcId)).isUserDC()
					&& !getDatacenterRequestedIdsList().contains(dcId)) {
				double tmpDelay = NetworkTopology.getDelay(userDcId, dcId);

				if (tmpDelay < delay && !(getDatacenterRequestedIdsList().contains(dcId))) {
					datacenterId = dcId;
					delay = tmpDelay;
				}
			}
		}
		return datacenterId;
	}

	/**
	 * get the next datacenter with the shortest delay to this broker. meaning
	 * the next best Dc after those that have already been requested.
	 * 
	 * @param srcDcId
	 * @return the next datacenter with the shortest delay
	 * @pre Network enable
	 * @post none
	 */
	public int getNextDcIdWithShortestDelayFrom(int srcDcId) {
		int nextDcId = -1;
		double delay = Double.MAX_VALUE;

		for (Integer dcId : datacenterIdsList) {
			if (!((NetworkDatacenter) CloudSim.getEntity(dcId)).isUserDC()
					&& !getDatacenterRequestedIdsList().contains(dcId)) {
				double tmpDelay = NetworkTopology.getDelay(srcDcId, dcId);

				if (tmpDelay < delay && !(getDatacenterRequestedIdsList().contains(dcId))) {
					nextDcId = dcId;
					delay = tmpDelay;
				}
			}
		}
		return nextDcId;
	}

	// ==============================BROKER========================================

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            - the name of the broker.
	 * @throws Exception
	 *             - from the superclass.
	 */
	public Service(String name) {
		this(name, -1);
	}

	/**
	 * @return the cloudletGenerated
	 */
	public boolean isCloudletGenerated() {
		return cloudletGenerated;
	}

	/**
	 * @param cloudletGenerated
	 *            the cloudletGenerated to set
	 */
	public void setCloudletGenerated(boolean cloudletGenerated) {
		this.cloudletGenerated = cloudletGenerated;
	}

	/**
	 * Gets the user or owner ID of this Cloudlet.
	 * 
	 * @return the user ID or <tt>-1</tt> if the user ID has not been set before
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getUserId() {
		return userId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		// Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" +
		// getId() + " is starting...");
		//System.out.println(TextUtil.toString(CloudSim.clock()) + ": Service " + getName() + " #" + getId() + " is starting...");
		
		if (!started) {
			started = true;

			for (ListIterator<PresetEvent> iter = presetEvents.listIterator(); iter.hasNext();) {
				PresetEvent event = iter.next();
				send(event.getId(), event.getDelay(), event.getTag(), event.getData());
				iter.remove();
			}

			// Tell the Service to destroy itself after its life time.
			// this event does not have to be processed, but as long
			// as there an event for a given entity in the future queue
			// of CloudSim, CloudSim will not shut down the entity.
			if (getLifeLength() > 0) {
				send(getId(), getLifeLength(), CloudSimTagsExt.SERVICE_DESTROY_ITSELF_NOW, null);
			}
		}
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.core.SimEntity#processEvent(org.cloudbus.cloudsim
	 * .core.SimEvent)
	 */
	@Override
	public void processEvent(SimEvent ev) {
		PlacementOptimizer po = new PlacementOptimizer();
		
		if (this.getLifeLength() > 0 && CloudSim.clock() > this.getLifeLength()) {
			// Drop Request, since it is over this entity lifetime
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Service #" + getReadableId()
					+ " DROPING Event... from Entity #" + ev.getSource() + "... since over this service lifetime");
			return;
		}

		switch (ev.getTag()) {
		// Resource characteristics request
		case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:		
			processResourceCharacteristicsRequest(ev);
			break;
		// Resource characteristics answer
		case CloudSimTags.RESOURCE_CHARACTERISTICS:		
			processResourceCharacteristics(ev);
			break;
		// Available resource request
		case CloudSimTagsExt.AVAILABLE_RESOURCE_REQUEST:
			processResourceConsumptionRequest(ev);
			break;
		// Available resource answer
		case CloudSimTagsExt.AVAILABLE_RESOURCE:			
			@SuppressWarnings("unchecked") 
			List<Host> hostList = (List<Host>) ev.getData();					
			po.setFreeHostList(hostList);
			break;
		// VM Creation answer
		case CloudSimTags.VM_CREATE_ACK:			
			int[] data = (int[]) ev.getData();
			int vmId = data[1];
			int result = data[2];
			Vm vm = VmList.getById(getVmList(), vmId);

			if (vm.isBeingInstantiated() && result == CloudSimTags.TRUE) {
				vm.setBeingInstantiated(false);
			}
					
			if (isOrchestration()) {
				processVmCreateOchestration(ev);
			} else if (isOptimization()) {
				processVmCreateOptimization(ev);
			} else {
				processVmCreate(ev);
			}
			
			break;
		// start submitting Cloudlets
		case CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT:
			submitCloudlets();
			break;
		// A finished cloudlet returned
		case CloudSimTagsExt.SERVICE_START_ACK:
			// TODO start the Service without submitting Cloudlet, create
			// Cloudlet and return the id of the first Cloudlet
			processServiceStart(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_RETURN:
			processCloudletReturn(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_PAUSE_ACK:
			processCloudletPausedAck(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_RESUME_ACK:
			processCloudletPausedAck(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTagsExt.BROKER_MESSAGE:
			processBrokerMessage(ev);
			break;
		// if the simulation finishes
		case CloudSimTags.END_OF_SIMULATION:
			shutdownEntity();
			break;
		case CloudSimTagsExt.SERVICE_DESTROY_ITSELF_NOW:
			//  Log.printLine
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getReadableId()
					+ " TIME TO LIVE reached: processing SERVICE_DESTROY_ITSELF_NOW.");
			List<String> ccServices = po.getConcurrentServices();
			if (ccServices.contains(getReadableId())) {
				po.removeConcurrentService(getReadableId());
			}			
			finishExecution();
			break;
		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	public void processServiceStart(SimEvent ev) {
			
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			
			int[] data = (int[]) ev.getData();
			setBrokerCloudletId(data[0]);
			setBrokerVmId(data[1]);
				
			if (getFirstCloudlet() != null && getFirstVmId() != -1) {
				int[] dat = { getFirstCloudlet().getCloudletId(), getFirstVmId() };
				
				// Send to EdgeDatacenterBroker - getUserId() = EdgeDatacenterBroker Id				
				sendNow(getUserId(), CloudSimTagsExt.SERVICE_START_ACK, dat);
			} else {
				generateCloudlets();
				assignVmToCloudlets();
				int[] dat = { getFirstCloudlet().getCloudletId(), getFirstVmId() };
				
				// Send to EdgeDatacenterBroker - getUserId() = EdgeDatacenterBroker Id
				sendNow(getUserId(), CloudSimTagsExt.SERVICE_START_ACK, dat);
			}

		} else {
			// System.out.println(TextUtil.toString(CloudSim.clock()) + ":
			// [DEBUG]: Service #" + getId()
			// + " all Vms not created yet, postponning Service start to 1.0 ");
			
			// Send back to Service class - getId() = Service Id			
			send(getId(), 1.0, CloudSimTagsExt.SERVICE_START_ACK, ev.getData());
		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker.
	 * This method is called by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Service #" + getReadableId()
					+ ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}
		switch (ev.getTag()) {
		case CloudSimTags.VM_DESTROY_ACK:
			processVMDestroy(ev);
			break;
		case CloudSimTagsExt.SERVICE_DESTROY_VMS_NOW:
			destroyVMList((List<Vm>) ev.getData());
			break;
		
		case CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW:
			// User Vm that has been created already
			Vm userVm = (Vm) ev.getData();
			// Get the information of VMs that are waiting to be created for service chain
			Vm vm1 = getFirstVm();
			Vm vm2 = getSecondVm();
			Vm vm3 = getThirdVm();
			List<Vm> vmList = Arrays.asList(userVm, vm1, vm2,vm3);			
			setUserVm(userVm);
			PlacementOptimizer po = new PlacementOptimizer();
			Object[] avr = po.availableResources(po.getFreeHostList(), vmList);
			po.serverUsageToConcurrentServices(avr);
			
			if (isOrchestration()) {
				// First send vm to the closest datacenter
				createVmsInDatacenterOchestration(getNextDcIdWithShortestDelay(), getFirstVm());
			} else if (isOptimization()) {																			
				ProblemSolver ps = new ProblemSolver();
				setCplexSolution(ps.findAvailableDatacenter(avr));
				
				createVmsInDatacenterOptimization(getCplexSolution(), vm1);
			} else {
				createVmsInDatacenter(getNextDcIdWithShortestDelay());
			}
			break;
			
		case CloudSimTagsExt.SERVICE_CLOUDLET_DONE_VM:
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": Service #"
					+ ev.getSource()
					+ ": almost all Cloudlets processed, but some are still waiting for their VMs to be created!");

			if (getNextDcIdWithShortestDelay() != -1) {
				createVmsInDatacenter(getNextDcIdWithShortestDelay());
			}
			break;
		default:
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Service #" + getReadableId()
					+ ".processOtherEvent(): " + "Error - event unknown by this Service: " + ev.getTag());
			break;
		}

	}

	/**
	 * process a Message sent by the broker/user.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessage(SimEvent ev) {
		Object[] data = (Object[]) ev.getData();
		Message msg = ((Message) data[0]);
		RequestId rId = (RequestId) data[1];
		setRequestBeingProcessed(rId);
		Log.printLine(TextUtil.toString(CloudSim.clock()) + "[DEBUG]: Service #" + getReadableId() + ": Message "
				+ msg.name() + " received from Broker #" + getUserId());

		if (this.getLifeLength() > 0 && CloudSim.clock() > this.getLifeLength()) {
			// Drop Request, since it is over this entity lifetime
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Service #" + getReadableId()
					+ " DROPING REQUEST... from Broker #" + ev.getSource() + "... since over this service lifetime");

		} else {
			if (!cloudletGenerated) {
				generateCloudlets();
				setCloudletGenerated(true);
			}
			for (int i = 0; i < getCloudletList().size(); i++) {
				getCloudletList().get(i)
						.setCloudletLength(getCloudletList().get(i).getCloudletLength() + msg.getMips());
			}
			createStages();
			submitCloudlets();
		}

	}

	protected void processCloudletPausedAck(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int cloudletId = data[1];
		if (cloudletIdToMessage.containsKey(cloudletId)) {
			Cloudlet cloudlet = CloudletList.getById(getCloudletList(), cloudletId);
			Message msg = cloudletIdToMessage.get(cloudletId);
			// get the previous length
			// cut the size that has already been processed
			// add the one from the Message
			cloudlet.setCloudletLength(
					(cloudlet.getCloudletLength() - cloudlet.getCloudletFinishedSoFar()) + msg.getMips()); // not
																											// sure
			// how to calculate this yet.
			// resume the Cloudlet
			sendNow(getVmsToDatacentersMap().get(cloudlet.getVmId()), CloudSimTags.CLOUDLET_RESUME, cloudlet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		// if ((getCloudletList().size() > 0 && cloudletsSubmitted > 0)) {
		// send(getId(), 100.0, CloudSimTags.END_OF_SIMULATION);

		// }

		for (Vm vm : getVmList()) {
			finilizeVM(vm);
		}
		clearDatacenters();

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + " is shutting down...");

	}

	@Override
	public String toString() {
		return String.valueOf(String.format("Service(%s, %s)", Objects.toString(getName(), "N/A"), getReadableId()));
	}

	/**
	 * Returns if this broker has started to respond to events.
	 * 
	 * @return if this broker has started to respond to events.
	 */
	protected boolean isStarted() {
		return started;
	}

	public double getLifeLength() {
		return lifeLength;
	}

	/**
	 * Returns the list of preset events.
	 * 
	 * @return the list of preset events.
	 */
	protected List<PresetEvent> getPresetEvents() {
		return presetEvents;
	}

	/**
	 * Schedule an event that will be run with a given delay after the
	 * simulation has started.
	 * 
	 * @param id
	 * @param tag
	 * @param data
	 * @param delay
	 */
	public void presetEvent(final int id, final int tag, final Object data, final double delay) {
		presetEvents.add(new PresetEvent(id, tag, data, delay));
	}

	/**
	 * Submits the cloudlets after a specified time period. Used mostly for
	 * testing purposes.
	 * 
	 * @param cloudlets
	 *            - the cloudlets to submit.
	 * @param delay
	 *            - the delay.
	 */
	public void submitCloudletList(List<Cloudlet> cloudlets, double delay) {
		if (started) {
			send(getId(), delay, CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT, cloudlets);
		} else {
			presetEvent(getId(), CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT, cloudlets, delay);
		}
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list
	 *            the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletList
	 *            the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet being bount to a vm
	 * @param vmId
	 *            the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletSubmittedList
	 *            the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletReceivedList
	 *            the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		// int clId = cloudlet.getCloudletId();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": Cloudlet #"
				+ cloudlet.getReadableId() + " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all
																		// cloudlets
																		// executed Log.printLine
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service " + getServiceTyp() + " - #"
					+ getReadableId() + ": All Cloudlets executed. Finishing...");
			
			// print cloudlets results
//			String indent = "    ";
//			System.out.println(
//					indent + indent + indent + indent + indent + "=============> Broker " + getUserId() + indent);
//			System.out.println(
//					indent + indent + indent + indent + indent + "=============> Service #" + getReadableId() + indent);
//			BaseDatacenter.printCloudletList(getCloudletReceivedList());
		
			// Notify Broker that our Cloudlet are done!			
			sendNow(getUserId(), CloudSimTagsExt.SERVICE_CLOUDLET_DONE);
			setProcessingRequest(false);
			resetCloudlets();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bound cloudlet is waiting its VM be created
			
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Service #" + getReadableId()
						+ " Cloudlets waiting for VM creation!");
			
				// Notify Broker that our Cloudlet are done! but some bount
				// cloudlet is waiting its VM be created
				sendNow(getId(), CloudSimTagsExt.SERVICE_CLOUDLET_DONE_VM);
			}
		}
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {

		Log.printLine(
				TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": called submitCloudlets() ");

		if (!cloudletGenerated) {
			generateCloudlets();
		}
		ArrayList<Cloudlet> toRemove = new ArrayList<>();
		CloudletList.sortById(getCloudletList());
		for (int i = 0; i < getCloudletList().size(); i++) {
			Cloudlet cloudlet = getCloudletList().get(i);
			int vmId = cloudlet.getVmId();

			if (VmList.getById(getVmsCreatedList(), cloudlet.getVmId()) == null) {
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
						+ ": Postponing execution of cloudlet " + cloudlet.getReadableId() + ": bount VM #"
						+ cloudlet.getVmId() + " not available");
				continue;
			}
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": Sending cloudlet #"
					+ cloudlet.getReadableId() + " to VM #" + vmId);
			
			// Send to Datacenter
			sendNow(getVmsToDatacentersMap().get(vmId), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
			// remove submitted cloudlets from waiting list
			toRemove.add(cloudlet);
		}
		getCloudletList().removeAll(toRemove);
		setProcessingRequest(true);
		if (getCloudletList().size() == 0) {
			sendNow(getUserId(), CloudSimTagsExt.SERVICE_ALL_CLOUDLETS_SENT);
		}

		// remove submitted cloudlets from waiting list
		// moved up in the loop to make sure that only the submitted cloudlets
		// are removed from the list
		// for (Cloudlet cloudlet : getCloudletSubmittedList()) {
		// getCloudletList().remove(cloudlet);
		// }
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlet(Cloudlet cloudlet) {
		int vmIndex = 0;
		Vm vm;
		// if user didn't bind this cloudlet and it has not been executed
		// yet
		if (cloudlet.getVmId() == -1) {
			vm = getVmsCreatedList().get(vmIndex);
		} else { // submit to the specific vm
			vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			if (vm == null) { // vm was not created
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId()
						+ ": Postponing execution of cloudlet " + cloudlet.getReadableId() + ": bount VM #"
						+ cloudlet.getVmId() + " not available");
			}
		}

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service #" + getReadableId() + ": Sending cloudlet #"
				+ cloudlet.getReadableId() + " to VM #" + vm.getReadableId());
		cloudlet.setVmId(vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
		cloudletsSubmitted++;
		vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
		getCloudletSubmittedList().add(cloudlet);

		// remove submitted cloudlets from waiting list
		getCloudletList().remove(cloudlet);
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap
	 *            the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	public Map<Integer, Integer> getVmCreationAttempts() {
		return vmCreationAttempts;
	}

	public void setVmCreationAttempts(Map<Integer, Integer> vmCreationAttempts) {
		this.vmCreationAttempts = vmCreationAttempts;
	}

	protected void addCloudlet(Cloudlet cl) {
		if (CloudletList.getById(getCloudletList(), cl.getCloudletId()) == null) {
			getCloudletList().add(cl);
		}
	}

	/**
	 * @return the firstCloudlet
	 */
	public NetworkCloudlet getFirstCloudlet() {
		return firstCloudlet;
	}

	/**
	 * @param firstCloudlet
	 *            the firstCloudlet to set
	 */
	public void setFirstCloudlet(NetworkCloudlet firstCloudlet) {
		this.firstCloudlet = firstCloudlet;
	}

	public NetworkCloudlet getSecondCloudlet() {
		return secondCloudlet;
	}

	public void setSecondCloudlet(NetworkCloudlet secondCloudlet) {
		this.secondCloudlet = secondCloudlet;
	}

	public NetworkCloudlet getThirdCloudlet() {
		return thirdCloudlet;
	}

	public void setThirdCloudlet(NetworkCloudlet thirdCloudlet) {
		this.thirdCloudlet = thirdCloudlet;
	}

	/**
	 * @return the brokerCloudletId
	 */
	public int getBrokerCloudletId() {
		return brokerCloudletId;
	}

	/**
	 * @param brokerCloudletId
	 *            the brokerCloudletId to set
	 */
	public void setBrokerCloudletId(int brokerCloudletId) {
		this.brokerCloudletId = brokerCloudletId;
	}

	/**
	 * @return the firstVmId
	 */
	public int getFirstVmId() {
		return firstVmId;
	}

	/**
	 * @param firstVmId
	 *            the firstVmId to set
	 */
	public void setFirstVmId(int firstVmId) {
		this.firstVmId = firstVmId;
	}

	/**
	 * @return the secondVm
	 */
	public Vm getSecondVm() {
		return secondVm;
	}

	/**
	 * @param secondVmId
	 *            the secondVmId to set
	 */
	public void setSecondVm(Vm secondVm) {
		this.secondVm = secondVm;
	}

	/**
	 * @return the thirdVmId
	 */
	public Vm getThirdVm() {
		return thirdVm;
	}

	/**
	 * @param thirdVm
	 *            the thirdVmId to set
	 */
	public void setThirdVm(Vm thirdVm) {
		this.thirdVm = thirdVm;
	}

	/**
	 * @return the firstVm
	 */
	public Vm getFirstVm() {
		return firstVm;
	}

	/**
	 * @param firstVm
	 *            the firstVm to set
	 */
	public void setFirstVm(Vm firstVm) {
		this.firstVm = firstVm;
	}

	/**
	 * @return the userVm
	 */
	public Vm getUserVm() {
		return userVm;
	}

	/**
	 * @param firstVm
	 *            the firstVm to set
	 */
	public void setUserVm(Vm userVm) {
		this.userVm = userVm;
	}	
	
	/**
	 * @return the the CPLEX solution
	 */
	public Map<Integer, Integer> getCplexSolution() {
		return solution;
	}
	
	/**
	 * @param solution
	 *            the CPLEX solution to set
	 */		
	public void setCplexSolution(Map<Integer, Integer> solution) {
		this.solution = solution;
	}	
	
	/**
	 * @return the brokerVmId
	 */
	public int getBrokerVmId() {
		return brokerVmId;
	}

	/**
	 * @return the vmBeingCreated
	 */
	public Vm getVmBeingCreated() {
		return vmBeingCreated;
	}

	/**
	 * @param vmBeingCreated
	 *            the vmBeingCreated to set
	 */
	public void setVmBeingCreated(Vm vmBeingCreated) {
		this.vmBeingCreated = vmBeingCreated;
	}

	/**
	 * @param brokerVmId
	 *            the brokerVmId to set
	 */
	public void setBrokerVmId(int brokerVmId) {
		this.brokerVmId = brokerVmId;
	}
	
	/**
	 * generate random double between 1000 and 10000
	 * 
	 * @return
	 */
	public double generateRandomData() {
		Random r = new Random();
		return 1000.0 + r.nextDouble() * 10000.0;
	}

	public int getCloudletsSubmitted() {
		return cloudletsSubmitted;
	}

	public void setCloudletsSubmitted(int cloudletsSubmitted) {
		this.cloudletsSubmitted = cloudletsSubmitted;
	}

	/**
	 * Reset the Broker Cloudlets, to process the next request.
	 */
	public void resetCloudlets() {
		setCloudletList(getCloudletReceivedList().size() > 0 ? getCloudletReceivedList() : getCloudletList());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		setCloudletsSubmitted(0);
		for (Cloudlet networkCloudlet : getCloudletList()) {
			((NetworkCloudlet) networkCloudlet).reset();
			((NetworkCloudlet) networkCloudlet).setStages(new ArrayList<TaskStage>());
		}
	}

	public boolean isProcessingRequest() {
		return processingRequest;
	}

	public void setProcessingRequest(boolean processingRequest) {
		this.processingRequest = processingRequest;
	}

	public static Map<Integer, Service> getCloudletIdToService() {
		return cloudletIdToService;
	}

	public static void addCloudletIdServiceMapping(int cloudletId, Service service) {
		cloudletIdToService.put(cloudletId, service);
	}

	public static Service getServiceWithCloudletId(int cloudletId) {
		return getCloudletIdToService().get(cloudletId);
	}

	public ServiceTyp getServiceTyp() {
		return serviceTyp;
	}

	public void setServiceTyp(ServiceTyp serviceTyp) {
		this.serviceTyp = serviceTyp;
	}

	/**
	 * @return a more readable Id
	 */
	public String getReadableId() {
		return ((EdgeDatacenterBroker) CloudSim.getEntity(getUserId())).getReadableId() + "." + getId();
	}

	/**
	 * @return the ochestration
	 */
	public boolean isOrchestration() {
		return getAlgorithmType() == AlgorithmType.ORCHESTRATION;
	}

	/**
	 * @return the optimization
	 */
	public boolean isOptimization() {
		return getAlgorithmType() == AlgorithmType.OPTIMIZATION;
	}
	
	/**
	 * @return the algorithmType
	 */
	public AlgorithmType getAlgorithmType() {
		return algorithmType;
	}

	/**
	 * @param algorithmType
	 *            the algorithmType to set
	 */
	public void setAlgorithmType(AlgorithmType algorithmType) {
		this.algorithmType = algorithmType;
	}

	/**
	 * @return the requestBeingProcessed
	 */
	public RequestId getRequestBeingProcessed() {
		return requestBeingProcessed;
	}

	/**
	 * @param requestBeingProcessed the requestBeingProcessed to set
	 */
	public void setRequestBeingProcessed(RequestId requestBeingProcessed) {
		this.requestBeingProcessed = requestBeingProcessed;
	}

	protected abstract void generateCloudlets();

	protected abstract void createStages();

	public abstract void assignVmToCloudlets();

}
