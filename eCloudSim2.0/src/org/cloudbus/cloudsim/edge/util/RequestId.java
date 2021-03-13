package org.cloudbus.cloudsim.edge.util;

import org.cloudbus.cloudsim.edge.ServiceTyp;

public class RequestId implements Comparable<RequestId> {
	private int natural;
	private int decimal;
	private ServiceTyp typ;

	public RequestId(int natural, int decimal, ServiceTyp typ) {
		this.natural = natural;
		this.decimal = decimal;
		this.typ = typ;
	}
	public RequestId(int decimal, ServiceTyp typ) {
		this.natural = Id.pollId(RequestId.class);
		this.decimal = decimal;
		this.typ = typ;
	}

	public RequestId(ServiceTyp typ) {
		this(0, typ);
	}

	/**
	 * @return the natural
	 */
	public int getNatural() {
		return natural;
	}

	/**
	 * @param natural
	 *            the natural to set
	 */
	public void setNatural(int natural) {
		this.natural = natural;
	}

	/**
	 * @return the decimal
	 */
	public int getDecimal() {
		return decimal;
	}

	/**
	 * @param decimal
	 *            the decimal to set
	 */
	public void setDecimal(int decimal) {
		this.decimal = decimal;
	}

	/**
	 * @return the typ
	 */
	public ServiceTyp getTyp() {
		return typ;
	}

	/**
	 * @param typ
	 *            the typ to set
	 */
	public void setTyp(ServiceTyp typ) {
		this.typ = typ;
	}

	@Override
	public int compareTo(RequestId o) {
		// TODO Auto-generated method stub
		if (this.getNatural() > o.getNatural()) {
			return 1;
		} else if (this.getNatural() < o.getNatural()) {
			return -1;
		} else {
			if (this.getDecimal() > o.getDecimal()) {
				return 1;
			} else if (this.getDecimal() < o.getDecimal()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + decimal;
		result = prime * result + natural;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestId other = (RequestId) obj;
		if (decimal != other.decimal)
			return false;
		if (natural != other.natural)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "#" + getNatural() + "." + getDecimal();
	}

	/**
	 * is this Id the first of its group
	 * 
	 * @return true if it is the first, false otherwise
	 */
	public boolean isFirst() {
		return getDecimal() == 0;
	}

}
