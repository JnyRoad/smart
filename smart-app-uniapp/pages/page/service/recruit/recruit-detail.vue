<template>
	<view class="mw bgf">
		<view class="pr30 pl30 pt20 mw">
			<view  class="card pl30 pr30">
				<view v-if="info.jobName">
					<view class="hbc pt26 pb26">
						<view class="f32pc bold">职位：{{ info.jobName }}</view>
						<view class="pc5">6K</view>
					</view>
					<view class="hlc border-top pt40 mb40">
						<view @click="previewImage(info.applicantPhoto)" class="wh228 borderC"><image class="mw mh" :src="info.applicantPhoto" alt=""></image></view>
						<view class="f1 pr40 pl20 ">
							<view class="">
								<view class="f30pc1 hlc">
									{{ info.applicantName }}
									<span class="ml26 f30pc1">{{ info.applicantNation }}</span>
								</view>
								<view class="f30pc1">
									{{ info.applicantGender }}
									<text class="block pl6 pr6">|</text>
									{{ info.applicantEducation || '无' }}
									<text class="block pl6 pr6">|</text>
									{{ info.applicantAge }}岁
								</view>
								<view class="f28pc9 hlc">
									<image class="w28 h36 mr10" src="/static/img/serive/recruit/Invitation/map.png" alt=""></image>
									{{ info.applicantAddress }}
								</view>
							</view>
							<view @click="callPhone(info.applicantMobile)" class="hlc">
								<view class="pc1">{{ info.applicantMobile }}</view>
								<image class="w46 h46 ml60" src="/static/img/serive/recruit/Invitation/tel.png" alt=""></image>
							</view>
						</view>
					</view>
					<view  class="hbc pt30 pb30 border-top">
						<view class="f30pc">
							<text class="pc9">年限：</text>
							{{ info.workAge }}年
						</view>
					</view>
				</view>
			</view>
			<!-- time -->

			<view class="hbc f26pc2 mt50 mb26">
				<view>投递时间</view>
				<view>{{ info.applyDate }}</view>
			</view>
			<view class="border-top pt26">
				<view class="vlc mt40">
					<view class="f30pc1 bold hlc">
						<image class="w62 h48 mr20" src="/static/img/serive/recruit/Invitation/Education.png" alt=""></image>
						学历
					</view>
					<view class="ml60 pl20" v-for="(item, index) in info.educationHis" :key="index">
						<view class="f28pc9 hlc">{{ item.startTime }} - {{ item.endTime }}</view>
						<view class="f30pc1">{{ item.schoolName }} - {{ item.education }} - {{ item.major }}</view>
					</view>
				</view>
				<view class="vlc mt40">
					<view class="f30pc1 bold hlc">
						<image class="w54 h54 mr20" src="/static/img/serive/recruit/Invitation/work.png" alt=""></image>
						工作经验
					</view>
					<view class="ml60 pl20" v-for="(item, index) in info.workHis" :key="index">
						<view class="f28pc9 hlc">{{ item.startTime }} - {{ item.endTime }}</view>
						<view class="f30pc1">{{ item.companyName }} - {{ item.jobName }}</view>
						<view class="f30pc1">{{ item.prover }} - {{ item.proverMobile }}</view>
					</view>
				</view>
				<view @click="toRecord(info.applicationId)" class="vlc mt40">
					<view class="f30pc1 bold hlc">
						<image class="w50 h48 mr20" src="/static/img/serive/recruit/Invitation/keep.png" alt=""></image>
						应聘记录
					</view>
				</view>
			</view>
		</view>
		<view style="width:100%;height:200upx"></view>
		<uni-fab v-if="content.length>0" :pattern="pattern" :content="content" horizontal="right" vertical="bottom" direction="vertical" @trigger="trigger" ref="fab"></uni-fab>
		<yt-loading></yt-loading>
	</view>
</template>
<script>
import uniFab from '@/components/uni-fab.vue';
import recruitment from '@/api/api-recruitment-management.js';
import { recruitCtrl } from '@/testjs/recruit/recruit.js';
import { RECURIT_APPLY, storage } from '@/tools/storage.js';
export default {
	components: {
		'uni-fab': uniFab
	},
	data() {
		return {
			applicationId: '', //id
			info: {},
			applyState: 0,
			content: [],
			pattern: {
				backgroundColor: '#fff',
				buttonColor: '#508BFF'
			},
			timeout: 0
		};
	},
	onLoad(e) {
		this.applicationId = e.applicationId;
		const { applicationId } = e;
		this.getDetail({ applicationId });
		this.applyState = e.applyState;
		this.content = this.fillterStatus();
	},
	onPullDownRefresh() {
		this.getDetail({ applicationId: this.applicationId });
	},
	methods: {
		// 详情
		async getDetail(obj) {
			try {
				const res = await recruitment.applicationDetail(obj);
				uni.stopPullDownRefresh();
				this.info = res.data.data;
			} catch (e) {
				console.log(e);
				throw e;
			}
		},
		// 简历塞选操作
		trigger(e) {
			storage.remove(RECURIT_APPLY);
			const { applicantPhoto, applicantAge, applicantGender, applicantEducation, applicantName, applicationId, jobAddress, jobDept, jobName } = this.info;
			const obj = {
				applicantPhoto,
				applicantAge,
				applicantGender,
				applicantEducation,
				applicantName,
				applicationId,
				jobAddress,
				jobDept,
				jobName
			};
			obj.status = this.applyState;
			storage.setSync(RECURIT_APPLY, [obj]);
			uni.redirectTo({
				url: `./refuse-entry?operationType=${e.item.operationType}&applyState=${this.applyState}`
			});
		},
		toRecord(id) {
			uni.navigateTo({
				url: `./recruit-record?applicationId=${id}`
			});
		},
		// 通过招聘状态筛选操作
		fillterStatus() {
			let num = parseInt(this.applyState);
			console.log(num);
			switch (num) {
				case 0:
					return recruitCtrl.status1; // 已投递
					break;
				case 2:
					return recruitCtrl.status2; // 已邀请
					break;
				case 3:
					return recruitCtrl.status3; // 待入职 待复试
					break;
				case 4:
					return recruitCtrl.status4; // 待入职 待复试
					break;
				case 1:
					return recruitCtrl.status5; // 已拒绝
					break;
				case 6:
					return recruitCtrl.status5; // 已拒绝 已入库
					break;
				default:
					break;
			}
		},
		callPhone(tel) {
			uni.makePhoneCall({
				phoneNumber:  `${tel}`
			});
		},
		// 查看大图
		previewImage(url) {
			uni.previewImage({
				current: '0',
				urls: [url]
			})
		}
	}
};
</script>

<style>
.wh228 {
	width: 228upx;
	height: 228upx;
}
.card {
	width: 690upx;
	height: 518upx;
	background: rgba(255, 255, 255, 1);
	box-shadow: 0px 5px 18px 0px rgba(185, 185, 185, 0.5);
	border-radius: 20upx;
}
.wh50 {
	width: 50upx;
	height: 50upx;
}
</style>
