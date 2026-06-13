package org.springframework.cloud.openfeign;

import feign.Feign;
import feign.Request;
import feign.RequestTemplate;
import feign.Target;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description: RouteTargeter
 * @date: 2020-08-31 15:23
 * @author: wuling
 * @version: 1.0
 */
public class RouteTargeter implements Targeter{

	private String parkId;

	/**
	 * bridge服务名以本字符串结尾的，会被替换为具体的bridge服务
	 */
	public static final String PARK_ID_SUFFIX = "PARK-ID";

	@Override
	public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context, Target.HardCodedTarget<T> target) {
		return feign.target(new RouteTarget<>(target));
	}

	public class RouteTarget<T> implements Target<T> {
		private Target<T> realTarget;

		public RouteTarget(Target<T> realTarget) {
			super();
			this.realTarget = realTarget;
		}

		@Override
		public Class<T> type() {
			return realTarget.type();
		}

		@Override
		public String name() {
			return realTarget.name();
		}

		@Override
		public String url() {
			String url = realTarget.url();
			if (url.endsWith(PARK_ID_SUFFIX)) {
				url = url.replace(PARK_ID_SUFFIX, parkId);
			}
			return url;
		}

		@Override
		public Request apply(RequestTemplate input) {
			if (input.url().indexOf("http") != 0) {
				input.target(url());
			}
			return input.request();

		}

	}
}
