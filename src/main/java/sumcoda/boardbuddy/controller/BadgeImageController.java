package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sumcoda.boardbuddy.dto.BadgeImageResponse;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.service.BadgeImageService;

import static sumcoda.boardbuddy.util.ResponseHandlerUtil.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BadgeImageController {

    private final BadgeImageService badgeImageService;

    /**
     * 뱃지 조회 요청
     *
     * @param nickname 유저 닉네임
     * @return 뱃지 리스트
     **/
    @GetMapping(value = "/api/badges/{nickname}")
    public ResponseEntity<ApiResponse<Object>> getBadges (@PathVariable String nickname) {
        log.info("get badges is working");

        BadgeImageResponse.BadgeImageListDTO badgeImageListDTO = badgeImageService.getBadges(nickname);

        return buildSuccessResponse(badgeImageListDTO,"뱃지가 조회되었습니다.", HttpStatus.OK);
    }
}