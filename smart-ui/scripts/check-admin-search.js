const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function readSource(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), 'utf8')
}

function findSearchableProps(relativePath) {
  const source = readSource(relativePath)
  const searchableProps = new Set()
  const columnRegex = /\{[\s\S]*?prop:\s*['"]([^'"]+)['"][\s\S]*?\}/g
  let match

  while ((match = columnRegex.exec(source)) !== null) {
    const block = match[0]
    if (/search:\s*true/.test(block)) {
      searchableProps.add(match[1])
    }
  }

  return searchableProps
}

function assertSearchResetsToFirstPage(relativePath, failures) {
  const source = readSource(relativePath)
  const resetsCurrentPage = /handleFilter\s*\([^)]*\)\s*\{[\s\S]*?this\.page\.currentPage\s*=\s*1/.test(source)
  if (!resetsCurrentPage) {
    failures.push(`${relativePath}: search must reset currentPage to 1`)
  }
}

const expectedSearchFields = [
  {
    file: 'src/const/crud/admin/user.js',
    fields: ['username', 'fullName', 'phone', 'roleName'],
    forbiddenFields: ['roleCode']
  },
  {
    file: 'src/const/crud/admin/role.js',
    fields: ['roleName', 'roleCode', 'roleDesc'],
    forbiddenFields: []
  }
]

const failures = []

expectedSearchFields.forEach(check => {
  const searchableProps = findSearchableProps(check.file)
  check.fields.forEach(field => {
    if (!searchableProps.has(field)) {
      failures.push(`${check.file}: ${field} must be searchable`)
    }
  })
  check.forbiddenFields.forEach(field => {
    if (searchableProps.has(field)) {
      failures.push(`${check.file}: ${field} must not be searchable`)
    }
  })
})

assertSearchResetsToFirstPage('src/views/admin/user/index.vue', failures)
assertSearchResetsToFirstPage('src/views/admin/role/index.vue', failures)

if (failures.length > 0) {
  console.error('Admin search contract check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('Admin search contract check passed.')
