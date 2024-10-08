package sumcoda.boardbuddy.repository.member;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sumcoda.boardbuddy.dto.AuthResponse;
import sumcoda.boardbuddy.dto.BadgeImageResponse;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.entity.Member;

import java.util.List;
import java.util.Optional;

import static sumcoda.boardbuddy.entity.QBadgeImage.badgeImage;
import static sumcoda.boardbuddy.entity.QMember.*;
import static sumcoda.boardbuddy.entity.QNearPublicDistrict.*;
import static sumcoda.boardbuddy.entity.QProfileImage.*;
import static sumcoda.boardbuddy.entity.QPublicDistrict.*;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<AuthResponse.ProfileDTO> findAuthDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(AuthResponse.ProfileDTO.class,
                        member.username,
                        member.password,
                        member.role
                ))
                .from(member)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public Optional<MemberResponse.ProfileDTO> findMemberDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.ProfileDTO.class,
                        member.nickname,
                        member.sido,
                        member.sgg,
                        member.emd,
                        member.phoneNumber,
                        member.memberType,
                        profileImage.profileImageS3SavedURL
                ))
                .from(member)
                .leftJoin(member.profileImage, profileImage)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public List<MemberResponse.RankingsDTO> findTop3RankingMembers() {
        return jpaQueryFactory
                .select(Projections.fields(MemberResponse.RankingsDTO.class,
                        member.nickname,
                        profileImage.profileImageS3SavedURL
                ))
                .from(member)
                .leftJoin(member.profileImage, profileImage)
                .where(member.rank.isNotNull())
                .orderBy(member.rank.asc())
                .limit(3)
                .fetch();
    }

    // 점수로 정렬
    @Override
    public List<Member> findAllOrderedByRankScore() {
        return jpaQueryFactory.selectFrom(member)
                .orderBy(member.rankScore.desc())
                .fetch();
    }

    @Override
    public Optional<MemberResponse.ProfileInfosDTO> findMemberProfileByNickname(String nickname) {
        List<BadgeImageResponse.BadgeImageInfosDTO> badges = jpaQueryFactory.select(
                Projections.fields(BadgeImageResponse.BadgeImageInfosDTO.class,
                        badgeImage.badgeImageS3SavedURL,
                        badgeImage.badgeYearMonth))
                .from(badgeImage)
                .leftJoin(badgeImage.member, member)
                .where(member.nickname.eq(nickname))
                .orderBy(badgeImage.id.desc())
                .limit(3)
                .fetch();

        return Optional.ofNullable(jpaQueryFactory
                        .select(Projections.fields(MemberResponse.ProfileInfosDTO.class,
                                profileImage.profileImageS3SavedURL,
                                member.description,
                                member.rank,
                                member.buddyScore,
                                member.joinCount,
                                member.totalExcellentCount,
                                member.totalGoodCount,
                                member.totalBadCount))
                        .from(member)
                        .leftJoin(member.profileImage, profileImage)
                        .where(member.nickname.eq(nickname))
                        .fetchOne())
                .map(profileInfosDTO -> profileInfosDTO.toBuilder().badges(badges).build());
    }

    @Override
    public Optional<MemberResponse.LocationWithRadiusDTO> findLocationWithRadiusDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.LocationWithRadiusDTO.class,
                        member.sido,
                        member.sgg,
                        member.emd,
                        member.radius
                ))
                .from(member)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public Optional<MemberResponse.UsernameDTO> findUserNameDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.UsernameDTO.class,
                        member.username))
                .from(member)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public Optional<MemberResponse.IdDTO> findIdDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.IdDTO.class,
                        member.id))
                .from(member)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public Optional<MemberResponse.UsernameDTO> findUsernameDTOByNickname(String nickname) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.UsernameDTO.class,
                        member.username))
                .from(member)
                .where(member.nickname.eq(nickname))
                .fetchOne());
    }

    @Override
    public Optional<MemberResponse.NicknameDTO> findNicknameDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.NicknameDTO.class,
                        member.nickname))
                .from(member)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public List<String> findUsernamesWithGatherArticleInRange(String username, String sido, String sgg, String emd) {
        return jpaQueryFactory
                .select(member.username)
                .from(member)
                .where(
                        member.username.ne(username)
                                .and(
                                        JPAExpressions
                                                .selectOne()
                                                .from(publicDistrict)
                                                .join(nearPublicDistrict).on(publicDistrict.id.eq(nearPublicDistrict.publicDistrict.id))
                                                .where(
                                                        nearPublicDistrict.sido.eq(sido)
                                                                .and(nearPublicDistrict.sgg.eq(sgg))
                                                                .and(nearPublicDistrict.emd.eq(emd))
                                                                .and(member.radius.goe(nearPublicDistrict.radius)) // 반경 조건
                                                )
                                                .exists()
                                )
                )
                .fetch();
    }

}
