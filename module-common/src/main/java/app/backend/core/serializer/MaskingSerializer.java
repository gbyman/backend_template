package app.backend.core.serializer;

import java.io.IOException;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import app.backend.core.annotation.Masking;
import app.backend.core.constants.MaskingType;
import app.backend.core.utils.MaskingUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/** JSON 직렬화 시 마스킹 처리를 담당하는 Serializer @Masking 어노테이션과 함께 사용됩니다. */
@NoArgsConstructor
@AllArgsConstructor
public class MaskingSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private MaskingType type;

    @Override
    public void serialize(
            String target, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {

        if (!StringUtils.hasText(target)) {
            jsonGenerator.writeNull();
            return;
        }

        switch (type) {
            case ID -> jsonGenerator.writeString(MaskingUtils.maskId(target));
            case NAME -> jsonGenerator.writeString(MaskingUtils.maskName(target));
            case EMAIL -> jsonGenerator.writeString(MaskingUtils.maskEmail(target));
            case IP -> jsonGenerator.writeString(MaskingUtils.maskIp(target));
            case PHONE -> jsonGenerator.writeString(MaskingUtils.maskPhone(target));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(
            SerializerProvider serializerProvider, BeanProperty beanProperty) {
        if (beanProperty != null && beanProperty.getAnnotation(Masking.class) != null) {
            Masking masking = beanProperty.getAnnotation(Masking.class);
            return new MaskingSerializer(masking.type());
        }
        return this;
    }
}
