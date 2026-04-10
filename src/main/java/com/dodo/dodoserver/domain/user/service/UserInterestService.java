package com.dodo.dodoserver.domain.user.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.domain.user.dao.UserInterestRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.dto.UserInterestRequestDto;
import com.dodo.dodoserver.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.user.entity.UserInterest;

/**
 * 유저의 관심 카테고리 등록 및 수정을 관리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInterestService {

    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 현재 유저가 선택한 관심 카테고리 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getMyInterests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        return userInterestRepository.findByUserId(user.getId()).stream()
                .map(userInterest -> CategoryResponseDto.from(userInterest.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * 유저의 모든 관심사를 삭제한 뒤 요청한 리스트로 다시 일괄 저장
     */
    @Transactional
    public void updateInterests(String email, UserInterestRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));


        userInterestRepository.deleteByUserId(user.getId());


        List<UserInterest> newInterests = requestDto.getCategoryIds().stream()
                .map(categoryId -> {
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID입니다: " + categoryId));
                    return UserInterest.builder()
                            .user(user)
                            .category(category)
                            .build();
                })
                .collect(Collectors.toList());

        userInterestRepository.saveAll(newInterests);
        log.info("유저 관심사 일괄 업데이트 완료: {}, 개수: {}", email, newInterests.size());
    }
}
