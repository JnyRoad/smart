/**
 * 获取小数后两位的百分比,不带'%'
 */
const getProportion = function (num, sum) {
  if (sum <= 0) {
    sum = 1;
  }
  return Math.round((num / sum) * 100 * 100) / 100;
};

export {
  getProportion
}
