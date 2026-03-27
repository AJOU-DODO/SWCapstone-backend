package com.dodo.dodoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
	private String accessToken;
	private String refreshToken;
	private Long accessTokenExpiresIn;

	public static TokenResponseDto of(String accessToken, String refreshToken, Long expiresIn) {
		return TokenResponseDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenExpiresIn(expiresIn)
			.build();
	}


}
