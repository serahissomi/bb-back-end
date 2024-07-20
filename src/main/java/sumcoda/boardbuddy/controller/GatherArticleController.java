package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumcoda.boardbuddy.dto.GatherArticleRequest;
import sumcoda.boardbuddy.dto.GatherArticleResponse;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.service.GatherArticleService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatherArticleController {

    private final GatherArticleService gatherArticleService;

    /**
     * 모집글 작성 컨트롤러
     *
     * @param createRequest
     * @param username
     * @return
     */
    @PostMapping(value = "/api/gatherArticles")
    public ResponseEntity<ApiResponse<GatherArticleResponse.CreateDTO>> createGatherArticle(
            @RequestBody GatherArticleRequest.CreateDTO createRequest,
            @ModelAttribute("username") String username){
        GatherArticleResponse.CreateDTO createResponse = gatherArticleService.createGatherArticle(createRequest, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(createResponse, "모집글이 업로드 되었습니다"));
    }

    /**
     * 모집글 조회 컨트롤러
     *
     * @param gatherArticleId
     * @param username
     * @return
     */
    @GetMapping(value = "/api/gatherArticles/{gatherArticleId}")
    public ResponseEntity<ApiResponse<GatherArticleResponse.ReadDTO>> getGatherArticle(
            @PathVariable Long gatherArticleId,
            @ModelAttribute("username") String username) {
        GatherArticleResponse.ReadDTO response = gatherArticleService.getGatherArticle(gatherArticleId, username);
        return ResponseEntity.ok(new ApiResponse<>(response, "성공적으로 조회되었습니다."));
    }

    /**
     * 모집글 수정 컨트롤러
     *
     * @param gatherArticleId
     * @param updateRequest
     * @param username
     * @return
     */
    @PutMapping(value = "/api/gatherArticles/{gatherArticleId}")
    public ResponseEntity<ApiResponse<GatherArticleResponse.UpdateDTO>> updateGatherArticle(
            @PathVariable Long gatherArticleId,
            @RequestBody GatherArticleRequest.UpdateDTO updateRequest,
            @ModelAttribute("username") String username) {
        GatherArticleResponse.UpdateDTO updateResponse = gatherArticleService.updateGatherArticle(gatherArticleId, updateRequest, username);
        return ResponseEntity.ok(new ApiResponse<>(updateResponse, "모집글이 수정되었습니다."));
    }

    /**
     * 모집글 삭제 컨트롤러
     *
     * @param gatherArticleId
     * @param username
     * @return
     */
    @DeleteMapping(value = "/api/gatherArticles/{gatherArticleId}")
    public ResponseEntity<ApiResponse<GatherArticleResponse.DeleteDTO>> deleteGatherArticle(
            @PathVariable Long gatherArticleId,
            @ModelAttribute("username") String username){
        GatherArticleResponse.DeleteDTO deleteResponse = gatherArticleService.deleteGatherArticle(gatherArticleId, username);
        return ResponseEntity.ok(new ApiResponse<>(deleteResponse, "모집글이 삭제되었습니다."));
    }

    /**
     * 내가 작성한 모집글 조회 요청
     *
     * @param username 사용자 username
     * @return 내가 작성한 모집글 리스트
     **/
    @GetMapping(value = "/api/my/gatherArticles")
    public ResponseEntity<ApiResponse<List<GatherArticleResponse.GatherArticleInfosDTO>>> getMyGatherArticles (
            @ModelAttribute("username") String username) {
        log.info("get gather articles is working");

        List<GatherArticleResponse.GatherArticleInfosDTO> gatherArticles = gatherArticleService.getMyGatherArticles(username);

        return ResponseEntity.ok(new ApiResponse<>(gatherArticles, "내 모집글이 성공적으로 조회되었습니다."));
    }

    /**
     * 참가한 모집글 조회 요청
     *
     * @param username 사용자 username
     * @return 참가한 모집글 리스트
     **/
    @GetMapping(value = "/api/my/participations")
    public ResponseEntity<ApiResponse<List<GatherArticleResponse.GatherArticleInfosDTO>>> getMyParticipations (
            @ModelAttribute("username") String username) {
        log.info("get my participations is working");

        List<GatherArticleResponse.GatherArticleInfosDTO> participations = gatherArticleService.getMyParticipations(username);

        return ResponseEntity.ok(new ApiResponse<>(participations, "참가한 모집글이 성공적으로 조회되었습니다."));
    }
}
