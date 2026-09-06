package com.tce.smart.platform.core.client.supplier;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 真实厂牌与入厂申请查询适配层提供的不可变资格快照。
 *
 * 本对象只承载已经查询出的可信字段，不解析厂牌原文，也不代表规则已经允许通行。
 */
public final class SupplierQualificationSnapshot {

	private final String badgeId;
	private final String personId;
	private final String companyId;
	private final String admissionId;
	private final boolean badgeActive;
	private final boolean personActive;
	private final boolean companyActive;
	private final boolean admissionActive;
	private final boolean admissionApproved;
	private final Instant validFrom;
	private final Instant validUntil;
	private final Set<String> authorizedAreaIds;
	private final String personName;
	private final String companyName;
	private final String photoUrl;
	private final String personPhone;
	private final String hostName;
	private final String hostPhone;

	private SupplierQualificationSnapshot(String badgeId, String personId, String companyId,
			String admissionId, boolean badgeActive, boolean personActive, boolean companyActive,
			boolean admissionActive, boolean admissionApproved, Instant validFrom, Instant validUntil,
			Set<String> authorizedAreaIds, String personName, String companyName, String photoUrl,
			String personPhone, String hostName, String hostPhone) {
		this.badgeId = badgeId;
		this.personId = personId;
		this.companyId = companyId;
		this.admissionId = admissionId;
		this.badgeActive = badgeActive;
		this.personActive = personActive;
		this.companyActive = companyActive;
		this.admissionActive = admissionActive;
		this.admissionApproved = admissionApproved;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.authorizedAreaIds = immutableSet(authorizedAreaIds);
		this.personName = personName;
		this.companyName = companyName;
		this.photoUrl = photoUrl;
		this.personPhone = personPhone;
		this.hostName = hostName;
		this.hostPhone = hostPhone;
	}

	public static SupplierQualificationSnapshot fromTrustedSource(String badgeId, String personId,
			String companyId, String admissionId, boolean badgeActive, boolean personActive,
			boolean companyActive, boolean admissionActive, boolean admissionApproved, Instant validFrom,
			Instant validUntil, Set<String> authorizedAreaIds, String personName, String companyName,
			String photoUrl, String personPhone, String hostName, String hostPhone) {
		return new SupplierQualificationSnapshot(badgeId, personId, companyId, admissionId, badgeActive,
				personActive, companyActive, admissionActive, admissionApproved, validFrom, validUntil,
				authorizedAreaIds, personName, companyName, photoUrl, personPhone, hostName, hostPhone);
	}

	public String getBadgeId() {
		return badgeId;
	}

	public String getPersonId() {
		return personId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public String getAdmissionId() {
		return admissionId;
	}

	public boolean isBadgeActive() {
		return badgeActive;
	}

	public boolean isPersonActive() {
		return personActive;
	}

	public boolean isCompanyActive() {
		return companyActive;
	}

	public boolean isAdmissionActive() {
		return admissionActive;
	}

	public boolean isAdmissionApproved() {
		return admissionApproved;
	}

	public Instant getValidFrom() {
		return validFrom;
	}

	public Instant getValidUntil() {
		return validUntil;
	}

	public Set<String> getAuthorizedAreaIds() {
		return authorizedAreaIds;
	}

	public String getPersonName() {
		return personName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getPersonPhone() {
		return personPhone;
	}

	public String getHostName() {
		return hostName;
	}

	public String getHostPhone() {
		return hostPhone;
	}

	private static Set<String> immutableSet(Set<String> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(new LinkedHashSet<>(values));
	}
}
