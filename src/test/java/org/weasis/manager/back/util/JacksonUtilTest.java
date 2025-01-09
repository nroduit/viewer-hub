package org.weasis.manager.back.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.weasis.manager.back.entity.OverrideConfigEntity;
import org.weasis.manager.back.entity.WeasisPropertyEntity;
import org.weasis.manager.back.entity.serializer.WeasisPropertyEntitySerializer;
import org.weasis.manager.back.enums.WeasisPropertyCategory;
import org.weasis.manager.back.enums.WeasisPropertyJavaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonUtilTest {

	// TODO W-34: to improve
	@Test
	void testDeserializeOverrideConfigEntityJsonFromInputStream() {
		OverrideConfigEntity entity = OverrideConfigEntity.builder()
			// .overrideConfigEntityPK(
			// OverrideConfigEntityPK.builder().launchConfigId(1L).packageVersionId(1L).targetId(1L).build())
			.weasisPropertyEntities(
					List.of(WeasisPropertyEntity.builder().code("code").description("description").build()))
			.build();
		String serialize = JacksonUtil.serializeIntoJson(entity);

		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serialize.getBytes())) {
			OverrideConfigEntity overrideConfigEntity = JacksonUtil.deserializeJsonOverrideConfigEntity(inputStream);

			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(0).getCode()).isEqualTo("code");
			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(0).getDescription())
				.isEqualTo("description");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	// TODO W-34: to improve
	@Test
	void testDeserializeOverrideConfigEntityPropertiesFromInputStream() throws JsonProcessingException {

		String test = "weasis.code=valueCode\nweasis.name=valueName";

		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(test.getBytes())) {
			OverrideConfigEntity overrideConfigEntity = JacksonUtil
				.deserializePropertiesOverrideConfigEntity(inputStream);

			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(0).getCode()).isEqualTo("weasis.code");
			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(0).getValue()).isEqualTo("valueCode");
			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(0).getDefaultValue())
				.isEqualTo("valueCode");
			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(1).getCode()).isEqualTo("weasis.name");
			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(1).getValue()).isEqualTo("valueName");
			assertThat(overrideConfigEntity.getWeasisPropertyEntities().get(1).getDefaultValue())
				.isEqualTo("valueName");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	// TODO W-34: to improve
	@Test
	void testSerializationProperties() {

		WeasisPropertyEntity weasisPropertyEntity = WeasisPropertyEntity.builder()
			.id(1L)
			.code("code")
			.value("value")
			.defaultValue("defaultValue")
			.category(WeasisPropertyCategory.WEASIS)
			.javaType(WeasisPropertyJavaType.BOOLEAN)
			.build();

		String toTest = JacksonUtil.serializeIntoProperties(weasisPropertyEntity);

		assertThat(toTest).isEqualTo("code=code\n" + "value=value\n" + "description=\n" + "defaultValue=defaultValue\n"
				+ "type=\n" + "category=WEASIS\n" + "javaType=" + WeasisPropertyJavaType.BOOLEAN.name() + "\n");

	}

	// TODO W-34: to improve
	@Test
	void testCustomPropertiesSerializer() {
		WeasisPropertyEntity weasisPropertyEntity = WeasisPropertyEntity.builder()
			.id(1L)
			.code("code")
			.value("value")
			.defaultValue("defaultValue")
			.category(WeasisPropertyCategory.WEASIS)
			.build();

		String toTest = JacksonUtil.customPropertiesSerializer(weasisPropertyEntity,
				new WeasisPropertyEntitySerializer(WeasisPropertyEntity.class), WeasisPropertyEntity.class);

		assertThat(toTest).isEqualTo("code=value\n");
	}

}
