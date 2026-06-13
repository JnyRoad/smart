const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function readSource(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), 'utf8')
}

function assertContains(source, expected, file, description, failures) {
  if (!source.includes(expected)) {
    failures.push(`${file}: missing ${description}`)
  }
}

function assertNotContains(source, unexpected, file, description, failures) {
  if (source.includes(unexpected)) {
    failures.push(`${file}: must not contain ${description}`)
  }
}

const userMapperFile = 'smart-upms/smart-upms-biz/src/main/resources/mapper/SysUserMapper.xml'
const userDtoFile = 'smart-upms/smart-upms-api/src/main/java/com/tce/smart/admin/api/dto/UserDTO.java'
const roleControllerFile = 'smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/controller/RoleController.java'

const userMapper = readSource(userMapperFile)
const userDto = readSource(userDtoFile)
const roleController = readSource(roleControllerFile)
const failures = []

assertContains(userMapper, 'query.username', userMapperFile, 'username filter', failures)
assertContains(userMapper, 'query.fullName', userMapperFile, 'full name filter', failures)
assertContains(userMapper, 'query.phone', userMapperFile, 'phone filter', failures)
assertContains(userMapper, 'query.roleName', userMapperFile, 'role name filter', failures)
assertNotContains(userMapper, 'query.roleCode', userMapperFile, 'user role code filter', failures)
assertNotContains(userDto, 'private String roleCode;', userDtoFile, 'user role code query field', failures)

assertContains(roleController, 'sysRole.getRoleName()', roleControllerFile, 'role name filter', failures)
assertContains(roleController, 'sysRole.getRoleCode()', roleControllerFile, 'role code filter', failures)
assertContains(roleController, 'sysRole.getRoleDesc()', roleControllerFile, 'role description filter', failures)
assertContains(roleController, 'StringUtils.isNotBlank', roleControllerFile, 'blank-safe role filters', failures)

if (failures.length > 0) {
  console.error('Admin search contract check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('Admin search contract check passed.')
