package org.cloudbus.cloudsim.edge.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.edge.EdgeCloudletSpaceSharedScheduler;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;

/**
 * Amazon t2.large instance. 4 vCPU, 6 GB RAM
 * Intel 5570 -> 11.72K/core
 * @author Brice Kamneng Kwam
 *
 */
public class T2Large extends EdgeVm {

	private T2Large(String name, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
	}

	public T2Large() {
		this("t2.large", -1, NetworkConstants.DEFAULT_CPU_MIPS, 4, 4096, 1024, 1024, "Xen", new EdgeCloudletSpaceSharedScheduler());
		setType(VmType.T2Large);
	}

}
