const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const pagePath = path.join(__dirname, '../pages/page/logins/findPwdPhone/findPwdPhone.vue');

function createDeferred() {
	let resolve;
	let reject;
	const promise = new Promise((resolvePromise, rejectPromise) => {
		resolve = resolvePromise;
		reject = rejectPromise;
	});
	return { promise, resolve, reject };
}

function loadPage(settingPassword, toasts) {
	const source = fs.readFileSync(pagePath, 'utf8');
	const script = source.match(/<script>([\s\S]*?)<\/script>/)[1]
		.replace("import settingPassword  from '@/api/api-password.js'", 'const settingPassword = __settingPassword')
		.replace("import { storage } from '@/tools/storage.js'", 'const storage = __storage')
		.replace('export default', 'module.exports =');
	const sandbox = {
		module: { exports: {} },
		__settingPassword: settingPassword,
		__storage: {},
		uni: {
			setStorageSync() {},
			setStorage() {},
			navigateTo() {},
		},
		console,
		setTimeout,
		clearTimeout,
	};
	vm.runInNewContext(script, sandbox, { filename: pagePath });
	const page = sandbox.module.exports;
	const state = page.data();
	const instance = {
		...state,
		$ytHint: {
			toast(option) {
				toasts.push(option.title);
			},
		},
	};
	Object.keys(page.methods).forEach((methodName) => {
		instance[methodName] = page.methods[methodName].bind(instance);
	});
	return instance;
}

async function testConcurrentBlurAndSendReuseOneChallenge() {
	const challengeRequest = createDeferred();
	const mobileRequests = [];
	const smsChallenges = [];
	const toasts = [];
	const page = loadPage({
		mobileQuery(employeeId) {
			mobileRequests.push(employeeId);
			return challengeRequest.promise;
		},
		sendSms(challengeId) {
			smsChallenges.push(challengeId);
			return Promise.resolve({ data: { code: 0 } });
		},
	}, toasts);

	page.employeeId = 'A001';
	const blurRequest = page.getMobile();
	const sendRequest = page.getCode();

	assert.deepStrictEqual(mobileRequests, ['A001'], 'blur 与获取验证码必须复用同一 challenge 请求');
	challengeRequest.resolve({ data: { data: 'challenge-A001' } });
	await Promise.all([blurRequest, sendRequest]);

	assert.deepStrictEqual(smsChallenges, ['challenge-A001'], '验证码必须使用复用请求返回的 challenge');
	assert.strictEqual(page.challengeId, 'challenge-A001');
}

async function testStaleChallengeCannotOverwriteNewEmployee() {
	const requests = [];
	const toasts = [];
	const page = loadPage({
		mobileQuery(employeeId) {
			const request = createDeferred();
			requests.push({ employeeId, request });
			return request.promise;
		},
		sendSms() {
			return Promise.resolve({ data: { code: 0 } });
		},
	}, toasts);

	page.employeeId = 'A001';
	const oldEmployeeRequest = page.getMobile();
	page.employeeId = 'B002';
	const newEmployeeRequest = page.getMobile();

	requests[1].request.resolve({ data: { data: 'challenge-B002' } });
	await newEmployeeRequest;
	requests[0].request.resolve({ data: { data: 'challenge-A001' } });
	await oldEmployeeRequest;

	assert.strictEqual(page.challengeId, 'challenge-B002', '旧员工号的乱序响应不能覆盖当前 challenge');
}

async function testRepeatedSendClickWaitsForOneSmsRequest() {
	const challengeRequest = createDeferred();
	const smsChallenges = [];
	const toasts = [];
	const page = loadPage({
		mobileQuery() {
			return challengeRequest.promise;
		},
		sendSms(challengeId) {
			smsChallenges.push(challengeId);
			return Promise.resolve({ data: { code: 0 } });
		},
	}, toasts);

	page.employeeId = 'A001';
	const firstSend = page.getCode();
	const repeatedSend = page.getCode();
	challengeRequest.resolve({ data: { data: 'challenge-A001' } });
	await Promise.all([firstSend, repeatedSend]);

	assert.deepStrictEqual(smsChallenges, ['challenge-A001'], 'challenge 创建期间重复点击不能并发发送两次短信');
}

async function testChallengeFailureShowsFeedbackAndStopsSms() {
	const toasts = [];
	let smsSent = false;
	const page = loadPage({
		mobileQuery() {
			return Promise.reject(new Error('network'));
		},
		sendSms() {
			smsSent = true;
			return Promise.resolve({ data: { code: 0 } });
		},
	}, toasts);

	page.employeeId = 'A001';
	await page.getCode().catch(() => undefined);

	assert.deepStrictEqual(toasts, ['获取验证码失败，请重试'], 'challenge 创建失败必须向用户反馈');
	assert.strictEqual(smsSent, false, 'challenge 创建失败后不能发送短信验证码');
}

async function run() {
	await testConcurrentBlurAndSendReuseOneChallenge();
	await testStaleChallengeCannotOverwriteNewEmployee();
	await testRepeatedSendClickWaitsForOneSmsRequest();
	await testChallengeFailureShowsFeedbackAndStopsSms();
	console.log('find password challenge race tests passed');
}

run().catch((error) => {
	console.error(error);
	process.exitCode = 1;
});
