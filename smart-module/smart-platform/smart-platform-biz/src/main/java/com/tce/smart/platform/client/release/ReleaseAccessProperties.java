package com.tce.smart.platform.client.release;

import com.tce.smart.platform.client.identity.ClientApiException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 物品放行 App 岗位与审批指派仅可由服务端配置，默认关闭且无默认审批人。 */
@ConfigurationProperties(prefix = "smart.client.item-pass")
public class ReleaseAccessProperties {
	private boolean enabled;
	private List<Post> posts = new ArrayList<>();
	private Map<String, String> applicantApprovers = new HashMap<>();

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public List<Post> getPosts() { return posts; }
	public void setPosts(List<Post> posts) { this.posts = posts; }
	public Map<String, String> getApplicantApprovers() { return applicantApprovers; }
	public void setApplicantApprovers(Map<String, String> applicantApprovers) { this.applicantApprovers = applicantApprovers; }

	/** 配置绑定不得因工号大小写或分隔符规范化丢失审批人；人员目录仍提供唯一、可信的工号。 */
	public String approverFor(String staffNo) {
		if (staffNo == null || applicantApprovers == null) return null;
		String direct = applicantApprovers.get(staffNo);
		if (direct != null) return direct;
		for (Map.Entry<String, String> entry : applicantApprovers.entrySet())
			if (normalizedIdentifier(staffNo).equals(normalizedIdentifier(entry.getKey()))) return entry.getValue();
		return null;
	}

	/** Spring 配置绑定会移除 Map 键中的连接符，比较时只归一化已受 identifier 限制的工号。 */
	private static String normalizedIdentifier(String value) {
		StringBuilder normalized = new StringBuilder();
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			if (Character.isLetterOrDigit(character)) normalized.append(Character.toUpperCase(character));
		}
		return normalized.toString();
	}

	public Post post(String id) {
		if (posts != null) for (Post post : posts) if (post != null && id != null && id.equals(post.getId())) return post;
		return null;
	}

	public void validate() {
		if (!enabled) throw new ClientApiException(503);
		if (posts == null || posts.isEmpty() || applicantApprovers == null || applicantApprovers.isEmpty()) invalid();
		Set<String> ids = new HashSet<>();
		for (Post post : posts) {
			if (post == null || !identifier(post.id) || blank(post.name) || post.parkId == null || post.parkId <= 0
					|| blank(post.parkName) || !ids.add(post.id)) invalid();
		}
		for (Map.Entry<String, String> entry : applicantApprovers.entrySet())
			if (!identifier(entry.getKey()) || !identifier(entry.getValue()) || entry.getKey().equals(entry.getValue())) invalid();
	}

	public static boolean identifier(String value) { return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,95}"); }
	private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
	private static void invalid() { throw new ClientApiException(503); }

	public static class Post {
		private String id;
		private String name;
		private Integer parkId;
		private String parkName;
		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Integer getParkId() { return parkId; }
		public void setParkId(Integer parkId) { this.parkId = parkId; }
		public String getParkName() { return parkName; }
		public void setParkName(String parkName) { this.parkName = parkName; }
	}
}
