package com.tce.smart.bridge.component;

import com.tce.smart.bridge.kafka.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

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
}
