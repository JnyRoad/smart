import list from './list'
import executeOnce from './executeOnce'

const mixins = {
  list,
  executeOnce
}

window.tce = window.tce || {}
window.tce.mixins = mixins

export default mixins
