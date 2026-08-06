package com.tce.smart.bridge.component;

import cn.hutool.json.JSONObject;
import com.tce.smart.bridge.kafka.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class WaterEleHelperTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @Test
    public void kafkaUsesConfiguredBridgeEventTopic() {
        WaterEleHelper waterEleHelper = new WaterEleHelper();
        ReflectionTestUtils.setField(waterEleHelper, "kafkaProducer", kafkaProducer);
        ReflectionTestUtils.setField(waterEleHelper, "bridgeEventTopic", "smart-local-bridge-event");

        ReflectionTestUtils.invokeMethod(waterEleHelper, "kafka", "water_repeater_read_nty", "{\"value\":1}");

        verify(kafkaProducer).sendMessage("smart-local-bridge-event", "water_repeater_read_nty", "{\"value\":1}");
    }

	@Test
	public void readingPayloadContainsOneSourceEventId() {
		WaterEleHelper waterEleHelper = new WaterEleHelper();
		JSONObject payload = new JSONObject();

		ReflectionTestUtils.invokeMethod(waterEleHelper, "assignSourceEventId", payload);
		String sourceEventId = payload.getStr("sourceEventId");
		ReflectionTestUtils.invokeMethod(waterEleHelper, "assignSourceEventId", payload);

		assertNotNull(sourceEventId);
		assertEquals("每条已构造的 payload 只生成一次事件标识", sourceEventId, payload.getStr("sourceEventId"));
	}
}
