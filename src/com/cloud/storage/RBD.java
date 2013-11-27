package com.cloud.storage;

public class RBD {
	public Integer id;
	public String name;
	public String size;
	public String vmid;
	public boolean isAttached;
	public int handle;
	
	public RBD(Integer id,String name,String size) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.isAttached = false;
		this.vmid = null;
		this.handle = 0;
	}
}
