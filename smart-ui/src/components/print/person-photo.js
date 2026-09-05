// 合成几何人像仅供设计画布定位，不是人员档案，也不能作为固定图片上传。
export const PERSON_PHOTO_PLACEHOLDER = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAABACAIAAADTQmMRAAAAvElEQVR4nO3WsRGAMAgFUPd1A7dwSxsrF9DeC4FvEiD+O+rPOwpgOc4rVC3uAoIIIoggb8FvQOu2+4MeRKkcQILmiwkEVTWwCQEpNZjJDDJpABNBk4MAjdWUfEIEZQRZTdbwKW6Z0oTFTvQPvXFNcub9qQmKDIq1GGOdDkBjNWlBMMXKyglqolGa6qCGGo0pG6i5pmpKBeqkkU15QF01gokgggjyAQ3QlExJJkQQQQQNAPU2lZqmAvUzCR1v8kKEZO6QAI4AAAAASUVORK5CYII='

/** 照片采用独立资源绑定，永远必填；只在画布内生成合成占位，保存时剥离字节和旧引用。 */
export function applyPersonPhotoBindings(canvas, fieldSchema, preview = true) {
  const names = new Set()
  for (const binding of fieldSchema.fields || []) {
    const schema = canvas.schemas.flat().find(item => item.name === binding.schemaName)
    if (binding.key !== 'personPhoto') {
      if (schema && schema.type === 'image') throw new Error('图片只允许绑定当前人员照片')
      continue
    }
    if (!schema || schema.type !== 'image') throw new Error('人员照片必须绑定图片组件')
    if (binding.required !== true) throw new Error('人员照片必须设为必填')
    if (names.has(schema.name)) throw new Error('人员照片组件不能重复绑定')
    names.add(schema.name)
    delete schema.resourceRef
    delete schema.content
    schema.readOnly = true
    if (preview) schema.content = PERSON_PHOTO_PLACEHOLDER
  }
  return names
}
