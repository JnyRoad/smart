### pdtPicker 日期区间选择器

可进行多粒度的时间选择器，组件名：``range-dtpicker``，代码块： rangeDatePick。

**使用方式：**

在 ``script`` 中引用组件 

```javascript
import rangeDatePick from '@/components/range-dtpicker/range-dtpicker.vue';
export default {
    components: {rangeDatePick}
}
```

在 ``template`` 中使用组件

```html
<rangeDatePick 
	:show="isShow"
	@showchange="showchange"
	start="1900-01"
	end="2200-12"
	:value="value"
	@change="bindChange"
	@cancel="bindCancel"
></rangeDatePick>
```

**pdtPicker 属性说明：**

|属性名		|类型	|默认值	                    |说明					|
|---		|----	|---	                    |---					|
|start		|String	|'1900-01'					|限制选择器选择的最小时间	|
|end		|String	|'2200-12'					|限制选择器选择的最大时间	|
|value		|Array	|''	                        |当前日期选择器显示的时间	|


**value 值说明：**

|值 		|类型	|说明					|
|---		|----	|---					|
|[]			|Array	|当前日期选择器为开始时间的默认值			|
|['1900-01']	|Array	|当前日期选择器开始时间为1900-01			|
|['1900-01','2010-12']		|Array	|当前日期选择器开始时间为1900-01,结束时间为2010-12			|

**事件说明：**

|事件名称	|说明		|
|---|---|
|showchange	|必传，用于控制显示隐藏|
|change	|时间选择器点击【确定】按钮时时触发的事件，参数为选择器的当前的 value|
|cancel	|时间选择器点击【取消】按钮时时触发的事件|

**showchange事件说明：**

showchange(){
	this.isShow=!this.isShow;
}

**更新记录：**

优化了开始日期和结束日期的切换，添加了粗略的日期选择错误提示

**版本预告：**

之后的版本会陆续添加时间判断、粒度选择等功能

**感谢：**

> 有更多优化建议和需求，请联系作者。谢谢！