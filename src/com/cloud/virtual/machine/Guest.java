package com.cloud.virtual.machine;

public class Guest {
	public String OSType;
	public Architecture architecture;

	public Guest( Architecture architecture,String OSType) {
		this.OSType = OSType;
		this.architecture = architecture;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((OSType == null) ? 0 : OSType.hashCode());
		result = prime * result
				+ ((architecture == null) ? 0 : architecture.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Guest other = (Guest) obj;
		if (OSType == null) {
			if (other.OSType != null)
				return false;
		} else if (!OSType.equals(other.OSType))
			return false;
		if (architecture == null) {
			if (other.architecture != null)
				return false;
		} else if (!architecture.equals(other.architecture))
			return false;
		return true;
	}

}
