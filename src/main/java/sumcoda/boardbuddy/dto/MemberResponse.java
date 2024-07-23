package sumcoda.boardbuddy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponse {

    @Getter
    @NoArgsConstructor
    public static class ProfileDTO {

        private String nickname;

        private String sido;

        private String sigu;

        private String dong;

        // phoneNumber 필드가 null 일 때 JSON 반환하지 않도록하는 어노테이션
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String phoneNumber;

        private Boolean isPhoneNumberVerified;

        private String profileImageS3SavedURL;

        @Builder
        public ProfileDTO(String nickname, String sido, String sigu, String dong, String phoneNumber, Boolean isPhoneNumberVerified, String profileImageS3SavedURL) {
            this.nickname = nickname;
            this.sido = sido;
            this.sigu = sigu;
            this.dong = dong;
            this.phoneNumber = phoneNumber;
            this.isPhoneNumberVerified = isPhoneNumberVerified;
            this.profileImageS3SavedURL = profileImageS3SavedURL;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class RankingsDTO {
        private String nickname;
        private String profileImageS3SavedURL;

        @Builder
        public RankingsDTO(String nickname, String profileImageS3SavedURL) {
            this.nickname = nickname;
            this.profileImageS3SavedURL = profileImageS3SavedURL;
        }
    }
}
