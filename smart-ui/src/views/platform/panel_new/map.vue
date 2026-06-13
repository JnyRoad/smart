
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="screen-map">
        <div class="animation-box">
          <canvas class="canva-line" ref="canvasLine" width="264.5" height="245"></canvas>
          <canvas class="canva-linemove" ref="canvasLineMove" width="264.5" height="245"></canvas>
          <div class="round"></div>
          <div class="round"></div>
          <div class="round"></div>
          <div class="round"></div>
          <div class="round"></div>
          <div class="round"></div>
          <div class="round"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {
    }
  },
  components: {
  },
  mounted() {
    this.mapEffect();
  },
  methods: {
    /**
     * map 画图
     */
    mapEffect() {
      // 添加所有线条
      let ctx = this.$refs.canvasLine.getContext("2d");
      let origin = [263 / 2, 397 / 2];
      let coordinate = [
        [257 / 2, 16 / 2, 224 / 2, 194 / 2],
        [62 / 2, 209 / 2, 162 / 2, 249 / 2],
        [360 / 2, 232 / 2, 304 / 2, 299 / 2],
        [304 / 2, 268 / 2, 261 / 2, 316 / 2],
        [138 / 2, 295 / 2, 197 / 2, 328 / 2],
        [15 / 2, 387 / 2, 130 / 2, 372 / 2]
      ];
      let brokenPoint = [322 / 2, 490 / 2, 529 / 2, 490 / 2];
      ctx.beginPath();
      coordinate.forEach(item => {
        ctx.moveTo(origin[0], origin[1]);
        ctx.quadraticCurveTo(item[2], item[3], item[0], item[1]);
      });
      ctx.strokeStyle = "#5db1ff";
      ctx.stroke();
      ctx.moveTo(origin[0], origin[1]);
      ctx.lineTo(brokenPoint[0], brokenPoint[1]);
      ctx.lineTo(brokenPoint[2], brokenPoint[3]);
      ctx.setLineDash([5, 5]);
      ctx.strokeStyle = "#5db1ff";
      ctx.stroke();

      //线条滚动代码
      let canvasLineMove = this.$refs.canvasLineMove;
      let ctxLineMove = this.$refs.canvasLineMove.getContext("2d");
      let t = 0.1;
      let draw = function() {
        if (t >= 1) {
          t = 0.01;
        }
        t += 0.01;
        ctxLineMove.clearRect(
          0,
          0,
          canvasLineMove.width,
          canvasLineMove.height
        );
        ctxLineMove.beginPath();
        coordinate.forEach(item => {
          ctxLineMove.moveTo(item[0], item[1]);
          ctxLineMove.quadraticCurveTo(item[2], item[3], origin[0], origin[1]);
        });
        ctxLineMove.strokeStyle = "rgba(255,255,255,.5)";
        ctxLineMove.setLineDash([10 * t]);
        ctxLineMove.stroke();
        // coordinate.forEach((item) => {
        //   let _origin = get_bezier_dot(t, {
        //     p0_x: item[0],
        //     p1_x: item[2],
        //     p2_x: origin[0],
        //     p0_y: item[1],
        //     p1_y: item[3],
        //     p2_y: origin[1],
        //   })
        //   ctxLineMove.arc(_origin.x, _origin.y, 1, 0, 2 * Math.PI);
        // });
        // let item = coordinate[0];
        // let _origin = get_bezier_dot(t, {
        //   p0_x: item[0],
        //   p1_x: item[2],
        //   p2_x: origin[0],
        //   p0_y: item[1],
        //   p1_y: item[3],
        //   p2_y: origin[1],
        // })
        // ctxLineMove.fillStyle = 'rgba(25,255,255,1)'
        // ctxLineMove.beginPath()
        // ctxLineMove.arc(_origin.x, _origin.y, 1, 0, 2 * Math.PI);
        // ctxLineMove.fill();
        // ctxLineMove.closePath()
        //ctxLineMove.clearRect(0, 0, canvasLineMove.width, canvasLineMove.height);
        window.requestAnimationFrame(draw);
      };
      draw();
    }
  }
}
</script>

<style lang="scss" scoped>
  .wrap{
    width: 490px;
    height: 361px;
    padding: 20px 10px 10px 10px !important;
    &_inner{
      flex-direction: row !important;
    }
  }

  $scale: 0.5;
  @function get-size($n) {
    @return $n * $scale;
  }
  .screen-map {
    flex: 1;
    margin: 0 auto;
    background: url(/img/big-screen/map.png) no-repeat center;
    background-size: contain;
    position: relative;
  }
  .animation-box {
    width: get-size(529px);
    height: get-size(490px);
    transform: scale(.6);
    overflow: hidden;
    position: absolute;
    bottom: -25px;
    right: 40px;
    .canva-line {
      position: absolute;
      top: 0;
      left: 0;
      z-index: 1;
    }
    .canva-linemove {
      position: absolute;
      top: 0;
      left: 0;
      z-index: 2;
    }
    .round {
      position: absolute;
      z-index: 3;
    }
    .round:nth-of-type(1) {
      left: get-size(263px) - 12px;
      top: get-size(397px) - 12px;
      width: get-size(46px);
      height: get-size(46px);
    }
    .round:nth-of-type(2) {
      left: get-size(257px) - 8px;
      top: get-size(16px) - 8px;
      width: get-size(32px);
      height: get-size(32px);
    }
    .round:nth-of-type(3) {
      left: get-size(62px) - 8px;
      top: get-size(209px) - 8px;
      width: get-size(32px);
      height: get-size(32px);
    }
    .round:nth-of-type(4) {
      left: get-size(360px) - 8px;
      top: get-size(232px) - 8px;
      width: get-size(32px);
      height: get-size(32px);
    }
    .round:nth-of-type(5) {
      left: get-size(304px) - 8px;
      top: get-size(268px) - 8px;
      width: get-size(32px);
      height: get-size(32px);
    }
    .round:nth-of-type(6) {
      left: get-size(138px) - 8px;
      top: get-size(295px) - 8px;
      width: get-size(32px);
      height: get-size(32px);
    }
    .round:nth-of-type(7) {
      left: get-size(15px) - 8px;
      top: get-size(387px) - 8px;
      width: get-size(32px);
      height: get-size(32px);
    }
  }
  // 小动画
  @keyframes round {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(0.6);
    }
    100% {
      transform: scale(1);
    }
  }
  @keyframes round-opacity {
    0% {
      opacity: 0.7;
    }
    50% {
      opacity: 1;
    }
    100% {
      opacity: 0.7;
    }
  }
  .round {
    position: relative;
    width: 60px;
    height: 60px;
    animation: round-opacity infinite 1.5s;
    &::before {
      content: "";
      position: absolute;
      top: 50%;
      left: 50%;
      z-index: 99;
      transform: translate(-50%, -50%);
      width: 50%;
      height: 50%;
      border-radius: 50%;
      background: #fff;
    }
    &:after {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: #5db1ff;
      border-radius: 50%;
      animation: round infinite 1.5s;
    }
  }
</style>
