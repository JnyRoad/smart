package com.tce.smart.algorithm.util;

import cn.hutool.json.JSONObject;
import com.alibaba.cloudapi.sdk.core.BaseApiClient;
import com.alibaba.cloudapi.sdk.core.BaseApiClientBuilder;
import com.alibaba.cloudapi.sdk.core.annotation.NotThreadSafe;
import com.alibaba.cloudapi.sdk.core.annotation.ThreadSafe;
import com.alibaba.cloudapi.sdk.core.enums.Method;
import com.alibaba.cloudapi.sdk.core.enums.Scheme;
import com.alibaba.cloudapi.sdk.core.model.ApiRequest;
import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.cloudapi.sdk.core.model.BuilderParams;
import com.tce.smart.algorithm.api.dto.resp.IdCardFrontDTO;
import com.tce.smart.algorithm.api.dto.resp.ItemDTO;
import com.tce.smart.algorithm.constant.AliOcrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wxjason
 */
@ThreadSafe
public final class AliOcrUtils extends BaseApiClient {

    private AliOcrUtils(BuilderParams builderParams) {
        super(builderParams);
    }

	@NotThreadSafe
    public static class Builder extends BaseApiClientBuilder<Builder, AliOcrUtils> {
        @Override
        protected AliOcrUtils build(BuilderParams params) {
            return new AliOcrUtils(params);
        }
    }

    public static Builder newBuilder(){
        return new AliOcrUtils.Builder();
    }

    public ApiResponse scanIndetity(byte[] body) {
        return syncInvoke(new ApiRequest(Scheme.HTTP, Method.POST_BODY, AliOcrConstants.GROUP_HOST, AliOcrConstants.API_PATH, body));
    }

    public static IdCardFrontDTO convertToIdCardMain(JSONObject out){
		IdCardFrontDTO ocrIdCardMainDTO = new IdCardFrontDTO();
        ocrIdCardMainDTO.setName(out.getStr("name"));
        ocrIdCardMainDTO.setIdNum(out.getStr("num"));
        ocrIdCardMainDTO.setSex(out.getStr("sex"));
        ocrIdCardMainDTO.setBirth(out.getStr("birth"));
        ocrIdCardMainDTO.setNation(out.getStr("nationality"));
        ocrIdCardMainDTO.setAddress(out.getStr("address"));
        return ocrIdCardMainDTO;
    }

	public static List<ItemDTO> convertIdCardMainToItem(JSONObject out) {
		List<ItemDTO> itemDTOList = new ArrayList<>();
		itemDTOList.add(ItemDTO.createItem("姓名", out.getStr("name")));
		itemDTOList.add(ItemDTO.createItem("性别", out.getStr("sex")));
		itemDTOList.add(ItemDTO.createItem("公民身份号码", out.getStr("num")));
		itemDTOList.add(ItemDTO.createItem("民族", out.getStr("nationality")));
		itemDTOList.add(ItemDTO.createItem("出生", out.getStr("birth")));
		itemDTOList.add(ItemDTO.createItem("住址", out.getStr("address")));
		return itemDTOList;
    }
}
