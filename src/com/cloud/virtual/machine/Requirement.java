package com.cloud.virtual.machine;

public class Requirement {
	public Long RAM;
	public Integer disk;
	public Integer CPUs;
	public Integer tid;
	
	public Requirement(Integer tid,Integer RAM, Integer disk, Integer CPUs) {
		this.tid = tid;
		this.RAM = (long) (RAM*1024);
		this.disk = disk;
		this.CPUs = CPUs;
	}
}
