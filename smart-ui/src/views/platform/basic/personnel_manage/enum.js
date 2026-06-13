
const staffStatusOption = [
  { label: "已离职", value: 0 },
  { label: "在职", value: 1 }
]

const staffStatus = {}
staffStatusOption.forEach(item => {
  staffStatus[item.value] = item.label
})

export { staffStatusOption, staffStatus }

//export const genderOption = [
//{ label: "男", value: 0 },
//{ label: "女", value: 1 },
//  { label: "未知", value: 2 }
//]

export const genderOption = [
  { label: "男", value: 0 },
  { label: "女", value: 1 }
]
