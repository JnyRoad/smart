package com.tce.smart.platform.core.client.supplier;

/**
 * 持久化适配层读取的人员区域在内状态及其版本。
 */
public final class SupplierPresenceSnapshot {

	private final String personId;
	private final String areaId;
	private final SupplierPresence presence;
	private final long version;

	private SupplierPresenceSnapshot(String personId, String areaId, SupplierPresence presence, long version) {
		this.personId = personId;
		this.areaId = areaId;
		this.presence = presence;
		this.version = version;
	}

	public static SupplierPresenceSnapshot current(String personId, String areaId, SupplierPresence presence,
			long version) {
		return new SupplierPresenceSnapshot(personId, areaId, presence, version);
	}

	public String getPersonId() {
		return personId;
	}

	public String getAreaId() {
		return areaId;
	}

	public SupplierPresence getPresence() {
		return presence;
	}

	public long getVersion() {
		return version;
	}
}
