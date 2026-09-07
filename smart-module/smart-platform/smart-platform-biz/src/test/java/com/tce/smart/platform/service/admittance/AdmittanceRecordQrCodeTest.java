package com.tce.smart.platform.service.admittance;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceFellowRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.service.admittance.impl.SmtAdmittanceFellowServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/** 厂牌二维码使用逐人记录ID，并由真实ZXing算法复核内容。 */
public class AdmittanceRecordQrCodeTest {

	@Test
	public void encodesOneSixAndNineteenDigitRecordIdsWithoutPrecisionLoss() throws Exception {
		Assert.assertEquals("1", decode(AdmittanceRecordQrCode.create(1L)));
		Assert.assertEquals("123456", decode(AdmittanceRecordQrCode.create(123456L)));
		Assert.assertEquals("1000000", decode(AdmittanceRecordQrCode.create(1000000L)));
		Assert.assertEquals("9223372036854775807", decode(AdmittanceRecordQrCode.create(Long.MAX_VALUE)));
	}

	@Test
	public void sameApplicationProducesOneDistinctCodePerFellowAndNeverUsesSmsCode() throws Exception {
		SmtAdmittanceFellow first = fellow(123456L, 654321L, "合成访客甲");
		SmtAdmittanceFellow second = fellow(2L, 654321L, "合成访客乙");
		StubFellowService service = new StubFellowService(Arrays.asList(first, second));

		List<AdmittanceFellowRespDTO> response = service.getRespByApplyId(654321L);

		Assert.assertEquals("123456", decode(response.get(0).getRecordQrCode()));
		Assert.assertEquals("2", decode(response.get(1).getRecordQrCode()));
		Assert.assertNotEquals(response.get(0).getRecordQrCode(), response.get(1).getRecordQrCode());
		Assert.assertNotEquals("654321", decode(response.get(0).getRecordQrCode()));
	}

	@Test
	public void rejectsNullAndNonPositiveIds() {
		assertInvalid(null);
		assertInvalid(0L);
		assertInvalid(-1000000L);
	}

	private void assertInvalid(Long id) {
		try {
			AdmittanceRecordQrCode.create(id);
			Assert.fail("应拒绝非法记录ID: " + id);
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("记录ID"));
		}
	}

	private String decode(String base64) throws Exception {
		byte[] png = Base64Utils.decodeFromString(base64);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
				new BufferedImageLuminanceSource(ImageIO.read(new ByteArrayInputStream(png)))));
		return new MultiFormatReader().decode(bitmap).getText();
	}

	private SmtAdmittanceFellow fellow(Long id, Long visitorId, String name) {
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(id);
		fellow.setVisitorId(visitorId);
		fellow.setFellowName(name);
		fellow.setCertType(0);
		return fellow;
	}

	private static final class StubFellowService extends SmtAdmittanceFellowServiceImpl {
		private final List<SmtAdmittanceFellow> fellows;

		private StubFellowService(List<SmtAdmittanceFellow> fellows) {
			this.fellows = fellows;
		}

		@Override
		public List<SmtAdmittanceFellow> getByApplyId(Long applyId) {
			return fellows;
		}
	}
}
