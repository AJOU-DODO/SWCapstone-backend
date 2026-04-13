package com.dodo.dodoserver.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenReissueRequestDto {
	@NotBlank(message = "Refresh Token은 필수이다.")
	private String refreshToken;
}
