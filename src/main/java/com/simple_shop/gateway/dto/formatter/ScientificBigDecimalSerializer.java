package com.simple_shop.gateway.dto.formatter;

import java.math.BigDecimal;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class ScientificBigDecimalSerializer extends ValueSerializer<BigDecimal> {
	public ScientificBigDecimalSerializer() {
		super();
	}

	@Override
	public void serialize(final BigDecimal value, final JsonGenerator gen, final SerializationContext context) {
		if (value != null) {
			gen.writeString(value.toString());
		} else {
			gen.writeNull();
		}
	}
}
