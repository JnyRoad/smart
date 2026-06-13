let lrzLoader

export default function loadLrz() {
  if (!lrzLoader) {
    lrzLoader = import(/* webpackChunkName: "chunk-lrz" */ 'lrz')
      .then(module => module.default || module)
      .catch(error => {
        lrzLoader = null
        throw error
      })
  }

  return lrzLoader
}
