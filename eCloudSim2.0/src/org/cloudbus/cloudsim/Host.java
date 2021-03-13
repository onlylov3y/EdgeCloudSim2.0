/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.EdgeDatacenterBroker;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * Host executes actions related to management of virtual machines (e.g.,
 * creation and destruction). A host has a defined policy for provisioning
 * memory and bw, as well as an allocation policy for Pe's to virtual machines.
 * A host is associated to a datacenter. It can host virtual machines.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Host {

	/** The id. */
	private int id;

	/** The storage. */
	private long storage;

	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The allocation policy. */
	private VmScheduler vmScheduler;

	/** The vm list. */
	private final List<? extends Vm> vmList = new ArrayList<Vm>();

	/** The pe list. */
	private List<? extends Pe> peList;

	/** Tells whether this machine is working properly or has failed. */
	private boolean failed;

	/** The vms migrating in. */
	private final List<Vm> vmsMigratingIn = new ArrayList<Vm>();

	/** The datacenter where the host is placed. */
	private Datacenter datacenter;

	/**
	 * Instantiates a new host.
	 * 
	 * @param id
	 *            the id
	 * @param ramProvisioner
	 *            the ram provisioner
	 * @param bwProvisioner
	 *            the bw provisioner
	 * @param storage
	 *            the storage
	 * @param peList
	 *            the pe list
	 * @param vmScheduler
	 *            the vm scheduler
	 */
	public Host(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		setId(id);
		setRamProvisioner(ramProvisioner);
		setBwProvisioner(bwProvisioner);
		setStorage(storage);
		setVmScheduler(vmScheduler);

		setPeList(peList);
		setFailed(false);
	}

	/**
	 * Requests updating of processing of cloudlets in the VMs running in this
	 * host.
	 * 
	 * @param currentTime
	 *            the current time
	 * @return expected time of completion of the next cloudlet in all VMs in
	 *         this host. Double.MAX_VALUE if there is no future events expected
	 *         in this host
	 * @pre currentTime >= 0.0
	 * @post $none
	 */
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (Vm vm : getVmList()) {
			double time = vm.updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

	/**
	 * Adds the migrating in vm.
	 * 
	 * @param vm
	 *            the vm
	 */
	public void addMigratingInVm(Vm vm) {
		vm.setInMigration(true);

		if (!getVmsMigratingIn().contains(vm)) {
			if (getStorage() < vm.getSize()) {
				Log.printLine("[ERROR]:[VmScheduler.addMigratingInVm] Allocation of VM #" + vm.getReadableId()
						+ " to Host #" + getReadableId() + " failed by storage");
				System.exit(0);
			}

			if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
				Log.printLine("[ERROR]:[VmScheduler.addMigratingInVm] Allocation of VM #" + vm.getReadableId()
						+ " to Host #" + getReadableId() + " failed by RAM");
				System.exit(0);
			}

			if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
				Log.printLine("[ERROR]:[VmScheduler.addMigratingInVm] Allocation of VM #" + vm.getReadableId()
						+ " to Host #" + getReadableId() + " failed by BW");
				System.exit(0);
			}

			getVmScheduler().getVmsMigratingIn().add(vm.getUid());
			if (!allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
				Log.printLine("[ERROR]:[VmScheduler.addMigratingInVm] Allocation of VM #" + vm.getReadableId()
						+ " to Host #" + getReadableId() + " failed by MIPS(CPU)");
				System.exit(0);
			}

			setStorage(getStorage() - vm.getSize());

			getVmsMigratingIn().add(vm);
			getVmList().add(vm);
			updateVmsProcessing(CloudSim.clock());
			vm.getHost().updateVmsProcessing(CloudSim.clock());
		}
	}

	/**
	 * Removes the migrating in vm.
	 * 
	 * @param vm
	 *            the vm
	 */
	public void removeMigratingInVm(Vm vm) {
		vmDeallocate(vm);
		getVmsMigratingIn().remove(vm);
		getVmList().remove(vm);
		getVmScheduler().getVmsMigratingIn().remove(vm.getUid());
		vm.setInMigration(false);
	}

	/**
	 * Reallocate migrating in vms.
	 */
	public void reallocateMigratingInVms() {
		for (Vm vm : getVmsMigratingIn()) {
			if (!getVmList().contains(vm)) {
				getVmList().add(vm);
			}
			if (!getVmScheduler().getVmsMigratingIn().contains(vm.getUid())) {
				getVmScheduler().getVmsMigratingIn().add(vm.getUid());
			}
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw());
			allocatePesForVm(vm, vm.getCurrentRequestedMips());
			setStorage(getStorage() - vm.getSize());
		}
	}

	/**
	 * Checks if is suitable for vm.
	 * 
	 * @param vm
	 *            the vm
	 * @return true, if is suitable for vm
	 */
	public boolean isSuitableForVm(Vm vm) {
		return (getVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
				&& getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
				&& getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam())
				&& getBwProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedBw()));
	}

	/**
	 * get the readable Id of the owner of this VM.
	 * 
	 * @param vm
	 *            the VM
	 * @return the readable Id
	 */
	public String getVmOwnerReadableId(Vm vm) {
		String readableId;
		try {
			EdgeDatacenterBroker owner = (EdgeDatacenterBroker) CloudSim.getEntity(vm.getUserId());
			readableId = owner.getReadableId();
		} catch (Exception e) {
			Service owner = (Service) CloudSim.getEntity(vm.getUserId());
			readableId = owner.getReadableId();
		}
		return readableId;
	}

	public void printVmRequest(final Vm vm, final String msg, final String status) {
		if (!((NetworkDatacenter) getDatacenter()).isUserDC()) {
			String ownerReadableId = getVmOwnerReadableId(vm);
			CustomLog.printVmRequest("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + getReadableId(), ((EdgeHost) this).getName(),
					"#" + vm.getReadableId(), "#" + getDatacenter().getReadableId(), "#" + ownerReadableId, msg,
					status);
		}
	}

	public void printServer() {
		if (!((NetworkDatacenter) getDatacenter()).isUserDC()) {
			CustomLog.printServer("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", CloudSim.clock(), "#" + getReadableId(), ((EdgeHost) this).getName(),
					"#" + getDatacenter().getReadableId(), getRamProvisioner().getAvailableRam(), getNumberOfFreePes(),
					getAvailableMips(), getBwProvisioner().getAvailableBw(), getStorage(), getVmList().size());
		}
	}

	/**
	 * Allocates PEs and memory to a new VM in the Host.
	 * 
	 * @param vm
	 *            Vm being started
	 * @return $true if the VM could be started in the host; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean vmCreate(Vm vm) {
				
//		System.out.println("=======RESOURCE UTILIZATION=======");
//		System.out.println("Datacenter: " + getDatacenter().getReadableId() + " Host: " + getReadableId());
//		System.out.println("Number of free PEs: " + TextUtil.toString(getNumberOfFreePes()));
//		System.out.println("RAM: " + TextUtil.toString(getRam()));
//		System.out.println("Bandwidth: " + TextUtil.toString(getBw()));
//		System.out.println("Virtual Machine ID: " + vm.getId());
		
		
		if (getStorage() < vm.getSize()) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Host #" + getReadableId() + " Datacenter #"
					+ getDatacenter().getReadableId() + ":  Allocation of VM #" + vm.getReadableId()
					+ " failed by storage");
			printVmRequest(vm, "failed by storage", "false");
			// CustomLog.printVmRequest("\t%s\t%s\t%s\t%s\t%s", "#" +
			// getReadableId(), "#" + vm.getReadableId(),
			// "#" + getDatacenter().getReadableId(), "#" + ownerReadableId,
			// "failed by storage");
			return false;
		}

		if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Host #" + getReadableId() + " Datacenter #"
					+ getDatacenter().getReadableId() + ":  Allocation of VM #" + vm.getReadableId()
					+ " failed by RAM");
			printVmRequest(vm, "failed by RAM", "false");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Host #" + getReadableId() + " Datacenter #"
					+ getDatacenter().getReadableId() + ":  Allocation of VM #" + vm.getReadableId() + " failed by BW");
			getRamProvisioner().deallocateRamForVm(vm);
			printVmRequest(vm, "failed by BW", "false");
			return false;
		}

		if (!allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Host #" + getReadableId() + " Datacenter #"
					+ getDatacenter().getReadableId() + ":  Allocation of VM #" + vm.getReadableId()
					+ " failed by CPU");
//					+ " failed by MIPS");
			printVmRequest(vm, "failed by CPU", "false");

			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}

		setStorage(getStorage() - vm.getSize());
		getVmList().add(vm);
		vm.setHost(this);
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Host #" + getReadableId() + " - VM #"
				+ vm.getReadableId() + " is created on Datacenter #" + getDatacenter().getReadableId());

		printVmRequest(vm, "success", "true");

		printServer();
		return true;
	}

	/**
	 * Destroys a VM running in the host.
	 * 
	 * @param vm
	 *            the VM
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroy(Vm vm) {
		if (vm != null) {
			// printVmRequest(vm, "destroy", "none");
			vmDeallocate(vm);
			getVmList().remove(vm);
			vm.setHost(null);
			printServer();
		}
	}

	/**
	 * Destroys all VMs running in the host.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroyAll() {
		vmDeallocateAll();
		for (Vm vm : getVmList()) {
			// printVmRequest(vm, "destroy", "none");
			vm.setHost(null);
			setStorage(getStorage() + vm.getSize());
		}
		getVmList().clear();
		printServer();
	}

	/**
	 * Deallocate all hostList for the VM.
	 * 
	 * @param vm
	 *            the VM
	 */
	protected void vmDeallocate(Vm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		deallocatePesForVm(vm);
		setStorage(getStorage() + vm.getSize());
	}

	/**
	 * Deallocate all hostList for the VM.
	 */
	protected void vmDeallocateAll() {
		getRamProvisioner().deallocateRamForAllVms();
		getBwProvisioner().deallocateBwForAllVms();
		getVmScheduler().deallocatePesForAllVms();
	}

	/**
	 * Returns a VM object.
	 * 
	 * @param vmId
	 *            the vm id
	 * @param userId
	 *            ID of VM's owner
	 * @return the virtual machine object, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public Vm getVm(int vmId, int userId) {
		for (Vm vm : getVmList()) {
			if (vm.getId() == vmId && vm.getUserId() == userId) {
				return vm;
			}
		}
		return null;
	}

	/**
	 * Gets the pes number.
	 * 
	 * @return the pes number
	 */
	public int getNumberOfPes() {
		return getPeList().size();
	}

	/**
	 * Gets the free pes number.
	 * 
	 * @return the free pes number
	 */
	public int getNumberOfFreePes() {
		return PeList.getNumberOfFreePes(getPeList());
	}

	/**
	 * Gets the total mips.
	 * 
	 * @return the total mips
	 */
	public int getTotalMips() {
		return PeList.getTotalMips(getPeList());
	}

	/**
	 * Allocates PEs for a VM.
	 * 
	 * @param vm
	 *            the vm
	 * @param mipsShare
	 *            the mips share
	 * @return $true if this policy allows a new VM in the host, $false
	 *         otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
		return getVmScheduler().allocatePesForVm(vm, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM.
	 * 
	 * @param vm
	 *            the vm
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForVm(Vm vm) {
		getVmScheduler().deallocatePesForVm(vm);
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 * 
	 * @param vm
	 *            the vm
	 * @return an array containing the amount of MIPS of each pe that is
	 *         available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		return getVmScheduler().getAllocatedMipsForVm(vm);
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 * 
	 * @param vm
	 *            the vm
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(Vm vm) {
		return getVmScheduler().getTotalAllocatedMipsForVm(vm);
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		return getVmScheduler().getMaxAvailableMips();
	}

	/**
	 * Gets the free mips.
	 * 
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return getVmScheduler().getAvailableMips();
	}

	/**
	 * Gets the machine bw.
	 * 
	 * @return the machine bw
	 * @pre $none
	 * @post $result > 0
	 */
	public long getBw() {
		return getBwProvisioner().getBw();
	}

	/**
	 * Gets the machine memory.
	 * 
	 * @return the machine memory
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getRamProvisioner().getRam();
	}

	/**
	 * Gets the machine storage.
	 * 
	 * @return the machine storage
	 * @pre $none
	 * @post $result >= 0
	 */
	public long getStorage() {
		return storage;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the ram provisioner.
	 * 
	 * @return the ram provisioner
	 */
	public RamProvisioner getRamProvisioner() {
		return ramProvisioner;
	}

	/**
	 * Sets the ram provisioner.
	 * 
	 * @param ramProvisioner
	 *            the new ram provisioner
	 */
	protected void setRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}

	/**
	 * Gets the bw provisioner.
	 * 
	 * @return the bw provisioner
	 */
	public BwProvisioner getBwProvisioner() {
		return bwProvisioner;
	}

	/**
	 * Sets the bw provisioner.
	 * 
	 * @param bwProvisioner
	 *            the new bw provisioner
	 */
	protected void setBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}

	/**
	 * Gets the VM scheduler.
	 * 
	 * @return the VM scheduler
	 */
	public VmScheduler getVmScheduler() {
		return vmScheduler;
	}

	/**
	 * Sets the VM scheduler.
	 * 
	 * @param vmScheduler
	 *            the vm scheduler
	 */
	protected void setVmScheduler(VmScheduler vmScheduler) {
		this.vmScheduler = vmScheduler;
	}

	/**
	 * Gets the pe list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the pe list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pe> List<T> getPeList() {
		return (List<T>) peList;
	}

	/**
	 * Sets the pe list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param peList
	 *            the new pe list
	 */
	protected <T extends Pe> void setPeList(List<T> peList) {
		this.peList = peList;
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
	 * Sets the storage.
	 * 
	 * @param storage
	 *            the new storage
	 */
	protected void setStorage(long storage) {
		this.storage = storage;
	}

	/**
	 * Checks if is failed.
	 * 
	 * @return true, if is failed
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Sets the PEs of this machine to a FAILED status. NOTE: <tt>resName</tt>
	 * is used for debugging purposes, which is <b>ON</b> by default. Use
	 * {@link #setFailed(boolean)} if you do not want this information.
	 * 
	 * @param resName
	 *            the name of the resource
	 * @param failed
	 *            the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(String resName, boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), resName, getId(), failed);
		return true;
	}

	/**
	 * Sets the PEs of this machine to a FAILED status.
	 * 
	 * @param failed
	 *            the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), failed);
		return true;
	}

	/**
	 * Sets the particular Pe status on this Machine.
	 * 
	 * @param peId
	 *            the pe id
	 * @param status
	 *            Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
	 *         otherwise (Pe id might not be exist)
	 * @pre peID >= 0
	 * @post $none
	 */
	public boolean setPeStatus(int peId, int status) {
		return PeList.setPeStatus(getPeList(), peId, status);
	}

	/**
	 * Gets the vms migrating in.
	 * 
	 * @return the vms migrating in
	 */
	public List<Vm> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Gets the data center.
	 * 
	 * @return the data center where the host runs
	 */
	public Datacenter getDatacenter() {
		return datacenter;
	}

	/**
	 * Sets the data center.
	 * 
	 * @param datacenter
	 *            the data center from this host
	 */
	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
	}

	/**
	 * @return a more readable Id
	 */
	public String getReadableId() {
		try {
			return getDatacenter().getReadableId() + "." + getId();
		} catch (Exception e) {
			return getId() + "";
		}
	}
}
