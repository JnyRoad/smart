export const DEFAULT_INLINE_AREA_LIMIT = 4

export const FACTORY_DEFINITIONS = [
  {
    factoryType: '15',
    factoryName: '新工厂',
    areaFlag: 1,
    sort: 1,
    areasKey: 'newAreas'
  },
  {
    factoryType: '16',
    factoryName: '老工厂',
    areaFlag: 0,
    sort: 2,
    areasKey: 'oldAreas'
  }
]

function parseConfigValue(value) {
  if (!value) {
    return {}
  }
  if (typeof value === 'object') {
    return value
  }
  try {
    return JSON.parse(value)
  } catch {
    return {}
  }
}

function toPositiveInteger(value, defaultValue) {
  const parsedValue = Number.parseInt(value, 10)
  return parsedValue > 0 ? parsedValue : defaultValue
}

function toCommonFlag(value) {
  return value === 1 || value === '1' || value === true
}

function getAreaCode(area) {
  if (area.code !== undefined && area.code !== null) {
    return String(area.code)
  }
  if (area.areaCode !== undefined && area.areaCode !== null) {
    return String(area.areaCode)
  }
  return ''
}

function getAreaName(area) {
  return area.desc || area.areaName || area.name || ''
}

function findConfiguredFactory(config, factoryDefinition) {
  const factories = Array.isArray(config.factories) ? config.factories : []
  const exactFactory = factories.find((factory) => {
    return factory.factoryType !== undefined && factory.factoryType !== null && String(factory.factoryType) === factoryDefinition.factoryType
  })
  if (exactFactory) {
    return exactFactory
  }
  return factories.find((factory) => {
    const hasFactoryType = factory.factoryType !== undefined && factory.factoryType !== null && String(factory.factoryType) !== ''
    return !hasFactoryType && Number(factory.areaFlag) === factoryDefinition.areaFlag
  }) || {}
}

function mapConfiguredAreas(configuredFactory) {
  const configuredAreas = Array.isArray(configuredFactory.areas) ? configuredFactory.areas : []
  return configuredAreas.reduce((areaMap, area) => {
    const areaCode = getAreaCode(area)
    if (areaCode) {
      areaMap[areaCode] = area
    }
    return areaMap
  }, {})
}

function normalizeFactory(config, standardAreas, factoryDefinition) {
  const configuredFactory = findConfiguredFactory(config, factoryDefinition)
  const configuredAreaMap = mapConfiguredAreas(configuredFactory)
  const standardFactoryAreas = factoryDefinition.areas || standardAreas[factoryDefinition.areasKey] || []
  const areas = standardFactoryAreas.map((area, index) => {
    const areaCode = getAreaCode(area)
    const configuredArea = configuredAreaMap[areaCode] || {}
    return {
      areaCode: areaCode,
      areaName: getAreaName(area),
      isCommon: toCommonFlag(configuredArea.isCommon),
      sort: toPositiveInteger(configuredArea.sort, index + 1)
    }
  }).filter((area) => area.areaCode)

  return {
    factoryType: factoryDefinition.factoryType,
    factoryName: factoryDefinition.factoryName,
    areaFlag: factoryDefinition.areaFlag,
    sort: factoryDefinition.sort,
    areas: areas
  }
}

function getFactoryDefinitions(standardAreas) {
  const factories = standardAreas && Array.isArray(standardAreas.factories) ? standardAreas.factories : []
  if (factories.length === 0) {
    return FACTORY_DEFINITIONS
  }
  return factories.filter((factory) => {
    return factory.factoryType !== undefined && factory.factoryType !== null && String(factory.factoryType) !== ''
  }).map((factory, index) => {
    return {
      factoryType: String(factory.factoryType),
      factoryName: factory.factoryName || '',
      areaFlag: factory.areaFlag,
      sort: toPositiveInteger(factory.sort, index + 1),
      areas: Array.isArray(factory.areas) ? factory.areas : []
    }
  }).filter((factory) => factory.factoryType)
}

export function normalizeCommonAreaConfig(value, standardAreas) {
  const config = parseConfigValue(value)
  const factoryDefinitions = getFactoryDefinitions(standardAreas || {})
  return {
    inlineAreaLimit: toPositiveInteger(config.inlineAreaLimit, DEFAULT_INLINE_AREA_LIMIT),
    factories: factoryDefinitions.map((factoryDefinition) => {
      return normalizeFactory(config, standardAreas || {}, factoryDefinition)
    })
  }
}
