/**
 * 
 */
package org.cloudbus.cloudsim.edge.util;

/**
 * @author Brice Kamneng Kwam
 *
 */
public enum AlgorithmType {

	/**
	 * Place the VMs as close as possible to the user.
	 */
	DEFAULT(0, "DB"),

	/**
	 * Place the next VM as close as possible to the previous VM.
	 */
	ORCHESTRATION(1, "WEB"),

	/**
	 * Place the next VMs following optimized locations calculated from CPLEX
	 */
	OPTIMIZATION(1, "WEB");
	
	private int typ;
	private String name;

	private AlgorithmType(int typ, String name) {
		this.typ = typ;
		this.name = name;
	}

	public int getTp() {
		return typ;
	}

	public String getName() {
		return name;
	}

}
