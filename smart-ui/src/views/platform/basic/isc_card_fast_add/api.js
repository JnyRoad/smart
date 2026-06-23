import { fetchList as fetchStaffList } from '@/api/platform/basic/staff_info'
import {
  deleteIscStaffCard,
  fetchIscParkConfigs,
  fetchIscStaffCards,
  saveIscStaffCard
} from '@/api/platform/basic/staff_info_detail'
import { fetchList as fetchIscCardTaskList } from '@/api/platform/records/isc_card_task'
import { trimValue } from './flow-rules'

function readResponsePayload(response) {
  if (!response || !response.data) {
    return null
  }
  return response.data.data
}

function readPageRecords(response) {
  const payload = readResponsePayload(response) || {}
  return payload.records || []
}

function readArrayPayload(response) {
  return readResponsePayload(response) || []
}

function createStaffSearchQuery(fieldName, keyword, park) {
  const query = {
    current: 1,
    size: 10,
    [fieldName]: keyword
  }
  if (park && park.parkId) {
    query.parkId = park.parkId
  }
  return query
}

function uniqueTrimmedValues(values) {
  return Array.from(new Set(values.map(item => trimValue(item)).filter(Boolean)))
}

export function fetchIscParkRecords() {
  return fetchIscParkConfigs({
    current: 1,
    size: 1000
  }).then(readPageRecords)
}

export function fetchRecentCardTaskRecords({ parkId, badge } = {}) {
  const query = {
    current: 1,
    size: 8
  }
  if (parkId) {
    query.parkId = parkId
  }
  if (badge) {
    query.badge = badge
  }
  return fetchIscCardTaskList(query).then(readPageRecords)
}

export function searchStaffByName(keyword, park) {
  return fetchStaffList(createStaffSearchQuery('name', keyword, park)).then(readPageRecords)
}

export function searchStaffByBadge(badge, park) {
  return fetchStaffList(createStaffSearchQuery('badges', badge, park)).then(readPageRecords)
}

export function fetchStaffCardRecords(staffId) {
  return fetchIscStaffCards(staffId).then(readArrayPayload)
}

export function saveStaffCard(payload) {
  return saveIscStaffCard(payload)
}

export function deleteStaffCard(staffCardId) {
  return deleteIscStaffCard(staffCardId)
}

export async function fetchStaffMapByBadges(badges) {
  const uniqueBadges = uniqueTrimmedValues(badges)
  if (!uniqueBadges.length) {
    return {}
  }
  const records = await fetchStaffList({
    current: 1,
    size: uniqueBadges.length,
    badges: uniqueBadges.join(' ')
  }).then(readPageRecords)
  return records.reduce((staffMap, staff) => {
    staffMap[String(staff.badge)] = staff
    return staffMap
  }, {})
}
