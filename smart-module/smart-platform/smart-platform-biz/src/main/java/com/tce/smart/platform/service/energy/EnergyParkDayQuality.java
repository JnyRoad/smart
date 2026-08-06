package com.tce.smart.platform.service.energy;

/** 园区日投影完整性判断，未投影主表必须保守标为 PARTIAL。 */
public final class EnergyParkDayQuality {
	private EnergyParkDayQuality() { }
	public static String status(int included, int excluded, int invalid, int missing, int expected, int projected) {
		int unprojected = Math.max(expected - projected, 0);
		if (included == 0) return excluded > 0 || invalid > 0 || missing > 0 || unprojected > 0 ? "PARTIAL" : "NO_DATA";
		return invalid > 0 || missing > 0 || unprojected > 0 ? "PARTIAL" : "READY";
	}
}
